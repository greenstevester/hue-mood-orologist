package io.github.greenstevester.hue_mood_orologist.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WeatherResponse {
    private double latitude;
    private double longitude;
    @JsonProperty("generationtime_ms")
    private double generationTimeMs;
    @JsonProperty("utc_offset_seconds")
    private int utcOffsetSeconds;
    private String timezone;
    @JsonProperty("timezone_abbreviation")
    private String timezoneAbbreviation;
    private double elevation;
    @JsonProperty("hourly_units")
    private HourlyUnits hourlyUnits;
    private HourlyData hourly;
}