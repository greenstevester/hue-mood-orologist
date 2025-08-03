package io.github.greenstevester.hue_mood_orologist.service;

import io.github.greenstevester.hue_mood_orologist.config.ColorMappingProperties;
import io.github.greenstevester.hue_mood_orologist.config.HueProperties;
import io.github.greenstevester.heuvana.v2.Hue;
import io.github.greenstevester.heuvana.v2.Light;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HueServiceDebugTest {

    @Mock
    private HueProperties hueProperties;
    
    @Mock
    private ColorMappingProperties colorMappingProperties;
    
    @Mock
    private ColorParserService colorParserService;
    
    @Mock
    private Hue hue;
    
    @Mock
    private Light light1;
    
    @Mock
    private Light light2;
    
    private HueService hueService;
    private Map<UUID, Light> mockLights;
    
    @BeforeEach
    void setUp() {
        hueService = new HueService(hueProperties, colorMappingProperties, colorParserService);
        
        // Setup mock lights
        mockLights = new HashMap<>();
        UUID light1Id = UUID.randomUUID();
        UUID light2Id = UUID.randomUUID();
        mockLights.put(light1Id, light1);
        mockLights.put(light2Id, light2);
        
        when(light1.getName()).thenReturn("Living Room");
        when(light1.isOn()).thenReturn(true);
        when(light1.getOwnerId()).thenReturn(UUID.randomUUID());
        when(light1.toString()).thenReturn("Light[name=Living Room, on=true, color=rgb(255,255,255)]");
        
        when(light2.getName()).thenReturn("Kitchen");
        when(light2.isOn()).thenReturn(false);
        when(light2.getOwnerId()).thenReturn(null);
        when(light2.toString()).thenReturn("Light[name=Kitchen, on=false]");
        
        // Setup default Hue properties
        when(hueProperties.getBridgeIp()).thenReturn("10.0.0.101");
        when(hueProperties.getApiKey()).thenReturn("test-api-key");
    }
    
    @Test
    @DisplayName("Should execute debug light colors without errors")
    void shouldExecuteDebugLightColorsWithoutErrors() {
        // Given
        setPrivateHueField();
        when(hue.getLights()).thenReturn(mockLights);
        
        // When & Then - should not throw exception
        hueService.debugLightColors();
        
        // Verify refresh was called to get latest state
        verify(hue).refresh();
        verify(hue).getLights();
    }
    
    @Test
    @DisplayName("Should handle empty lights in debug mode")
    void shouldHandleEmptyLightsInDebugMode() {
        // Given
        Map<UUID, Light> emptyLights = new HashMap<>();
        setPrivateHueField();
        when(hue.getLights()).thenReturn(emptyLights);
        
        // When & Then - should not throw exception
        hueService.debugLightColors();
        
        verify(hue).refresh();
        verify(hue).getLights();
    }
    
    @Test
    @DisplayName("Should execute raw API debug without errors")
    void shouldExecuteRawApiDebugWithoutErrors() {
        // Given
        setPrivateHueField();
        
        // When & Then - should not throw exception
        hueService.debugLightColorsWithRawApi();
        
        // Verify bridge properties were accessed
        verify(hueProperties).getBridgeIp();
        verify(hueProperties).getApiKey();
    }
    
    @Test
    @DisplayName("Should handle missing bridge IP in raw API debug")
    void shouldHandleMissingBridgeIpInRawApiDebug() {
        // Given
        when(hueProperties.getBridgeIp()).thenReturn(null);
        setPrivateHueField();
        
        // When & Then - should not throw exception
        hueService.debugLightColorsWithRawApi();
        
        verify(hueProperties).getBridgeIp();
    }
    
    @Test
    @DisplayName("Should handle missing API key in raw API debug")
    void shouldHandleMissingApiKeyInRawApiDebug() {
        // Given
        when(hueProperties.getApiKey()).thenReturn(null);
        setPrivateHueField();
        
        // When & Then - should not throw exception
        hueService.debugLightColorsWithRawApi();
        
        verify(hueProperties).getBridgeIp();
        verify(hueProperties).getApiKey();
    }
    
    @Test
    @DisplayName("Should handle Hue connection errors in debug mode")
    void shouldHandleHueConnectionErrorsInDebugMode() {
        // Given
        setPrivateHueField();
        doThrow(new RuntimeException("Connection error")).when(hue).refresh();
        
        // When & Then - should not throw exception
        hueService.debugLightColors();
        
        verify(hue).refresh();
    }
    
    @Test
    @DisplayName("Should handle no Hue connection in debug mode")
    void shouldHandleNoHueConnectionInDebugMode() {
        // Given - no hue connection set (hue field remains null)
        
        // When & Then - should not throw exception
        hueService.debugLightColors();
        hueService.debugLightColorsWithRawApi();
        
        // No verifications needed as methods should handle null connection gracefully
    }
    
    @Test
    @DisplayName("Should handle light errors during debug")
    void shouldHandleLightErrorsDuringDebug() {
        // Given
        setPrivateHueField();
        when(hue.getLights()).thenReturn(mockLights);
        when(light1.getName()).thenThrow(new RuntimeException("Light error"));
        
        // When & Then - should not throw exception
        hueService.debugLightColors();
        
        verify(hue).refresh();
        verify(hue).getLights();
    }
    
    private void setPrivateHueField() {
        // Use reflection to set the private hue field
        try {
            java.lang.reflect.Field hueField = HueService.class.getDeclaredField("hue");
            hueField.setAccessible(true);
            hueField.set(hueService, hue);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set private hue field", e);
        }
    }
}