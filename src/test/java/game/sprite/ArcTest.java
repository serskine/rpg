package game.sprite;

import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArcTest {


    final Point2D.Double c = new Point2D.Double(0D, 0D);

    final Point2D.Double ne = new Point2D.Double(10D, -10D);
    final Point2D.Double se = new Point2D.Double(10D, 10D);
    final Point2D.Double sw = new Point2D.Double(-10D, 10D);
    final Point2D.Double nw = new Point2D.Double(-10D, -10D);

    final Arc arcLeftNE = new Arc(null, null, c, ne, false);
    final Arc arcLeftSE = new Arc(null, null, c, se, false);
    final Arc arcLeftSW = new Arc(null, null, c, sw, false);
    final Arc arcLeftNW = new Arc(null, null, c, nw, false);

    final Arc arcRightNE = new Arc(null, null, c, ne, true);
    final Arc arcRightSE = new Arc(null, null, c, se, true);
    final Arc arcRightSW = new Arc(null, null, c, sw, true);
    final Arc arcRightNW = new Arc(null, null, c, nw, true);

    private Arc createArc(Point2D.Double p1, Point2D.Double p2, final boolean isClockwise) {
        return new Arc(null, null, p1, p2, true);
    }

    @Test
    public void getControlPoint_left_NE() {

        final Point2D.Double expected = new Point2D.Double(c.getX(), ne.getY());
        final Point2D.Double observed = arcLeftNE.getControlPoint();

        assertEquals(expected, observed);
    }

    @Test
    public void getControlPoint_right_NE() {
        final Point2D.Double expected = new Point2D.Double(ne.getX(), c.getY());
        final Point2D.Double observed = arcRightNE.getControlPoint();

        assertEquals(expected, observed);
    }


    @Test
    public void getControlPoint_left_SE() {
        final Point2D.Double expected = new Point2D.Double(se.getX(), c.getY());
        final Point2D.Double observed = arcLeftSE.getControlPoint();

        assertEquals(expected, observed);
    }

    @Test
    public void getControlPoint_right_SE() {
        final Point2D.Double expected = new Point2D.Double(c.getX(), se.getY());
        final Point2D.Double observed = arcRightSE.getControlPoint();

        assertEquals(expected, observed);
    }


    @Test
    public void getControlPoint_left_SW() {
        final Point2D.Double expected = new Point2D.Double(c.getX(), sw.getY());
        final Point2D.Double observed = arcLeftSW.getControlPoint();

        assertEquals(expected, observed);
    }

    @Test
    public void getControlPoint_right_SW() {
        final Point2D.Double expected = new Point2D.Double(sw.getX(), c.getY());
        final Point2D.Double observed = arcRightSW.getControlPoint();

        assertEquals(expected, observed);
    }

    @Test
    public void getControlPoint_left_NW() {
        final Point2D.Double expected = new Point2D.Double(nw.getX(), c.getY());
        final Point2D.Double observed = arcLeftNW.getControlPoint();

        assertEquals(expected, observed);
    }

    @Test
    public void getControlPoint_right_NW() {
        final Point2D.Double expected = new Point2D.Double(c.getX(), nw.getY());
        final Point2D.Double observed = arcRightNW.getControlPoint();

        assertEquals(expected, observed);
    }

}
