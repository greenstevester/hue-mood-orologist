package io.github.greenstevester.hue_mood_orologist.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class WeatherAnalysis {
    private LocalDateTime analysisTime;
    private boolean isRaining;
    private boolean willRainSoon;
    private int maxPrecipitationProbability;
    private double maxPrecipitationAmount;
    private boolean isVeryCold;
    private boolean willBeVeryCold;
    private double currentTemperature;
    private double minTemperature;
    private String summary;
    
    // New field for specific weather condition mapping
    private String weatherCondition;
}