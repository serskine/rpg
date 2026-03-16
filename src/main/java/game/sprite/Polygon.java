package game.sprite;

import java.util.List;
import java.util.Objects;

/**
 * Represents a polygon sprite with fill and line colors and a path of points.
 * Each point in the path has an associated segment type that defines how the line
 * is drawn to the next point (straight, curved left, curved right, or no line).
 * By default polygons are closed, but can be open if isOpen is true.
 */
public record Polygon(
    String fillColor,
    String lineColor,
    List<PathPoint> path,
    boolean isOpen
) implements Shape {
    public Polygon {
        Objects.requireNonNull(path, "Path cannot be null");
        path = List.copyOf(path); // Make immutable
    }
    
    /**
     * Create a polygon that is closed by default.
     */
    public Polygon(final String fillColor, final String lineColor, final List<PathPoint> path) {
        this(fillColor, lineColor, path, false);
    }

    @Override
    public ShapeType getType() {
        return ShapeType.POLYGON;
    }

    /**
     * Creates a new Polygon with updated path.
     */
    public Polygon withPath(final List<PathPoint> newPath) {
        return new Polygon(fillColor, lineColor, newPath, isOpen);
    }

    /**
     * Creates a new Polygon with updated fill color.
     */
    public Polygon withFillColor(final String newFillColor) {
        return new Polygon(newFillColor, lineColor, path, isOpen);
    }

    /**
     * Creates a new Polygon with updated line color.
     */
    public Polygon withLineColor(final String newLineColor) {
        return new Polygon(fillColor, newLineColor, path, isOpen);
    }
    
    /**
     * Creates a new Polygon with updated open state.
     */
    public Polygon withIsOpen(final boolean newIsOpen) {
        return new Polygon(fillColor, lineColor, path, newIsOpen);
    }
    
    /**
     * Get the points list for backwards compatibility.
     * This extracts the Point2D.Double coordinates from the path.
     */
    public List<java.awt.geom.Point2D.Double> getPoints() {
        return path.stream()
            .map(PathPoint::point)
            .toList();
    }
}
