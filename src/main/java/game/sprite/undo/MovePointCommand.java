package game.sprite.undo;

import game.sprite.PathPoint;
import game.sprite.Polygon;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Command to move a point in a polygon.
 */
public class MovePointCommand extends UndoRedoCommand {
    private final List<Polygon> polygons;
    private final int polygonIndex;
    private final int pointIndex;
    private final PathPoint oldPoint;
    private final PathPoint newPoint;
    
    public MovePointCommand(
        final List<Polygon> polygons,
        final int polygonIndex,
        final int pointIndex,
        final Point2D.Double newCoords
    ) {
        this.polygons = polygons;
        this.polygonIndex = polygonIndex;
        this.pointIndex = pointIndex;
        this.oldPoint = polygons.get(polygonIndex).path().get(pointIndex);
        this.newPoint = new PathPoint(oldPoint.type(), (int)newCoords.x, (int)newCoords.y);
    }
    
    @Override
    public void execute() {
        final Polygon polygon = polygons.get(polygonIndex);
        final List<PathPoint> newPath = new java.util.ArrayList<>(polygon.path());
        newPath.set(pointIndex, newPoint);
        polygons.set(polygonIndex, polygon.withPath(newPath));
    }
    
    @Override
    public void undo() {
        final Polygon polygon = polygons.get(polygonIndex);
        final List<PathPoint> newPath = new java.util.ArrayList<>(polygon.path());
        newPath.set(pointIndex, oldPoint);
        polygons.set(polygonIndex, polygon.withPath(newPath));
    }
    
    @Override
    public String getDescription() {
        return "Move point";
    }
}
