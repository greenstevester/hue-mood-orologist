package io.github.greenstevester.hue_mood_orologist.service;

import io.github.greenstevester.hue_mood_orologist.config.HueProperties;
import io.github.greenstevester.heuvana.HueBridge;
import io.github.greenstevester.heuvana.HueBridgeConnectionBuilder;
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
    
    public void setLightMoodForWeather(boolean isRaining, boolean isCold) {
        getHueConnection().ifPresent(hueConnection -> {
            try {
                Map<UUID, Light> allLights = hueConnection.getLights();
                Map<UUID, Light> targetLights = getTargetLights(allLights);
                
                if (targetLights.isEmpty()) {
                    log.warn("No target lights found to control");
                    return;
                }
                
                log.info("Controlling {} light(s)", targetLights.size());
                
                targetLights.values().forEach(light -> {
                    try {
                        if (isRaining) {
                            // Blue/grey mood for rain
                            light.turnOn();
                            log.info("Set {} to rainy mood", light.getName());
                        } else if (isCold) {
                            // Warm orange/yellow for cold weather
                            light.turnOn();
                            log.info("Set {} to cold weather mood", light.getName());
                        } else {
                            // Normal white light for good weather
                            light.turnOn();
                            log.info("Set {} to normal mood", light.getName());
                        }
                    } catch (Exception e) {
                        log.error("Error controlling light: {}", light.getName(), e);
                    }
                });
            } catch (Exception e) {
                log.error("Error setting light mood", e);
            }
        });
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