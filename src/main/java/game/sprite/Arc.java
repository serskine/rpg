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

    public double getDx() {
        return (end.getX() - start.getX());
    }

    public double getDy() {
        return (end.getY() - start.getY());
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
        final double dx = getDx();
        final double dy = getDy();

        if (dx==0D) {
            throw new RuntimeException("Arc cannot have zero width: " + dx);
        }

        if (dy==0D) {
            throw new RuntimeException("Arc cannot have zero height: " + dy);
        }

        if (dx < 0D) {
            if (dy < 0D) {
                // NW
                if (counterClockwise) {
                    return new Point2D.Double(start.getX(), end.getY()) ;
                } else {
                    return new Point2D.Double(end.getX(), start.getY());
                }
            } else {
                // SW
                if (counterClockwise) {
                    return new Point2D.Double(end.getX(), start.getY());
                } else {
                    return new Point2D.Double(start.getX(), end.getY());
                }
            }
        } else {
            if (dy < 0D) {
                // NE
                if (counterClockwise) {
                    return new Point2D.Double(end.getX(), start.getY());
                } else {
                    return new Point2D.Double(start.getX(), end.getY());
                }
            } else {
                // SE
                if (counterClockwise) {
                    return new Point2D.Double(start.getX(), end.getY());
                } else {
                    return new Point2D.Double(end.getX(), start.getY());
                }
            }
        }
    }
}
