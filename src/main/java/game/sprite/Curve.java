package game.sprite;

import java.awt.geom.Point2D;
import java.util.Objects;

/**
 * Represents a cubic Bezier curve shape.
 * A Bezier curve is defined by a start point, two control points, and an end point.
 */
public record Curve(
    String fillColor,
    String lineColor,
    Point2D.Double start,
    Point2D.Double controlPoint1,
    Point2D.Double controlPoint2,
    Point2D.Double end
) implements Shape {
    public Curve {
        Objects.requireNonNull(start, "Start point cannot be null");
        Objects.requireNonNull(controlPoint1, "Control point 1 cannot be null");
        Objects.requireNonNull(controlPoint2, "Control point 2 cannot be null");
        Objects.requireNonNull(end, "End point cannot be null");
    }

    @Override
    public ShapeType getType() {
        return ShapeType.CURVE;
    }

    /**
     * Creates a new Curve with updated fill color.
     */
    public Curve withFillColor(final String newFillColor) {
        return new Curve(newFillColor, lineColor, start, controlPoint1, controlPoint2, end);
    }

    /**
     * Creates a new Curve with updated line color.
     */
    public Curve withLineColor(final String newLineColor) {
        return new Curve(fillColor, newLineColor, start, controlPoint1, controlPoint2, end);
    }

    /**
     * Creates a new Curve with updated start point.
     */
    public Curve withStart(final Point2D.Double newStart) {
        return new Curve(fillColor, lineColor, newStart, controlPoint1, controlPoint2, end);
    }

    /**
     * Creates a new Curve with updated first control point.
     */
    public Curve withControlPoint1(final Point2D.Double newControlPoint1) {
        return new Curve(fillColor, lineColor, start, newControlPoint1, controlPoint2, end);
    }

    /**
     * Creates a new Curve with updated second control point.
     */
    public Curve withControlPoint2(final Point2D.Double newControlPoint2) {
        return new Curve(fillColor, lineColor, start, controlPoint1, newControlPoint2, end);
    }

    /**
     * Creates a new Curve with updated end point.
     */
    public Curve withEnd(final Point2D.Double newEnd) {
        return new Curve(fillColor, lineColor, start, controlPoint1, controlPoint2, newEnd);
    }
}
