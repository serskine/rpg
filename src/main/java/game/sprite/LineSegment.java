package game.sprite;

import java.util.List;
import java.util.Objects;

/**
 * Represents a line segment sprite with line color and a list of points with curve information.
 */
public record LineSegment(
    String lineColor,
    List<LinePoint> points
) implements Shape {
    public LineSegment {
        Objects.requireNonNull(points, "Points list cannot be null");
        points = List.copyOf(points); // Make immutable
    }

    @Override
    public String fillColor() {
        return null;  // Line segments don't have fill
    }

    @Override
    public ShapeType getType() {
        return ShapeType.LINE_SEGMENT;
    }

    /**
     * Creates a new LineSegment with updated points list.
     */
    public LineSegment withPoints(final List<LinePoint> newPoints) {
        return new LineSegment(lineColor, newPoints);
    }

    /**
     * Creates a new LineSegment with updated line color.
     */
    public LineSegment withLineColor(final String newLineColor) {
        return new LineSegment(newLineColor, points);
    }
}
