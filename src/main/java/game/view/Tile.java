package game.view;

import game.common.Direction;
import game.util.Geom;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Tile {

    private final Map<Direction, Edge> wallColors = new HashMap<>();

    public Color floorColor;

    public final void render(Graphics2D G, Rectangle R) {
        renderFloor(G, R);
        for(Direction dir : Direction.values()) {
            renderEdge(G, R, dir, wallColors.get(dir));
        }
    }

    public final void renderFloor(Graphics2D G, Rectangle R) {
        if (floorColor != null) {
            G.setColor(floorColor);
            G.fillRect(R.x, R.y, R.width, R.height);
        }
    }

    public final void renderEdge(Graphics2D G, Rectangle R, Direction D, Edge E) {
        Optional.ofNullable(wallColors.get(D)).ifPresent(edge -> {

            if (edge.stroke != null) {
                Point start, end;
                switch (D) {
                    case NORTH -> {
                        start = new Point(R.x, R.y);
                        end = new Point(R.x + R.width, R.y);
                    }
                    case EAST -> {
                        start = new Point(R.x + R.width, R.y);
                        end = new Point(R.x + R.width, R.y + R.height);
                    }
                    case SOUTH -> {
                        start = new Point(R.x, R.y + R.height);
                        end = new Point(R.x + R.width, R.y + R.height);
                    }
                    case WEST -> {
                        start = new Point(R.x, R.y);
                        end = new Point(R.x, R.y + R.height);
                    }
                    default -> {
                        start = null;
                        end = null;
                    }
                }

                if (start != null && end != null) {
                    G.setStroke(edge.stroke);
                    G.setColor(edge.color);

                    if (edge.openDoor == null) {
                        Geom.drawLine(G, start, end);
                    } else {
                        final Point2D doorStart = Geom.midPoint(start, end, 0.25D);
                        final Point2D doorEnd = Geom.midPoint(start, end, 0.75D);
                        final double doorLength = Geom.distance(doorStart, doorEnd);
                        final double doorWidth = doorLength / 4D;
                        final double halfDoorWidth = doorWidth / 2D;

                        Geom.drawLine(G, start, doorStart);
                        Geom.drawLine(G, doorEnd, end);

                        final Point2D startCornerLeft = Geom.gridPoint(start, end, -0.125D, 0.25D);
                        final Point2D startCornerRight = Geom.gridPoint(start, end, 0.125D, 0.25D);
                        final Point2D endCornerLeft = Geom.gridPoint(start, end, -0.125D, 0.75D);
                        final Point2D endCornerRight = Geom.gridPoint(start, end, 0.125D, 0.75D);

                        Geom.drawPoly(G, startCornerLeft, startCornerRight, endCornerRight, endCornerLeft);
                        if (edge.openDoor == false) {
                            Geom.drawLine(G, startCornerLeft, endCornerRight);
                            Geom.drawLine(G, startCornerRight, endCornerLeft);
                        }
                    }
                }
            }
        });
    }



}
