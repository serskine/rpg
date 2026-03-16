package game.sprite;

/**
 * Base interface for all sprite shapes (polygons, circles, arcs, curves, paths).
 */
public interface Shape {
    /**
     * Get the fill color name for this shape.
     * @return color name or null if no fill
     */
    String fillColor();
    
    /**
     * Get the line color name for this shape.
     * @return color name or null if no line
     */
    String lineColor();
    
    /**
     * Get the type of this shape.
     * @return shape type
     */
    ShapeType getType();
    
    /**
     * Enum for shape types.
     */
    enum ShapeType {
        POLYGON,
        CIRCLE,
        ARC,
        LINE_SEGMENT,
        CURVE,
        PATH
    }
}
