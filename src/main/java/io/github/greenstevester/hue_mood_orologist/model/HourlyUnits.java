package io.github.greenstevester.hue_mood_orologist.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class HourlyUnits {
    private String time;
    @JsonProperty("temperature_2m")
    private String temperature2m;
    @JsonProperty("precipitation_probability")
    private String precipitationProbability;
    private String precipitation;
}