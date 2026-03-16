package game.sprite.undo;

import game.sprite.PathPoint;
import game.sprite.PathSegmentType;
import game.sprite.Polygon;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Command to add a point to a polygon.
 */
public class AddPointCommand extends UndoRedoCommand {
    private final List<Polygon> polygons;
    private final int polygonIndex;
    private final PathPoint point;
    private final int pointIndex;
    
    public AddPointCommand(
        final List<Polygon> polygons,
        final int polygonIndex,
        final Point2D.Double point,
        final int pointIndex
    ) {
        this.polygons = polygons;
        this.polygonIndex = polygonIndex;
        // New points default to STRAIGHT segment type
        this.point = new PathPoint(PathSegmentType.STRAIGHT, (int)point.x, (int)point.y);
        this.pointIndex = pointIndex;
    }
    
    @Override
    public void execute() {
        final Polygon polygon = polygons.get(polygonIndex);
        final List<PathPoint> newPath = new java.util.ArrayList<>(polygon.path());
        newPath.add(pointIndex, point);
        polygons.set(polygonIndex, polygon.withPath(newPath));
    }
    
    @Override
    public void undo() {
        final Polygon polygon = polygons.get(polygonIndex);
        final List<PathPoint> newPath = new java.util.ArrayList<>(polygon.path());
        newPath.remove(pointIndex);
        polygons.set(polygonIndex, polygon.withPath(newPath));
    }
    
    @Override
    public String getDescription() {
        return "Add point";
    }
}
