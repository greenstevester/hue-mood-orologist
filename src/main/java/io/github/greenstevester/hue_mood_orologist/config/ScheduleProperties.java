package io.github.greenstevester.hue_mood_orologist.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "schedule")
public class ScheduleProperties {
    private ScheduleInterval interval = ScheduleInterval.HOUR;
    private int initialDelaySeconds = 5;
    
    public enum ScheduleInterval {
        MINUTE(60_000, "every minute"),
        HOUR(3_600_000, "every hour"), 
        FOUR_HOURS(14_400_000, "every 4 hours"),
        TWELVE_HOURS(43_200_000, "every 12 hours");
        
        private final long intervalMillis;
        private final String description;
        
        ScheduleInterval(long intervalMillis, String description) {
            this.intervalMillis = intervalMillis;
            this.description = description;
        }
        
        public long getIntervalMillis() {
            return intervalMillis;
        }
        
        public String getDescription() {
            return description;
        }
    }
}