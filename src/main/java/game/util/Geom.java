package game.util;

import java.awt.*;
import java.awt.geom.Point2D;

public class Geom {

    public static final double pT(final double start, final double end, final double t) {
        return start + t * (end - start);
    }

    public static Point2D midPoint(final Point2D p1, final Point2D p2, final double t) {
        double x = pT(p1.getX(), p2.getX(), t);
        double y = pT(p2.getY(), p2.getY(), t);
        return new Point2D.Double(x, y);
    }

    public static Point2D midPoint(final Point2D p1, final Point2D p2) {
        return midPoint(p1, p2, 0.5D);
    }

    public static void drawLine(final Graphics2D g, final Point2D p1, final Point2D p2) {
        g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
    }

    public static Point2D polarPoint(final Point2D center, double bearingRadians, final double distance) {
        return new Point2D.Double(
                center.getX() + distance * Math.cos(bearingRadians),
                center.getY() + distance * Math.sin(bearingRadians)
        );
    }

    public static double bearingRadians(final Point2D p1, final Point2D p2) {
        return Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX());
    }

    public static double distance(final Point2D p1, final Point2D p2) {
        return Math.hypot(p2.getX() - p1.getX(), p2.getY() - p1.getY());
    }

    public static void fillPoly(final Graphics2D g, Point2D... points) {
        final int[] xPoints = new int[points.length];
        final int[] yPoints = new int[points.length];
        for (int i = 0; i < points.length; i++) {
            xPoints[i] = (int) points[i].getX();
            yPoints[i] = (int) points[i].getY();
        }
        g.fillPolygon(xPoints, yPoints, points.length);
    }

    public static void drawPoly(final Graphics2D g, Point2D... points) {
        final int[] xPoints = new int[points.length];
        final int[] yPoints = new int[points.length];
        for (int i = 0; i < points.length; i++) {
            xPoints[i] = (int) points[i].getX();
            yPoints[i] = (int) points[i].getY();
        }
        g.drawPolygon(xPoints, yPoints, points.length);
    }

    public static Point2D gridPoint(Point2D start, Point2D end, double px, double py) {
        final double yAxis = Geom.bearingRadians(start, end);
        final double xAxis = yAxis + Math.PI/2D;
        final double dist = distance(start, end);

        final Point2D p1 = midPoint(start, end, py);
        final Point2D p2 = polarPoint(p1, xAxis, px * dist);

        return p2;
    }
}
