package game.view;

import game.common.Path;
import game.common.Room;
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
    
    // Constants
    private static final double MIN_ZOOM = 50.0;
    private static final double MAX_ZOOM = 500.0; 
    
    private final List<PathShape> pathShapes = new ArrayList<>();

    public DungeonPanel() {
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

    public void setDungeon(Graph<Room, Path> dungeon) {
        this.dungeon = dungeon;
        initializeLayout();
        centerView();
        repaint();
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
                
                // Target distance in grid units (5ft per unit)
                double targetDist = edge.path.distance / 5.0;
                
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
        
        int targetLen = edge.path.distance / 5;
        int pathWidth = (edge.path.distance >= 30) ? 2 : 1;
        
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
        // Find shortest Manhattan path first
        Point start = new Point((int)r1.getCenterX(), (int)r1.getCenterY());
        Point end = new Point((int)r2.getCenterX(), (int)r2.getCenterY());
        
        List<Point> path = new ArrayList<>();
        Point current = new Point(start);
        
        // Basic L-shape
        while (current.x != end.x) {
            path.add(new Point(current));
            if (current.x < end.x) current.x++; else current.x--;
        }
        while (current.y != end.y) {
             path.add(new Point(current));
             if (current.y < end.y) current.y++; else current.y--;
        }
        path.add(end);
        
        // Calculate current length (excluding inside rooms)
        // Actually, we need to count steps. 
        // Let's refine the list to be just the segments between rooms.
        List<Point> validTiles = new ArrayList<>();
        for (Point p : path) {
             // A tile is valid if it's NOT inside the source/dest room
             // (Use slightly smaller rect to allow touching edges)
             if (!r1.contains(p) && !r2.contains(p)) {
                 validTiles.add(p);
             }
        }
        
        // If we are too short, we need to add "wiggles" or detours
        // For now, if we are short, just extend the path visually? 
        // Or accepts that layout might not be perfect.
        // Given the complexity of "exact distance" pathfinding on a dynamic graph, 
        // making the path exactly X tiles long is hard without very smart routing.
        // For this iteration, we accept the shortest path provided by layout.
        // Future improvement: Add 'wiggle' logic.
        
        return validTiles;
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
        
        // Draw Details
        drawPathDetails(g2d);
        
        g2d.setTransform(saveXform);
    }
    
    private void drawPath(Graphics2D g2d, PathShape ps) {
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fill(ps.area);
        
        // Outline
        g2d.setColor(Color.BLACK);
        float strokeW = (float)(1.0/scale);
        if (ps.isLower) {
             g2d.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{0.2f, 0.2f}, 0.0f));
        } else {
             g2d.setStroke(new BasicStroke(strokeW));
        }
        g2d.draw(ps.area);
        
        // Grid
        drawGrid(g2d, ps.area);
    }
    
    private void drawRoom(Graphics2D g2d, Room room, Point center) {
        Rectangle r = getRoomRect(room, center);
        
        g2d.setColor(Color.WHITE);
        g2d.fillRect(r.x, r.y, r.width, r.height);
        
        g2d.setColor(Color.BLACK);
        float strokeW = (float)(2.0/scale);
        g2d.setStroke(new BasicStroke(strokeW));
        g2d.drawRect(r.x, r.y, r.width, r.height);
        
        drawGrid(g2d, r);
        
        // Title
        AffineTransform t = g2d.getTransform();
        g2d.translate(center.x, center.y); // Center of room, which is integer
        g2d.scale(1/scale, 1/scale);
        
        g2d.setColor(Color.BLUE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(room.title, -fm.stringWidth(room.title)/2, fm.getHeight()/2);
        
        g2d.setTransform(t);
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
         for (PathShape ps : pathShapes) {
             if (ps.tiles.isEmpty()) continue;
             
             // Draw distance label (at midpoint)
             if (!ps.tiles.isEmpty()) {
                 Point mid = ps.tiles.get(ps.tiles.size()/2);
                 AffineTransform t = g2d.getTransform();
                 g2d.translate(mid.x + 0.5, mid.y + 0.5);
                 g2d.scale(1/scale, 1/scale);
                 g2d.setColor(Color.BLACK);
                 g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
                 String label = ps.edge.path.distance + " ft";
                 g2d.drawString(label, 0, 0);
                 g2d.setTransform(t);
             }

             // Draw Doors (only at start)
             if (ps.tiles.isEmpty()) continue;
             Point doorTile = ps.tiles.get(0);
             boolean vertical = false;
             
             // Determine direction for door line
             if (ps.tiles.size() > 1) {
                 Point next = ps.tiles.get(1);
                 vertical = (next.x == doorTile.x); // If X is same, we are moving vertical
             }
             
             // Draw Doors
             // Handle both secret and locked
             if (ps.edge.path.stealthDc.isPresent() && ps.edge.path.lockDc.isPresent()) {
                 // Secret AND Locked: Draw as Secret (Magenta) but with both texts
                 drawDoorSymbol(g2d, doorTile, vertical, true, ps.edge.path.stealthDc.get(), ps.edge.path.lockDc);
             } else if (ps.edge.path.stealthDc.isPresent()) {
                 // Secret only
                 drawDoorSymbol(g2d, doorTile, vertical, true, ps.edge.path.stealthDc.get(), Optional.empty());
             } else if (ps.edge.path.lockDc.isPresent()) {
                 // Locked only
                 drawDoorSymbol(g2d, doorTile, vertical, false, ps.edge.path.lockDc.get(), Optional.empty());
             }
         }
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
         g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
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
}
