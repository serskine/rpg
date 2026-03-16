package game.sprite;

import java.awt.geom.Point2D;
import java.util.Objects;

/**
 * Represents a point in a polygon path with segment type information.
 * The segment type indicates how the line segment to the next point is drawn.
 */
public record PathPoint(
    PathSegmentType type,
    Point2D.Double point
) {
    public PathPoint {
        Objects.requireNonNull(type, "Segment type cannot be null");
        Objects.requireNonNull(point, "Point cannot be null");
    }
    
    /**
     * Convenience constructor using coordinates.
     */
    public PathPoint(final PathSegmentType type, final double x, final double y) {
        this(type, new Point2D.Double(x, y));
    }
    
    /**
     * Default constructor with STRAIGHT segment type.
     */
    public PathPoint(final double x, final double y) {
        this(PathSegmentType.STRAIGHT, new Point2D.Double(x, y));
    }
    
    /**
     * Get the x coordinate.
     */
    public double x() {
        return point.x;
    }
    
    /**
     * Get the y coordinate.
     */
    public double y() {
        return point.y;
    }
}
