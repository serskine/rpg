package game.view;

import game.common.RoomFeature;
import game.sprite.SpriteDataParser;
import game.sprite.SpriteFile;
import game.sprite.SpriteValidationResult;
import game.sprite.Circle;
import game.sprite.Polygon;
import game.sprite.Curve;
import game.sprite.Path;
import game.sprite.Arc;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.CubicCurve2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Utility class for loading and rendering SVG and .dat sprite features.
 * Each RoomFeature has a corresponding folder with SVG or .dat files that can be loaded and rendered.
 */
public class SVGFeatureLoader {
    
    private static final Map<RoomFeature, BufferedImage> svgCache = new HashMap<>();
    private static final String SVG_RESOURCE_PATH = "/features/";
    
    /**
     * Load a feature sprite for a given RoomFeature and render it to a BufferedImage.
     * Tries .dat files first (human-designed sprites), then falls back to SVG.
     * Uses caching to avoid repeated loads.
     *
     * @param feature the RoomFeature to load
     * @param width desired width in pixels
     * @param height desired height in pixels
     * @return BufferedImage of the rendered sprite, or null if not found
     */
    public static BufferedImage loadFeatureSVG(final RoomFeature feature, final int width, final int height) {
        // Try to load .dat file first (human-designed sprite)
        String datResourcePath = SVG_RESOURCE_PATH + feature.name() + "/default.dat";
        try (InputStream inputStream = SVGFeatureLoader.class.getResourceAsStream(datResourcePath)) {
            if (inputStream != null) {
                return renderDatSpriteToImage(inputStream, width, height);
            }
        } catch (Exception e) {
            // Fall through to SVG attempt
        }
        
        // Try to load the SVG file
        String resourcePath = SVG_RESOURCE_PATH + feature.name() + "/default.svg";
        
        try (InputStream inputStream = SVGFeatureLoader.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                return createPlaceholderImage(feature, width, height);
            }
            
            // Parse SVG and render to BufferedImage
            return renderSVGToImage(inputStream, width, height);
            
        } catch (Exception e) {
            // Fallback: create a placeholder image
            return createPlaceholderImage(feature, width, height);
        }
    }
    
    /**
     * Render a .dat sprite file to a BufferedImage.
     */
    private static BufferedImage renderDatSpriteToImage(final InputStream datStream, final int width, final int height) 
            throws IOException {
        // Read the entire stream into a string
        String content;
        try (Scanner scanner = new Scanner(datStream, StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            content = scanner.hasNext() ? scanner.next() : "";
        }
        
        // Parse the sprite data
        SpriteValidationResult result = SpriteDataParser.parse(content);
        if (!result.isValid()) {
            throw new IOException("Failed to parse sprite file: " + result.getErrorMessage());
        }
        
        SpriteFile spriteFile = result.getSpriteFile();
        
        // Create a BufferedImage to render into
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable anti-aliasing for better rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        try {
            // Calculate scaling from tile coordinates (0-8) to image pixels
            int tileSize = spriteFile.tileSize();
            double scaleX = (double) width / tileSize;
            double scaleY = (double) height / tileSize;
            
            // Render all shapes in order (circles, polygons, curves, paths, arcs)
            for (final Circle circle : spriteFile.getCircles()) {
                renderDatCircle(g2d, circle, scaleX, scaleY, spriteFile.colors());
            }
            
            for (final Polygon polygon : spriteFile.getPolygons()) {
                renderDatPolygon(g2d, polygon, scaleX, scaleY, spriteFile.colors());
            }
            
            for (final Curve curve : spriteFile.getCurves()) {
                renderDatCurve(g2d, curve, scaleX, scaleY, spriteFile.colors());
            }
            
            for (final Path path : spriteFile.getPaths()) {
                renderDatPath(g2d, path, scaleX, scaleY, spriteFile.colors());
            }
            
            for (final Arc arc : spriteFile.getArcs()) {
                renderDatArc(g2d, arc, scaleX, scaleY, spriteFile.colors());
            }
            
        } finally {
            g2d.dispose();
        }
        
        return image;
    }
    
    /**
     * Render a single circle from a .dat sprite file.
     */
    private static void renderDatCircle(final Graphics2D g2d, final Circle circle, 
                                        final double scaleX, final double scaleY, 
                                        final Map<String, String> colorMap) {
        double cx = circle.center().getX() * scaleX;
        double cy = circle.center().getY() * scaleY;
        double r = circle.radius() * Math.min(scaleX, scaleY);
        
        // Set fill color
        if (circle.fillColor() != null && !circle.fillColor().isEmpty()) {
            String colorHex = colorMap.getOrDefault(circle.fillColor(), "#000000");
            g2d.setColor(parseHexColor(colorHex));
            java.awt.geom.Ellipse2D filledCircle = 
                new java.awt.geom.Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2);
            g2d.fill(filledCircle);
        }
        
        // Set line color and draw stroke
        if (circle.lineColor() != null && !circle.lineColor().isEmpty()) {
            String colorHex = colorMap.getOrDefault(circle.lineColor(), "#000000");
            g2d.setColor(parseHexColor(colorHex));
            g2d.setStroke(new BasicStroke((float) (0.5 * Math.min(scaleX, scaleY))));
            java.awt.geom.Ellipse2D strokeCircle = 
                new java.awt.geom.Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2);
            g2d.draw(strokeCircle);
        }
    }
    
    /**
     * Render a polygon from a .dat sprite file.
     */
    private static void renderDatPolygon(final Graphics2D g2d, final Polygon polygon,
                                         final double scaleX, final double scaleY,
                                         final Map<String, String> colorMap) {
        if (polygon.path() == null || polygon.path().isEmpty()) {
            return;
        }
        
        // Convert path points to scaled pixel coordinates
        int[] xPoints = new int[polygon.path().size()];
        int[] yPoints = new int[polygon.path().size()];
        
        for (int i = 0; i < polygon.path().size(); i++) {
            Point2D.Double pt = polygon.path().get(i).point();
            xPoints[i] = (int) (pt.getX() * scaleX);
            yPoints[i] = (int) (pt.getY() * scaleY);
        }
        
        java.awt.Polygon awtPolygon = new java.awt.Polygon(xPoints, yPoints, xPoints.length);
        
        // Set fill color
        if (polygon.fillColor() != null && !polygon.fillColor().isEmpty()) {
            String colorHex = colorMap.getOrDefault(polygon.fillColor(), "#000000");
            g2d.setColor(parseHexColor(colorHex));
            g2d.fill(awtPolygon);
        }
        
        // Set line color and draw stroke
        if (polygon.lineColor() != null && !polygon.lineColor().isEmpty()) {
            String colorHex = colorMap.getOrDefault(polygon.lineColor(), "#000000");
            g2d.setColor(parseHexColor(colorHex));
            g2d.setStroke(new BasicStroke((float) (0.5 * Math.min(scaleX, scaleY))));
            if (!polygon.isOpen()) {
                g2d.draw(awtPolygon);
            } else {
                // Draw as a polyline (open path)
                for (int i = 0; i < xPoints.length - 1; i++) {
                    g2d.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
                }
            }
        }
    }
    
    /**
     * Render a curve (Bezier) from a .dat sprite file.
     */
    private static void renderDatCurve(final Graphics2D g2d, final Curve curve,
                                       final double scaleX, final double scaleY,
                                       final Map<String, String> colorMap) {
        double x1 = curve.start().getX() * scaleX;
        double y1 = curve.start().getY() * scaleY;
        double ctrlX1 = curve.controlPoint1().getX() * scaleX;
        double ctrlY1 = curve.controlPoint1().getY() * scaleY;
        double ctrlX2 = curve.controlPoint2().getX() * scaleX;
        double ctrlY2 = curve.controlPoint2().getY() * scaleY;
        double x2 = curve.end().getX() * scaleX;
        double y2 = curve.end().getY() * scaleY;
        
        CubicCurve2D bezier = new CubicCurve2D.Double(x1, y1, ctrlX1, ctrlY1, ctrlX2, ctrlY2, x2, y2);
        
        // Draw stroke
        if (curve.lineColor() != null && !curve.lineColor().isEmpty()) {
            String colorHex = colorMap.getOrDefault(curve.lineColor(), "#000000");
            g2d.setColor(parseHexColor(colorHex));
            g2d.setStroke(new BasicStroke((float) (0.5 * Math.min(scaleX, scaleY))));
            g2d.draw(bezier);
        }
    }
    
    /**
     * Render a path from a .dat sprite file.
     */
    private static void renderDatPath(final Graphics2D g2d, final Path path,
                                      final double scaleX, final double scaleY,
                                      final Map<String, String> colorMap) {
        if (path.points() == null || path.points().isEmpty()) {
            return;
        }
        
        // Create a GeneralPath from the points
        java.awt.geom.GeneralPath generalPath = new java.awt.geom.GeneralPath();
        
        for (int i = 0; i < path.points().size(); i++) {
            Point2D.Double pt = path.points().get(i);
            double x = pt.getX() * scaleX;
            double y = pt.getY() * scaleY;
            
            if (i == 0) {
                generalPath.moveTo(x, y);
            } else {
                generalPath.lineTo(x, y);
            }
        }
        
        // Draw stroke
        if (path.lineColor() != null && !path.lineColor().isEmpty()) {
            String colorHex = colorMap.getOrDefault(path.lineColor(), "#000000");
            g2d.setColor(parseHexColor(colorHex));
            g2d.setStroke(new BasicStroke((float) (0.5 * Math.min(scaleX, scaleY))));
            g2d.draw(generalPath);
        }
    }
    
    /**
     * Render an arc from a .dat sprite file.
     */
    private static void renderDatArc(final Graphics2D g2d, final Arc arc,
                                     final double scaleX, final double scaleY,
                                     final Map<String, String> colorMap) {
        double x1 = arc.start().getX() * scaleX;
        double y1 = arc.start().getY() * scaleY;
        double x2 = arc.end().getX() * scaleX;
        double y2 = arc.end().getY() * scaleY;
        
        // Calculate control point (where the arc curves)
        Point2D controlPt = arc.getControlPoint();
        double ctrlX = controlPt.getX() * scaleX;
        double ctrlY = controlPt.getY() * scaleY;
        
        // Create a quadratic curve approximation of the arc
        java.awt.geom.QuadCurve2D quadCurve = new java.awt.geom.QuadCurve2D.Double(
            x1, y1, ctrlX, ctrlY, x2, y2
        );
        
        // Draw stroke
        if (arc.lineColor() != null && !arc.lineColor().isEmpty()) {
            String colorHex = colorMap.getOrDefault(arc.lineColor(), "#000000");
            g2d.setColor(parseHexColor(colorHex));
            g2d.setStroke(new BasicStroke((float) (0.5 * Math.min(scaleX, scaleY))));
            g2d.draw(quadCurve);
        }
    }
    
    /**
     * Parse a hex color string (e.g., "#FF0000") to a Color object.
     */
    private static Color parseHexColor(final String hexColor) {
        try {
            String cleanHex = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
            return new Color(Integer.parseInt(cleanHex, 16));
        } catch (NumberFormatException e) {
            return Color.BLACK;
        }
    }
    
    /**
     * Render SVG from input stream to a BufferedImage.
     */
    private static BufferedImage renderSVGToImage(final InputStream svgStream, final int width, final int height) 
            throws ParserConfigurationException, SAXException, IOException {
        
        // Create a BufferedImage to render into
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable anti-aliasing for better rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        try {
            // Parse SVG document
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(svgStream);
            
            // Get root SVG element to find viewBox
            Element rootElement = doc.getDocumentElement();
            String viewBox = rootElement.getAttribute("viewBox");
            
            // Parse viewBox to determine scaling
            double[] viewBoxValues = parseViewBox(viewBox);
            double scaleX = width / viewBoxValues[2];
            double scaleY = height / viewBoxValues[3];
            
            // Apply scaling transform
            AffineTransform transform = AffineTransform.getScaleInstance(scaleX, scaleY);
            transform.translate(-viewBoxValues[0], -viewBoxValues[1]);
            
            // Render SVG elements
            renderSVGElement(g2d, rootElement, transform);
            
        } finally {
            g2d.dispose();
        }
        
        return image;
    }
    
    /**
     * Parse viewBox attribute to extract x, y, width, height.
     */
    private static double[] parseViewBox(final String viewBox) {
        if (viewBox == null || viewBox.isEmpty()) {
            return new double[]{0, 0, 100, 100};
        }
        
        String[] parts = viewBox.trim().split("\\s+");
        if (parts.length == 4) {
            try {
                return new double[]{
                    Double.parseDouble(parts[0]),
                    Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2]),
                    Double.parseDouble(parts[3])
                };
            } catch (NumberFormatException e) {
                return new double[]{0, 0, 100, 100};
            }
        }
        return new double[]{0, 0, 100, 100};
    }
    
    /**
     * Recursively render SVG elements.
     * This is a simplified SVG renderer that handles basic shapes and paths.
     */
    private static void renderSVGElement(final Graphics2D g2d, final Element element, final AffineTransform baseTransform) {
        String tagName = element.getTagName();
        
        // Apply element-specific styling
        applyStyling(g2d, element);
        
        // Handle different element types
        switch (tagName) {
            case "rect":
                renderRect(g2d, element, baseTransform);
                break;
            case "circle":
                renderCircle(g2d, element, baseTransform);
                break;
            case "ellipse":
                renderEllipse(g2d, element, baseTransform);
                break;
            case "line":
                renderLine(g2d, element, baseTransform);
                break;
            case "path":
                renderPath(g2d, element, baseTransform);
                break;
            case "polygon":
                renderPolygon(g2d, element, baseTransform);
                break;
            case "text":
                renderText(g2d, element, baseTransform);
                break;
            case "defs":
            case "linearGradient":
            case "radialGradient":
                // Skip definition elements
                break;
            default:
                // Recursively render child elements
                for (int i = 0; i < element.getChildNodes().getLength(); i++) {
                    if (element.getChildNodes().item(i) instanceof Element) {
                        renderSVGElement(g2d, (Element) element.getChildNodes().item(i), baseTransform);
                    }
                }
        }
    }
    
    /**
     * Apply styling from SVG element attributes.
     */
    private static void applyStyling(final Graphics2D g2d, final Element element) {
        // Extract fill color
        String fill = element.getAttribute("fill");
        if (fill != null && !fill.isEmpty() && !fill.equals("none")) {
            g2d.setColor(parseColor(fill));
        }
        
        // Extract stroke color and width
        String stroke = element.getAttribute("stroke");
        String strokeWidth = element.getAttribute("stroke-width");
        
        if (stroke != null && !stroke.isEmpty() && !stroke.equals("none")) {
            g2d.setColor(parseColor(stroke));
            float width = strokeWidth != null && !strokeWidth.isEmpty() ? 
                         Float.parseFloat(strokeWidth) : 1.0f;
            g2d.setStroke(new BasicStroke(width));
        }
    }
    
    /**
     * Parse color from SVG color string (e.g., "#FF0000", "rgb(255,0,0)", or color name).
     */
    private static Color parseColor(final String colorStr) {
        if (colorStr.startsWith("#")) {
            try {
                return new Color(Integer.parseInt(colorStr.substring(1), 16));
            } catch (NumberFormatException e) {
                return Color.BLACK;
            }
        }
        
        // Handle rgb() format
        if (colorStr.startsWith("rgb")) {
            // Simplified parser for rgb()
            String[] parts = colorStr.replaceAll("[^0-9,]", "").split(",");
            if (parts.length >= 3) {
                try {
                    return new Color(Integer.parseInt(parts[0].trim()),
                                   Integer.parseInt(parts[1].trim()),
                                   Integer.parseInt(parts[2].trim()));
                } catch (NumberFormatException e) {
                    return Color.BLACK;
                }
            }
        }
        
        // Handle named colors
        try {
            return (Color) Color.class.getField(colorStr.toUpperCase()).get(null);
        } catch (Exception e) {
            return Color.BLACK;
        }
    }
    
    private static void renderRect(final Graphics2D g2d, final Element element, final AffineTransform transform) {
        double x = getAttributeAsDouble(element, "x", 0);
        double y = getAttributeAsDouble(element, "y", 0);
        double width = getAttributeAsDouble(element, "width", 0);
        double height = getAttributeAsDouble(element, "height", 0);
        
        java.awt.geom.Rectangle2D rect = new java.awt.geom.Rectangle2D.Double(x, y, width, height);
        Shape transformedRect = transform.createTransformedShape(rect);
        g2d.fill(transformedRect);
    }
    
    private static void renderCircle(final Graphics2D g2d, final Element element, final AffineTransform transform) {
        double cx = getAttributeAsDouble(element, "cx", 0);
        double cy = getAttributeAsDouble(element, "cy", 0);
        double r = getAttributeAsDouble(element, "r", 0);
        
        java.awt.geom.Ellipse2D circle = new java.awt.geom.Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2);
        Shape transformedCircle = transform.createTransformedShape(circle);
        g2d.fill(transformedCircle);
    }
    
    private static void renderEllipse(final Graphics2D g2d, final Element element, final AffineTransform transform) {
        double cx = getAttributeAsDouble(element, "cx", 0);
        double cy = getAttributeAsDouble(element, "cy", 0);
        double rx = getAttributeAsDouble(element, "rx", 0);
        double ry = getAttributeAsDouble(element, "ry", 0);
        
        java.awt.geom.Ellipse2D ellipse = new java.awt.geom.Ellipse2D.Double(cx - rx, cy - ry, rx * 2, ry * 2);
        Shape transformedEllipse = transform.createTransformedShape(ellipse);
        g2d.fill(transformedEllipse);
    }
    
    private static void renderLine(final Graphics2D g2d, final Element element, final AffineTransform transform) {
        double x1 = getAttributeAsDouble(element, "x1", 0);
        double y1 = getAttributeAsDouble(element, "y1", 0);
        double x2 = getAttributeAsDouble(element, "x2", 0);
        double y2 = getAttributeAsDouble(element, "y2", 0);
        
        java.awt.geom.Line2D line = new java.awt.geom.Line2D.Double(x1, y1, x2, y2);
        Shape transformedLine = transform.createTransformedShape(line);
        g2d.draw(transformedLine);
    }
    
    private static void renderPath(final Graphics2D g2d, final Element element, final AffineTransform transform) {
        // Simplified path rendering - just skip for now as it's complex
        // In a full implementation, you'd parse SVG path data
    }
    
    private static void renderPolygon(final Graphics2D g2d, final Element element, final AffineTransform transform) {
        // Simplified polygon rendering
        String points = element.getAttribute("points");
        if (points != null && !points.isEmpty()) {
            String[] coords = points.trim().split("[, ]+");
            int[] xPoints = new int[coords.length / 2];
            int[] yPoints = new int[coords.length / 2];
            
            for (int i = 0; i < coords.length - 1; i += 2) {
                xPoints[i / 2] = (int) Double.parseDouble(coords[i]);
                yPoints[i / 2] = (int) Double.parseDouble(coords[i + 1]);
            }
            
            java.awt.Polygon polygon = new java.awt.Polygon(xPoints, yPoints, xPoints.length);
            Shape transformedPolygon = transform.createTransformedShape(polygon);
            g2d.fill(transformedPolygon);
        }
    }
    
    private static void renderText(final Graphics2D g2d, final Element element, final AffineTransform transform) {
        // Simplified text rendering
        String text = element.getTextContent();
        double x = getAttributeAsDouble(element, "x", 0);
        double y = getAttributeAsDouble(element, "y", 0);
        
        java.awt.geom.Point2D.Double point = new java.awt.geom.Point2D.Double(x, y);
        java.awt.geom.Point2D transformed = new java.awt.geom.Point2D.Double();
        transform.transform(point, transformed);
        
        g2d.drawString(text, (float) transformed.getX(), (float) transformed.getY());
    }
    
    private static double getAttributeAsDouble(final Element element, final String attr, final double defaultValue) {
        String value = element.getAttribute(attr);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Create a placeholder image if SVG cannot be loaded.
     */
    private static BufferedImage createPlaceholderImage(final RoomFeature feature, final int width, final int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Draw background
        g2d.setColor(new Color(200, 180, 160));
        g2d.fillRect(0, 0, width, height);
        
        // Draw border
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRect(0, 0, width - 1, height - 1);
        
        // Draw feature name
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 8));
        String name = feature.name().substring(0, Math.min(3, feature.name().length()));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(name, (width - fm.stringWidth(name)) / 2, height / 2 + fm.getAscent() / 2);
        
        g2d.dispose();
        return image;
    }
}
