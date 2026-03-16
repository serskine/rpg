package game.sprite;

import java.awt.geom.Point2D;
import java.util.Objects;

/**
 * Represents a circle sprite with center point and radius.
 */
public record Circle(
    String fillColor,
    String lineColor,
    Point2D.Double center,
    double radius
) implements Shape {
    public Circle {
        Objects.requireNonNull(center, "Center point cannot be null");
        if (radius < 0) {
            throw new IllegalArgumentException("Radius cannot be negative");
        }
    }

    @Override
    public ShapeType getType() {
        return ShapeType.CIRCLE;
    }

    /**
     * Creates a new Circle with updated fill color.
     */
    public Circle withFillColor(final String newFillColor) {
        return new Circle(newFillColor, lineColor, center, radius);
    }

    /**
     * Creates a new Circle with updated line color.
     */
    public Circle withLineColor(final String newLineColor) {
        return new Circle(fillColor, newLineColor, center, radius);
    }

    /**
     * Creates a new Circle with updated center.
     */
    public Circle withCenter(final Point2D.Double newCenter) {
        return new Circle(fillColor, lineColor, newCenter, radius);
    }

    /**
     * Creates a new Circle with updated radius.
     */
    public Circle withRadius(final double newRadius) {
        return new Circle(fillColor, lineColor, center, newRadius);
    }
}
