package game.sprite.undo;

import game.sprite.PathPoint;
import game.sprite.Polygon;
import java.util.List;

/**
 * Command to remove a point from a polygon.
 */
public class RemovePointCommand extends UndoRedoCommand {
    private final List<Polygon> polygons;
    private final int polygonIndex;
    private final PathPoint point;
    private final int pointIndex;
    
    public RemovePointCommand(
        final List<Polygon> polygons,
        final int polygonIndex,
        final int pointIndex
    ) {
        this.polygons = polygons;
        this.polygonIndex = polygonIndex;
        this.point = polygons.get(polygonIndex).path().get(pointIndex);
        this.pointIndex = pointIndex;
    }
    
    @Override
    public void execute() {
        final Polygon polygon = polygons.get(polygonIndex);
        final List<PathPoint> newPath = new java.util.ArrayList<>(polygon.path());
        newPath.remove(pointIndex);
        polygons.set(polygonIndex, polygon.withPath(newPath));
    }
    
    @Override
    public void undo() {
        final Polygon polygon = polygons.get(polygonIndex);
        final List<PathPoint> newPath = new java.util.ArrayList<>(polygon.path());
        newPath.add(pointIndex, point);
        polygons.set(polygonIndex, polygon.withPath(newPath));
    }
    
    @Override
    public String getDescription() {
        return "Remove point";
    }
}
