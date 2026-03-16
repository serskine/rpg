package game.sprite.view;

import game.sprite.Arc;
import game.sprite.Circle;
import game.sprite.ColorPaletteLoader;
import game.sprite.Curve;
import game.sprite.LineSegment;
import game.sprite.Path;
import game.sprite.PathPoint;
import game.sprite.PathSegmentType;
import game.sprite.Polygon;
import game.sprite.SpriteFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

/**
 * Interactive grid-based sprite editor canvas.
 * Displays all shapes (polygons, circles, arcs, curves, paths), handles point selection/addition/dragging, and supports pan/zoom.
 */
public class SpriteCanvasPanel extends JPanel {
    
    // Grid and viewport
    private static final int GRID_CELL_SIZE_PX = 25;
    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 10.0;
    private static final double ZOOM_FACTOR = 1.1;
    
    // Interaction
    private static final int SNAP_DISTANCE = 10;  // pixels
    private static final int HOVER_RADIUS = 50;   // pixels
    
    // Colors
    private static final Color GRID_COLOR = new Color(100, 100, 100);
    private static final Color HOVER_POINT_COLOR = new Color(0, 255, 255);
    private static final Color POINT_COLOR = new Color(255, 100, 0);
    private static final Color SELECTED_POLYGON_OUTLINE = new Color(255, 200, 0);
    private static final Color UNSELECTED_POLYGON_ALPHA = new Color(0, 0, 0, 0);  // Will use AlphaComposite
    private static final float UNSELECTED_POLYGON_OPACITY = 0.5f;
    private static final Color BOUNDS_COLOR = new Color(255, 255, 0);  // Yellow for bounds
    
    // State
    private SpriteFile spriteFile;
    private List<Polygon> polygons = new ArrayList<>();
    private Map<String, Color> colorCache = new HashMap<>();
    private int selectedPolygonIndex = -1;
    
    // Viewport state
    private double scale = 1.0;  // Pixels per grid cell (at 1.0, each grid cell = GRID_CELL_SIZE_PX)
    private double offsetX = 0;
    private double offsetY = 0;
    
    // Mouse state
     private Point lastMousePoint;
     private Point2D.Double hoveredGridPoint;
     
     // Point dragging state
     private Point2D.Double dragSourcePoint;  // Original point being dragged (in grid coords)
     private Point2D.Double dragCurrentPoint; // Current destination during drag (in grid coords)
     private String dragShapeType;            // Type of shape being dragged (POLYGON, CURVE, PATH, LINE_SEGMENT)
     private int dragShapeIndex;              // Index of shape in its respective list
     private int dragPointIndex;              // Index of point within the shape
     
     // Callbacks
     private Runnable onPolygonsChanged;
     private Runnable onPolygonSelectionChanged;
    
    public SpriteCanvasPanel() {
        setBackground(new Color(45, 45, 45));
        
        // Mouse interaction
        final MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                lastMousePoint = e.getPoint();
                
                if (SwingUtilities.isRightMouseButton(e)) {
                    // Right-click: prepare for pan/zoom
                    e.consume();
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    // Left-click: select polygon or add point
                    handleLeftClick(e.getPoint());
                    e.consume();
                }
            }
            
             @Override
             public void mouseDragged(final MouseEvent e) {
                 if (lastMousePoint == null) {
                     return;
                 }
                 
                 if ((e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
                     // Right-click drag: pan
                     final double dx = e.getX() - lastMousePoint.getX();
                     final double dy = e.getY() - lastMousePoint.getY();
                     offsetX += dx;
                     offsetY += dy;
                     lastMousePoint = e.getPoint();
                     repaint();
                     e.consume();
                 } else if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
                     // Left-click drag: drag point
                     final Point2D.Double gridPoint = screenToGrid(e.getPoint());
                     final Point2D.Double snappedPoint = snapToGrid(gridPoint);
                     
                     // If we're not currently dragging, try to start a drag
                     if (dragSourcePoint == null) {
                         dragSourcePoint = findPointAtScreenLocation(e.getPoint());
                         if (dragSourcePoint != null) {
                             dragCurrentPoint = new Point2D.Double(dragSourcePoint.x, dragSourcePoint.y);
                         }
                     }
                     
                     // If we have an active drag, update the destination
                     if (dragSourcePoint != null) {
                         dragCurrentPoint = snappedPoint;
                         repaint();
                         e.consume();
                     }
                 }
             }
            
            @Override
            public void mouseMoved(final MouseEvent e) {
                // Update hovered grid point
                final Point2D.Double gridPoint = screenToGrid(e.getPoint());
                final Point2D.Double snappedGrid = snapToGrid(gridPoint);
                
                // Check if there's a grid point nearby
                if (isWithinHoverRadius(gridPoint, snappedGrid)) {
                    hoveredGridPoint = snappedGrid;
                } else {
                    hoveredGridPoint = null;
                }
                
                repaint();
            }
            
             @Override
             public void mouseWheelMoved(final MouseWheelEvent e) {
                 if ((e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
                     // Right-click + scroll: zoom
                     final double oldScale = scale;
                     if (e.getWheelRotation() < 0) {
                         scale *= ZOOM_FACTOR;
                     } else {
                         scale /= ZOOM_FACTOR;
                     }
                     
                     // Clamp zoom
                     scale = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, scale));
                     
                     // Adjust offset to zoom toward mouse
                     final double mouseX = e.getX();
                     final double mouseY = e.getY();
                     final double worldX = (mouseX - offsetX) / oldScale;
                     final double worldY = (mouseY - offsetY) / oldScale;
                     offsetX = mouseX - worldX * scale;
                     offsetY = mouseY - worldY * scale;
                     
                     repaint();
                     e.consume();
                 }
             }
             
             @Override
             public void mouseReleased(final MouseEvent e) {
                 // Complete point drag if one is in progress
                 if (dragSourcePoint != null && dragCurrentPoint != null && 
                     !dragSourcePoint.equals(dragCurrentPoint)) {
                     // Apply the point move
                     applyPointDrag(dragSourcePoint, dragCurrentPoint);
                 }
                 
                 // Clear drag state
                 dragSourcePoint = null;
                 dragCurrentPoint = null;
                 dragShapeType = null;
                 dragPointIndex = -1;
                 dragShapeIndex = -1;
                 
                 repaint();
             }
        };
        
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        addMouseWheelListener(mouseHandler);
    }
    
    /**
     * Set the sprite file to display.
     */
    public void setSpriteFile(final SpriteFile spriteFile) {
        this.spriteFile = spriteFile;
        this.polygons = spriteFile == null ? new ArrayList<>() : new ArrayList<>(spriteFile.getPolygons());
        this.colorCache = spriteFile == null
            ? new HashMap<>()
            : ColorPaletteLoader.createColorCache(spriteFile.colors());
        this.selectedPolygonIndex = -1;
        centerView();
        repaint();
    }
    
    /**
     * Get the current polygons list.
     */
    public List<Polygon> getPolygons() {
        return polygons;
    }
    
    /**
     * Get the selected polygon index, or -1 if none selected.
     */
    public int getSelectedPolygonIndex() {
        return selectedPolygonIndex;
    }
    
    /**
     * Select a polygon by index.
     */
    public void selectPolygon(final int index) {
        if (index >= -1 && index < polygons.size()) {
            selectedPolygonIndex = index;
            repaint();
            if (onPolygonSelectionChanged != null) {
                onPolygonSelectionChanged.run();
            }
        }
    }
    
    /**
     * Set callback for when polygons change.
     */
    public void setOnPolygonsChanged(final Runnable callback) {
        this.onPolygonsChanged = callback;
    }
    
    /**
     * Set callback for when polygon selection changes.
     */
    public void setOnPolygonSelectionChanged(final Runnable callback) {
        this.onPolygonSelectionChanged = callback;
    }
    
    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw grid
        drawGrid(g2d);
        
        // Draw bounds
        if (spriteFile != null) {
            drawBounds(g2d);
        }
        
        // Draw polygons
        if (!polygons.isEmpty()) {
            drawPolygons(g2d);
        }
        
        // Draw circles
        if (spriteFile != null && !spriteFile.getCircles().isEmpty()) {
            drawCircles(g2d);
        }
        
        // Draw arcs
        if (spriteFile != null && !spriteFile.getArcs().isEmpty()) {
            drawArcs(g2d);
        }
        
        // Draw line segments
         if (spriteFile != null && !spriteFile.getLineSegments().isEmpty()) {
             drawLineSegments(g2d);
         }
         
         // Draw curves
         if (spriteFile != null && !spriteFile.getCurves().isEmpty()) {
             drawCurves(g2d);
         }
         
         // Draw paths
         if (spriteFile != null && !spriteFile.getPaths().isEmpty()) {
             drawPaths(g2d);
         }
         
          // Draw hovered point
          if (hoveredGridPoint != null) {
              drawHoveredPoint(g2d);
          }
          
          // Draw drag arrow if dragging a point
          if (dragSourcePoint != null && dragCurrentPoint != null) {
              drawDragArrow(g2d);
          }
     }
    
    private void drawGrid(final Graphics2D g2d) {
        g2d.setColor(GRID_COLOR);
        
        final int gridPixelSize = (int)(GRID_CELL_SIZE_PX * scale);
        
        // Calculate visible grid bounds
        final int gridStartX = (int)Math.floor(-offsetX / gridPixelSize);
        final int gridEndX = (int)Math.ceil((getWidth() - offsetX) / gridPixelSize);
        final int gridStartY = (int)Math.floor(-offsetY / gridPixelSize);
        final int gridEndY = (int)Math.ceil((getHeight() - offsetY) / gridPixelSize);
        
        // Draw vertical lines
        for (int x = gridStartX; x <= gridEndX; x++) {
            final int screenX = (int)(x * gridPixelSize + offsetX);
            // Thicker line if x is divisible by 8
            if (x % 8 == 0) {
                g2d.setStroke(new BasicStroke(2));
            } else {
                g2d.setStroke(new BasicStroke(1));
            }
            g2d.drawLine(screenX, 0, screenX, getHeight());
        }
        
        // Draw horizontal lines
        for (int y = gridStartY; y <= gridEndY; y++) {
            final int screenY = (int)(y * gridPixelSize + offsetY);
            // Thicker line if y is divisible by 8
            if (y % 8 == 0) {
                g2d.setStroke(new BasicStroke(2));
            } else {
                g2d.setStroke(new BasicStroke(1));
            }
            g2d.drawLine(0, screenY, getWidth(), screenY);
        }
    }
    
    private void drawBounds(final Graphics2D g2d) {
        final game.sprite.Bounds bounds = spriteFile.bounds();
        if (bounds == null || (bounds.width() == 0 && bounds.height() == 0)) {
            return;
        }
        
        // Convert bounds from grid coordinates to screen coordinates
        final Point topLeftScreen = gridToScreen(new Point2D.Double(bounds.x(), bounds.y()));
        final Point bottomRightScreen = gridToScreen(new Point2D.Double(
            bounds.x() + bounds.width(),
            bounds.y() + bounds.height()
        ));
        
        final int screenX = topLeftScreen.x;
        final int screenY = topLeftScreen.y;
        final int screenWidth = bottomRightScreen.x - topLeftScreen.x;
        final int screenHeight = bottomRightScreen.y - topLeftScreen.y;
        
        // Draw thick dashed yellow rectangle
        g2d.setColor(BOUNDS_COLOR);
        final float[] dashPattern = {5.0f, 5.0f};  // 5 pixels on, 5 pixels off
        g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, dashPattern, 0.0f));
        g2d.drawRect(screenX, screenY, screenWidth, screenHeight);
    }
    
    private void drawPolygons(final Graphics2D g2d) {
        for (int i = 0; i < polygons.size(); i++) {
            final Polygon polygon = polygons.get(i);
            final boolean isSelected = (i == selectedPolygonIndex);
            drawPolygon(g2d, polygon, isSelected);
        }
    }
    
     private void drawPolygon(final Graphics2D g2d, final Polygon polygon, final boolean isSelected) {
         if (polygon.path().isEmpty()) {
             return;
         }
         
         // Set alpha for unselected polygons
         if (!isSelected) {
             g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, UNSELECTED_POLYGON_OPACITY));
         }
         
         // Build path from polygon points with curve support
         final java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();
         final List<PathPoint> pathPoints = polygon.path();
         
         for (int i = 0; i < pathPoints.size(); i++) {
             final PathPoint currentPoint = pathPoints.get(i);
             final Point screenPoint = gridToScreen(new Point2D.Double(currentPoint.x(), currentPoint.y()));
             
             if (i == 0) {
                 path.moveTo(screenPoint.x, screenPoint.y);
             } else {
                 final PathPoint prevPoint = pathPoints.get(i - 1);
                 final Point prevScreen = gridToScreen(new Point2D.Double(prevPoint.x(), prevPoint.y()));
                 
                 // Handle segment types
                 if (currentPoint.type() == PathSegmentType.NONE) {
                     // NONE: skip rendering line, just move to next point
                     path.moveTo(screenPoint.x, screenPoint.y);
                 } else if (currentPoint.type() == PathSegmentType.LEFT) {
                     // Left curve: control point is to the left
                     final int midX = (prevScreen.x + screenPoint.x) / 2;
                     final int midY = (prevScreen.y + screenPoint.y) / 2;
                     final int dx = screenPoint.x - prevScreen.x;
                     final int dy = screenPoint.y - prevScreen.y;
                     final int controlX = midX - dy / 2;
                     final int controlY = midY + dx / 2;
                     path.quadTo(controlX, controlY, screenPoint.x, screenPoint.y);
                 } else if (currentPoint.type() == PathSegmentType.RIGHT) {
                     // Right curve: control point is to the right
                     final int midX = (prevScreen.x + screenPoint.x) / 2;
                     final int midY = (prevScreen.y + screenPoint.y) / 2;
                     final int dx = screenPoint.x - prevScreen.x;
                     final int dy = screenPoint.y - prevScreen.y;
                     final int controlX = midX + dy / 2;
                     final int controlY = midY - dx / 2;
                     path.quadTo(controlX, controlY, screenPoint.x, screenPoint.y);
                 } else {
                     // STRAIGHT: regular line (default)
                     path.lineTo(screenPoint.x, screenPoint.y);
                 }
             }
         }
         
         // Close path if it's a closed polygon
         if (!polygon.isOpen()) {
             path.closePath();
         }
         
         // Draw fill (only for closed polygons)
         if (!polygon.isOpen() && polygon.fillColor() != null) {
             final Color fillColor = colorCache.get(polygon.fillColor());
             if (fillColor != null) {
                 g2d.setColor(fillColor);
                 g2d.fill(path);
             }
         }
         
         // Draw line
         if (polygon.lineColor() != null) {
             final Color lineColor = colorCache.get(polygon.lineColor());
             if (lineColor != null) {
                 g2d.setColor(lineColor);
                 g2d.setStroke(new BasicStroke(2));
                 g2d.draw(path);
             }
         }
         
         // Reset composite
         if (!isSelected) {
             g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
         }
         
         // Draw selection outline
         if (isSelected) {
             g2d.setColor(SELECTED_POLYGON_OUTLINE);
             g2d.setStroke(new BasicStroke(3));
             g2d.draw(path);
         }
         
         // Draw points
         g2d.setColor(POINT_COLOR);
         for (final PathPoint pathPoint : pathPoints) {
             final Point screenPoint = gridToScreen(new Point2D.Double(pathPoint.x(), pathPoint.y()));
             final int radius = 4;
             g2d.fillOval(screenPoint.x - radius, screenPoint.y - radius, radius * 2, radius * 2);
         }
     }
    
    private void drawCircles(final Graphics2D g2d) {
        for (final Circle circle : spriteFile.getCircles()) {
            drawCircle(g2d, circle);
        }
    }
    
    private void drawCircle(final Graphics2D g2d, final Circle circle) {
        final Point centerScreen = gridToScreen(circle.center());
        final int radiusPixels = (int)(circle.radius() * GRID_CELL_SIZE_PX * scale);
        
        // Set alpha for unselected circles
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, UNSELECTED_POLYGON_OPACITY));
        
        // Draw fill
        if (circle.fillColor() != null) {
            final Color fillColor = colorCache.get(circle.fillColor());
            if (fillColor != null) {
                g2d.setColor(fillColor);
                g2d.fillOval(
                    centerScreen.x - radiusPixels,
                    centerScreen.y - radiusPixels,
                    radiusPixels * 2,
                    radiusPixels * 2
                );
            }
        }
        
        // Draw line
        if (circle.lineColor() != null) {
            final Color lineColor = colorCache.get(circle.lineColor());
            if (lineColor != null) {
                g2d.setColor(lineColor);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(
                    centerScreen.x - radiusPixels,
                    centerScreen.y - radiusPixels,
                    radiusPixels * 2,
                    radiusPixels * 2
                );
            }
        }
        
        // Reset composite
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    private void drawArcs(final Graphics2D g2d) {
        for (final Arc arc : spriteFile.getArcs()) {
            drawArc(g2d, arc);
        }
    }
    
    private void drawArc(final Graphics2D g2d, final Arc arc) {
        final Point startScreen = gridToScreen(arc.start());
        final Point endScreen = gridToScreen(arc.end());
        final Point controlScreen = gridToScreen(arc.getControlPoint());
        
        // Set alpha for unselected arcs
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, UNSELECTED_POLYGON_OPACITY));
        
        // Draw fill using quadratic Bezier curve
        if (arc.fillColor() != null) {
            final Color fillColor = colorCache.get(arc.fillColor());
            if (fillColor != null) {
                g2d.setColor(fillColor);
                final java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();
                path.moveTo(startScreen.x, startScreen.y);
                path.quadTo(controlScreen.x, controlScreen.y, endScreen.x, endScreen.y);
                path.lineTo(startScreen.x, startScreen.y);
                g2d.fill(path);
            }
        }
        
        // Draw line
        if (arc.lineColor() != null) {
            final Color lineColor = colorCache.get(arc.lineColor());
            if (lineColor != null) {
                g2d.setColor(lineColor);
                g2d.setStroke(new BasicStroke(2));
                final java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();
                path.moveTo(startScreen.x, startScreen.y);
                path.quadTo(controlScreen.x, controlScreen.y, endScreen.x, endScreen.y);
                g2d.draw(path);
            }
        }
        
        // Reset composite
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    private void drawLineSegments(final Graphics2D g2d) {
        for (final LineSegment lineSegment : spriteFile.getLineSegments()) {
            drawLineSegment(g2d, lineSegment);
        }
    }
    
     private void drawLineSegment(final Graphics2D g2d, final LineSegment lineSegment) {
         if (lineSegment.points().isEmpty()) {
             return;
         }
         
         // Set alpha for line segments
         g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, UNSELECTED_POLYGON_OPACITY));
         
         // Draw line
         if (lineSegment.lineColor() != null) {
             final Color lineColor = colorCache.get(lineSegment.lineColor());
             if (lineColor != null) {
                 g2d.setColor(lineColor);
                 g2d.setStroke(new BasicStroke(2));
                 
                 final java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();
                 
                 // Build path from line points
                 final java.util.List<game.sprite.LinePoint> points = lineSegment.points();
                 for (int i = 0; i < points.size(); i++) {
                     final game.sprite.LinePoint linePoint = points.get(i);
                     final Point screenPoint = gridToScreen(linePoint.point());
                     
                     if (i == 0) {
                         path.moveTo(screenPoint.x, screenPoint.y);
                     } else {
                         final game.sprite.LinePoint prevPoint = points.get(i - 1);
                         final Point prevScreen = gridToScreen(prevPoint.point());
                         
                         // Handle curve types
                         if (linePoint.curve() == game.sprite.CurveType.NONE) {
                             path.lineTo(screenPoint.x, screenPoint.y);
                         } else if (linePoint.curve() == game.sprite.CurveType.LEFT) {
                             // Left curve: control point is to the left
                             final int midX = (prevScreen.x + screenPoint.x) / 2;
                             final int midY = (prevScreen.y + screenPoint.y) / 2;
                             final int dx = screenPoint.x - prevScreen.x;
                             final int dy = screenPoint.y - prevScreen.y;
                             final int controlX = midX - dy / 2;
                             final int controlY = midY + dx / 2;
                             path.quadTo(controlX, controlY, screenPoint.x, screenPoint.y);
                         } else if (linePoint.curve() == game.sprite.CurveType.RIGHT) {
                             // Right curve: control point is to the right
                             final int midX = (prevScreen.x + screenPoint.x) / 2;
                             final int midY = (prevScreen.y + screenPoint.y) / 2;
                             final int dx = screenPoint.x - prevScreen.x;
                             final int dy = screenPoint.y - prevScreen.y;
                             final int controlX = midX + dy / 2;
                             final int controlY = midY - dx / 2;
                             path.quadTo(controlX, controlY, screenPoint.x, screenPoint.y);
                         }
                     }
                 }
                 
                 g2d.draw(path);
             }
         }
         
         // Reset composite
         g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
         
         // Draw points
         g2d.setColor(POINT_COLOR);
         for (final game.sprite.LinePoint linePoint : lineSegment.points()) {
             final Point screenPoint = gridToScreen(linePoint.point());
             final int radius = 4;
             g2d.fillOval(screenPoint.x - radius, screenPoint.y - radius, radius * 2, radius * 2);
         }
     }
     
     private void drawCurves(final Graphics2D g2d) {
         for (final game.sprite.Curve curve : spriteFile.getCurves()) {
             drawCurve(g2d, curve);
         }
     }
     
     private void drawCurve(final Graphics2D g2d, final game.sprite.Curve curve) {
         // Set alpha for curves
         g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, UNSELECTED_POLYGON_OPACITY));
         
         // Draw fill (if present)
         if (curve.fillColor() != null) {
             final Color fillColor = colorCache.get(curve.fillColor());
             if (fillColor != null) {
                 g2d.setColor(fillColor);
                 // For a filled curve, create a closed path
                 final java.awt.geom.Path2D.Double fillPath = new java.awt.geom.Path2D.Double();
                 final Point start = gridToScreen(curve.start());
                 final Point cp1 = gridToScreen(curve.controlPoint1());
                 final Point cp2 = gridToScreen(curve.controlPoint2());
                 final Point end = gridToScreen(curve.end());
                 
                 fillPath.moveTo(start.x, start.y);
                 fillPath.curveTo(cp1.x, cp1.y, cp2.x, cp2.y, end.x, end.y);
                 fillPath.lineTo(end.x, end.y);
                 fillPath.closePath();
                 g2d.fill(fillPath);
             }
         }
         
         // Draw line
         if (curve.lineColor() != null) {
             final Color lineColor = colorCache.get(curve.lineColor());
             if (lineColor != null) {
                 g2d.setColor(lineColor);
                 g2d.setStroke(new BasicStroke(2));
                 
                 final java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();
                 final Point start = gridToScreen(curve.start());
                 final Point cp1 = gridToScreen(curve.controlPoint1());
                 final Point cp2 = gridToScreen(curve.controlPoint2());
                 final Point end = gridToScreen(curve.end());
                 
                 path.moveTo(start.x, start.y);
                 path.curveTo(cp1.x, cp1.y, cp2.x, cp2.y, end.x, end.y);
                 g2d.draw(path);
             }
         }
         
         // Reset composite
         g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
         
         // Draw control points
         g2d.setColor(POINT_COLOR);
         final Point[] points = {
             gridToScreen(curve.start()),
             gridToScreen(curve.controlPoint1()),
             gridToScreen(curve.controlPoint2()),
             gridToScreen(curve.end())
         };
         final int radius = 4;
         for (final Point p : points) {
             g2d.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
         }
     }
     
     private void drawPaths(final Graphics2D g2d) {
         for (final game.sprite.Path path : spriteFile.getPaths()) {
             drawPath(g2d, path);
         }
     }
     
     private void drawPath(final Graphics2D g2d, final game.sprite.Path path) {
         if (path.points().isEmpty()) {
             return;
         }
         
         // Set alpha for paths
         g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, UNSELECTED_POLYGON_OPACITY));
         
         // Draw fill (if present)
         if (path.fillColor() != null) {
             final Color fillColor = colorCache.get(path.fillColor());
             if (fillColor != null) {
                 g2d.setColor(fillColor);
                 final int[] xPoints = new int[path.points().size()];
                 final int[] yPoints = new int[path.points().size()];
                 for (int i = 0; i < path.points().size(); i++) {
                     final Point p = gridToScreen(path.points().get(i));
                     xPoints[i] = p.x;
                     yPoints[i] = p.y;
                 }
                 g2d.fillPolygon(xPoints, yPoints, xPoints.length);
             }
         }
         
         // Draw line
         if (path.lineColor() != null) {
             final Color lineColor = colorCache.get(path.lineColor());
             if (lineColor != null) {
                 g2d.setColor(lineColor);
                 g2d.setStroke(new BasicStroke(2));
                 
                 final java.awt.geom.Path2D.Double drawPath = new java.awt.geom.Path2D.Double();
                 final java.util.List<java.awt.geom.Point2D.Double> points = path.points();
                 
                 for (int i = 0; i < points.size(); i++) {
                     final Point p = gridToScreen(points.get(i));
                     if (i == 0) {
                         drawPath.moveTo(p.x, p.y);
                     } else {
                         drawPath.lineTo(p.x, p.y);
                     }
                 }
                 
                 g2d.draw(drawPath);
             }
         }
         
         // Reset composite
         g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
         
         // Draw points
         g2d.setColor(POINT_COLOR);
         final int radius = 4;
         for (final java.awt.geom.Point2D.Double pt : path.points()) {
             final Point p = gridToScreen(pt);
             g2d.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
         }
     }

    
     private void drawHoveredPoint(final Graphics2D g2d) {
         final Point screenPoint = gridToScreen(hoveredGridPoint);
         g2d.setColor(HOVER_POINT_COLOR);
         final int radius = 6;
         g2d.fillOval(screenPoint.x - radius, screenPoint.y - radius, radius * 2, radius * 2);
     }
     
     private void drawDragArrow(final Graphics2D g2d) {
         final Point sourceScreen = gridToScreen(dragSourcePoint);
         final Point destScreen = gridToScreen(dragCurrentPoint);
         
         // Set yellow color and stroke for arrow
         g2d.setColor(new Color(255, 255, 0));  // Yellow
         g2d.setStroke(new BasicStroke(2));
         
         // Draw line from source to destination
         g2d.drawLine(sourceScreen.x, sourceScreen.y, destScreen.x, destScreen.y);
         
         // Draw arrowhead at destination
         final double angle = Math.atan2(
             destScreen.y - sourceScreen.y,
             destScreen.x - sourceScreen.x
         );
         
         final int arrowSize = 10;
         final int x1 = (int)(destScreen.x - arrowSize * Math.cos(angle - Math.PI / 6));
         final int y1 = (int)(destScreen.y - arrowSize * Math.sin(angle - Math.PI / 6));
         final int x2 = (int)(destScreen.x - arrowSize * Math.cos(angle + Math.PI / 6));
         final int y2 = (int)(destScreen.y - arrowSize * Math.sin(angle + Math.PI / 6));
         
         g2d.drawLine(destScreen.x, destScreen.y, x1, y1);
         g2d.drawLine(destScreen.x, destScreen.y, x2, y2);
         
         // Draw source and destination points
         final int radius = 5;
         g2d.fillOval(sourceScreen.x - radius, sourceScreen.y - radius, radius * 2, radius * 2);
         g2d.fillOval(destScreen.x - radius, destScreen.y - radius, radius * 2, radius * 2);
     }

    
    private void handleLeftClick(final Point screenPoint) {
        final Point2D.Double gridPoint = screenToGrid(screenPoint);
        final Point2D.Double snappedGrid = snapToGrid(gridPoint);
        
        // Check if we clicked on a polygon
        final int clickedPolygonIndex = getPolygonAtPoint(screenPoint);
        if (clickedPolygonIndex != -1) {
            selectPolygon(clickedPolygonIndex);
            return;
        }
        
        // Check if we're near a grid point and have a selected polygon
        if (selectedPolygonIndex != -1 && isWithinSnapDistance(gridPoint, snappedGrid)) {
            // Add point to selected polygon
            addPointToSelectedPolygon(snappedGrid);
        }
    }
    
     private void addPointToSelectedPolygon(final Point2D.Double gridPoint) {
         final Polygon polygon = polygons.get(selectedPolygonIndex);
         final List<PathPoint> newPath = new ArrayList<>(polygon.path());
         // New points default to STRAIGHT segment type
         newPath.add(new PathPoint(PathSegmentType.STRAIGHT, Math.round(gridPoint.x), Math.round(gridPoint.y)));
         polygons.set(selectedPolygonIndex, polygon.withPath(newPath));
         
         if (onPolygonsChanged != null) {
             onPolygonsChanged.run();
         }
         repaint();
     }
     
     private int getPolygonAtPoint(final Point screenPoint) {
         for (int i = polygons.size() - 1; i >= 0; i--) {
             final Polygon polygon = polygons.get(i);
             if (polygon.path().isEmpty()) {
                 continue;
             }
             
             final int[] xPoints = new int[polygon.path().size()];
             final int[] yPoints = new int[polygon.path().size()];
             
             for (int j = 0; j < polygon.path().size(); j++) {
                 final PathPoint pathPoint = polygon.path().get(j);
                 final Point gridPoint = gridToScreen(new Point2D.Double(pathPoint.x(), pathPoint.y()));
                 xPoints[j] = gridPoint.x;
                 yPoints[j] = gridPoint.y;
             }
             
             final java.awt.Polygon screenPolygon = new java.awt.Polygon(xPoints, yPoints, xPoints.length);
             if (screenPolygon.contains(screenPoint)) {
                 return i;
             }
         }
         
         return -1;
     }
    
    private Point2D.Double screenToGrid(final Point screenPoint) {
        final double gridX = (screenPoint.x - offsetX) / (GRID_CELL_SIZE_PX * scale);
        final double gridY = (screenPoint.y - offsetY) / (GRID_CELL_SIZE_PX * scale);
        return new Point2D.Double(gridX, gridY);
    }
    
    private Point gridToScreen(final Point2D.Double gridPoint) {
        final int screenX = (int)(gridPoint.x * GRID_CELL_SIZE_PX * scale + offsetX);
        final int screenY = (int)(gridPoint.y * GRID_CELL_SIZE_PX * scale + offsetY);
        return new Point(screenX, screenY);
    }
    
    private Point2D.Double snapToGrid(final Point2D.Double gridPoint) {
        final int gridX = (int)Math.round(gridPoint.x);
        final int gridY = (int)Math.round(gridPoint.y);
        return new Point2D.Double(gridX, gridY);
    }
    
    private boolean isWithinSnapDistance(final Point2D.Double gridPoint, final Point2D.Double snappedGrid) {
        final Point screenPoint = gridToScreen(gridPoint);
        final Point snappedScreen = gridToScreen(snappedGrid);
        final double distance = screenPoint.distance(snappedScreen);
        return distance <= SNAP_DISTANCE;
    }
    
    private boolean isWithinHoverRadius(final Point2D.Double gridPoint, final Point2D.Double snappedGrid) {
        final Point screenPoint = gridToScreen(gridPoint);
        final Point snappedScreen = gridToScreen(snappedGrid);
        final double distance = screenPoint.distance(snappedScreen);
        return distance <= HOVER_RADIUS;
    }
    
    private void centerView() {
        if (polygons.isEmpty()) {
            offsetX = 0;
            offsetY = 0;
            return;
        }
        
        // Find bounds of all points
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
         double maxY = -Double.MAX_VALUE;
         
         for (final Polygon polygon : polygons) {
             for (final PathPoint pathPoint : polygon.path()) {
                 minX = Math.min(minX, pathPoint.x());
                 maxX = Math.max(maxX, pathPoint.x());
                 minY = Math.min(minY, pathPoint.y());
                 maxY = Math.max(maxY, pathPoint.y());
             }
         }
        
        if (minX == Double.MAX_VALUE) {
            return;
        }
        
        final double centerX = (minX + maxX) / 2;
        final double centerY = (minY + maxY) / 2;
        
         offsetX = getWidth() / 2.0 - centerX * GRID_CELL_SIZE_PX * scale;
         offsetY = getHeight() / 2.0 - centerY * GRID_CELL_SIZE_PX * scale;
     }
     
     /**
      * Find a point at the given screen location and initialize drag tracking.
      * Returns the point's grid coordinates if found, null otherwise.
      */
     private Point2D.Double findPointAtScreenLocation(final Point screenPoint) {
         final Point2D.Double gridPoint = screenToGrid(screenPoint);
          
          // Check all shape types for a nearby point
          // Check polygons
          for (int i = 0; i < spriteFile.getPolygons().size(); i++) {
              final Polygon polygon = spriteFile.getPolygons().get(i);
              for (int j = 0; j < polygon.path().size(); j++) {
                  final PathPoint pathPoint = polygon.path().get(j);
                  final Point2D.Double pt = new Point2D.Double(pathPoint.x(), pathPoint.y());
                  if (isPointNearby(screenPoint, pt)) {
                      dragShapeType = "POLYGON";
                      dragShapeIndex = i;
                      dragPointIndex = j;
                      return pt;
                  }
              }
          }
         
         // Check curves
         for (int i = 0; i < spriteFile.getCurves().size(); i++) {
             final Curve curve = spriteFile.getCurves().get(i);
             // Check all 4 curve points
             Point2D.Double[] points = {
                 curve.start(),
                 curve.controlPoint1(),
                 curve.controlPoint2(),
                 curve.end()
             };
             String[] pointNames = {"start", "cp1", "cp2", "end"};
             
             for (int j = 0; j < points.length; j++) {
                 if (isPointNearby(screenPoint, points[j])) {
                     dragShapeType = "CURVE";
                     dragShapeIndex = i;
                     dragPointIndex = j;  // 0=start, 1=cp1, 2=cp2, 3=end
                     return points[j];
                 }
             }
         }
         
         // Check paths
         for (int i = 0; i < spriteFile.getPaths().size(); i++) {
             final Path path = spriteFile.getPaths().get(i);
             for (int j = 0; j < path.points().size(); j++) {
                 final Point2D.Double pt = path.points().get(j);
                 if (isPointNearby(screenPoint, pt)) {
                     dragShapeType = "PATH";
                     dragShapeIndex = i;
                     dragPointIndex = j;
                     return pt;
                 }
             }
         }
         
         // Check line segments
         for (int i = 0; i < spriteFile.getLineSegments().size(); i++) {
             final LineSegment lineSegment = spriteFile.getLineSegments().get(i);
             for (int j = 0; j < lineSegment.points().size(); j++) {
                 final Point2D.Double pt = lineSegment.points().get(j).point();
                 if (isPointNearby(screenPoint, pt)) {
                     dragShapeType = "LINE_SEGMENT";
                     dragShapeIndex = i;
                     dragPointIndex = j;
                     return pt;
                 }
             }
         }
         
         return null;
     }
     
     /**
      * Check if a screen point is close to a grid point (within snap distance).
      */
     private boolean isPointNearby(final Point screenPoint, final Point2D.Double gridPoint) {
         final Point screenGridPoint = gridToScreen(gridPoint);
         final int dx = screenPoint.x - screenGridPoint.x;
         final int dy = screenPoint.y - screenGridPoint.y;
         final int distance = (int) Math.sqrt(dx * dx + dy * dy);
         return distance <= SNAP_DISTANCE;
     }
     
     /**
      * Apply the point drag by updating the shape.
      */
     private void applyPointDrag(final Point2D.Double sourcePoint, final Point2D.Double destPoint) {
         if (dragShapeType == null) {
             return;
         }
         
         final Point2D.Double newPoint = new Point2D.Double(
             Math.round(destPoint.x),
             Math.round(destPoint.y)
         );
         
         switch (dragShapeType) {
             case "POLYGON":
                 applyPolygonPointDrag(newPoint);
                 break;
             case "CURVE":
                 applyCurvePointDrag(newPoint);
                 break;
             case "PATH":
                 applyPathPointDrag(newPoint);
                 break;
             case "LINE_SEGMENT":
                 applyLineSegmentPointDrag(newPoint);
                 break;
         }
         
         if (onPolygonsChanged != null) {
             onPolygonsChanged.run();
         }
     }
     
     private void applyPolygonPointDrag(final Point2D.Double newPoint) {
          final List<Polygon> polygonList = new ArrayList<>(spriteFile.getPolygons());
          final Polygon polygon = polygonList.get(dragShapeIndex);
          final List<PathPoint> newPath = new ArrayList<>(polygon.path());
          // Update the point while preserving its segment type
          final PathPoint oldPoint = newPath.get(dragPointIndex);
          newPath.set(dragPointIndex, new PathPoint(oldPoint.type(), (int)newPoint.x, (int)newPoint.y));
          polygonList.set(dragShapeIndex, polygon.withPath(newPath));
          
          // Update the displayed polygons if this is one of them
          if (dragShapeIndex < polygons.size()) {
              polygons.set(dragShapeIndex, polygon.withPath(newPath));
          }
     }
     
     private void applyCurvePointDrag(final Point2D.Double newPoint) {
         final List<Curve> curveList = new ArrayList<>(spriteFile.getCurves());
         Curve curve = curveList.get(dragShapeIndex);
         
         // Update the appropriate curve point based on dragPointIndex
         switch (dragPointIndex) {
             case 0:  // start
                 curve = curve.withStart(newPoint);
                 break;
             case 1:  // controlPoint1
                 curve = curve.withControlPoint1(newPoint);
                 break;
             case 2:  // controlPoint2
                 curve = curve.withControlPoint2(newPoint);
                 break;
             case 3:  // end
                 curve = curve.withEnd(newPoint);
                 break;
         }
         
         curveList.set(dragShapeIndex, curve);
     }
     
     private void applyPathPointDrag(final Point2D.Double newPoint) {
         final List<Path> pathList = new ArrayList<>(spriteFile.getPaths());
         final Path path = pathList.get(dragShapeIndex);
         final List<Point2D.Double> newPoints = new ArrayList<>(path.points());
         newPoints.set(dragPointIndex, newPoint);
         pathList.set(dragShapeIndex, path.withPoints(newPoints));
     }
     
     private void applyLineSegmentPointDrag(final Point2D.Double newPoint) {
          final List<LineSegment> lineSegmentList = new ArrayList<>(spriteFile.getLineSegments());
          final LineSegment lineSegment = lineSegmentList.get(dragShapeIndex);
          final List<game.sprite.LinePoint> newPoints = new ArrayList<>(lineSegment.points());
          
          // Update the point while preserving the curve type
          final game.sprite.LinePoint oldPoint = newPoints.get(dragPointIndex);
          newPoints.set(dragPointIndex, new game.sprite.LinePoint(oldPoint.curve(), newPoint));
          lineSegmentList.set(dragShapeIndex, lineSegment.withPoints(newPoints));
      }
}

