package io.github.greenstevester.hue_mood_orologist.service;

import io.github.greenstevester.heuvana.Color;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ColorParserService {

    private static final Map<String, int[]> NAMED_COLORS = new HashMap<>();
    
    static {
        // Basic colors
        NAMED_COLORS.put("red", new int[]{255, 0, 0});
        NAMED_COLORS.put("green", new int[]{0, 255, 0});
        NAMED_COLORS.put("blue", new int[]{0, 0, 255});
        NAMED_COLORS.put("yellow", new int[]{255, 255, 0});
        NAMED_COLORS.put("purple", new int[]{128, 0, 128});
        NAMED_COLORS.put("orange", new int[]{255, 165, 0});
        NAMED_COLORS.put("white", new int[]{255, 255, 255});
        NAMED_COLORS.put("black", new int[]{0, 0, 0});
        NAMED_COLORS.put("grey", new int[]{128, 128, 128});
        NAMED_COLORS.put("gray", new int[]{128, 128, 128});
        NAMED_COLORS.put("pink", new int[]{255, 192, 203});
        NAMED_COLORS.put("cyan", new int[]{0, 255, 255});
        NAMED_COLORS.put("magenta", new int[]{255, 0, 255});
        
        // Weather-specific colors
        NAMED_COLORS.put("light purple", new int[]{221, 160, 221});
        NAMED_COLORS.put("dark purple", new int[]{72, 61, 139});
        NAMED_COLORS.put("light blue", new int[]{173, 216, 230});
        NAMED_COLORS.put("dark blue", new int[]{0, 0, 139});
        NAMED_COLORS.put("light grey", new int[]{211, 211, 211});
        NAMED_COLORS.put("light gray", new int[]{211, 211, 211});
        NAMED_COLORS.put("dark grey", new int[]{169, 169, 169});
        NAMED_COLORS.put("dark gray", new int[]{169, 169, 169});
        NAMED_COLORS.put("warm white", new int[]{255, 239, 213});
        NAMED_COLORS.put("cool white", new int[]{248, 248, 255});
        NAMED_COLORS.put("warm yellow", new int[]{255, 223, 0});
        NAMED_COLORS.put("light orange", new int[]{255, 204, 153});
        NAMED_COLORS.put("dark orange", new int[]{255, 140, 0});
        
        // Sky/weather colors
        NAMED_COLORS.put("sky blue", new int[]{135, 206, 235});
        NAMED_COLORS.put("storm grey", new int[]{112, 128, 144});
        NAMED_COLORS.put("storm gray", new int[]{112, 128, 144});
        NAMED_COLORS.put("rain blue", new int[]{100, 149, 237});
        NAMED_COLORS.put("snow white", new int[]{255, 250, 250});
        NAMED_COLORS.put("sunshine yellow", new int[]{255, 215, 0});
        NAMED_COLORS.put("overcast grey", new int[]{128, 128, 128});
        NAMED_COLORS.put("overcast gray", new int[]{128, 128, 128});
    }
    
    /**
     * Parse a color string into a Huevana Color object
     * Supports named colors, hex colors (#RRGGBB), and RGB values (r,g,b)
     */
    public Color parseColor(String colorString) {
        if (colorString == null || colorString.trim().isEmpty()) {
            log.warn("Empty color string, using default white");
            return Color.of(255, 255, 255);
        }
        
        String color = colorString.trim().toLowerCase();
        
        try {
            // Try hex color (#RRGGBB or #RGB)
            if (color.startsWith("#")) {
                return parseHexColor(color);
            }
            
            // Try RGB format (r,g,b)
            if (color.contains(",")) {
                return parseRgbColor(color);
            }
            
            // Try named color
            int[] rgb = NAMED_COLORS.get(color);
            if (rgb != null) {
                log.debug("Parsed color '{}' as RGB({}, {}, {})", colorString, rgb[0], rgb[1], rgb[2]);
                return Color.of(rgb[0], rgb[1], rgb[2]);
            }
            
            // Fallback: try partial matches for compound color names
            for (Map.Entry<String, int[]> entry : NAMED_COLORS.entrySet()) {
                if (entry.getKey().contains(color) || color.contains(entry.getKey())) {
                    rgb = entry.getValue();
                    log.debug("Partial match for '{}' -> '{}' as RGB({}, {}, {})", 
                            colorString, entry.getKey(), rgb[0], rgb[1], rgb[2]);
                    return Color.of(rgb[0], rgb[1], rgb[2]);
                }
            }
            
            log.warn("Unknown color '{}', using default white", colorString);
            return Color.of(255, 255, 255);
            
        } catch (Exception e) {
            log.error("Error parsing color '{}', using default white: {}", colorString, e.getMessage());
            return Color.of(255, 255, 255);
        }
    }
    
    private Color parseHexColor(String hex) {
        // Remove # and handle 3-digit hex
        String hexValue = hex.substring(1);
        if (hexValue.length() == 3) {
            // Convert #RGB to #RRGGBB
            hexValue = String.valueOf(hexValue.charAt(0)) + hexValue.charAt(0) +
                      hexValue.charAt(1) + hexValue.charAt(1) +
                      hexValue.charAt(2) + hexValue.charAt(2);
        }
        
        if (hexValue.length() != 6) {
            throw new IllegalArgumentException("Invalid hex color format: " + hex);
        }
        
        int r = Integer.parseInt(hexValue.substring(0, 2), 16);
        int g = Integer.parseInt(hexValue.substring(2, 4), 16);
        int b = Integer.parseInt(hexValue.substring(4, 6), 16);
        
        log.debug("Parsed hex color '{}' as RGB({}, {}, {})", hex, r, g, b);
        return Color.of(r, g, b);
    }
    
    private Color parseRgbColor(String rgb) {
        String[] parts = rgb.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("RGB format must be 'r,g,b': " + rgb);
        }
        
        int r = Integer.parseInt(parts[0].trim());
        int g = Integer.parseInt(parts[1].trim());
        int b = Integer.parseInt(parts[2].trim());
        
        // Validate range
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            throw new IllegalArgumentException("RGB values must be 0-255: " + rgb);
        }
        
        log.debug("Parsed RGB color '{}' as RGB({}, {}, {})", rgb, r, g, b);
        return Color.of(r, g, b);
    }
    
    /**
     * Get all available named colors for documentation
     */
    public Map<String, int[]> getAvailableColors() {
        return new HashMap<>(NAMED_COLORS);
    }
}