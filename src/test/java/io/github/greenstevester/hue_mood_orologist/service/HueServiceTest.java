package io.github.greenstevester.hue_mood_orologist.service;

import io.github.greenstevester.hue_mood_orologist.config.ColorMappingProperties;
import io.github.greenstevester.hue_mood_orologist.config.HueProperties;
import io.github.greenstevester.hue_mood_orologist.model.WeatherAnalysis;
import io.github.greenstevester.heuvana.Color;
import io.github.greenstevester.heuvana.v2.Hue;
import io.github.greenstevester.heuvana.v2.Light;
import io.github.greenstevester.heuvana.v2.UpdateState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HueServiceTest {

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
        when(light2.getName()).thenReturn("Kitchen");
        
        // Setup default Hue properties
        when(hueProperties.getBridgeIp()).thenReturn("10.0.0.101");
        when(hueProperties.getApiKey()).thenReturn("test-api-key");
        when(hueProperties.isTargetAllLights()).thenReturn(true);
        
        // Setup default color mapping properties
        when(colorMappingProperties.isEnabled()).thenReturn(true);
        when(colorMappingProperties.getDefaultColor()).thenReturn("white");
        
        Map<String, String> defaultMappings = new HashMap<>();
        defaultMappings.put("rain", "light purple");
        defaultMappings.put("showers", "dark purple");
        defaultMappings.put("sunshine", "yellow");
        defaultMappings.put("overcast", "grey");
        defaultMappings.put("snow", "white");
        defaultMappings.put("cold", "warm yellow");
        defaultMappings.put("clear", "cool white");
        when(colorMappingProperties.getConditions()).thenReturn(defaultMappings);
    }
    
    @Test
    @DisplayName("Should control all lights with rain weather condition")
    void shouldControlAllLightsWithRainCondition() {
        // Given
        WeatherAnalysis rainAnalysis = createWeatherAnalysis("rain", true, false);
        Color expectedColor = Color.of(221, 160, 221); // Light purple
        
        when(colorParserService.parseColor("light purple")).thenReturn(expectedColor);
        
        // Setup HueService with mock Hue connection
        setPrivateHueField();
        when(hue.getLights()).thenReturn(mockLights);
        
        // When
        hueService.setLightMoodForWeather(rainAnalysis);
        
        // Then
        verify(colorParserService).parseColor("light purple");
        
        ArgumentCaptor<UpdateState> stateCaptor = ArgumentCaptor.forClass(UpdateState.class);
        verify(light1).setState(stateCaptor.capture());
        verify(light2).setState(stateCaptor.capture());
        
        // Verify both lights received update commands
        assertThat(stateCaptor.getAllValues()).hasSize(2);
    }
    
    @Test
    @DisplayName("Should control all lights with showers weather condition")
    void shouldControlAllLightsWithShowersCondition() {
        // Given
        WeatherAnalysis showersAnalysis = createWeatherAnalysis("showers", true, false);
        Color expectedColor = Color.of(72, 61, 139); // Dark purple
        
        when(colorParserService.parseColor("dark purple")).thenReturn(expectedColor);
        
        setPrivateHueField();
        when(hue.getLights()).thenReturn(mockLights);
        
        // When
        hueService.setLightMoodForWeather(showersAnalysis);
        
        // Then
        verify(colorParserService).parseColor("dark purple");
        verify(light1).setState(any(UpdateState.class));
        verify(light2).setState(any(UpdateState.class));
    }
    
    @Test
    @DisplayName("Should control all lights with snow weather condition")
    void shouldControlAllLightsWithSnowCondition() {
        // Given
        WeatherAnalysis snowAnalysis = createWeatherAnalysis("snow", false, true);
        Color expectedColor = Color.of(255, 255, 255); // White
        
        when(colorParserService.parseColor("white")).thenReturn(expectedColor);
        
        setPrivateHueField();
        when(hue.getLights()).thenReturn(mockLights);
        
        // When
        hueService.setLightMoodForWeather(snowAnalysis);
        
        // Then
        verify(colorParserService).parseColor("white");
        verify(light1).setState(any(UpdateState.class));
        verify(light2).setState(any(UpdateState.class));
    }
    
    @Test
    @DisplayName("Should control all lights with sunshine weather condition")
    void shouldControlAllLightsWithSunshineCondition() {
        // Given
        WeatherAnalysis sunshineAnalysis = createWeatherAnalysis("sunshine", false, false);
        Color expectedColor = Color.of(255, 255, 0); // Yellow
        
        when(colorParserService.parseColor("yellow")).thenReturn(expectedColor);
        
        setPrivateHueField();
        when(hue.getLights()).thenReturn(mockLights);
        
        // When
        hueService.setLightMoodForWeather(sunshineAnalysis);
        
        // Then
        verify(colorParserService).parseColor("yellow");
        verify(light1).setState(any(UpdateState.class));
        verify(light2).setState(any(UpdateState.class));
    }
    
    @Test
    @DisplayName("Should control all lights with overcast weather condition")
    void shouldControlAllLightsWithOvercastCondition() {
        // Given
        WeatherAnalysis overcastAnalysis = createWeatherAnalysis("overcast", false, false);
        Color expectedColor = Color.of(128, 128, 128); // Grey
        
        when(colorParserService.parseColor("grey")).thenReturn(expectedColor);
        
        setPrivateHueField();
        when(hue.getLights()).thenReturn(mockLights);
        
        // When
        hueService.setLightMoodForWeather(overcastAnalysis);
        
        // Then
        verify(colorParserService).parseColor("grey");
        verify(light1).setState(any(UpdateState.class));
        verify(light2).setState(any(UpdateState.class));
    }
    
    @Test
    @DisplayName("Should control all lights with cold weather condition")
    void shouldControlAllLightsWithColdCondition() {
        // Given
        WeatherAnalysis coldAnalysis = createWeatherAnalysis("cold", false, true);
        Color expectedColor = Color.of(255, 223, 0); // Warm yellow
        
        when(colorParserService.parseColor("warm yellow")).thenReturn(expectedColor);
        
        setPrivateHueField();
        when(hue.getLights()).thenReturn(mockLights);
        
        // When
        hueService.setLightMoodForWeather(coldAnalysis);
        
        // Then
        verify(colorParserService).parseColor("warm yellow");
        verify(light1).setState(any(UpdateState.class));
        verify(light2).setState(any(UpdateState.class));
    }
    
    @Test
    @DisplayName("Should control all lights with clear weather condition")
    void shouldControlAllLightsWithClearCondition() {
        // Given
        WeatherAnalysis clearAnalysis = createWeatherAnalysis("clear", false, false);
        Color expectedColor = Color.of(248, 248, 255); // Cool white
        
        when(colorParserService.parseColor("cool white")).thenReturn(expectedColor);
        
        setPrivateHueField();
        when(hue.getLights()).thenReturn(mockLights);
        
        // When
        hueService.setLightMoodForWeather(clearAnalysis);
        
        // Then
        verify(colorParserService).parseColor("cool white");
        verify(light1).setState(any(UpdateState.class));
        verify(light2).setState(any(UpdateState.class));
    }
    
    @Test
    @DisplayName("Should target specific light when configured")
    void shouldTargetSpecificLightWhenConfigured() {
        // Given
        when(hueProperties.isTargetAllLights()).thenReturn(false);
        when(hueProperties.getTargetLightName()).thenReturn("Living Room");
        
        WeatherAnalysis analysis = createWeatherAnalysis("rain", true, false);
        Color expectedColor = Color.of(221, 160, 221);
        when(colorParserService.parseColor("light purple")).thenReturn(expectedColor);
        
        setPrivateHueField();
        when(hue.getLights()).thenReturn(mockLights);
        
        // When
        hueService.setLightMoodForWeather(analysis);
        
        // Then
        verify(light1).setState(any(UpdateState.class)); // Living Room should be controlled
        verify(light2, never()).setState(any(UpdateState.class)); // Kitchen should not be controlled
    }
    
    @Test
    @DisplayName("Should use default color for unmapped weather condition")
    void shouldUseDefaultColorForUnmappedCondition() {
        // Given
        WeatherAnalysis unknownAnalysis = createWeatherAnalysis("unknown", false, false);
        Color defaultColor = Color.of(255, 255, 255); // White
        
        when(colorParserService.parseColor("white")).thenReturn(defaultColor);
        
        setPrivateHueField();
        when(hue.getLights()).thenReturn(mockLights);
        
        // When
        hueService.setLightMoodForWeather(unknownAnalysis);
        
        // Then
        verify(colorParserService).parseColor("white"); // Should use default color
        verify(light1).setState(any(UpdateState.class));
        verify(light2).setState(any(UpdateState.class));
    }
    
    @Test
    @DisplayName("Should use legacy logic when color mapping is disabled")
    void shouldUseLegacyLogicWhenColorMappingDisabled() {
        // Given
        when(colorMappingProperties.isEnabled()).thenReturn(false);
        
        WeatherAnalysis rainAnalysis = createWeatherAnalysis("rain", true, false);
        
        setPrivateHueField();
        when(hue.getLights()).thenReturn(mockLights);
        
        // When
        hueService.setLightMoodForWeather(rainAnalysis);
        
        // Then
        verify(colorParserService, never()).parseColor(anyString()); // Should not use color parser
        verify(light1).setState(any(UpdateState.class));
        verify(light2).setState(any(UpdateState.class));
    }
    
    @Test
    @DisplayName("Should handle legacy method with boolean parameters")
    void shouldHandleLegacyMethodWithBooleanParameters() {
        // Given
        setPrivateHueField();
        when(hue.getLights()).thenReturn(mockLights);
        when(colorParserService.parseColor("light purple")).thenReturn(Color.of(221, 160, 221));
        
        // When
        hueService.setLightMoodForWeather(true, false); // isRaining=true, isCold=false
        
        // Then
        verify(colorParserService).parseColor("light purple"); // Should map to rain condition
        verify(light1).setState(any(UpdateState.class));
        verify(light2).setState(any(UpdateState.class));
    }
    
    @Test
    @DisplayName("Should handle no lights gracefully")
    void shouldHandleNoLightsGracefully() {
        // Given
        Map<UUID, Light> emptyLights = new HashMap<>();
        WeatherAnalysis analysis = createWeatherAnalysis("rain", true, false);
        
        setPrivateHueField();
        when(hue.getLights()).thenReturn(emptyLights);
        
        // When & Then - should not throw exception
        hueService.setLightMoodForWeather(analysis);
        
        // No lights to verify, but method should execute without error
    }
    
    @Test
    @DisplayName("Should handle light control errors gracefully")
    void shouldHandleLightControlErrorsGracefully() {
        // Given
        WeatherAnalysis analysis = createWeatherAnalysis("rain", true, false);
        Color expectedColor = Color.of(221, 160, 221);
        when(colorParserService.parseColor("light purple")).thenReturn(expectedColor);
        
        setPrivateHueField();
        when(hue.getLights()).thenReturn(mockLights);
        
        // Make one light throw an exception
        doThrow(new RuntimeException("Light error")).when(light1).setState(any(UpdateState.class));
        
        // When & Then - should not throw exception
        hueService.setLightMoodForWeather(analysis);
        
        // Should still try to control the second light
        verify(light2).setState(any(UpdateState.class));
    }
    
    private WeatherAnalysis createWeatherAnalysis(String condition, boolean isRaining, boolean isCold) {
        return WeatherAnalysis.builder()
            .analysisTime(LocalDateTime.now())
            .weatherCondition(condition)
            .isRaining(isRaining)
            .isVeryCold(isCold)
            .summary("Test weather: " + condition)
            .currentTemperature(15.0)
            .build();
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