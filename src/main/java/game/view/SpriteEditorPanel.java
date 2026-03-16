package game.view;

import game.common.Room;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpriteEditorPanel extends JPanel {
    // Viewport state
    private double scale = 50.0; // Pixels per 5ft unit (tile size)
    private double offsetX = 0;
    private double offsetY = 0;

    // Interaction state
    private Point lastMousePoint;

    // Constants
    private static final double MIN_ZOOM = 50.0;
    private static final double MAX_ZOOM = 500.0;

    public final List<Sprite> sprites = new ArrayList<>();

    public SpriteEditorPanel() {
        setBackground(Color.WHITE);

        // Add interaction listeners
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePoint = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePoint != null) {
                    double dx = e.getX() - lastMousePoint.getX();
                    double dy = e.getY() - lastMousePoint.getY();
                    offsetX += dx;
                    offsetY += dy;
                    lastMousePoint = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double zoomFactor = 1.1;
                double oldScale = scale;

                if (e.getWheelRotation() < 0) {
                    scale *= zoomFactor;
                } else {
                    scale /= zoomFactor;
                }

                // Clamp zoom
                scale = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, scale));

                // Adjust offset to zoom toward mouse pointer
                double mouseX = e.getX();
                double mouseY = e.getY();

                // Calculate world point under mouse before zoom
                double worldX = (mouseX - offsetX) / oldScale;
                double worldY = (mouseY - offsetY) / oldScale;

                // Update offset to keep that world point under mouse
                offsetX = mouseX - worldX * scale;
                offsetY = mouseY - worldY * scale;

                repaint();
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        addMouseWheelListener(mouseHandler);
    }

    private void initializeLayout() {
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        final Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF); // Pixel art style

        final AffineTransform saveXform = g2d.getTransform();
        g2d.translate(offsetX, offsetY);
        g2d.scale(scale, scale);

        g2d.setTransform(saveXform);

        for (Sprite s : sprites) {
            renderSprite(g2d, s);
        }
    }

    protected void renderSprite(final Graphics2D g2d, final Sprite s) {
        g2d.setColor(Color.LIGHT_GRAY);

        final Shape spriteShape = s.getShape();
        if (spriteShape != null) {
            if (s.getFillColor() != null) {
                g2d.setColor(s.getFillColor());
                g2d.fill(spriteShape);
            }
            if (s.getLineColor() != null) {
                g2d.setColor(s.getLineColor());
                g2d.draw(spriteShape);
            }
        }
    }

}
