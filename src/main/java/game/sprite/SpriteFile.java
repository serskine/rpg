package game.sprite;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the contents of a sprite data file.
 * Contains a color palette, a list of shapes (polygons, circles, arcs, curves, paths), optional hit box, and tileSize.
 */
public record SpriteFile(
    Map<String, String> colors,
    List<Shape> shapes,
    HitBox hitBox,
    int tileSize
) {
    public SpriteFile {
        Objects.requireNonNull(colors, "Colors map cannot be null");
        Objects.requireNonNull(shapes, "Shapes list cannot be null");
        colors = Map.copyOf(colors);     // Make immutable
        shapes = List.copyOf(shapes); // Make immutable
    }
    
    /**
     * Create a SpriteFile with default hit box (0, 0, 0, 0) and tileSize of 8.
     */
    public SpriteFile(final Map<String, String> colors, final List<Shape> shapes) {
        this(colors, shapes, new HitBox(0, 0, 0, 0), 8);
    }
    
    /**
     * Create a SpriteFile with specified hit box and default tileSize of 8.
     */
    public SpriteFile(final Map<String, String> colors, final List<Shape> shapes, final HitBox hitBox) {
        this(colors, shapes, hitBox, 8);
    }
    
    /**
     * Get only the polygon shapes from the sprite file.
     */
    public List<Polygon> getPolygons() {
        return shapes.stream()
            .filter(shape -> shape instanceof Polygon)
            .map(shape -> (Polygon) shape)
            .collect(Collectors.toUnmodifiableList());
    }
    
    /**
     * Get only the circle shapes from the sprite file.
     */
    public List<Circle> getCircles() {
        return shapes.stream()
            .filter(shape -> shape instanceof Circle)
            .map(shape -> (Circle) shape)
            .collect(Collectors.toUnmodifiableList());
    }
    
    /**
     * Get only the arc shapes from the sprite file.
     */
    public List<Arc> getArcs() {
        return shapes.stream()
            .filter(shape -> shape instanceof Arc)
            .map(shape -> (Arc) shape)
            .collect(Collectors.toUnmodifiableList());
    }
    
    /**
     * Get only the line segment shapes from the sprite file.
     */
    public List<LineSegment> getLineSegments() {
        return shapes.stream()
            .filter(shape -> shape instanceof LineSegment)
            .map(shape -> (LineSegment) shape)
            .collect(Collectors.toUnmodifiableList());
    }
    
    /**
     * Get only the curve shapes from the sprite file.
     */
    public List<Curve> getCurves() {
        return shapes.stream()
            .filter(shape -> shape instanceof Curve)
            .map(shape -> (Curve) shape)
            .collect(Collectors.toUnmodifiableList());
    }
    
    /**
     * Get only the path shapes from the sprite file.
     */
    public List<Path> getPaths() {
        return shapes.stream()
            .filter(shape -> shape instanceof Path)
            .map(shape -> (Path) shape)
            .collect(Collectors.toUnmodifiableList());
    }
}
