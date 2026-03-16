package game.sprite;

/**
 * Represents the type of curve/line segment in a polygon path.
 */
public enum PathSegmentType {
    STRAIGHT,  // Straight line segment (default)
    NONE,      // No line rendered (only impacts fill)
    LEFT,      // Curve with ellipse center on the left side
    RIGHT;     // Curve with ellipse center on the right side
    
    /**
     * Parse a segment type from a string (case-insensitive).
     */
    public static PathSegmentType parse(final String str) {
        if (str == null) {
            return STRAIGHT;  // Default to straight
        }
        return PathSegmentType.valueOf(str.toUpperCase());
    }
}
