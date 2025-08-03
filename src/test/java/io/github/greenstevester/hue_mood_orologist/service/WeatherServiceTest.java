package io.github.greenstevester.hue_mood_orologist.service;

import io.github.greenstevester.hue_mood_orologist.model.HourlyData;
import io.github.greenstevester.hue_mood_orologist.model.WeatherAnalysis;
import io.github.greenstevester.hue_mood_orologist.model.WeatherResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private RestClient.Builder restClientBuilder;
    
    @Mock
    private RestClient restClient;
    
    @Mock
    private RestClient.RequestHeadersUriSpec requestSpec;
    
    @Mock
    private RestClient.ResponseSpec responseSpec;
    
    private WeatherService weatherService;
    
    @BeforeEach
    void setUp() {
        // Use reflection to set up WeatherService with proper field values
        weatherService = new WeatherService(restClientBuilder);
        
        try {
            // Set the threshold fields using reflection
            setPrivateField(weatherService, "coldThreshold", 5.0);
            setPrivateField(weatherService, "rainProbabilityThreshold", 30);
            setPrivateField(weatherService, "rainAmountThreshold", 0.1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set private fields", e);
        }
        
        // Setup default mock chain
        when(restClientBuilder.build()).thenReturn(restClient);
        when(restClient.get()).thenReturn(requestSpec);
        when(requestSpec.uri(anyString())).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
    }
    
    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
    
    @Test
    @DisplayName("Should detect rain condition with light precipitation")
    void shouldDetectRainCondition() {
        // Given
        WeatherResponse response = createWeatherResponse(
            Arrays.asList(15.0, 14.0, 13.0), // temperatures
            Arrays.asList(0.2, 0.3, 0.1),   // precipitation amounts
            Arrays.asList(35, 40, 30)       // precipitation probabilities
        );
        when(responseSpec.body(WeatherResponse.class)).thenReturn(response);
        
        // When
        WeatherAnalysis analysis = weatherService.fetchAndAnalyzeWeather();
        
        // Then
        assertThat(analysis.getWeatherCondition()).isEqualTo("rain");
        assertThat(analysis.isRaining()).isTrue();
        assertThat(analysis.getMaxPrecipitationProbability()).isEqualTo(40);
    }
    
    @Test
    @DisplayName("Should detect showers condition with heavy precipitation")
    void shouldDetectShowersCondition() {
        // Given
        WeatherResponse response = createWeatherResponse(
            Arrays.asList(16.0, 15.0, 14.0), // temperatures
            Arrays.asList(2.5, 3.0, 1.8),   // heavy precipitation
            Arrays.asList(75, 80, 70)       // high probabilities
        );
        when(responseSpec.body(WeatherResponse.class)).thenReturn(response);
        
        // When
        WeatherAnalysis analysis = weatherService.fetchAndAnalyzeWeather();
        
        // Then
        assertThat(analysis.getWeatherCondition()).isEqualTo("showers");
        assertThat(analysis.isRaining()).isTrue();
        assertThat(analysis.getMaxPrecipitationAmount()).isGreaterThan(2.0);
    }
    
    @Test
    @DisplayName("Should detect snow condition with freezing temperature and precipitation")
    void shouldDetectSnowCondition() {
        // Given
        WeatherResponse response = createWeatherResponse(
            Arrays.asList(-2.0, -1.0, -3.0), // freezing temperatures
            Arrays.asList(0.5, 0.8, 0.3),   // precipitation
            Arrays.asList(45, 50, 40)       // precipitation probabilities
        );
        when(responseSpec.body(WeatherResponse.class)).thenReturn(response);
        
        // When
        WeatherAnalysis analysis = weatherService.fetchAndAnalyzeWeather();
        
        // Then
        assertThat(analysis.getWeatherCondition()).isEqualTo("snow");
        assertThat(analysis.isVeryCold()).isTrue();
        assertThat(analysis.getCurrentTemperature()).isLessThanOrEqualTo(0);
    }
    
    @Test
    @DisplayName("Should detect sunshine condition with high temperature")
    void shouldDetectSunshineCondition() {
        // Given
        WeatherResponse response = createWeatherResponse(
            Arrays.asList(28.0, 30.0, 26.0), // hot temperatures
            Arrays.asList(0.0, 0.0, 0.0),   // no precipitation
            Arrays.asList(5, 3, 8)          // low precipitation probability
        );
        when(responseSpec.body(WeatherResponse.class)).thenReturn(response);
        
        // When
        WeatherAnalysis analysis = weatherService.fetchAndAnalyzeWeather();
        
        // Then
        assertThat(analysis.getWeatherCondition()).isEqualTo("sunshine");
        assertThat(analysis.isRaining()).isFalse();
        assertThat(analysis.isVeryCold()).isFalse();
        assertThat(analysis.getCurrentTemperature()).isGreaterThan(25);
    }
    
    @Test
    @DisplayName("Should detect overcast condition with moderate precipitation probability")
    void shouldDetectOvercastCondition() {
        // Given
        WeatherResponse response = createWeatherResponse(
            Arrays.asList(18.0, 17.0, 19.0), // moderate temperatures
            Arrays.asList(0.0, 0.0, 0.0),   // no precipitation
            Arrays.asList(25, 20, 28)       // moderate precipitation probability
        );
        when(responseSpec.body(WeatherResponse.class)).thenReturn(response);
        
        // When
        WeatherAnalysis analysis = weatherService.fetchAndAnalyzeWeather();
        
        // Then
        assertThat(analysis.getWeatherCondition()).isEqualTo("overcast");
        assertThat(analysis.isRaining()).isFalse();
        assertThat(analysis.getMaxPrecipitationProbability()).isBetween(10, 30);
    }
    
    @Test
    @DisplayName("Should detect cold condition with very low temperature but no precipitation")
    void shouldDetectColdCondition() {
        // Given
        WeatherResponse response = createWeatherResponse(
            Arrays.asList(2.0, 1.0, 3.0),   // cold temperatures
            Arrays.asList(0.0, 0.0, 0.0),  // no precipitation
            Arrays.asList(5, 8, 3)         // low precipitation probability
        );
        when(responseSpec.body(WeatherResponse.class)).thenReturn(response);
        
        // When
        WeatherAnalysis analysis = weatherService.fetchAndAnalyzeWeather();
        
        // Then
        assertThat(analysis.getWeatherCondition()).isEqualTo("cold");
        assertThat(analysis.isVeryCold()).isTrue();
        assertThat(analysis.isRaining()).isFalse();
        assertThat(analysis.getCurrentTemperature()).isLessThan(5.0);
    }
    
    @Test
    @DisplayName("Should detect clear condition with moderate temperature and no precipitation")
    void shouldDetectClearCondition() {
        // Given
        WeatherResponse response = createWeatherResponse(
            Arrays.asList(20.0, 22.0, 18.0), // moderate temperatures
            Arrays.asList(0.0, 0.0, 0.0),   // no precipitation
            Arrays.asList(5, 8, 3)          // low precipitation probability
        );
        when(responseSpec.body(WeatherResponse.class)).thenReturn(response);
        
        // When
        WeatherAnalysis analysis = weatherService.fetchAndAnalyzeWeather();
        
        // Then
        assertThat(analysis.getWeatherCondition()).isEqualTo("clear");
        assertThat(analysis.isRaining()).isFalse();
        assertThat(analysis.isVeryCold()).isFalse();
        assertThat(analysis.getCurrentTemperature()).isBetween(15.0, 25.0);
    }
    
    @Test
    @DisplayName("Should handle API error gracefully")
    void shouldHandleApiError() {
        // Given
        when(responseSpec.body(WeatherResponse.class)).thenThrow(new RuntimeException("API Error"));
        
        // When
        WeatherAnalysis analysis = weatherService.fetchAndAnalyzeWeather();
        
        // Then
        assertThat(analysis.getWeatherCondition()).isEqualTo("unknown");
        assertThat(analysis.getSummary()).contains("Error");
    }
    
    @Test
    @DisplayName("Should handle null weather response")
    void shouldHandleNullResponse() {
        // Given
        when(responseSpec.body(WeatherResponse.class)).thenReturn(null);
        
        // When
        WeatherAnalysis analysis = weatherService.fetchAndAnalyzeWeather();
        
        // Then
        assertThat(analysis.getWeatherCondition()).isEqualTo("unknown");
        assertThat(analysis.getSummary()).contains("Invalid weather data received");
    }
    
    private WeatherResponse createWeatherResponse(
            java.util.List<Double> temperatures, 
            java.util.List<Double> precipitation, 
            java.util.List<Integer> precipitationProbability) {
        
        HourlyData hourlyData = new HourlyData();
        hourlyData.setTemperature2m(temperatures);
        hourlyData.setPrecipitation(precipitation);
        hourlyData.setPrecipitationProbability(precipitationProbability);
        
        WeatherResponse response = new WeatherResponse();
        response.setHourly(hourlyData);
        
        return response;
    }
}