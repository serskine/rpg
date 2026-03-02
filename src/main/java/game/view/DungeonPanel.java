package game.view;

import game.common.Creature;
import game.common.CreatureSize;
import game.common.Path;
import game.common.Room;
import game.common.RoomFeature;
import game.util.Edge;
import game.util.Graph;

import javax.swing.*;
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
    
    // Viewport state
    private double scale = 50.0; // Pixels per 5ft unit (tile size)
    private double offsetX = 0;
    private double offsetY = 0;
    
    // Interaction state
    private Point lastMousePoint;
    private Room selectedRoom;
    
    // Constants
    private static final double MIN_ZOOM = 1.0;
    private static final double MAX_ZOOM = 500.0;
    
    private final List<PathShape> pathShapes = new ArrayList<>();
    
    // Callback for room selection
    private RoomSelectionListener roomSelectionListener;

    public DungeonPanel() {
        setBackground(new Color(45, 45, 45));
        
        // Add interaction listeners
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePoint = e.getPoint();
                
                // Check if a room was clicked
                Room clickedRoom = getRoomAtPoint(e.getPoint());
                if (clickedRoom != null) {
                    setSelectedRoom(clickedRoom);
                    e.consume();
                    return;
                }
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
        
        // Convert screen point to world coordinates
        double worldX = (screenPoint.x - offsetX) / scale;
        double worldY = (screenPoint.y - offsetY) / scale;
        
        // Check which room contains this point
        for (Map.Entry<Room, Point> entry : roomPositions.entrySet()) {
            Room room = entry.getKey();
            Point center = entry.getValue();
            Rectangle rect = getRoomRect(room, center);
            
            if (rect.contains((int)worldX, (int)worldY)) {
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
        
        offsetX = getWidth() / 2.0 - centerX * scale;
        offsetY = getHeight() / 2.0 - centerY * scale;
    }

    private void initializeLayout() {
        roomPositions.clear();
        pathShapes.clear();
        if (dungeon == null) return;

        // Step 1: Force Directed Layout (Grid-Aware)
        runGridForceLayout();
        
        // Step 2: Generate Orthogonal Paths
        generateOrthogonalPaths();
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

    private void generateOrthogonalPaths() {
        pathShapes.clear();
        Set<Edge<Room, Path>> processedEdges = new HashSet<>();

        for (Edge<Room, Path> edge : dungeon.getAllEdges()) {
             // Check processed
            boolean alreadyProcessed = false;
            for (Edge<Room, Path> existing : processedEdges) {
                if ((existing.from == edge.from && existing.to == edge.to) || 
                    (existing.from == edge.to && existing.to == edge.from)) {
                    alreadyProcessed = true;
                    break;
                }
            }
            if (alreadyProcessed) continue;
            processedEdges.add(edge);

            generateSinglePath(edge);
        }
        
        // Detect overlaps for Z-ordering (simplified: just mark all intersections)
        // For a true "tunnel" effect we'd need to split paths, but dashed rendering 
        // for the lower path works as a visual approximation.
        for (int i=0; i<pathShapes.size(); i++) {
            for (int j=i+1; j<pathShapes.size(); j++) {
                PathShape ps1 = pathShapes.get(i);
                PathShape ps2 = pathShapes.get(j);
                if (ps1.area.intersects(ps2.area.getBounds2D())) { // Quick check
                    Area a1 = new Area(ps1.area);
                    a1.intersect(ps2.area);
                    if (!a1.isEmpty()) {
                        ps2.isLower = true; // Mark one as lower
                    }
                }
            }
        }
    }
    
    private void generateSinglePath(Edge<Room, Path> edge) {
        Room r1 = edge.from;
        Room r2 = edge.to;
        Point p1 = roomPositions.get(r1);
        Point p2 = roomPositions.get(r2);
        
        // Path length based on enum ordinal + base length
        int targetLen = edge.path.distance.ordinal() + 3;
        int pathWidth = (edge.path.distance.ordinal() >= 2) ? 2 : 1;
        
        // Find connection points on room edges
        Rectangle rect1 = getRoomRect(r1, p1);
        Rectangle rect2 = getRoomRect(r2, p2);
        
        // Simple A* / BFS to find path
        // Since we want specific length, standard A* finds shortest.
        // We will find shortest, then extend if needed.
        
        List<Point> pathTiles = findPath(rect1, rect2, targetLen);
        
        // Build Area from tiles
        Area pathArea = new Area();
        for (Point t : pathTiles) {
            pathArea.add(new Area(new Rectangle(t.x, t.y, 1, 1)));
            if (pathWidth == 2) {
                // Add adjacent tile for width
                // Primitive logic: expand right/down depending on flow?
                // For simplicity, expand towards +X and +Y to simulate thickness
                // (Though a true thickening algorithm would depend on path direction)
                 Area thicker = new Area(pathArea);
                 AffineTransform tx = AffineTransform.getTranslateInstance(1, 0);
                 thicker.add(pathArea.createTransformedArea(tx));
                 tx = AffineTransform.getTranslateInstance(0, 1);
                 thicker.add(pathArea.createTransformedArea(tx));
                 tx = AffineTransform.getTranslateInstance(1, 1);
                 thicker.add(pathArea.createTransformedArea(tx));
                 pathArea = thicker;
            }
        }

        pathShapes.add(new PathShape(pathArea, edge, pathWidth, pathTiles));
    }
    
    private List<Point> findPath(Rectangle r1, Rectangle r2, int targetLength) {
        Point start = new Point((int)r1.getCenterX(), (int)r1.getCenterY());
        Point end = new Point((int)r2.getCenterX(), (int)r2.getCenterY());
        
        // Find shortest Manhattan path (L-shape)
        List<Point> path = new ArrayList<>();
        Point current = new Point(start);
        
        // Basic L-shape: horizontal then vertical
        while (current.x != end.x) {
            path.add(new Point(current));
            if (current.x < end.x) current.x++; else current.x--;
        }
        while (current.y != end.y) {
            path.add(new Point(current));
            if (current.y < end.y) current.y++; else current.y--;
        }
        path.add(end);
        
        // Get tiles outside rooms
        List<Point> validTiles = new ArrayList<>();
        for (Point p : path) {
            if (!r1.contains(p) && !r2.contains(p)) {
                validTiles.add(p);
            }
        }
        
        int shortestLen = validTiles.size();
        
        // If path is shorter than target, add wiggles
        if (shortestLen < targetLength) {
            validTiles = addWiggles(validTiles, start, end, r1, r2, targetLength);
        }
        
        return validTiles;
    }
    
    private List<Point> addWiggles(List<Point> path, Point start, Point end, Rectangle r1, Rectangle r2, int targetLength) {
        // Determine primary direction (horizontal or vertical)
        boolean horizontalFirst = Math.abs(end.x - start.x) >= Math.abs(end.y - start.y);
        
        List<Point> result = new ArrayList<>(path);
        int needed = targetLength - result.size();
        
        // Add zigzags to extend path
        // Each wiggle adds 2 tiles: go perpendicular, then back
        int wiggleCount = needed / 2;
        int remainder = needed % 2;
        
        // Find a good insertion point (middle of path)
        int insertPos = result.size() / 2;
        
        for (int w = 0; w < wiggleCount; w++) {
            // Alternate wiggle direction
            boolean wiggleHorizontal = (w % 2 == 0) != horizontalFirst;
            
            // Find a position to insert wiggle
            if (insertPos > 0 && insertPos < result.size()) {
                Point prev = result.get(insertPos - 1);
                Point curr = result.get(insertPos);
                
                int wx = curr.x;
                int wy = curr.y;
                
                if (wiggleHorizontal) {
                    // Go perpendicular (horizontal wiggle)
                    wy += 1; // Go down
                    // Check if valid (not inside room)
                    Point wigglePoint = new Point(wx, wy);
                    if (!r1.contains(wigglePoint) && !r2.contains(wigglePoint)) {
                        result.add(insertPos, wigglePoint);
                        insertPos++;
                        // Add return point
                        result.add(insertPos, new Point(curr.x, curr.y));
                        insertPos++;
                    }
                } else {
                    // Vertical wiggle
                    wx += 1; // Go right
                    Point wigglePoint = new Point(wx, wy);
                    if (!r1.contains(wigglePoint) && !r2.contains(wigglePoint)) {
                        result.add(insertPos, wigglePoint);
                        insertPos++;
                        result.add(insertPos, new Point(curr.x, curr.y));
                        insertPos++;
                    }
                }
            }
        }
        
        // Handle odd remainder by extending in a valid direction at end
        if (remainder > 0 && result.size() < targetLength) {
            Point last = result.get(result.size() - 1);
            Point extend = new Point(last.x + 1, last.y);
            if (!r1.contains(extend) && !r2.contains(extend)) {
                result.add(extend);
            } else {
                extend = new Point(last.x, last.y + 1);
                if (!r1.contains(extend) && !r2.contains(extend)) {
                    result.add(extend);
                }
            }
        }
        
        return result;
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
        g2d.translate(offsetX, offsetY);
        g2d.scale(scale, scale);
        
        // Draw Paths (Lower)
        for (PathShape ps : pathShapes) {
            if (ps.isLower) drawPath(g2d, ps);
        }
        // Draw Paths (Upper)
        for (PathShape ps : pathShapes) {
            if (!ps.isLower) drawPath(g2d, ps);
        }
        
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
        
        // Draw Details
        drawPathDetails(g2d);
        
        g2d.setTransform(saveXform);
    }
    
    private void drawPath(Graphics2D g2d, PathShape ps) {
        if (ps.tiles.isEmpty()) return;
        
        g2d.setColor(Color.DARK_GRAY);
        float strokeW = (float)(2.0 / scale);
        g2d.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f));
        
        Point prev = ps.tiles.get(0);
        for (int i = 1; i < ps.tiles.size(); i++) {
            Point curr = ps.tiles.get(i);
            double x1 = prev.x + 0.5;
            double y1 = prev.y + 0.5;
            double x2 = curr.x + 0.5;
            double y2 = curr.y + 0.5;
            g2d.draw(new Line2D.Double(x1, y1, x2, y2));
            prev = curr;
        }
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
         g2d.scale(1/scale, 1/scale);
         
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
        float strokeW = (float)(1.5 / scale);
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
            strokeW = (float)(1.0 / scale);
            g2d.setStroke(new BasicStroke(strokeW));
            g2d.drawOval(circleX, circleY, circleSize, circleSize);
        }
        
        // Draw creature initial
        if (width >= 1 && height >= 1) {
            AffineTransform t = g2d.getTransform();
            g2d.translate(x + width / 2.0, y + height / 2.0);
            g2d.scale(1.0 / scale, 1.0 / scale);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, (int)(Math.min(width, height) * scale * 0.4)));
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
        float strokeW = (float)(0.5/scale);
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
    
     private void drawPathDetails(Graphics2D g2d) {
          for (int pathIdx = 0; pathIdx < pathShapes.size(); pathIdx++) {
              PathShape ps = pathShapes.get(pathIdx);
              if (ps.tiles.isEmpty()) continue;
              
              // Draw distance label (at midpoint)
              if (!ps.tiles.isEmpty()) {
                  Point mid = ps.tiles.get(ps.tiles.size()/2);
                  AffineTransform t = g2d.getTransform();
                  g2d.translate(mid.x + 0.5, mid.y + 0.5);
                  g2d.scale(1/scale, 1/scale);
                  g2d.setColor(Color.BLACK);
                  g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
                   String label = ps.edge.path.distance.name();
                  g2d.drawString(label, 0, 0);
                  g2d.setTransform(t);
              }

              // Draw doors/wall breaks at both ends (where path meets room edges)
              if (ps.tiles.isEmpty()) continue;
              
              // Get room rectangles
              Room room1 = ps.edge.from;
              Room room2 = ps.edge.to;
              Point p1 = roomPositions.get(room1);
              Point p2 = roomPositions.get(room2);
              Rectangle rect1 = getRoomRect(room1, p1);
              Rectangle rect2 = getRoomRect(room2, p2);
              
              // Find door positions at room edges
              Point doorTile1 = findDoorTileAtRoom(ps.tiles, rect1);
              Point doorTile2 = findDoorTileAtRoom(ps.tiles, rect2);
              
              // Determine if this path should have doors or wall breaks
              boolean hasDoor = pathIdx % 2 == 0;
              
              // Draw entrance at first room
              if (doorTile1 != null) {
                  boolean vertical = isPathVertical(ps.tiles, doorTile1);
                  if (hasDoor) {
                      drawDoorAtEdge(g2d, doorTile1, vertical, rect1, ps.edge.path.stealthDc, ps.edge.path.lockDc);
                  } else {
                      drawWallBreakAtEdge(g2d, doorTile1, vertical, rect1);
                  }
              }
              
              // Draw entrance at second room
              if (doorTile2 != null) {
                  boolean vertical = isPathVertical(ps.tiles, doorTile2);
                  if (hasDoor) {
                      drawDoorAtEdge(g2d, doorTile2, vertical, rect2, ps.edge.path.stealthDc, ps.edge.path.lockDc);
                  } else {
                      drawWallBreakAtEdge(g2d, doorTile2, vertical, rect2);
                  }
              }
          }
     }
    
    private Point findDoorTileAtRoom(List<Point> tiles, Rectangle roomRect) {
        for (Point tile : tiles) {
            if (roomRect.contains(tile.x, tile.y) || 
                roomRect.contains(tile.x + 0.5, tile.y + 0.5)) {
                return tile;
            }
        }
        return tiles.isEmpty() ? null : tiles.get(0);
    }
    
    private boolean isPathVertical(List<Point> tiles, Point doorTile) {
        int idx = tiles.indexOf(doorTile);
        if (idx < 0) {
            for (int i = 0; i < tiles.size(); i++) {
                if (tiles.get(i).x == doorTile.x && tiles.get(i).y == doorTile.y) {
                    idx = i;
                    break;
                }
            }
        }
        if (idx < 0 || idx >= tiles.size() - 1) {
            idx = tiles.size() - 1;
        }
        Point next = tiles.get(idx + 1);
        return next.x == doorTile.x;
    }
    
    private void drawDoorAtEdge(Graphics2D g2d, Point doorTile, boolean vertical, Rectangle roomRect, 
            Optional<Integer> stealthDc, Optional<Integer> lockDc) {
        // Determine which edge of the room the door is on
        double doorX, doorY;
        
        // Find the edge and position door at the actual connection point
        if (doorTile.x < roomRect.x) {
            // Door on left edge - position at the intersection with the path tile
            doorX = roomRect.x;
            doorY = roomRect.y + ((doorTile.y - Math.floor(doorTile.y)) * roomRect.height);
        } else if (doorTile.x >= roomRect.x + roomRect.width) {
            // Door on right edge
            doorX = roomRect.x + roomRect.width;
            doorY = roomRect.y + ((doorTile.y - Math.floor(doorTile.y)) * roomRect.height);
        } else if (doorTile.y < roomRect.y) {
            // Door on top edge
            doorX = roomRect.x + ((doorTile.x - Math.floor(doorTile.x)) * roomRect.width);
            doorY = roomRect.y;
        } else {
            // Door on bottom edge
            doorX = roomRect.x + ((doorTile.x - Math.floor(doorTile.x)) * roomRect.width);
            doorY = roomRect.y + roomRect.height;
        }
        
        drawDoorSymbolAt(g2d, doorX, doorY, vertical, stealthDc, lockDc);
    }
    
    private void drawDoorSymbolAt(Graphics2D g2d, double doorX, double doorY, boolean vertical, 
            Optional<Integer> stealthDc, Optional<Integer> lockDc) {
         AffineTransform t = g2d.getTransform();
         g2d.translate(doorX, doorY);
         
         // Determine door orientation based on which edge
         // If door is on left/right edge, draw horizontal line; top/bottom, draw vertical
         boolean isHorizontalEdge = (doorX == Math.floor(doorX)) || (doorX == Math.ceil(doorX));
         boolean drawHorizontal = !isHorizontalEdge;
         
         g2d.setColor(new Color(101, 67, 33)); // Brown
         float strokeW = (float)(3.0/scale);
         g2d.setStroke(new BasicStroke(strokeW));
         
         // Draw line across doorway (perpendicular to hallway)
         if (drawHorizontal) {
             // Horizontal line across the opening
             g2d.draw(new Line2D.Double(-0.5, 0, 0.5, 0));
         } else {
             // Vertical line across the opening
             g2d.draw(new Line2D.Double(0, -0.5, 0, 0.5));
         }
         
         // Draw box in center
         double boxSize = 0.3;
         double bx = -boxSize/2;
         double by = -boxSize/2;
         Rectangle2D.Double box = new Rectangle2D.Double(bx, by, boxSize, boxSize);
         
         boolean isSecret = stealthDc.isPresent();
         g2d.setColor(isSecret ? Color.MAGENTA : Color.ORANGE);
         g2d.fill(box);
         g2d.setColor(Color.BLACK);
         g2d.setStroke(new BasicStroke((float)(1.0/scale)));
         g2d.draw(box);
         
         // Draw Text (Question mark)
         if (isSecret) {
             g2d.setColor(Color.BLACK);
             Font font = new Font("Monospaced", Font.BOLD, (int)(0.25 * scale)); 
             g2d.setFont(font);
             FontMetrics fm = g2d.getFontMetrics();
             
             AffineTransform t2 = g2d.getTransform();
             g2d.scale(1/scale, 1/scale);
             g2d.drawString("?", (float)(0.5 * scale - fm.stringWidth("?")/2), (float)(0.5 * scale + fm.getAscent()/3));
             g2d.setTransform(t2);
         }
         
         // Draw DC Text
         g2d.setColor(Color.RED);
         AffineTransform t3 = g2d.getTransform();
         g2d.scale(1/scale, 1/scale);
         g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
         FontMetrics fm = g2d.getFontMetrics();
         
         if (stealthDc.isPresent()) {
             String label1 = "S " + stealthDc.get();
             float textY = (float)(-boxSize*scale/2 - 2);
             g2d.drawString(label1, (float)(-fm.stringWidth(label1)/2), textY);
             
             if (lockDc.isPresent()) {
                 String label2 = "DC " + lockDc.get();
                 g2d.drawString(label2, (float)(-fm.stringWidth(label2)/2), textY - fm.getHeight());
             }
         } else if (lockDc.isPresent()) {
             String label = "DC " + lockDc.get();
             float textY = (float)(-boxSize*scale/2 - 2);
             g2d.drawString(label, (float)(-fm.stringWidth(label)/2), textY);
         }
         
          g2d.setTransform(t3);
          g2d.setTransform(t);
     }
     
     private void drawWallBreakAtEdge(Graphics2D g2d, Point doorTile, boolean vertical, Rectangle roomRect) {
         // Determine which edge of the room the wall break is on
         double breakX, breakY;
         
         // Find the edge and position break at the actual connection point
         if (doorTile.x < roomRect.x) {
             // Break on left edge
             breakX = roomRect.x;
             breakY = roomRect.y + ((doorTile.y - Math.floor(doorTile.y)) * roomRect.height);
         } else if (doorTile.x >= roomRect.x + roomRect.width) {
             // Break on right edge
             breakX = roomRect.x + roomRect.width;
             breakY = roomRect.y + ((doorTile.y - Math.floor(doorTile.y)) * roomRect.height);
         } else if (doorTile.y < roomRect.y) {
             // Break on top edge
             breakX = roomRect.x + ((doorTile.x - Math.floor(doorTile.x)) * roomRect.width);
             breakY = roomRect.y;
         } else {
             // Break on bottom edge
             breakX = roomRect.x + ((doorTile.x - Math.floor(doorTile.x)) * roomRect.width);
             breakY = roomRect.y + roomRect.height;
         }
         
         drawWallBreakSymbolAt(g2d, breakX, breakY, vertical);
     }
     
     private void drawWallBreakSymbolAt(Graphics2D g2d, double breakX, double breakY, boolean vertical) {
         AffineTransform t = g2d.getTransform();
         g2d.translate(breakX, breakY);
         
         // Draw wall line with break segments on either side of opening
         g2d.setColor(new Color(100, 80, 60)); // Wall color
         float strokeW = (float)(3.0/scale);
         g2d.setStroke(new BasicStroke(strokeW));
         
         // Draw partial wall lines with gap in the center (opening)
         double segmentLength = 0.35;
         if (vertical) {
             // Vertical wall with horizontal opening
             g2d.draw(new Line2D.Double(0, -0.5, 0, -segmentLength));
             g2d.draw(new Line2D.Double(0, segmentLength, 0, 0.5));
         } else {
             // Horizontal wall with vertical opening
             g2d.draw(new Line2D.Double(-0.5, 0, -segmentLength, 0));
             g2d.draw(new Line2D.Double(segmentLength, 0, 0.5, 0));
         }
         
         g2d.setTransform(t);
     }
     
     private void drawDoorSymbol(Graphics2D g2d, Point tile, boolean vertical, boolean isSecret, int mainVal, Optional<Integer> secondaryVal) {
         AffineTransform t = g2d.getTransform();
         g2d.translate(tile.x, tile.y);
         
         // 1 unit = 1.0 here (viewport scale handled by outer transform)
         
         g2d.setColor(new Color(101, 67, 33)); // Brown
         float strokeW = (float)(3.0/scale);
         g2d.setStroke(new BasicStroke(strokeW));
         
         // Draw line across hallway
         if (vertical) {
             // Hallway is vertical (North-South), line is Horizontal (East-West)
             // Tile is (0,0) to (1,1) in local coords
             g2d.draw(new Line2D.Double(0, 0.5, 1, 0.5));
         } else {
             // Hallway is horizontal, line is Vertical
             g2d.draw(new Line2D.Double(0.5, 0, 0.5, 1));
         }
         
         // Draw Box in center
         double boxSize = 0.3;
         double bx = 0.5 - boxSize/2;
         double by = 0.5 - boxSize/2;
         Rectangle2D.Double box = new Rectangle2D.Double(bx, by, boxSize, boxSize);
         
         g2d.setColor(isSecret ? Color.MAGENTA : Color.ORANGE);
         g2d.fill(box);
         g2d.setColor(Color.BLACK);
         g2d.setStroke(new BasicStroke((float)(1.0/scale)));
         g2d.draw(box);
         
         // Draw Text (Question mark or nothing)
         if (isSecret) {
             g2d.setColor(Color.BLACK);
             // We need to scale text to fit in box (0.3 units)
             // 0.3 units * scale = pixels
             // Font size ~ pixels
             Font font = new Font("Monospaced", Font.BOLD, (int)(0.25 * scale)); 
             g2d.setFont(font);
             // Center '?'
             FontMetrics fm = g2d.getFontMetrics();
             String text = "?";
             
             // Since we are in 1.0 = 1 unit space, we can't use fm directly for positioning relative to unit
             // Actually, we are scaled. 
             // Let's reset scale for text drawing to be cleaner
             AffineTransform t2 = g2d.getTransform();
             g2d.scale(1/scale, 1/scale);
             g2d.drawString("?", (float)(0.5 * scale - fm.stringWidth("?")/2), (float)(0.5 * scale + fm.getAscent()/3));
             g2d.setTransform(t2);
         }
         
         // Draw DC Text above box
         // "Above" depends on orientation? Or just physically above (-Y)
         // Let's put it physically above
         g2d.setColor(Color.RED);
         AffineTransform t3 = g2d.getTransform();
         g2d.scale(1/scale, 1/scale);
         g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
         FontMetrics fm = g2d.getFontMetrics();
         
         String label1 = (isSecret ? "S " : "DC ") + mainVal;
         float textY = (float)(0.5*scale - boxSize*scale/2 - 2);
         g2d.drawString(label1, (float)(0.5*scale - fm.stringWidth(label1)/2), textY);
         
         if (secondaryVal.isPresent()) {
             String label2 = "DC " + secondaryVal.get();
             // Draw above the first label
             g2d.drawString(label2, (float)(0.5*scale - fm.stringWidth(label2)/2), textY - fm.getHeight());
         }
         
         g2d.setTransform(t3);
         
         g2d.setTransform(t);
    }

    private static class PathShape {
        Area area;
        Edge<Room, Path> edge;
        int width;
        List<Point> tiles; // Ordered list of tiles
        boolean isLower = false;

        PathShape(Area area, Edge<Room, Path> edge, int width, List<Point> tiles) {
            this.area = area;
            this.edge = edge;
            this.width = width;
            this.tiles = tiles;
        }
    }
    
    public interface RoomSelectionListener {
        void onRoomSelected(Room room);
    }
}
