package game.sprite;

import java.awt.geom.Point2D;
import java.util.Objects;

/**
 * Represents a point on a line segment with curve information.
 */
public record LinePoint(
    CurveType curve,
    Point2D.Double point
) {
    public LinePoint {
        Objects.requireNonNull(curve, "Curve type cannot be null");
        Objects.requireNonNull(point, "Point cannot be null");
    }
    
    /**
     * Convenience method to get X coordinate.
     */
    public double x() {
        return point.x;
    }
    
    /**
     * Convenience method to get Y coordinate.
     */
    public double y() {
        return point.y;
    }
}
