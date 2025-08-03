package io.github.greenstevester.hue_mood_orologist.service;

import io.github.greenstevester.hue_mood_orologist.config.ColorMappingProperties;
import io.github.greenstevester.hue_mood_orologist.config.HueProperties;
import io.github.greenstevester.hue_mood_orologist.model.WeatherAnalysis;
import io.github.greenstevester.heuvana.Color;
import io.github.greenstevester.heuvana.HueBridge;
import io.github.greenstevester.heuvana.HueBridgeConnectionBuilder;
import io.github.greenstevester.heuvana.v2.UpdateState;
import io.github.greenstevester.heuvana.discovery.HueBridgeDiscoveryService;
import io.github.greenstevester.heuvana.v2.Hue;
import io.github.greenstevester.heuvana.v2.Light;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class HueService {
    
    private final HueProperties hueProperties;
    private final ColorMappingProperties colorMappingProperties;
    private final ColorParserService colorParserService;
    private Hue hue;
    
    public Optional<Hue> getHueConnection() {
        if (hue != null) {
            return Optional.of(hue);
        }
        
        try {
            String bridgeIp = hueProperties.getBridgeIp();
            String apiKey = hueProperties.getApiKey();
            
            // If bridge IP is not configured, try to discover it
            if ((bridgeIp == null || bridgeIp.isEmpty()) && hueProperties.isAutoDiscoverBridge()) {
                log.info("Bridge IP not configured, attempting to discover Hue bridges...");
                Optional<String> discoveredIp = discoverBridge();
                if (discoveredIp.isPresent()) {
                    bridgeIp = discoveredIp.get();
                    log.info("Discovered Hue bridge at: {}", bridgeIp);
                } else {
                    log.warn("No Hue bridges found on the network");
                    return Optional.empty();
                }
            }
            
            // If API key is not configured, we need to register with the bridge
            if (apiKey == null || apiKey.isEmpty()) {
                log.warn("Hue API key not configured. Please press the button on your Hue bridge and configure the API key.");
                log.info("To register: Press the button on your Hue bridge and run the following:");
                log.info("String apiKey = new HueBridgeConnectionBuilder(\"{}\").initializeApiConnection(\"{}\").get();", 
                        bridgeIp, hueProperties.getAppName());
                return Optional.empty();
            }
            
            // Create Hue connection
            hue = new Hue(bridgeIp, apiKey);
            log.info("Successfully connected to Hue bridge at {}", bridgeIp);
            return Optional.of(hue);
            
        } catch (Exception e) {
            log.error("Failed to connect to Hue bridge", e);
            return Optional.empty();
        }
    }
    
    private Optional<String> discoverBridge() {
        try {
            Future<List<HueBridge>> bridgesFuture = new HueBridgeDiscoveryService()
                .discoverBridges(bridge -> log.debug("Found bridge: {}", bridge));
            
            List<HueBridge> bridges = bridgesFuture.get(hueProperties.getDiscoveryTimeout(), TimeUnit.SECONDS);
            
            if (!bridges.isEmpty()) {
                return Optional.of(bridges.get(0).getIp());
            }
        } catch (Exception e) {
            log.error("Error during bridge discovery", e);
        }
        return Optional.empty();
    }
    
    public void listLights() {
        getHueConnection().ifPresentOrElse(
            hueConnection -> {
                try {
                    Map<UUID, Light> lights = hueConnection.getLights();
                    if (lights.isEmpty()) {
                        log.info("No lights found on the Hue bridge");
                    } else {
                        log.info("Found {} lights:", lights.size());
                        lights.forEach((uuid, light) -> 
                            log.info("  - {} (ID: {})", 
                                light.getName(), 
                                uuid)
                        );
                        
                        // Show targeting configuration
                        if (hueProperties.isTargetAllLights()) {
                            log.info("Configuration: Targeting ALL lights");
                        } else if (hueProperties.getTargetLightName() != null) {
                            log.info("Configuration: Targeting specific light: '{}'", hueProperties.getTargetLightName());
                            boolean foundTarget = lights.values().stream()
                                .anyMatch(light -> light.getName().equalsIgnoreCase(hueProperties.getTargetLightName()));
                            if (!foundTarget) {
                                log.warn("Target light '{}' not found! Available lights: {}", 
                                    hueProperties.getTargetLightName(),
                                    lights.values().stream().map(Light::getName).toList());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Error listing lights", e);
                }
            },
            () -> log.warn("No Hue connection available")
        );
    }
    
    public void setLightMoodForWeather(WeatherAnalysis weatherAnalysis) {
        getHueConnection().ifPresent(hueConnection -> {
            try {
                Map<UUID, Light> allLights = hueConnection.getLights();
                Map<UUID, Light> targetLights = getTargetLights(allLights);
                
                if (targetLights.isEmpty()) {
                    log.warn("No target lights found to control");
                    return;
                }
                
                log.info("Controlling {} light(s) for weather condition: {}", 
                    targetLights.size(), weatherAnalysis.getWeatherCondition());
                
                // Determine color to use
                Color lightColor = determineColorForWeather(weatherAnalysis);
                
                targetLights.values().forEach(light -> {
                    try {
                        light.setState(new UpdateState().color(lightColor).on());
                        log.info("Set {} to {} mood (RGB: {})", 
                            light.getName(), 
                            weatherAnalysis.getWeatherCondition(),
                            lightColor.toString());
                    } catch (Exception e) {
                        log.error("Error controlling light: {}", light.getName(), e);
                    }
                });
            } catch (Exception e) {
                log.error("Error setting light mood", e);
            }
        });
    }
    
    public void setLightMoodForWeather(boolean isRaining, boolean isCold) {
        // Legacy method for backward compatibility
        WeatherAnalysis legacyAnalysis = WeatherAnalysis.builder()
            .isRaining(isRaining)
            .isVeryCold(isCold)
            .weatherCondition(isRaining ? "rain" : (isCold ? "cold" : "clear"))
            .build();
        setLightMoodForWeather(legacyAnalysis);
    }
    
    private Color determineColorForWeather(WeatherAnalysis weatherAnalysis) {
        String weatherCondition = weatherAnalysis.getWeatherCondition();
        
        // Use color mapping if enabled and condition is mapped
        if (colorMappingProperties.isEnabled() && weatherCondition != null) {
            String mappedColor = colorMappingProperties.getConditions().get(weatherCondition.toLowerCase());
            if (mappedColor != null) {
                log.debug("Using mapped color '{}' for condition '{}'", mappedColor, weatherCondition);
                return colorParserService.parseColor(mappedColor);
            }
            
            // If mapping enabled but condition not found, use default
            if (!colorMappingProperties.getConditions().isEmpty()) {
                log.debug("No mapping found for condition '{}', using default color '{}'", 
                    weatherCondition, colorMappingProperties.getDefaultColor());
                return colorParserService.parseColor(colorMappingProperties.getDefaultColor());
            }
        }
        
        // Fallback to legacy logic if mapping disabled or no mappings configured
        log.debug("Using legacy color logic for weather condition '{}'", weatherCondition);
        return getLegacyColorForWeather(weatherAnalysis);
    }
    
    private Color getLegacyColorForWeather(WeatherAnalysis weatherAnalysis) {
        if (weatherAnalysis.isRaining()) {
            return Color.of(100, 149, 237); // Cornflower blue for rain
        } else if (weatherAnalysis.isVeryCold()) {
            return Color.of(255, 140, 0);   // Dark orange for cold
        } else {
            return Color.of(255, 255, 255); // White for normal weather
        }
    }
    
    private Map<UUID, Light> getTargetLights(Map<UUID, Light> allLights) {
        if (allLights.isEmpty()) {
            return allLights;
        }
        
        // If targeting all lights or no specific target configured
        if (hueProperties.isTargetAllLights() || 
            hueProperties.getTargetLightName() == null || 
            hueProperties.getTargetLightName().trim().isEmpty()) {
            log.debug("Targeting all {} lights", allLights.size());
            return allLights;
        }
        
        // Filter to specific target light(s)
        String targetName = hueProperties.getTargetLightName().trim();
        Map<UUID, Light> targetLights = allLights.entrySet().stream()
            .filter(entry -> entry.getValue().getName().equalsIgnoreCase(targetName))
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));
        
        if (targetLights.isEmpty()) {
            log.warn("Target light '{}' not found. Available lights: {}", 
                targetName, 
                allLights.values().stream().map(Light::getName).toList());
        } else {
            log.debug("Found target light: '{}'", targetName);
        }
        
        return targetLights;
    }
}