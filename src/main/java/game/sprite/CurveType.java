package game.sprite;

/**
 * Represents the curve type at a point on a line segment.
 */
public enum CurveType {
    NONE,
    LEFT,
    RIGHT;
    
    /**
     * Parse a curve type from a string (case-insensitive).
     */
    public static CurveType parse(final String str) {
        if (str == null) {
            return NONE;
        }
        return CurveType.valueOf(str.toUpperCase());
    }
}
