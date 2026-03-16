package game.sprite.undo;

import game.sprite.PathPoint;
import game.sprite.Polygon;
import java.util.ArrayList;
import java.util.List;

/**
 * Command to reorder points in a polygon.
 */
public class ReorderPointsCommand extends UndoRedoCommand {
    private final List<Polygon> polygons;
    private final int polygonIndex;
    private final List<Integer> oldOrder;
    private final List<Integer> newOrder;
    
    public ReorderPointsCommand(
        final List<Polygon> polygons,
        final int polygonIndex,
        final List<Integer> newOrder
    ) {
        this.polygons = polygons;
        this.polygonIndex = polygonIndex;
        this.newOrder = new ArrayList<>(newOrder);
        
        // Create old order (identity mapping)
        this.oldOrder = new ArrayList<>();
        for (int i = 0; i < polygons.get(polygonIndex).path().size(); i++) {
            this.oldOrder.add(i);
        }
    }
    
    @Override
    public void execute() {
        reorderPoints(newOrder);
    }
    
    @Override
    public void undo() {
        reorderPoints(oldOrder);
    }
    
    private void reorderPoints(final List<Integer> order) {
        final Polygon polygon = polygons.get(polygonIndex);
        final List<PathPoint> originalPoints = new ArrayList<>(polygon.path());
        final List<PathPoint> reorderedPoints = new ArrayList<>();
        
        for (final int index : order) {
            reorderedPoints.add(originalPoints.get(index));
        }
        
        polygons.set(polygonIndex, polygon.withPath(reorderedPoints));
    }
    
    @Override
    public String getDescription() {
        return "Reorder points";
    }
}
