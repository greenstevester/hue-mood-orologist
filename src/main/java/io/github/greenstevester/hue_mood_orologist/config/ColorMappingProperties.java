package io.github.greenstevester.hue_mood_orologist.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "color-mapping")
public class ColorMappingProperties {
    
    /**
     * Weather condition to color mappings
     * Key: weather condition (e.g., "rain", "showers", "sunshine", "overcast", "snow")
     * Value: color name (e.g., "light purple", "dark purple", "yellow", "grey", "white")
     */
    private Map<String, String> conditions = new HashMap<>();
    
    /**
     * Default color for unmatched conditions
     */
    private String defaultColor = "white";
    
    /**
     * Whether to enable color mapping (false = use legacy rain/cold logic)
     */
    private boolean enabled = true;
}