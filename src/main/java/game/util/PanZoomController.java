package game.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.SwingUtilities;

/**
 * Reusable pan/zoom controller for any JPanel.
 * Handles mouse-based panning and mouse-wheel zooming with zoom-toward-cursor behavior.
 * 
 * Usage:
 * <pre>
 * PanZoomController controller = new PanZoomController(50.0, 0.5, 200.0);
 * panel.addMouseListener(controller);
 * panel.addMouseMotionListener(controller);
 * panel.addMouseWheelListener(controller);
 * 
 * // In paintComponent:
 * Graphics2D g2d = ...;
 * g2d.translate(controller.getOffsetX(), controller.getOffsetY());
 * g2d.scale(controller.getScale(), controller.getScale());
 * </pre>
 */
public class PanZoomController extends MouseAdapter {
    
    private double scale;
    private double offsetX = 0;
    private double offsetY = 0;
    
    private final double minZoom;
    private final double maxZoom;
    private final double zoomFactor;
    
    private Point lastMousePoint;
    private boolean isPanning = false;
    
    // Callback to trigger repaints
    private Runnable onStateChanged;
    
    /**
     * Create a new PanZoomController.
     * 
     * @param initialScale initial zoom level (e.g., 50.0 for DungeonPanel)
     * @param minZoom minimum allowed zoom level (e.g., 0.5)
     * @param maxZoom maximum allowed zoom level (e.g., 200.0)
     */
    public PanZoomController(final double initialScale, final double minZoom, final double maxZoom) {
        this.scale = initialScale;
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
        this.zoomFactor = 1.1;
    }
    
    /**
     * Set callback to trigger when pan/zoom state changes (usually calls repaint()).
     */
    public void setOnStateChanged(final Runnable callback) {
        this.onStateChanged = callback;
    }
    
    @Override
    public void mousePressed(final MouseEvent e) {
        lastMousePoint = e.getPoint();
        isPanning = false;
        
        // Right-click initiates panning
        if (SwingUtilities.isRightMouseButton(e)) {
            isPanning = true;
        }
    }
    
    @Override
    public void mouseDragged(final MouseEvent e) {
        if (lastMousePoint == null) {
            return;
        }
        
        if (isPanning) {
            final double dx = e.getX() - lastMousePoint.getX();
            final double dy = e.getY() - lastMousePoint.getY();
            offsetX += dx;
            offsetY += dy;
            lastMousePoint = e.getPoint();
            triggerRepaint();
        }
    }
    
    @Override
    public void mouseReleased(final MouseEvent e) {
        isPanning = false;
        lastMousePoint = null;
    }
    
    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
        final double oldScale = scale;
        
        if (e.getWheelRotation() > 0) {
            scale /= zoomFactor;
        } else {
            scale *= zoomFactor;
        }
        
        // Clamp zoom
        scale = Math.max(minZoom, Math.min(maxZoom, scale));
        
        if (scale == oldScale) {
            return; // No zoom change, don't adjust offset
        }
        
        // Zoom toward mouse cursor
        final int mouseX = e.getX();
        final int mouseY = e.getY();
        
        final double worldX = (mouseX - offsetX) / oldScale;
        final double worldY = (mouseY - offsetY) / oldScale;
        
        offsetX = mouseX - worldX * scale;
        offsetY = mouseY - worldY * scale;
        
        triggerRepaint();
    }
    
    /**
     * Get current offset X (for graphics translation).
     */
    public double getOffsetX() {
        return offsetX;
    }
    
    /**
     * Get current offset Y (for graphics translation).
     */
    public double getOffsetY() {
        return offsetY;
    }
    
    /**
     * Get current scale factor (for graphics scaling).
     */
    public double getScale() {
        return scale;
    }
    
    /**
     * Set scale directly (useful for programmatic zoom).
     */
    public void setScale(final double newScale) {
        this.scale = Math.max(minZoom, Math.min(maxZoom, newScale));
        triggerRepaint();
    }
    
    /**
     * Set offset directly (useful for programmatic pan).
     */
    public void setOffset(final double newOffsetX, final double newOffsetY) {
        this.offsetX = newOffsetX;
        this.offsetY = newOffsetY;
        triggerRepaint();
    }
    
    /**
     * Center view on a world coordinate.
     * 
     * @param centerX world x coordinate to center on
     * @param centerY world y coordinate to center on
     * @param panelWidth width of the panel
     * @param panelHeight height of the panel
     */
    public void centerOn(final double centerX, final double centerY, 
                         final int panelWidth, final int panelHeight) {
        offsetX = panelWidth / 2.0 - centerX * scale;
        offsetY = panelHeight / 2.0 - centerY * scale;
        triggerRepaint();
    }
    
    /**
     * Convert screen coordinates to world coordinates.
     * 
     * @param screenPoint point in screen space
     * @return point in world space
     */
    public Point2D worldCoordinates(final Point screenPoint) {
        double worldX = (screenPoint.x - offsetX) / scale;
        double worldY = (screenPoint.y - offsetY) / scale;
        return new Point2D(worldX, worldY);
    }
    
    /**
     * Simple 2D point class for world coordinates.
     */
    public static class Point2D {
        public final double x;
        public final double y;
        
        public Point2D(final double x, final double y) {
            this.x = x;
            this.y = y;
        }
    }
    
    private void triggerRepaint() {
        if (onStateChanged != null) {
            onStateChanged.run();
        }
    }
}
