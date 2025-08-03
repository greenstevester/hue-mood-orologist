package io.github.greenstevester.hue_mood_orologist.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class HourlyData {
    private List<String> time;
    @JsonProperty("temperature_2m")
    private List<Double> temperature2m;
    @JsonProperty("precipitation_probability")
    private List<Integer> precipitationProbability;
    private List<Double> precipitation;
}