package game.sprite;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard Java Color names with their hex representations.
 */
public class StandardColors {
    private static final Map<String, String> STANDARD_COLORS = new HashMap<>();
    
    static {
        // Named colors from java.awt.Color
        STANDARD_COLORS.put("black", "#000000");
        STANDARD_COLORS.put("blue", "#0000FF");
        STANDARD_COLORS.put("cyan", "#00FFFF");
        STANDARD_COLORS.put("darkGray", "#404040");
        STANDARD_COLORS.put("darkGrey", "#404040");
        STANDARD_COLORS.put("gray", "#808080");
        STANDARD_COLORS.put("grey", "#808080");
        STANDARD_COLORS.put("green", "#00FF00");
        STANDARD_COLORS.put("lightGray", "#C0C0C0");
        STANDARD_COLORS.put("lightGrey", "#C0C0C0");
        STANDARD_COLORS.put("magenta", "#FF00FF");
        STANDARD_COLORS.put("orange", "#FFC800");
        STANDARD_COLORS.put("pink", "#FFAFAF");
        STANDARD_COLORS.put("red", "#FF0000");
        STANDARD_COLORS.put("white", "#FFFFFF");
        STANDARD_COLORS.put("yellow", "#FFFF00");
    }
    
    /**
     * Get the hex color value for a standard color name.
     * Returns null if the color name is not recognized.
     */
    public static String getHexValue(final String colorName) {
        if (colorName == null) {
            return null;
        }
        return STANDARD_COLORS.get(colorName);
    }
    
    /**
     * Get all standard colors as a map.
     */
    public static Map<String, String> getAllColors() {
        return new HashMap<>(STANDARD_COLORS);
    }
}
