package io.github.greenstevester.hue_mood_orologist.scheduler;

import io.github.greenstevester.hue_mood_orologist.config.ScheduleProperties;
import io.github.greenstevester.hue_mood_orologist.model.WeatherAnalysis;
import io.github.greenstevester.hue_mood_orologist.service.HueService;
import io.github.greenstevester.hue_mood_orologist.service.WeatherService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherCheckScheduler {
    
    private final WeatherService weatherService;
    private final HueService hueService;
    private final ScheduleProperties scheduleProperties;
    private final TaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledTask;
    
    @PostConstruct
    public void initializeScheduler() {
        long intervalMillis = scheduleProperties.getInterval().getIntervalMillis();
        long initialDelayMillis = scheduleProperties.getInitialDelaySeconds() * 1000L;
        
        log.info("Initializing weather check scheduler: {} ({}ms interval)", 
                scheduleProperties.getInterval().getDescription(), intervalMillis);
        
        PeriodicTrigger trigger = new PeriodicTrigger(Duration.ofMillis(intervalMillis));
        trigger.setInitialDelay(Duration.ofMillis(initialDelayMillis));
        
        scheduledTask = taskScheduler.schedule(this::checkWeather, trigger);
        
        log.info("Weather check scheduler started with initial delay of {}s", 
                scheduleProperties.getInitialDelaySeconds());
    }
    
    public void updateSchedule(ScheduleProperties.ScheduleInterval newInterval) {
        log.info("Updating scheduler from {} to {}", 
                scheduleProperties.getInterval().getDescription(), 
                newInterval.getDescription());
        
        // Cancel current schedule
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(false);
        }
        
        // Update configuration
        scheduleProperties.setInterval(newInterval);
        
        // Start new schedule
        PeriodicTrigger trigger = new PeriodicTrigger(Duration.ofMillis(newInterval.getIntervalMillis()));
        trigger.setInitialDelay(Duration.ofMillis(1000)); // 1 second delay for restart
        
        scheduledTask = taskScheduler.schedule(this::checkWeather, trigger);
        
        log.info("Weather check scheduler updated to run {}", newInterval.getDescription());
    }
    
    public void checkWeather() {
        log.info("Starting scheduled weather check");
        
        WeatherAnalysis analysis = weatherService.fetchAndAnalyzeWeather();
        
        log.info("Weather Analysis Results:");
        log.info("  - Is Raining: {}", analysis.isRaining());
        log.info("  - Will Rain Soon: {}", analysis.isWillRainSoon());
        log.info("  - Max Precipitation Probability: {}%", analysis.getMaxPrecipitationProbability());
        log.info("  - Is Very Cold: {}", analysis.isVeryCold());
        log.info("  - Will Be Very Cold: {}", analysis.isWillBeVeryCold());
        log.info("  - Current Temperature: {}°C", analysis.getCurrentTemperature());
        log.info("  - Summary: {}", analysis.getSummary());
        
        // Update Hue lights based on weather conditions
        boolean rainCondition = analysis.isRaining() || analysis.isWillRainSoon();
        boolean coldCondition = analysis.isVeryCold() || analysis.isWillBeVeryCold();
        
        if (rainCondition) {
            log.info("Rain detected or expected - setting blue/grey Hue light mood");
        } else if (coldCondition) {
            log.info("Cold weather detected - setting warm Hue light mood");
        } else {
            log.info("Normal weather conditions - setting standard Hue light mood");
        }
        
        hueService.setLightMoodForWeather(rainCondition, coldCondition);
    }
}