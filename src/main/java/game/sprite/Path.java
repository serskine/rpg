package game.sprite;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Objects;

/**
 * Represents a path shape - a sequence of curves forming a polyline with smooth curves.
 */
public record Path(
    String fillColor,
    String lineColor,
    List<Point2D.Double> points
) implements Shape {
    public Path {
        Objects.requireNonNull(points, "Points list cannot be null");
        points = List.copyOf(points); // Make immutable
    }

    @Override
    public ShapeType getType() {
        return ShapeType.PATH;
    }

    /**
     * Creates a new Path with updated fill color.
     */
    public Path withFillColor(final String newFillColor) {
        return new Path(newFillColor, lineColor, points);
    }

    /**
     * Creates a new Path with updated line color.
     */
    public Path withLineColor(final String newLineColor) {
        return new Path(fillColor, newLineColor, points);
    }

    /**
     * Creates a new Path with updated points list.
     */
    public Path withPoints(final List<Point2D.Double> newPoints) {
        return new Path(fillColor, lineColor, newPoints);
    }
}
