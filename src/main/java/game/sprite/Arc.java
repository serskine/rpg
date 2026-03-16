package game.sprite;

import java.awt.geom.Point2D;
import java.util.Objects;

/**
 * Represents an arc sprite - a curved line from start to end point.
 * The arc curves such that the axis from start meets perpendicular axis at destination.
 */
public record Arc(
    String fillColor,
    String lineColor,
    Point2D.Double start,
    Point2D.Double end,
    boolean counterClockwise
) implements Shape {
    public Arc {
        Objects.requireNonNull(start, "Start point cannot be null");
        Objects.requireNonNull(end, "End point cannot be null");
    }

    @Override
    public ShapeType getType() {
        return ShapeType.ARC;
    }

    /**
     * Creates a new Arc with updated fill color.
     */
    public Arc withFillColor(final String newFillColor) {
        return new Arc(newFillColor, lineColor, start, end, counterClockwise);
    }

    /**
     * Creates a new Arc with updated line color.
     */
    public Arc withLineColor(final String newLineColor) {
        return new Arc(fillColor, newLineColor, start, end, counterClockwise);
    }

    /**
     * Creates a new Arc with updated start point.
     */
    public Arc withStart(final Point2D.Double newStart) {
        return new Arc(fillColor, lineColor, newStart, end, counterClockwise);
    }

    /**
     * Creates a new Arc with updated end point.
     */
    public Arc withEnd(final Point2D.Double newEnd) {
        return new Arc(fillColor, lineColor, start, newEnd, counterClockwise);
    }

    /**
     * Creates a new Arc with updated direction.
     */
    public Arc withCounterClockwise(final boolean newCounterClockwise) {
        return new Arc(fillColor, lineColor, start, end, newCounterClockwise);
    }

    /**
     * Calculate the control point for rendering the arc.
     * This is the intersection of the two perpendicular axes.
     * @return the control point that determines the arc curve
     */
    public Point2D.Double getControlPoint() {
        // Axis from start: the direction is (end - start)
        final double dx = end.x - start.x;
        final double dy = end.y - start.y;
        
        // Perpendicular axis at end: perpendicular to (dx, dy)
        // Perpendicular vectors to (dx, dy) are (-dy, dx) or (dy, -dx)
        
        // The control point is where these axes intersect
        // Line from start in direction (dx, dy): start + t*(dx, dy)
        // Line from end in perpendicular direction: end + s*(perpendicular)
        
        // Choose perpendicular direction based on counterClockwise flag
        final double perpX = counterClockwise ? -dy : dy;
        final double perpY = counterClockwise ? dx : -dx;
        
        // Find intersection of:
        // P1: start + t*(dx, dy)
        // P2: end + s*(perpX, perpY)
        // 
        // start.x + t*dx = end.x + s*perpX
        // start.y + t*dy = end.y + s*perpY
        
        // Solving for t:
        // t*dx - s*perpX = end.x - start.x
        // t*dy - s*perpY = end.y - start.y
        
        // Using Cramer's rule:
        final double det = dx * (-perpY) - dy * (-perpX);
        if (Math.abs(det) < 1e-10) {
            // Lines are parallel, return midpoint
            return new Point2D.Double((start.x + end.x) / 2, (start.y + end.y) / 2);
        }
        
        final double t = ((end.x - start.x) * (-perpY) - (end.y - start.y) * (-perpX)) / det;
        
        return new Point2D.Double(
            start.x + t * dx,
            start.y + t * dy
        );
    }
}
