package game.sprite.undo;

import game.sprite.Polygon;
import java.util.List;

/**
 * Command to change a polygon's fill or line color.
 */
public class ChangeColorCommand extends UndoRedoCommand {
    private final List<Polygon> polygons;
    private final int polygonIndex;
    private final boolean isFillColor;
    private final String oldColor;
    private final String newColor;
    
    public ChangeColorCommand(
        final List<Polygon> polygons,
        final int polygonIndex,
        final boolean isFillColor,
        final String newColor
    ) {
        this.polygons = polygons;
        this.polygonIndex = polygonIndex;
        this.isFillColor = isFillColor;
        final Polygon polygon = polygons.get(polygonIndex);
        this.oldColor = isFillColor ? polygon.fillColor() : polygon.lineColor();
        this.newColor = newColor;
    }
    
    @Override
    public void execute() {
        changeColor(newColor);
    }
    
    @Override
    public void undo() {
        changeColor(oldColor);
    }
    
    private void changeColor(final String color) {
        final Polygon polygon = polygons.get(polygonIndex);
        final Polygon updated = isFillColor
            ? polygon.withFillColor(color)
            : polygon.withLineColor(color);
        polygons.set(polygonIndex, updated);
    }
    
    @Override
    public String getDescription() {
        return isFillColor ? "Change fill color" : "Change line color";
    }
}
