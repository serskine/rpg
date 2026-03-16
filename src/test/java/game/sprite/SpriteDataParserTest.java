package game.sprite;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class SpriteDataParserTest {
    
    @Test
    public void testParseValidFile() {
        final String content = "{\n" +
            "    colors: {\n" +
            "        black: '#000000',\n" +
            "        brown: '#A52A2A'\n" +
            "    },\n" +
            "    shapes: [\n" +
            "        {\n" +
            "            fillColor: brown,\n" +
            "            lineColor: black,\n" +
            "            points: [\n" +
            "                {x: 1, y: 1},\n" +
            "                {x: 6, y: 1},\n" +
            "                {x: 6, y: 2}\n" +
            "            ]\n" +
            "        }\n" +
            "    ]\n" +
            "}\n";
        
        final SpriteValidationResult result = SpriteDataParser.parse(content);
        
        assertTrue(result.isValid(), "Should parse valid file");
        assertNotNull(result.getSpriteFile());
        assertEquals(1, result.getSpriteFile().getPolygons().size());
        assertEquals(3, result.getSpriteFile().getPolygons().get(0).path().size());
    }
    
    @Test
    public void testParseCaseInsensitiveKeys() {
        final String content = "{\n" +
            "    colors: {\n" +
            "        black: '#000000'\n" +
            "    },\n" +
            "    shapes: [\n" +
            "        {\n" +
            "            fillColor: black,\n" +
            "            lineColor: null,\n" +
            "            points: [\n" +
            "                {X: 1, Y: 1},\n" +
            "                {x: 6, y: 2}\n" +
            "            ]\n" +
            "        }\n" +
            "    ]\n" +
            "}\n";
        
        final SpriteValidationResult result = SpriteDataParser.parse(content);
        
         assertTrue(result.isValid(), "Should handle case-insensitive x/y keys");
         assertEquals(2, result.getSpriteFile().getPolygons().get(0).path().size());
    }
    
    @Test
    public void testParseNullColors() {
        final String content = "{\n" +
            "    colors: {\n" +
            "        black: '#000000'\n" +
            "    },\n" +
            "    shapes: [\n" +
            "        {\n" +
            "            fillColor: black,\n" +
            "            lineColor: null,\n" +
            "            points: [\n" +
            "                {x: 1, y: 1}\n" +
            "            ]\n" +
            "        }\n" +
            "    ]\n" +
            "}\n";
        
        final SpriteValidationResult result = SpriteDataParser.parse(content);
        
        assertTrue(result.isValid());
        final Polygon polygon = result.getSpriteFile().getPolygons().get(0);
        assertNotNull(polygon.fillColor());
        assertNull(polygon.lineColor());
    }
    
     @Test
     public void testFormatToText() {
         final SpriteFile file = new SpriteFile(
             java.util.Map.of("black", "#000000", "white", "#FFFFFF"),
             Arrays.asList(
                 new Polygon("black", "white", Arrays.asList(
                     new PathPoint(PathSegmentType.STRAIGHT, 1, 1),
                     new PathPoint(PathSegmentType.STRAIGHT, 2, 2)
                 ))
             )
         );
         
         final String formatted = SpriteDataParser.formatToText(file);
         
         assertNotNull(formatted);
         assertTrue(formatted.contains("colors:"));
         assertTrue(formatted.contains("shapes:"));
         assertTrue(formatted.contains("black"));
         assertTrue(formatted.contains("fillColor: black"));
     }
    
     @Test
     public void testParseTombDat() throws IOException {
         // Test with actual tomb.dat file
         final String content = new String(Files.readAllBytes(
             Paths.get("src/main/resources/features/human/tomb.dat")
         ));
         
         final SpriteValidationResult result = SpriteDataParser.parse(content);
         
         assertTrue(result.isValid(), "Should parse tomb.dat successfully");
         assertNotNull(result.getSpriteFile());
         assertTrue(result.getSpriteFile().getPolygons().size() > 0, "Should have polygons");
         assertTrue(result.getSpriteFile().colors().containsKey("black"), "Should have color definitions");
     }
     
     @Test
     public void testParseCurveShape() {
         final String content = "{\n" +
             "    colors: {\n" +
             "        red: '#FF0000'\n" +
             "    },\n" +
             "    shapes: [\n" +
             "        {\n" +
             "            fillColor: red,\n" +
             "            lineColor: null,\n" +
             "            start: {x: 0, y: 0},\n" +
             "            controlPoint1: {x: 10, y: 10},\n" +
             "            controlPoint2: {x: 20, y: 10},\n" +
             "            end: {x: 30, y: 0}\n" +
             "        }\n" +
             "    ]\n" +
             "}\n";
         
         final SpriteValidationResult result = SpriteDataParser.parse(content);
         
         assertTrue(result.isValid(), "Should parse curve shape");
         assertNotNull(result.getSpriteFile());
         assertEquals(1, result.getSpriteFile().getCurves().size(), "Should have one curve");
         
         final Curve curve = result.getSpriteFile().getCurves().get(0);
         assertEquals("red", curve.fillColor());
         assertNull(curve.lineColor());
         assertEquals(0, curve.start().x);
         assertEquals(0, curve.start().y);
         assertEquals(10, curve.controlPoint1().x);
         assertEquals(10, curve.controlPoint1().y);
         assertEquals(20, curve.controlPoint2().x);
         assertEquals(10, curve.controlPoint2().y);
         assertEquals(30, curve.end().x);
         assertEquals(0, curve.end().y);
     }
     
     @Test
     public void testParsePathShape() {
         final String content = "{\n" +
             "    colors: {\n" +
             "        blue: '#0000FF'\n" +
             "    },\n" +
             "    shapes: [\n" +
             "        {\n" +
             "            type: 'path',\n" +
             "            fillColor: null,\n" +
             "            lineColor: blue,\n" +
             "            points: [\n" +
             "                {x: 0, y: 0},\n" +
             "                {x: 10, y: 10},\n" +
             "                {x: 20, y: 5}\n" +
             "            ]\n" +
             "        }\n" +
             "    ]\n" +
             "}\n";
         
         final SpriteValidationResult result = SpriteDataParser.parse(content);
         
         assertTrue(result.isValid(), "Should parse path shape");
         assertNotNull(result.getSpriteFile());
         assertEquals(1, result.getSpriteFile().getPaths().size(), "Should have one path");
         
         final Path path = result.getSpriteFile().getPaths().get(0);
         assertNull(path.fillColor());
         assertEquals("blue", path.lineColor());
         assertEquals(3, path.points().size());
         assertEquals(0, path.points().get(0).x);
         assertEquals(10, path.points().get(1).x);
         assertEquals(20, path.points().get(2).x);
     }
     
     @Test
     public void testFormatCurveShape() {
         final Curve curve = new Curve(
             "red", "blue",
             new java.awt.geom.Point2D.Double(0, 0),
             new java.awt.geom.Point2D.Double(10, 10),
             new java.awt.geom.Point2D.Double(20, 10),
             new java.awt.geom.Point2D.Double(30, 0)
         );
         
         final SpriteFile file = new SpriteFile(
             java.util.Map.of("red", "#FF0000", "blue", "#0000FF"),
             java.util.List.of(curve)
         );
         
         final String formatted = SpriteDataParser.formatToText(file);
         
         assertNotNull(formatted);
         assertTrue(formatted.contains("controlPoint1:"));
         assertTrue(formatted.contains("controlPoint2:"));
         assertTrue(formatted.contains("start:"));
         assertTrue(formatted.contains("end:"));
         assertTrue(formatted.contains("red"));
         assertTrue(formatted.contains("blue"));
     }
     
     @Test
     public void testFormatPathShape() {
         final Path path = new Path(
             "green", "black",
             java.util.List.of(
                 new java.awt.geom.Point2D.Double(0, 0),
                 new java.awt.geom.Point2D.Double(10, 10),
                 new java.awt.geom.Point2D.Double(20, 5)
             )
         );
         
         final SpriteFile file = new SpriteFile(
             java.util.Map.of("green", "#00FF00", "black", "#000000"),
             java.util.List.of(path)
         );
         
         final String formatted = SpriteDataParser.formatToText(file);
         
         assertNotNull(formatted);
         assertTrue(formatted.contains("points:"));
         assertTrue(formatted.contains("green"));
         assertTrue(formatted.contains("black"));
         // Should have 3 points in the formatted output
         assertTrue(formatted.contains("{x: 0, y: 0}"));
         assertTrue(formatted.contains("{x: 10, y: 10}"));
         assertTrue(formatted.contains("{x: 20, y: 5}"));
     }
     
     @Test
     public void testCurveAndPathRoundTrip() {
         final String original = "{\n" +
             "    colors: {\n" +
             "        red: '#FF0000',\n" +
             "        blue: '#0000FF'\n" +
             "    },\n" +
             "    shapes: [\n" +
             "        {\n" +
             "            fillColor: red,\n" +
             "            lineColor: blue,\n" +
             "            start: {x: 0, y: 0},\n" +
             "            controlPoint1: {x: 10, y: 10},\n" +
             "            controlPoint2: {x: 20, y: 10},\n" +
             "            end: {x: 30, y: 0}\n" +
             "        },\n" +
             "        {\n" +
             "            type: 'path',\n" +
             "            fillColor: blue,\n" +
             "            lineColor: red,\n" +
             "            points: [\n" +
             "                {x: 5, y: 5},\n" +
             "                {x: 15, y: 15},\n" +
             "                {x: 25, y: 10}\n" +
             "            ]\n" +
             "        }\n" +
             "    ]\n" +
             "}\n";
         
         // Parse the original
         final SpriteValidationResult result = SpriteDataParser.parse(original);
         assertTrue(result.isValid(), "Should parse original");
         final SpriteFile file = result.getSpriteFile();
         
         // Check that we have one curve and one path
         assertEquals(1, file.getCurves().size(), "Should have one curve");
         assertEquals(1, file.getPaths().size(), "Should have one path");
         
         // Format back to text
         final String formatted = SpriteDataParser.formatToText(file);
         
         // Parse the formatted version
         final SpriteValidationResult result2 = SpriteDataParser.parse(formatted);
         assertTrue(result2.isValid(), "Should parse formatted output");
         
         final SpriteFile file2 = result2.getSpriteFile();
         assertEquals(1, file2.getCurves().size(), "Round-trip should preserve curve");
         assertEquals(1, file2.getPaths().size(), "Round-trip should preserve path");
         
         // Verify curve details are preserved
         final Curve curve1 = file.getCurves().get(0);
         final Curve curve2 = file2.getCurves().get(0);
         assertEquals(curve1.fillColor(), curve2.fillColor());
         assertEquals(curve1.lineColor(), curve2.lineColor());
         assertEquals((int)curve1.start().x, (int)curve2.start().x);
         assertEquals((int)curve1.end().x, (int)curve2.end().x);
         
         // Verify path details are preserved
         final Path path1 = file.getPaths().get(0);
         final Path path2 = file2.getPaths().get(0);
         assertEquals(path1.fillColor(), path2.fillColor());
         assertEquals(path1.lineColor(), path2.lineColor());
         assertEquals(path1.points().size(), path2.points().size());
     }
}

