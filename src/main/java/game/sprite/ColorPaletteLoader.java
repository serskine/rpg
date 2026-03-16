package game.sprite;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for loading and converting color definitions from sprite files.
 */
public class ColorPaletteLoader {
    
    /**
     * Convert a hex color string to a Color object.
     * Handles formats like '#000000' or 'FF0000'.
     */
    public static Color hexToColor(final String hex) {
        if (hex == null || hex.isEmpty()) {
            return null;
        }
        
        String cleanHex = hex.startsWith("#") ? hex.substring(1) : hex;
        try {
            return new Color(Integer.parseInt(cleanHex, 16));
        } catch (final NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Get the Color object for a named color from the sprite file's color palette.
     * Returns null if the color name is not found in the palette.
     */
    public static Color getColor(final String colorName, final Map<String, String> colorPalette) {
        if (colorName == null || colorPalette == null) {
            return null;
        }
        
        final String hexValue = colorPalette.get(colorName);
        if (hexValue == null) {
            return null;
        }
        
        return hexToColor(hexValue);
    }
    
    /**
     * Create a cache map of color names to Color objects.
     */
    public static Map<String, Color> createColorCache(final Map<String, String> colorPalette) {
        final Map<String, Color> cache = new HashMap<>();
        for (final Map.Entry<String, String> entry : colorPalette.entrySet()) {
            final Color color = hexToColor(entry.getValue());
            if (color != null) {
                cache.put(entry.getKey(), color);
            }
        }
        return cache;
    }
}
