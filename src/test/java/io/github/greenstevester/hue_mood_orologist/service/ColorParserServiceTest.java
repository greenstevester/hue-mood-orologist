package io.github.greenstevester.hue_mood_orologist.service;

import io.github.greenstevester.heuvana.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ColorParserServiceTest {

    private ColorParserService colorParserService;
    
    @BeforeEach
    void setUp() {
        colorParserService = new ColorParserService();
    }
    
    @ParameterizedTest
    @DisplayName("Should parse basic named colors correctly")
    @CsvSource({
        "red, 255, 0, 0",
        "green, 0, 255, 0", 
        "blue, 0, 0, 255",
        "yellow, 255, 255, 0",
        "purple, 128, 0, 128",
        "white, 255, 255, 255",
        "black, 0, 0, 0"
    })
    void shouldParseBasicNamedColors(String colorName, int expectedR, int expectedG, int expectedB) {
        // When
        Color color = colorParserService.parseColor(colorName);
        
        // Then
        assertThat(color).isNotNull();
        // Note: We can't directly access RGB values from Color object, 
        // so we test by comparing with known Color instances
        Color expected = Color.of(expectedR, expectedG, expectedB);
        assertThat(color.toString()).isEqualTo(expected.toString());
    }
    
    @ParameterizedTest
    @DisplayName("Should parse weather-specific named colors correctly")
    @CsvSource({
        "'light purple', 221, 160, 221",
        "'dark purple', 72, 61, 139",
        "'light blue', 173, 216, 230",
        "'warm white', 255, 239, 213",
        "'cool white', 248, 248, 255",
        "'sunshine yellow', 255, 215, 0",
        "'storm grey', 112, 128, 144",
        "'snow white', 255, 250, 250"
    })
    void shouldParseWeatherSpecificColors(String colorName, int expectedR, int expectedG, int expectedB) {
        // When
        Color color = colorParserService.parseColor(colorName);
        
        // Then
        assertThat(color).isNotNull();
        Color expected = Color.of(expectedR, expectedG, expectedB);
        assertThat(color.toString()).isEqualTo(expected.toString());
    }
    
    @ParameterizedTest
    @DisplayName("Should parse hex colors correctly")
    @CsvSource({
        "#FF0000, 255, 0, 0",    // Red
        "#00FF00, 0, 255, 0",    // Green  
        "#0000FF, 0, 0, 255",    // Blue
        "#FFFFFF, 255, 255, 255", // White
        "#000000, 0, 0, 0",      // Black
        "#4B0082, 75, 0, 130",   // Indigo
        "#FFA500, 255, 165, 0"   // Orange
    })
    void shouldParseHexColors(String hexColor, int expectedR, int expectedG, int expectedB) {
        // When
        Color color = colorParserService.parseColor(hexColor);
        
        // Then
        assertThat(color).isNotNull();
        Color expected = Color.of(expectedR, expectedG, expectedB);
        assertThat(color.toString()).isEqualTo(expected.toString());
    }
    
    @ParameterizedTest
    @DisplayName("Should parse 3-digit hex colors correctly")
    @CsvSource({
        "#F00, 255, 0, 0",    // Red shorthand
        "#0F0, 0, 255, 0",    // Green shorthand
        "#00F, 0, 0, 255",    // Blue shorthand
        "#FFF, 255, 255, 255", // White shorthand
        "#000, 0, 0, 0"       // Black shorthand
    })
    void shouldParse3DigitHexColors(String hexColor, int expectedR, int expectedG, int expectedB) {
        // When
        Color color = colorParserService.parseColor(hexColor);
        
        // Then
        assertThat(color).isNotNull();
        Color expected = Color.of(expectedR, expectedG, expectedB);
        assertThat(color.toString()).isEqualTo(expected.toString());
    }
    
    @ParameterizedTest
    @DisplayName("Should parse RGB tuple colors correctly")
    @CsvSource({
        "'255,0,0', 255, 0, 0",
        "'0,255,0', 0, 255, 0",
        "'0,0,255', 0, 0, 255", 
        "'128,128,128', 128, 128, 128",
        "'255, 165, 0', 255, 165, 0",  // With spaces
        "'75,0,130', 75, 0, 130"
    })
    void shouldParseRgbTupleColors(String rgbColor, int expectedR, int expectedG, int expectedB) {
        // When
        Color color = colorParserService.parseColor(rgbColor);
        
        // Then
        assertThat(color).isNotNull();
        Color expected = Color.of(expectedR, expectedG, expectedB);
        assertThat(color.toString()).isEqualTo(expected.toString());
    }
    
    @Test
    @DisplayName("Should handle case insensitive color names")
    void shouldHandleCaseInsensitiveColorNames() {
        // Given
        String[] colorVariations = {"RED", "Red", "red", "rEd"};
        
        // When & Then
        for (String colorName : colorVariations) {
            Color color = colorParserService.parseColor(colorName);
            Color expectedRed = Color.of(255, 0, 0);
            assertThat(color.toString()).isEqualTo(expectedRed.toString());
        }
    }
    
    @Test
    @DisplayName("Should handle empty or null color strings")
    void shouldHandleEmptyOrNullColorStrings() {
        // When
        Color nullColor = colorParserService.parseColor(null);
        Color emptyColor = colorParserService.parseColor("");
        Color whitespaceColor = colorParserService.parseColor("   ");
        
        // Then - should default to white
        Color expectedWhite = Color.of(255, 255, 255);
        assertThat(nullColor.toString()).isEqualTo(expectedWhite.toString());
        assertThat(emptyColor.toString()).isEqualTo(expectedWhite.toString());
        assertThat(whitespaceColor.toString()).isEqualTo(expectedWhite.toString());
    }
    
    @Test
    @DisplayName("Should handle unknown color names by defaulting to white")
    void shouldHandleUnknownColorNames() {
        // When
        Color unknownColor = colorParserService.parseColor("unknowncolor");
        Color weirdColor = colorParserService.parseColor("blurplish");
        
        // Then - should default to white
        Color expectedWhite = Color.of(255, 255, 255);
        assertThat(unknownColor.toString()).isEqualTo(expectedWhite.toString());
        assertThat(weirdColor.toString()).isEqualTo(expectedWhite.toString());
    }
    
    @Test
    @DisplayName("Should handle partial color name matches")
    void shouldHandlePartialColorNameMatches() {
        // When - should find "purple" in "light purple"
        Color lightPurpleVariant = colorParserService.parseColor("purple light");
        
        // Then - should match something containing purple
        assertThat(lightPurpleVariant).isNotNull();
    }
    
    @ParameterizedTest
    @DisplayName("Should handle invalid hex colors gracefully")
    @CsvSource({
        "#GGGGGG",  // Invalid hex characters
        "#FF00",    // Too short
        "#FF000000", // Too long
        "#",        // Just hash
        "FF0000"    // Missing hash
    })
    void shouldHandleInvalidHexColors(String invalidHex) {
        // When
        Color color = colorParserService.parseColor(invalidHex);
        
        // Then - should default to white for invalid hex
        Color expectedWhite = Color.of(255, 255, 255);
        assertThat(color.toString()).isEqualTo(expectedWhite.toString());
    }
    
    @ParameterizedTest
    @DisplayName("Should handle invalid RGB tuples gracefully")
    @CsvSource({
        "'256,0,0'",     // Out of range
        "'255,0'",       // Too few values
        "'255,0,0,0'",   // Too many values
        "'abc,0,0'",     // Non-numeric
        "'-1,0,0'"       // Negative values
    })
    void shouldHandleInvalidRgbTuples(String invalidRgb) {
        // When
        Color color = colorParserService.parseColor(invalidRgb);
        
        // Then - should default to white for invalid RGB
        Color expectedWhite = Color.of(255, 255, 255);
        assertThat(color.toString()).isEqualTo(expectedWhite.toString());
    }
    
    @Test
    @DisplayName("Should provide available colors map")
    void shouldProvideAvailableColorsMap() {
        // When
        var availableColors = colorParserService.getAvailableColors();
        
        // Then
        assertThat(availableColors).isNotEmpty();
        assertThat(availableColors).containsKey("red");
        assertThat(availableColors).containsKey("light purple");
        assertThat(availableColors).containsKey("sunshine yellow");
        
        // Verify red color RGB values
        int[] redRgb = availableColors.get("red");
        assertThat(redRgb).containsExactly(255, 0, 0);
    }
}