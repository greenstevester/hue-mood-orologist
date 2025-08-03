package io.github.greenstevester.hue_mood_orologist.service;

import io.github.greenstevester.hue_mood_orologist.model.HourlyData;
import io.github.greenstevester.hue_mood_orologist.model.WeatherAnalysis;
import io.github.greenstevester.hue_mood_orologist.model.WeatherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.OptionalDouble;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {
    
    private final RestClient.Builder restClientBuilder;
    
    @Value("${weather.api.url:https://api.open-meteo.com/v1/forecast}")
    private String apiBaseUrl;
    
    @Value("${weather.cold.threshold:5.0}")
    private double coldThreshold;
    
    @Value("${weather.rain.probability.threshold:30}")
    private int rainProbabilityThreshold;
    
    @Value("${weather.rain.amount.threshold:0.1}")
    private double rainAmountThreshold;
    
    public WeatherAnalysis fetchAndAnalyzeWeather() {
        log.info("Fetching weather data from Open-Meteo API");
        
        try {
            RestClient restClient = restClientBuilder.build();
            
            String url = apiBaseUrl + 
                "?latitude=47.3769&longitude=8.5417" +
                "&hourly=temperature_2m,precipitation_probability,precipitation" +
                "&timezone=Europe/Berlin&forecast_hours=8";
            
            WeatherResponse response = restClient
                .get()
                .uri(url)
                .retrieve()
                .body(WeatherResponse.class);
            
            if (response == null || response.getHourly() == null) {
                log.error("Invalid weather response received");
                return createErrorAnalysis("Invalid weather data received");
            }
            
            return analyzeWeatherData(response);
            
        } catch (Exception e) {
            log.error("Error fetching weather data", e);
            return createErrorAnalysis("Error fetching weather: " + e.getMessage());
        }
    }
    
    private WeatherAnalysis analyzeWeatherData(WeatherResponse response) {
        HourlyData hourly = response.getHourly();
        
        // Current conditions (first hour)
        double currentTemp = hourly.getTemperature2m().get(0);
        double currentPrecipitation = hourly.getPrecipitation().get(0);
        int currentPrecipProb = hourly.getPrecipitationProbability().get(0);
        
        // Analyze rain conditions
        boolean isRaining = currentPrecipitation > rainAmountThreshold || 
                           currentPrecipProb > rainProbabilityThreshold;
        
        // Check if it will rain in the next hours
        boolean willRainSoon = false;
        int maxPrecipProb = currentPrecipProb;
        double maxPrecipAmount = currentPrecipitation;
        
        for (int i = 1; i < hourly.getPrecipitation().size(); i++) {
            double precip = hourly.getPrecipitation().get(i);
            int precipProb = hourly.getPrecipitationProbability().get(i);
            
            if (precip > maxPrecipAmount) {
                maxPrecipAmount = precip;
            }
            if (precipProb > maxPrecipProb) {
                maxPrecipProb = precipProb;
            }
            
            if (precip > rainAmountThreshold || precipProb > rainProbabilityThreshold) {
                willRainSoon = true;
            }
        }
        
        // Analyze temperature conditions
        boolean isVeryCold = currentTemp < coldThreshold;
        
        OptionalDouble minTempOpt = hourly.getTemperature2m().stream()
            .mapToDouble(Double::doubleValue)
            .min();
        
        double minTemp = minTempOpt.orElse(currentTemp);
        boolean willBeVeryCold = minTemp < coldThreshold;
        
        // Determine specific weather condition
        String weatherCondition = determineWeatherCondition(
            currentTemp, currentPrecipitation, currentPrecipProb, 
            maxPrecipAmount, maxPrecipProb, isRaining, isVeryCold
        );
        
        // Create summary
        String summary = createWeatherSummary(
            isRaining, willRainSoon, maxPrecipProb, 
            isVeryCold, willBeVeryCold, currentTemp, minTemp
        );
        
        log.info("Weather analysis complete: {} (condition: {})", summary, weatherCondition);
        
        return WeatherAnalysis.builder()
            .analysisTime(LocalDateTime.now())
            .isRaining(isRaining)
            .willRainSoon(willRainSoon)
            .maxPrecipitationProbability(maxPrecipProb)
            .maxPrecipitationAmount(maxPrecipAmount)
            .isVeryCold(isVeryCold)
            .willBeVeryCold(willBeVeryCold)
            .currentTemperature(currentTemp)
            .minTemperature(minTemp)
            .summary(summary)
            .weatherCondition(weatherCondition)
            .build();
    }
    
    private String createWeatherSummary(boolean isRaining, boolean willRainSoon, int maxPrecipProb,
                                       boolean isVeryCold, boolean willBeVeryCold, 
                                       double currentTemp, double minTemp) {
        StringBuilder summary = new StringBuilder();
        
        // Rain status
        if (isRaining) {
            summary.append("Currently raining. ");
        } else if (willRainSoon) {
            summary.append("Rain expected (").append(maxPrecipProb).append("% chance). ");
        } else {
            summary.append("No rain expected. ");
        }
        
        // Temperature status
        summary.append("Current temp: ").append(String.format("%.1f°C", currentTemp)).append(". ");
        
        if (isVeryCold) {
            summary.append("It's very cold! ");
        } else if (willBeVeryCold) {
            summary.append("It will be very cold (min: ")
                   .append(String.format("%.1f°C", minTemp)).append("). ");
        }
        
        return summary.toString().trim();
    }
    
    private String determineWeatherCondition(double currentTemp, double currentPrecip, int currentPrecipProb,
                                           double maxPrecipAmount, int maxPrecipProb, 
                                           boolean isRaining, boolean isVeryCold) {
        
        // Snow conditions (cold + precipitation)
        if (currentTemp <= 0 && (currentPrecip > 0.1 || currentPrecipProb > 20)) {
            return "snow";
        }
        
        // Rain conditions based on intensity
        if (isRaining || maxPrecipAmount > rainAmountThreshold || maxPrecipProb > rainProbabilityThreshold) {
            if (maxPrecipAmount > 2.0 || maxPrecipProb > 70) {
                return "showers"; // Heavy precipitation
            } else {
                return "rain"; // Light to moderate precipitation
            }
        }
        
        // Temperature-based conditions when no precipitation
        if (isVeryCold) {
            return "cold"; // Very cold but dry
        }
        
        // Cloud/visibility conditions based on temperature ranges and lack of precipitation
        if (currentTemp > 25) {
            return "sunshine"; // Hot and clear
        } else if (currentTemp > 15) {
            // Moderate temperature - check if it might be overcast
            // If low precipitation probability but not sunny, likely overcast
            if (maxPrecipProb > 10 && maxPrecipProb < 30) {
                return "overcast";
            } else {
                return "clear"; // Clear moderate weather
            }
        } else {
            // Cool temperature
            if (maxPrecipProb > 20) {
                return "overcast"; // Cool and cloudy
            } else {
                return "clear"; // Cool but clear
            }
        }
    }
    
    private WeatherAnalysis createErrorAnalysis(String error) {
        return WeatherAnalysis.builder()
            .analysisTime(LocalDateTime.now())
            .summary("Error: " + error)
            .weatherCondition("unknown")
            .build();
    }
}