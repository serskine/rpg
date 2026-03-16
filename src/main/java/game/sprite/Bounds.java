package game.sprite;

/**
 * Represents the bounding rectangle for a sprite.
 * Defines the clipping bounds for sprite rendering.
 */
public record Bounds(
    int x,
    int y,
    int width,
    int height
) {
    public Bounds {
        if (width < 0) {
            throw new IllegalArgumentException("Width cannot be negative");
        }
        if (height < 0) {
            throw new IllegalArgumentException("Height cannot be negative");
        }
    }
}
