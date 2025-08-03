package io.github.greenstevester.hue_mood_orologist.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "hue")
public class HueProperties {
    private String bridgeIp;
    private String apiKey;
    private String appName = "HueMoodOrologist";
    private boolean autoDiscoverBridge = true;
    private int discoveryTimeout = 10;
    private String targetLightName;
    private boolean targetAllLights = true;
}