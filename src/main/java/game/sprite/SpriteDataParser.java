package game.sprite;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for sprite data files (.dat format).
 * Handles JSON-like format with lenient parsing for ease of editing.
 * Supports case-insensitive x/y keys and null color values.
 */
public class SpriteDataParser {
    
    // Patterns for parsing
    private static final Pattern COLOR_ENTRY = Pattern.compile("(\\w+)\\s*:\\s*['\"]([^'\"]+)['\"]");
    private static final Pattern POINT_PATTERN = Pattern.compile("[{]\\s*[xX]\\s*:\\s*([-\\d.]+)\\s*,\\s*[yY]\\s*:\\s*([-\\d.]+)\\s*[}]");
    private static final Pattern POLYGON_START = Pattern.compile("[{]");
    
    /**
     * Parse a sprite data file from text content.
     * 
     * @param content the text content of a .dat file
     * @return validation result with parsed sprite file or error details
     */
    public static SpriteValidationResult parse(final String content) {
        try {
            // Extract colors object
            final Map<String, String> colors = parseColors(content);
            if (colors == null) {
                return SpriteValidationResult.failure("Could not find colors object", 1);
            }
            
            // Extract bounds (optional)
            final Bounds bounds = parseBounds(content);
            
            // Extract shapes array (supports polygons, circles, and arcs)
            final List<Shape> shapes = parseShapes(content, colors);
            
            // Auto-add missing color definitions
            addMissingColorDefinitions(colors, shapes);
            
            final SpriteFile spriteFile = bounds != null
                ? new SpriteFile(colors, shapes, bounds)
                : new SpriteFile(colors, shapes);
            return SpriteValidationResult.success(spriteFile);
            
        } catch (final Exception e) {
            return SpriteValidationResult.failure("Parse error: " + e.getMessage(), 1);
        }
    }
    
    /**
     * Extract the colors object from the file content.
     */
    private static Map<String, String> parseColors(final String content) {
        final int colorsStart = content.indexOf("colors:");
        if (colorsStart == -1) {
            return null;
        }
        
        final int objectStart = content.indexOf("{", colorsStart);
        if (objectStart == -1) {
            return null;
        }
        
        // Find matching closing brace
        int braceCount = 0;
        int objectEnd = -1;
        for (int i = objectStart; i < content.length(); i++) {
            if (content.charAt(i) == '{') {
                braceCount++;
            } else if (content.charAt(i) == '}') {
                braceCount--;
                if (braceCount == 0) {
                    objectEnd = i;
                    break;
                }
            }
        }
        
        if (objectEnd == -1) {
            return null;
        }
        
        final String colorsObject = content.substring(objectStart, objectEnd + 1);
        final Map<String, String> colors = new HashMap<>();
        
        // Extract color entries using regex
        final Matcher matcher = COLOR_ENTRY.matcher(colorsObject);
        while (matcher.find()) {
            final String name = matcher.group(1);
            final String hexValue = matcher.group(2);
            colors.put(name, hexValue);
        }
        
        return colors;
    }
    
    /**
     * Extract the bounds object from the file content (optional).
     */
    private static Bounds parseBounds(final String content) {
        final Pattern pattern = Pattern.compile("bounds\\s*:\\s*\\{\\s*x\\s*:\\s*(\\d+)\\s*,\\s*y\\s*:\\s*(\\d+)\\s*,\\s*width\\s*:\\s*(\\d+)\\s*,\\s*height\\s*:\\s*(\\d+)\\s*\\}");
        final Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            try {
                final int x = Integer.parseInt(matcher.group(1));
                final int y = Integer.parseInt(matcher.group(2));
                final int width = Integer.parseInt(matcher.group(3));
                final int height = Integer.parseInt(matcher.group(4));
                return new Bounds(x, y, width, height);
            } catch (final NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Extract all shapes (polygons, circles, arcs, line segments) from the file content.
     * Shapes are expected to be in a "shapes" array property.
     */
    private static List<Shape> parseShapes(final String content, final Map<String, String> colors) {
        final List<Shape> shapes = new ArrayList<>();
        
        // Find the shapes array (starts with "shapes:")
        final int shapesStart = content.indexOf("shapes:");
        if (shapesStart == -1) {
            return shapes;
        }
        
        // Find the array bracket after "shapes:"
        final int arrayStart = content.indexOf("[", shapesStart);
        if (arrayStart == -1) {
            return shapes;
        }
        
        // Find the closing bracket of the shapes array
        int bracketCount = 0;
        int arrayEnd = -1;
        for (int i = arrayStart; i < content.length(); i++) {
            final char c = content.charAt(i);
            if (c == '[') {
                bracketCount++;
            } else if (c == ']') {
                bracketCount--;
                if (bracketCount == 0) {
                    arrayEnd = i;
                    break;
                }
            }
        }
        
        if (arrayEnd == -1) {
            return shapes;
        }
        
        final String shapesArray = content.substring(arrayStart, arrayEnd + 1);
        
        // Find all shape objects within the array
        int braceCount = 0;
        int shapeStart = -1;
        
        for (int i = 0; i < shapesArray.length(); i++) {
            final char c = shapesArray.charAt(i);
            
            if (c == '{') {
                if (braceCount == 0) {
                    shapeStart = i;
                }
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0 && shapeStart != -1) {
                    final String shapeText = shapesArray.substring(shapeStart, i + 1);
                    final Shape shape = parseShape(shapeText, colors);
                    if (shape != null) {
                        shapes.add(shape);
                    }
                    shapeStart = -1;
                }
            }
        }
        
        return shapes;
    }
    
    /**
     * Parse a single shape object (polygon, circle, arc, line segment, curve, or path).
     */
    private static Shape parseShape(final String shapeText, final Map<String, String> colors) {
        // Determine shape type based on content
        // Check for explicit type field first
        if (shapeText.contains("\"type\"") || shapeText.contains("type:")) {
            String type = extractStringField(shapeText, "type");
            if ("circle".equalsIgnoreCase(type)) {
                return parseCircle(shapeText, colors);
            } else if ("arc".equalsIgnoreCase(type)) {
                return parseArc(shapeText, colors);
            } else if ("lineSegment".equalsIgnoreCase(type) || "line_segment".equalsIgnoreCase(type)) {
                return parseLineSegment(shapeText, colors);
            } else if ("curve".equalsIgnoreCase(type)) {
                return parseCurve(shapeText, colors);
            } else if ("path".equalsIgnoreCase(type)) {
                return parsePath(shapeText, colors);
            }
        }
        
        // Check for shape-specific fields (in order of specificity)
        if (shapeText.contains("radius")) {
            // Has radius field -> it's a circle
            return parseCircle(shapeText, colors);
        } else if (shapeText.contains("counterClockwise") || shapeText.contains("counter")) {
            // Has counterClockwise field -> it's an arc
            return parseArc(shapeText, colors);
        } else if (shapeText.contains("controlPoint1") || shapeText.contains("control1") || 
                   shapeText.contains("controlPoint2") || shapeText.contains("control2")) {
            // Has control point field -> it's a curve
            return parseCurve(shapeText, colors);
        } else if (shapeText.contains("path:") || shapeText.contains("\"path\"")) {
            // Has path array -> it's a polygon (with optional segment type properties)
            return parsePolygon(shapeText, colors);
        } else if (shapeText.contains("points:") || shapeText.contains("\"points\"")) {
            // Has points array -> could be polygon or line segment
            // Default to polygon - SpriteFile constructor will sort them correctly
            return parsePolygon(shapeText, colors);
        }
        
        // Default: treat as polygon (has points array)
        return parsePolygon(shapeText, colors);
    }
    
    /**
     * Parse a circle shape.
     */
    private static Circle parseCircle(final String circleText, final Map<String, String> colors) {
        String fillColor = extractColorField(circleText, "fillColor");
        String lineColor = extractColorField(circleText, "lineColor");
        
        final Point2D.Double center = extractPoint(circleText, "center");
        if (center == null) {
            return null;
        }
        
        final double radius = extractDouble(circleText, "radius", -1);
        if (radius < 0) {
            return null;
        }
        
        return new Circle(fillColor, lineColor, center, radius);
    }
    
    /**
     * Parse an arc shape.
     */
    private static Arc parseArc(final String arcText, final Map<String, String> colors) {
        String fillColor = extractColorField(arcText, "fillColor");
        String lineColor = extractColorField(arcText, "lineColor");
        
        final Point2D.Double start = extractPoint(arcText, "start");
        final Point2D.Double end = extractPoint(arcText, "end");
        
        if (start == null || end == null) {
            return null;
        }
        
        // Default to counterClockwise = true
        final boolean counterClockwise = extractBoolean(arcText, "counterClockwise", true);
        
        return new Arc(fillColor, lineColor, start, end, counterClockwise);
    }
    
     /**
      * Parse a line segment shape.
      */
     private static LineSegment parseLineSegment(final String lineSegmentText, final Map<String, String> colors) {
         String lineColor = extractColorField(lineSegmentText, "lineColor");
         
         final java.util.List<LinePoint> points = extractLinePoints(lineSegmentText);
         
         if (points.isEmpty()) {
             return null;  // Invalid line segment with no points
         }
         
         return new LineSegment(lineColor, points);
     }
     
     /**
      * Parse a curve shape (cubic Bezier curve).
      */
     private static Curve parseCurve(final String curveText, final Map<String, String> colors) {
         String fillColor = extractColorField(curveText, "fillColor");
         String lineColor = extractColorField(curveText, "lineColor");
         
         final Point2D.Double start = extractPoint(curveText, "start");
         final Point2D.Double controlPoint1 = extractPoint(curveText, "controlPoint1");
         final Point2D.Double controlPoint2 = extractPoint(curveText, "controlPoint2");
         final Point2D.Double end = extractPoint(curveText, "end");
         
         if (start == null || controlPoint1 == null || controlPoint2 == null || end == null) {
             return null;
         }
         
         return new Curve(fillColor, lineColor, start, controlPoint1, controlPoint2, end);
     }
     
     /**
      * Parse a path shape (sequence of points forming a polyline).
      */
     private static Path parsePath(final String pathText, final Map<String, String> colors) {
         String fillColor = extractColorField(pathText, "fillColor");
         String lineColor = extractColorField(pathText, "lineColor");
         
         final java.util.List<Point2D.Double> points = extractPoints(pathText);
         
         if (points.isEmpty()) {
             return null;  // Invalid path with no points
         }
         
         return new Path(fillColor, lineColor, points);
     }

    
    /**
     * Extract all line points from a line segment's points array.
     * Each point has a curve type and x, y coordinates.
     */
    private static java.util.List<LinePoint> extractLinePoints(final String text) {
        final java.util.List<LinePoint> points = new java.util.ArrayList<>();
        
        // Find the points array
        final Pattern pointsArrayPattern = Pattern.compile("points\\s*:\\s*\\[([^\\]]+)\\]");
        final Matcher arrayMatcher = pointsArrayPattern.matcher(text);
        
        if (!arrayMatcher.find()) {
            return points;
        }
        
        final String pointsArray = arrayMatcher.group(1);
        
        // Extract each point object
        final Pattern pointObjectPattern = Pattern.compile("\\{\\s*curve\\s*:\\s*(\\w+)\\s*,\\s*[xX]\\s*:\\s*([-\\d.]+)\\s*,\\s*[yY]\\s*:\\s*([-\\d.]+)\\s*\\}");
        final Matcher pointMatcher = pointObjectPattern.matcher(pointsArray);
        
        while (pointMatcher.find()) {
            try {
                final String curveStr = pointMatcher.group(1);
                final double x = Double.parseDouble(pointMatcher.group(2));
                final double y = Double.parseDouble(pointMatcher.group(3));
                
                final CurveType curve = CurveType.parse(curveStr);
                final LinePoint linePoint = new LinePoint(curve, new Point2D.Double(x, y));
                points.add(linePoint);
            } catch (final Exception e) {
                // Skip invalid points
            }
        }
        
        return points;
    }
    
    /**
     * Extract a point from text with given field name.
     */
    private static Point2D.Double extractPoint(final String text, final String fieldName) {
        final Pattern pattern = Pattern.compile(fieldName + "\\s*:\\s*\\{\\s*[xX]\\s*:\\s*([-\\d.]+)\\s*,\\s*[yY]\\s*:\\s*([-\\d.]+)\\s*\\}");
        final Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                final double x = Double.parseDouble(matcher.group(1));
                final double y = Double.parseDouble(matcher.group(2));
                return new Point2D.Double(x, y);
            } catch (final NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Extract a double value from text with given field name.
     */
    private static double extractDouble(final String text, final String fieldName, final double defaultValue) {
        final Pattern pattern = Pattern.compile(fieldName + "\\s*:\\s*([-\\d.]+)");
        final Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (final NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Extract a boolean value from text with given field name.
     */
    private static boolean extractBoolean(final String text, final String fieldName, final boolean defaultValue) {
        final Pattern pattern = Pattern.compile(fieldName + "\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return "true".equalsIgnoreCase(matcher.group(1));
        }
        return defaultValue;
    }
    
    /**
     * Extract a string value from text with given field name.
     */
    private static String extractStringField(final String text, final String fieldName) {
        final Pattern pattern = Pattern.compile(fieldName + "\\s*:\\s*['\"]([^'\"]+)['\"]");
        final Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    
    /**
     * Parse a single polygon object.
     */
     private static Polygon parsePolygon(final String polygonText, final Map<String, String> colors) {
         // Extract fillColor
         String fillColor = extractColorField(polygonText, "fillColor");
         
         // Extract lineColor
         String lineColor = extractColorField(polygonText, "lineColor");
         
         // Extract path array with segment types
         final List<PathPoint> pathPoints = extractPathPoints(polygonText);
         
         if (pathPoints.isEmpty()) {
             return null; // Invalid polygon with no points
         }
         
         // Extract isOpen property, default to false (polygon is closed)
         final boolean isOpen = extractBoolean(polygonText, "isOpen", false);
         
         return new Polygon(fillColor, lineColor, pathPoints, isOpen);
     }
    
    /**
     * Extract a color field value (returns the color name, not the hex code).
     */
    private static String extractColorField(final String text, final String fieldName) {
        final Pattern pattern = Pattern.compile(fieldName + "\\s*:\\s*(\\w+|null)");
        final Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            final String value = matcher.group(1);
            return "null".equals(value) ? null : value;
        }
        return null;
    }
    
    /**
     * Extract all points from a polygon's points array.
     */
     private static List<Point2D.Double> extractPoints(final String text) {
         final List<Point2D.Double> points = new ArrayList<>();
         
         final Matcher matcher = POINT_PATTERN.matcher(text);
         while (matcher.find()) {
             try {
                 final double x = Double.parseDouble(matcher.group(1));
                 final double y = Double.parseDouble(matcher.group(2));
                 points.add(new Point2D.Double(x, y));
             } catch (final NumberFormatException e) {
                 // Skip malformed points
             }
         }
         
         return points;
     }
     
     /**
      * Extract all path points from a polygon's path array.
      * Each point may have an optional type field (straight, none, left, right).
      * Default is straight if not specified.
      */
     private static List<PathPoint> extractPathPoints(final String text) {
         final List<PathPoint> pathPoints = new ArrayList<>();
         
         // Try to find path array first, fall back to points array
         String pathArray = null;
         
         // Look for "path:" 
         final Pattern pathArrayPattern = Pattern.compile("path\\s*:\\s*\\[([^\\]]+)\\]");
         final Matcher pathMatcher = pathArrayPattern.matcher(text);
         if (pathMatcher.find()) {
             pathArray = pathMatcher.group(1);
         } else {
             // Fall back to "points:" for backwards compatibility
             final Pattern pointsArrayPattern = Pattern.compile("points\\s*:\\s*\\[([^\\]]+)\\]");
             final Matcher pointsMatcher = pointsArrayPattern.matcher(text);
             if (pointsMatcher.find()) {
                 pathArray = pointsMatcher.group(1);
             }
         }
         
         if (pathArray == null) {
             return pathPoints;
         }
         
         // Extract each point object from the array
         // Pattern matches: {curve: left, x: 10, y: 20} or {type: LEFT, x: 10, y: 20} or {x: 10, y: 20}
         final Pattern pointObjectPattern = Pattern.compile("\\{\\s*(?:(?:curve|type)\\s*:\\s*(\\w+)\\s*,\\s*)?[xX]\\s*:\\s*([-\\d.]+)\\s*,\\s*[yY]\\s*:\\s*([-\\d.]+)\\s*\\}");
         final Matcher pointMatcher = pointObjectPattern.matcher(pathArray);
         
         while (pointMatcher.find()) {
             try {
                 final String curveStr = pointMatcher.group(1);
                 final double x = Double.parseDouble(pointMatcher.group(2));
                 final double y = Double.parseDouble(pointMatcher.group(3));
                 
                 final PathSegmentType segmentType = curveStr == null 
                     ? PathSegmentType.STRAIGHT 
                     : PathSegmentType.parse(curveStr);
                 
                 final PathPoint pathPoint = new PathPoint(segmentType, new Point2D.Double(x, y));
                 pathPoints.add(pathPoint);
             } catch (final Exception e) {
                 // Skip malformed points
             }
         }
         
         return pathPoints;
     }

    
    /**
     * Format a SpriteFile back to .dat text format.
     */
    public static String formatToText(final SpriteFile spriteFile) {
        final StringBuilder sb = new StringBuilder();
        
        // Write colors object
        sb.append("{\n");
        sb.append("    colors: {\n");
        
        final Map<String, String> colors = spriteFile.colors();
        final List<String> colorKeys = new ArrayList<>(colors.keySet());
        for (int i = 0; i < colorKeys.size(); i++) {
            final String key = colorKeys.get(i);
            final String value = colors.get(key);
            sb.append("        ").append(key).append(": '").append(value).append("'");
            if (i < colorKeys.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("    },\n");
        
        // Write bounds
        final Bounds bounds = spriteFile.bounds();
        if (bounds != null) {
            sb.append("    bounds: {x: ").append(bounds.x()).append(", y: ").append(bounds.y());
            sb.append(", width: ").append(bounds.width()).append(", height: ").append(bounds.height()).append("},\n");
        }
        
        // Write shapes array (polygons, circles, arcs, line segments)
        sb.append("    shapes: [\n");
        final List<Shape> shapes = spriteFile.shapes();
        for (int i = 0; i < shapes.size(); i++) {
            final Shape shape = shapes.get(i);
            formatShape(sb, shape, i < shapes.size() - 1);
        }
        sb.append("    ]\n");
        sb.append("}\n");
        
        return sb.toString();
    }
    
     /**
      * Format a single shape (polygon, circle, arc, line segment, curve, or path).
      */
     private static void formatShape(final StringBuilder sb, final Shape shape, final boolean hasNext) {
         if (shape instanceof Polygon) {
             formatPolygon(sb, (Polygon) shape, hasNext);
         } else if (shape instanceof Circle) {
             formatCircle(sb, (Circle) shape, hasNext);
         } else if (shape instanceof Arc) {
             formatArc(sb, (Arc) shape, hasNext);
         } else if (shape instanceof LineSegment) {
             formatLineSegment(sb, (LineSegment) shape, hasNext);
         } else if (shape instanceof Curve) {
             formatCurve(sb, (Curve) shape, hasNext);
         } else if (shape instanceof Path) {
             formatPath(sb, (Path) shape, hasNext);
         }
     }
    
    /**
     * Format a circle shape.
     */
    private static void formatCircle(final StringBuilder sb, final Circle circle, final boolean hasNext) {
        sb.append("        {\n");
        sb.append("            fillColor: ").append(circle.fillColor()).append(",\n");
        sb.append("            lineColor: ").append(circle.lineColor()).append(",\n");
        sb.append("            center: {x: ").append((int)circle.center().x).append(", y: ").append((int)circle.center().y).append("},\n");
        sb.append("            radius: ").append((int)circle.radius()).append("\n");
        sb.append("        }");
        if (hasNext) {
            sb.append(",");
        }
        sb.append("\n");
    }
    
    /**
     * Format an arc shape.
     */
    private static void formatArc(final StringBuilder sb, final Arc arc, final boolean hasNext) {
        sb.append("        {\n");
        sb.append("            fillColor: ").append(arc.fillColor()).append(",\n");
        sb.append("            lineColor: ").append(arc.lineColor()).append(",\n");
        sb.append("            start: {x: ").append((int)arc.start().x).append(", y: ").append((int)arc.start().y).append("},\n");
        sb.append("            end: {x: ").append((int)arc.end().x).append(", y: ").append((int)arc.end().y).append("},\n");
        sb.append("            counterClockwise: ").append(arc.counterClockwise()).append("\n");
        sb.append("        }");
        if (hasNext) {
            sb.append(",");
        }
        sb.append("\n");
    }
    
     /**
      * Format a line segment shape.
      */
     private static void formatLineSegment(final StringBuilder sb, final LineSegment lineSegment, final boolean hasNext) {
         sb.append("        {\n");
         sb.append("            lineColor: ").append(lineSegment.lineColor()).append(",\n");
         sb.append("            points: [\n");
         
         final java.util.List<LinePoint> points = lineSegment.points();
         for (int i = 0; i < points.size(); i++) {
             final LinePoint p = points.get(i);
             sb.append("                {curve: ").append(p.curve()).append(", x: ").append((int)p.x()).append(", y: ").append((int)p.y()).append("}");
             if (i < points.size() - 1) {
                 sb.append(",");
             }
             sb.append("\n");
         }
         
         sb.append("            ]\n");
         sb.append("        }");
         if (hasNext) {
             sb.append(",");
         }
         sb.append("\n");
     }
     
     /**
      * Format a curve shape (cubic Bezier curve).
      */
     private static void formatCurve(final StringBuilder sb, final Curve curve, final boolean hasNext) {
         sb.append("        {\n");
         sb.append("            fillColor: ").append(curve.fillColor()).append(",\n");
         sb.append("            lineColor: ").append(curve.lineColor()).append(",\n");
         sb.append("            start: {x: ").append((int)curve.start().x).append(", y: ").append((int)curve.start().y).append("},\n");
         sb.append("            controlPoint1: {x: ").append((int)curve.controlPoint1().x).append(", y: ").append((int)curve.controlPoint1().y).append("},\n");
         sb.append("            controlPoint2: {x: ").append((int)curve.controlPoint2().x).append(", y: ").append((int)curve.controlPoint2().y).append("},\n");
         sb.append("            end: {x: ").append((int)curve.end().x).append(", y: ").append((int)curve.end().y).append("}\n");
         sb.append("        }");
         if (hasNext) {
             sb.append(",");
         }
         sb.append("\n");
     }
     
     /**
      * Format a path shape (sequence of points).
      */
     private static void formatPath(final StringBuilder sb, final Path path, final boolean hasNext) {
         sb.append("        {\n");
         sb.append("            type: 'path',\n");
         sb.append("            fillColor: ").append(path.fillColor()).append(",\n");
         sb.append("            lineColor: ").append(path.lineColor()).append(",\n");
         sb.append("            points: [\n");
         
         final java.util.List<Point2D.Double> points = path.points();
         for (int i = 0; i < points.size(); i++) {
             final Point2D.Double p = points.get(i);
             sb.append("                {x: ").append((int)p.x).append(", y: ").append((int)p.y).append("}");
             if (i < points.size() - 1) {
                 sb.append(",");
             }
             sb.append("\n");
         }
         
         sb.append("            ]\n");
         sb.append("        }");
         if (hasNext) {
             sb.append(",");
         }
         sb.append("\n");
     }

    
    /**
     * Format a single polygon.
     */
    private static void formatPolygon(final StringBuilder sb, final Polygon polygon, final boolean hasNext) {
        sb.append("        {\n");
        sb.append("            fillColor: ").append(polygon.fillColor()).append(",\n");
        sb.append("            lineColor: ").append(polygon.lineColor()).append(",\n");
        sb.append("            path: [\n");
        
        final List<PathPoint> path = polygon.path();
        for (int i = 0; i < path.size(); i++) {
            final PathPoint p = path.get(i);
            sb.append("                {");
            // Only write curve if it's not the default STRAIGHT
            if (p.type() != PathSegmentType.STRAIGHT) {
                sb.append("curve: ").append(p.type().toString().toLowerCase()).append(", ");
            }
            sb.append("x: ").append((int)p.x()).append(", y: ").append((int)p.y()).append("}");
            if (i < path.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        
        sb.append("            ]");
        // Only write isOpen if it's true (since false is the default)
        if (polygon.isOpen()) {
            sb.append(",\n");
            sb.append("            isOpen: true");
        }
        sb.append("\n");
        sb.append("        }");
        if (hasNext) {
            sb.append(",");
        }
        sb.append("\n");
    }

    
    /**
     * Auto-add missing color definitions from all shapes to the colors map.
     * Uses standard Java color names when a referenced color is not yet defined.
     */
    private static void addMissingColorDefinitions(final Map<String, String> colors, final List<Shape> shapes) {
        for (final Shape shape : shapes) {
            addColorIfMissing(colors, shape.fillColor());
            addColorIfMissing(colors, shape.lineColor());
        }
    }
    
    /**
     * Helper to add a color if it's missing from the palette.
     */
    private static void addColorIfMissing(final Map<String, String> colors, final String colorName) {
        if (colorName != null && !colors.containsKey(colorName)) {
            final String hexValue = StandardColors.getHexValue(colorName);
            if (hexValue != null) {
                colors.put(colorName, hexValue);
            }
        }
    }
}
