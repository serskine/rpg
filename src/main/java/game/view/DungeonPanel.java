package game.view;

import game.common.Creature;
import game.common.CreatureSize;
import game.common.Path;
import game.common.PathDistance;
import game.common.Room;
import game.common.RoomFeature;
import game.util.Edge;
import game.util.Graph;
import game.util.PanZoomController;

import javax.swing.*;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DungeonPanel extends JPanel {

    private Graph<Room, Path> dungeon;
    // Map room to its center position in "Grid Units" (1 unit = 5ft)
    private final Map<Room, Point> roomPositions = new HashMap<>();
    
    // Pan/Zoom Controller (50.0 scale for pixels per 5ft unit)
    private final PanZoomController panZoomController;
    
    // Interaction state
    private Room selectedRoom;
    
    // Callback for room selection
    private RoomSelectionListener roomSelectionListener;

    public DungeonPanel() {
        setBackground(new Color(45, 45, 45));
        
        // Initialize pan/zoom controller with 50.0 scale and zoom range 1.0-500.0
        this.panZoomController = new PanZoomController(50.0, 1.0, 500.0);
        panZoomController.setOnStateChanged(this::repaint);
        
        // Add interaction listeners
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Handle pan/zoom first (right-click)
                panZoomController.mousePressed(e);
                
                // Check if a room was clicked (left-click)
                if (!SwingUtilities.isRightMouseButton(e)) {
                    Room clickedRoom = getRoomAtPoint(e.getPoint());
                    if (clickedRoom != null) {
                        setSelectedRoom(clickedRoom);
                        e.consume();
                        return;
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                panZoomController.mouseDragged(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                panZoomController.mouseReleased(e);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                panZoomController.mouseWheelMoved(e);
            }
        };
        
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        addMouseWheelListener(mouseHandler);
    }

    public void setDungeon(Graph<Room, Path> dungeon) {
        this.dungeon = dungeon;
        initializeLayout();
        centerView();
        repaint();
    }
    
    public void setRoomSelectionListener(RoomSelectionListener listener) {
        this.roomSelectionListener = listener;
    }
    
    private void setSelectedRoom(Room room) {
        this.selectedRoom = room;
        repaint();
        if (roomSelectionListener != null) {
            roomSelectionListener.onRoomSelected(room);
        }
    }
    
    private Room getRoomAtPoint(Point screenPoint) {
        if (dungeon == null || roomPositions.isEmpty()) return null;
        
        // Convert screen point to world coordinates using controller
        PanZoomController.Point2D worldPoint = panZoomController.worldCoordinates(screenPoint);
        
        // Check which room contains this point
        for (Map.Entry<Room, Point> entry : roomPositions.entrySet()) {
            Room room = entry.getKey();
            Point center = entry.getValue();
            Rectangle rect = getRoomRect(room, center);
            
            if (rect.contains((int)worldPoint.x, (int)worldPoint.y)) {
                return room;
            }
        }
        
        return null;
    }
    
    private void centerView() {
        if (roomPositions.isEmpty()) return;
        
        // Find bounds
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        
        for (Point p : roomPositions.values()) {
            minX = Math.min(minX, p.x);
            maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
        }
        
        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;
        
        panZoomController.centerOn(centerX, centerY, getWidth(), getHeight());
    }

    private void initializeLayout() {
        roomPositions.clear();
        if (dungeon == null) return;

        // Step 1: Force Directed Layout (Grid-Aware)
        runGridForceLayout();
        
        // Step 2: Graph rendering now happens directly in paintComponent
    }

    private void runGridForceLayout() {
        // Initial random placement
        Random rand = new Random(42);
        for (Room room : dungeon.getAllVertex()) {
            roomPositions.put(room, new Point(rand.nextInt(40), rand.nextInt(40)));
        }

        int iterations = 2000;
        double k = 5.0; // Optimal distance factor
        
        for (int i = 0; i < iterations; i++) {
            Map<Room, Point2D.Double> forces = new HashMap<>();
            for (Room r : dungeon.getAllVertex()) forces.put(r, new Point2D.Double(0, 0));
            
            // Repulsive
            for (Room v : dungeon.getAllVertex()) {
                for (Room u : dungeon.getAllVertex()) {
                    if (v != u) {
                        Point pv = roomPositions.get(v);
                        Point pu = roomPositions.get(u);
                        double dx = pv.x - pu.x;
                        double dy = pv.y - pu.y;
                        double distSq = dx*dx + dy*dy;
                        double dist = Math.sqrt(distSq);
                        
                        if (dist > 0) {
                            // Ensure rooms don't overlap - stronger repulsion at short range
                            // Room radius approx 2-3 units
                            double minSep = 8.0; 
                            double force = (k * k) / dist;
                            if (dist < minSep) force *= 5.0;
                            
                            forces.get(v).x += (dx/dist) * force;
                            forces.get(v).y += (dy/dist) * force;
                        }
                    }
                }
            }
            
            // Attractive (Springs)
            for (Edge<Room, Path> edge : dungeon.getAllEdges()) {
                Room v = edge.from;
                Room u = edge.to;
                Point pv = roomPositions.get(v);
                Point pu = roomPositions.get(u);
                double dx = pv.x - pu.x;
                double dy = pv.y - pu.y;
                double dist = Math.sqrt(dx*dx + dy*dy);
                
                // Target distance in grid units based on enum ordinal (0=MELEE, 1=SHORT, etc.)
                // Using ordinal + 1 to give some spacing between rooms
                double targetDist = edge.path.distance.ordinal() + 3;
                
                if (dist > 0) {
                    // Pull towards target distance
                    double displacement = dist - targetDist;
                    double force = displacement * 0.5; // Spring constant
                    
                    forces.get(v).x -= (dx/dist) * force;
                    forces.get(v).y -= (dy/dist) * force;
                    forces.get(u).x += (dx/dist) * force;
                    forces.get(u).y += (dy/dist) * force;
                }
            }
            
            // Apply forces & Snap to Grid
            double temp = 10.0 * Math.exp(-i / (double)iterations * 5);
            
            for (Room v : dungeon.getAllVertex()) {
                Point2D.Double f = forces.get(v);
                double fMag = Math.sqrt(f.x*f.x + f.y*f.y);
                
                if (fMag > 0) {
                    Point p = roomPositions.get(v);
                    // Move
                    double moveX = (f.x / fMag) * Math.min(fMag, temp);
                    double moveY = (f.y / fMag) * Math.min(fMag, temp);
                    
                    p.x += (int)Math.round(moveX);
                    p.y += (int)Math.round(moveY);
                }
            }
        }
        
        // Resolve collisions strictly
        resolveCollisions();
    }
    
    private void resolveCollisions() {
        // Simple iterative collision resolution
        boolean changed = true;
        int iter = 0;
        while (changed && iter < 100) {
            changed = false;
            List<Room> rooms = new ArrayList<>(dungeon.getAllVertex());
            for (int i = 0; i < rooms.size(); i++) {
                for (int j = i + 1; j < rooms.size(); j++) {
                    Room r1 = rooms.get(i);
                    Room r2 = rooms.get(j);
                    Point p1 = roomPositions.get(r1);
                    Point p2 = roomPositions.get(r2);
                    
                    Rectangle rect1 = getRoomRect(r1, p1);
                    Rectangle rect2 = getRoomRect(r2, p2);
                    
                    // Add buffer
                    rect1.grow(2, 2);
                    
                    if (rect1.intersects(rect2)) {
                        // Move apart
                        if (p1.x < p2.x) p1.x--; else p1.x++;
                        if (p1.y < p2.y) p1.y--; else p1.y++;
                        changed = true;
                    }
                }
            }
            iter++;
        }
    }

    private Rectangle getRoomRect(Room room, Point center) {
        int size = 0;
        switch (room.size) {
            case CRAMPED: size = 2; break;
            case ROOMY: size = 4; break;
            case VAST: size = 6; break;
        }
        return new Rectangle(center.x - size/2, center.y - size/2, size, size);
    }

    private Dimension getCreatureSizeTiles(CreatureSize size) {
        switch (size) {
            case TINY: return new Dimension(1, 1);       // Quarter of a 5ft square = 1 tile (2.5ft effective)
            case SMALL: return new Dimension(1, 1);      // Less than medium, assume 1 tile
            case MEDIUM: return new Dimension(1, 1);     // 5ft x 5ft = 1 tile
            case LARGE: return new Dimension(2, 2);      // 10ft x 10ft = 2x2 tiles
            case HUGE: return new Dimension(1, 3);       // 5ft x 15ft = 1x3 tiles
            case GARGANTUAN: return new Dimension(4, 4); // 20ft x 20ft = 4x4 tiles
            default: return new Dimension(1, 1);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (dungeon == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF); // Pixel art style
        
        AffineTransform saveXform = g2d.getTransform();
        g2d.translate(panZoomController.getOffsetX(), panZoomController.getOffsetY());
        g2d.scale(panZoomController.getScale(), panZoomController.getScale());
        
        // Draw Directed Graph Edges (straight lines)
        drawDirectedGraphEdges(g2d);
        
        // Draw Rooms
        for (Map.Entry<Room, Point> entry : roomPositions.entrySet()) {
            drawRoom(g2d, entry.getKey(), entry.getValue());
        }
        
        // Draw Creatures
        for (Map.Entry<Room, Point> entry : roomPositions.entrySet()) {
            Room room = entry.getKey();
            Point roomCenter = entry.getValue();
            if (!room.creatures.isEmpty()) {
                drawCreatures(g2d, room, roomCenter);
            }
        }
        
        // Draw Doors on Room Edges
        drawPathDetailsOnRoomEdges(g2d);
        
        g2d.setTransform(saveXform);
    }
    
     private void drawRoom(Graphics2D g2d, Room room, Point center) {
         Rectangle r = getRoomRect(room, center);
         int roomTiles = (int) Math.sqrt(room.size.numSquares);
         int tileSize = r.width / roomTiles;
         
         // Create a Graphics2D context for rendering in room pixel coordinates
         // RoomRenderer works in pixel coordinates, so we need to temporarily
         // work in that space within the transform
         AffineTransform saveTransform = g2d.getTransform();
         
         // Draw room layout using shared renderer
         RoomRenderer.drawRoomLayout(g2d, room, r.x, r.y, r.width, roomTiles, tileSize, 
                                    null); // doors are handled in drawPathDetails
         
         // Restore transform for title drawing
         g2d.setTransform(saveTransform);
         
         // Draw title above the room
         AffineTransform titleTransform = g2d.getTransform();
         g2d.translate(center.x, r.y);
         g2d.scale(1/panZoomController.getScale(), 1/panZoomController.getScale());
         
         g2d.setColor(Color.BLUE);
         g2d.setFont(new Font("Arial", Font.BOLD, 16));
         FontMetrics fm = g2d.getFontMetrics();
         g2d.drawString(room.title, -fm.stringWidth(room.title)/2, -fm.getDescent() - 2);
         
         g2d.setTransform(titleTransform);
         
         // Highlight selected room (drawn on top)
         if (room == selectedRoom) {
             g2d.setColor(new Color(255, 255, 100, 100)); // Light yellow for selected with transparency
             g2d.fillRect(r.x, r.y, r.width, r.height);
         }
         
         g2d.setTransform(saveTransform);
     }
    
    private void drawCreatures(Graphics2D g2d, Room room, Point roomCenter) {
        if (room.creatures.isEmpty()) return;
        
        Rectangle roomRect = getRoomRect(room, roomCenter);
        int roomWidth = roomRect.width;
        int roomHeight = roomRect.height;
        
        // Position creatures in a grid within the room
        int creatureIndex = 0;
        int row = 0;
        int col = 0;
        
        for (Creature creature : room.creatures) {
            Dimension size = getCreatureSizeTiles(creature.size);
            int creatureWidth = size.width;
            int creatureHeight = size.height;
            
            // Calculate position - center creature in available space
            // Start from top-left of room with some padding
            int startX = roomRect.x + 1;
            int startY = roomRect.y + 1;
            
            // Simple layout: place creatures in a row, wrap to next row if needed
            int x = startX + col * 2;
            int y = startY + row * 2;
            
            // Ensure creature fits in room
            if (x + creatureWidth > roomRect.x + roomWidth - 1) {
                col = 0;
                row++;
                x = startX;
                y = startY + row * 2;
            }
            
            if (y + creatureHeight > roomRect.y + roomHeight - 1) {
                // Room is too small, just draw at center
                x = roomCenter.x - creatureWidth / 2;
                y = roomCenter.y - creatureHeight / 2;
            }
            
            // Draw creature token as circle
            drawCreatureToken(g2d, creature, x, y, creatureWidth, creatureHeight);
            
            col++;
            if (col > 2) {
                col = 0;
                row++;
            }
        }
    }
    
    private void drawCreatureToken(Graphics2D g2d, Creature creature, int x, int y, int width, int height) {
        // Fill with a color based on creature's job or alignment
        Color tokenColor = getCreatureColor(creature);
        
        g2d.setColor(tokenColor);
        
        // Draw filled rectangle for the creature's space
        g2d.fillRect(x, y, width, height);
        
        // Draw border
        g2d.setColor(Color.BLACK);
        float strokeW = (float)(1.5 / panZoomController.getScale());
        g2d.setStroke(new BasicStroke(strokeW));
        g2d.drawRect(x, y, width, height);
        
        // Draw circle inside to represent token
        int circleSize = Math.min(width, height) - 2;
        if (circleSize > 0) {
            int circleX = x + width / 2 - circleSize / 2;
            int circleY = y + height / 2 - circleSize / 2;
            
            g2d.setColor(tokenColor.darker());
            g2d.fillOval(circleX, circleY, circleSize, circleSize);
            
            g2d.setColor(Color.WHITE);
            strokeW = (float)(1.0 / panZoomController.getScale());
            g2d.setStroke(new BasicStroke(strokeW));
            g2d.drawOval(circleX, circleY, circleSize, circleSize);
        }
        
        // Draw creature initial
        if (width >= 1 && height >= 1) {
            AffineTransform t = g2d.getTransform();
            g2d.translate(x + width / 2.0, y + height / 2.0);
            g2d.scale(1.0 / panZoomController.getScale(), 1.0 / panZoomController.getScale());
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, (int)(Math.min(width, height) * panZoomController.getScale() * 0.4)));
            String initial = creature.job.name().substring(0, Math.min(1, creature.job.name().length()));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(initial, -fm.stringWidth(initial) / 2, fm.getAscent() / 2);
            
            g2d.setTransform(t);
        }
    }
    
    private Color getCreatureColor(Creature creature) {
        // Color based on job
        switch (creature.job) {
            case FIGHTER:
                return new Color(139, 69, 19);   // Brown
            case WIZARD:
                return new Color(75, 0, 130);    // Indigo
            case ROGUE:
                return new Color(34, 139, 34);   // Forest Green
            case CLERIC:
                return new Color(255, 215, 0);   // Gold
            case PALADIN:
                return new Color(220, 20, 60);    // Crimson
            case RANGER:
                return new Color(0, 100, 0);      // Dark Green
            case BARBARIAN:
                return new Color(178, 34, 34);   // Firebrick
            case MONK:
                return new Color(128, 128, 0);    // Olive
            case BARD:
                return new Color(148, 0, 211);   // Dark Violet
            case DRUID:
                return new Color(0, 128, 0);      // Green
            case WARLOCK:
                return new Color(75, 0, 130);    // Dark Violet
            case SORCERER:
                return new Color(255, 0, 0);      // Red
            case COMMONER:
            default:
                return new Color(128, 128, 128);  // Gray
        }
    }
    
    private void drawGrid(Graphics2D g2d, Shape shape) {
        Shape oldClip = g2d.getClip();
        g2d.setClip(shape);
        
        g2d.setColor(new Color(220, 220, 220));
        float strokeW = (float)(0.5/panZoomController.getScale());
        g2d.setStroke(new BasicStroke(strokeW));
        
        Rectangle2D bounds = shape.getBounds2D();
        for (double x = Math.floor(bounds.getMinX()); x <= bounds.getMaxX(); x += 1.0) {
            g2d.draw(new Line2D.Double(x, bounds.getMinY(), x, bounds.getMaxY()));
        }
        for (double y = Math.floor(bounds.getMinY()); y <= bounds.getMaxY(); y += 1.0) {
            g2d.draw(new Line2D.Double(bounds.getMinX(), y, bounds.getMaxX(), y));
        }
        
        g2d.setClip(oldClip);
    }
    
    
    private void drawDirectedGraphEdges(Graphics2D g2d) {
        for (final Edge<Room, Path> edge : dungeon.getAllEdges()) {
            final Room from = edge.from;
            final Room to = edge.to;
            final Point fromCenter = roomPositions.get(from);
            final Point toCenter = roomPositions.get(to);
            
            // Calculate direction angle from source to destination
            double dx = toCenter.x - fromCenter.x;
            double dy = toCenter.y - fromCenter.y;
            double angle = Math.atan2(dy, dx);
            
            // Get source and destination room rectangles
            Rectangle fromRect = getRoomRect(from, fromCenter);
            Rectangle toRect = getRoomRect(to, toCenter);
            
            // Get perimeter intersection points
            Point2D.Double fromEdge = getRoomPerimeterPoint(fromRect, angle);
            Point2D.Double toEdge = getRoomPerimeterPoint(toRect, angle + Math.PI);
            
            // Draw line
            g2d.setColor(Color.DARK_GRAY);
            float strokeW = (float)(2.0 / panZoomController.getScale());
            g2d.setStroke(new BasicStroke(strokeW));
            g2d.draw(new Line2D.Double(fromEdge.x, fromEdge.y, toEdge.x, toEdge.y));
            
            // Draw distance label at midpoint
            double midX = (fromEdge.x + toEdge.x) / 2.0;
            double midY = (fromEdge.y + toEdge.y) / 2.0;
            drawPathDistanceLabel(g2d, edge.path.distance, midX, midY);
        }
    }
    
    private void drawPathDistanceLabel(Graphics2D g2d, PathDistance distance, double midX, double midY) {
        AffineTransform t = g2d.getTransform();
        g2d.translate(midX, midY);
        g2d.scale(1/panZoomController.getScale(), 1/panZoomController.getScale());
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
        String label = distance.name();
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(label, -fm.stringWidth(label)/2, fm.getAscent()/2);
        g2d.setTransform(t);
    }
    
    private Point2D.Double getRoomPerimeterPoint(Rectangle rect, double angle) {
        final double centerX = rect.getCenterX();
        final double centerY = rect.getCenterY();
        final double halfWidth = rect.width / 2.0;
        final double halfHeight = rect.height / 2.0;
        
        // Normalize angle to [0, 2π)
        while (angle < 0) angle += 2 * Math.PI;
        while (angle >= 2 * Math.PI) angle -= 2 * Math.PI;
        
        // Find intersection with rectangle edge using parametric ray equation
        // Find minimum t where ray intersects rectangle boundary
        double t = Double.POSITIVE_INFINITY;
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);
        
        // Check right edge (x = centerX + halfWidth)
        if (cosAngle > 0) {
            t = Math.min(t, halfWidth / cosAngle);
        } else if (cosAngle < 0) {
            t = Math.min(t, -halfWidth / cosAngle);
        }
        
        // Check top/bottom edges (y = centerY ± halfHeight)
        if (sinAngle > 0) {
            t = Math.min(t, halfHeight / sinAngle);
        } else if (sinAngle < 0) {
            t = Math.min(t, -halfHeight / sinAngle);
        }
        
        return new Point2D.Double(
            centerX + cosAngle * t,
            centerY + sinAngle * t
        );
    }
    
    private double calculateAngleToRoom(Point from, Point to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        return Math.atan2(dy, dx);
    }
    
    private boolean isOnVerticalWall(double angle) {
        // Normalize angle to [0, π) since [π, 2π) is just opposite direction
        while (angle < 0) angle += 2 * Math.PI;
        angle = angle % Math.PI;
        
        // Vertical walls: angles near 0 (right edge) or π (left edge)
        // Horizontal walls: angles near π/2 (bottom) or 3π/2 (top)
        // So angles near 0 or π indicate vertical walls
        return angle < Math.PI / 4 || angle > 3 * Math.PI / 4;
    }
    
    private void drawPathDetailsOnRoomEdges(Graphics2D g2d) {
        for (final Room room : dungeon.getAllVertex()) {
            final Point center = roomPositions.get(room);
            final Rectangle rect = getRoomRect(room, center);
            final Map<Room, Path> children = dungeon.getChildrenOf(room);
            
            if (children.isEmpty()) continue;
            
            // Create list of edges from this room
            final List<Edge<Room, Path>> outgoingEdges = new ArrayList<>();
            for (final Map.Entry<Room, Path> entry : children.entrySet()) {
                outgoingEdges.add(new Edge<>(room, entry.getKey(), entry.getValue()));
            }
            
            // Sort edges by angle for consistent, distributed placement
            outgoingEdges.sort((e1, e2) -> {
                double angle1 = calculateAngleToRoom(center, roomPositions.get(e1.to));
                double angle2 = calculateAngleToRoom(center, roomPositions.get(e2.to));
                return Double.compare(angle1, angle2);
            });
            
            // Draw doors at source room edge
            for (final Edge<Room, Path> edge : outgoingEdges) {
                final Room dest = edge.to;
                final Point destCenter = roomPositions.get(dest);
                double angle = calculateAngleToRoom(center, destCenter);
                
                // Get door position on room perimeter
                Point2D.Double doorPos = getRoomPerimeterPoint(rect, angle);
                
                // Snap to nearest tile (0.5-unit alignment)
                doorPos.x = Math.round(doorPos.x * 2) / 2.0;
                doorPos.y = Math.round(doorPos.y * 2) / 2.0;
                
                // Determine which wall we're on
                boolean isVerticalWall = isOnVerticalWall(angle);
                
                // Draw door on room edge
                drawDoorOnRoomEdge(g2d, doorPos, isVerticalWall, edge.path.lockDc, edge.path.stealthDc);
            }
        }
    }
    
    private void drawDoorOnRoomEdge(Graphics2D g2d, Point2D.Double position, boolean isVerticalWall,
            Optional<Integer> lockDc, Optional<Integer> stealthDc) {
        final AffineTransform t = g2d.getTransform();
        g2d.translate(position.x, position.y);
        
        // Draw door symbol PARALLEL to the wall
        g2d.setColor(new Color(101, 67, 33)); // Brown
        g2d.setStroke(new BasicStroke((float)(3.0/panZoomController.getScale())));
        
        if (isVerticalWall) {
            // Vertical wall → draw vertical line (parallel to wall)
            g2d.draw(new Line2D.Double(0, -0.5, 0, 0.5));
        } else {
            // Horizontal wall → draw horizontal line (parallel to wall)
            g2d.draw(new Line2D.Double(-0.5, 0, 0.5, 0));
        }
        
        // Draw box in center
        double boxSize = 0.3;
        Rectangle2D.Double box = new Rectangle2D.Double(-boxSize/2, -boxSize/2, boxSize, boxSize);
        boolean isSecret = stealthDc.isPresent();
        g2d.setColor(isSecret ? Color.MAGENTA : Color.ORANGE);
        g2d.fill(box);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke((float)(1.0/panZoomController.getScale())));
        g2d.draw(box);
        
        // Draw question mark if secret
        if (isSecret) {
            g2d.setColor(Color.BLACK);
            Font font = new Font("Monospaced", Font.BOLD, (int)(0.25 * panZoomController.getScale())); 
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics();
            
            AffineTransform t2 = g2d.getTransform();
            g2d.scale(1/panZoomController.getScale(), 1/panZoomController.getScale());
            g2d.drawString("?", (float)(0.5 * panZoomController.getScale() - fm.stringWidth("?")/2), (float)(0.5 * panZoomController.getScale() + fm.getAscent()/3));
            g2d.setTransform(t2);
        }
        
        // Draw DC labels
        g2d.setColor(Color.RED);
        AffineTransform t3 = g2d.getTransform();
        g2d.scale(1/panZoomController.getScale(), 1/panZoomController.getScale());
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        
        if (stealthDc.isPresent()) {
            String label1 = "S " + stealthDc.get();
            float textY = (float)(-boxSize*panZoomController.getScale()/2 - 2);
            g2d.drawString(label1, (float)(-fm.stringWidth(label1)/2), textY);
            
            if (lockDc.isPresent()) {
                String label2 = "DC " + lockDc.get();
                g2d.drawString(label2, (float)(-fm.stringWidth(label2)/2), textY - fm.getHeight());
            }
        } else if (lockDc.isPresent()) {
            String label = "DC " + lockDc.get();
            float textY = (float)(-boxSize*panZoomController.getScale()/2 - 2);
            g2d.drawString(label, (float)(-fm.stringWidth(label)/2), textY);
        }
        
        g2d.setTransform(t3);
        g2d.setTransform(t);
    }
    
    public interface RoomSelectionListener {
        void onRoomSelected(Room room);
    }
}
