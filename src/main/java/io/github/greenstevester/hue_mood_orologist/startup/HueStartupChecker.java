package io.github.greenstevester.hue_mood_orologist.startup;

import io.github.greenstevester.hue_mood_orologist.config.HueProperties;
import io.github.greenstevester.hue_mood_orologist.service.HueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HueStartupChecker {
    
    private final HueService hueService;
    private final HueProperties hueProperties;
    
    @EventListener(ApplicationReadyEvent.class)
    public void checkHueLightsOnStartup() {
        log.info("=== Checking for Philips Hue lights on startup ===");
        
        // Try to connect to Hue bridge and list available lights
        hueService.listLights();
        
        // Run debug color information if enabled
        if (hueProperties.isDebugColors()) {
            log.info("Debug colors enabled - reading current light color information...");
            hueService.debugLightColors();
            hueService.debugLightColorsWithRawApi();
        }
        
        log.info("=== Hue lights check completed ===");
    }
}