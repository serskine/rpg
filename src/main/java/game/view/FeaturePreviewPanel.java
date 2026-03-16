package game.view;

import game.common.Room;
import game.common.RoomFeature;
import game.common.Path;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FeaturePreviewPanel extends JPanel {

    private RoomFeature feature;
    private Room selectedRoom;
    private Map<Room, Path> doors;
    private final Random random = new Random();

    public FeaturePreviewPanel() {
        setBackground(new Color(255, 250, 240));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    public void setFeature(RoomFeature feature, Room room, Map<Room, Path> doors) {
        this.feature = feature;
        this.selectedRoom = room;
        this.doors = doors;
        repaint();
    }
    
    public void setRoom(Room room, Map<Room, Path> doors) {
        this.selectedRoom = room;
        this.doors = doors;
        this.feature = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (selectedRoom == null) {
            drawEmptyState(g);
            return;
        }
        drawRoomWithFeatures(g);
    }

    private void drawEmptyState(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(100, 100, 100));
        g2d.setFont(new Font("Serif", Font.ITALIC, 20));
        FontMetrics fm = g2d.getFontMetrics();
        String msg = "Select a room to view layout";
        g2d.drawString(msg, getWidth() / 2 - fm.stringWidth(msg) / 2, getHeight() / 2);
    }

    private void drawRoomWithFeatures(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = 15;
        int y = 25;
        int width = getWidth() - 40;

        String roomName = (selectedRoom != null) ? selectedRoom.title : "Room";
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Serif", Font.BOLD, 24));
        g2d.drawString(roomName, x, y);
        y += 30;

        if (selectedRoom != null) {
            g2d.setFont(new Font("Serif", Font.ITALIC, 16));
            g2d.drawString(selectedRoom.size + " " + selectedRoom.type, x, y);
            y += 25;
        }

        g2d.setColor(new Color(139, 69, 19));
        g2d.fillRect(x, y, width, 3);
        y += 15;

        g2d.setFont(new Font("Serif", Font.BOLD, 18));
        g2d.setColor(Color.BLACK);
        g2d.drawString("ROOM LAYOUT", x, y);
        y += 30;

        drawRoomLayout(g2d, x, y, width);

        int roomHeight = getRoomHeight();
        y += roomHeight + 20;

        g2d.setColor(new Color(139, 69, 19));
        g2d.drawLine(x, y, x + width, y);
        y += 20;

        g2d.setFont(new Font("Serif", Font.BOLD, 18));
        g2d.setColor(Color.BLACK);
        g2d.drawString("FEATURES", x, y);
        y += 28;

        drawFeatureList(g2d, x, y, width);

        y += 20 + (selectedRoom != null && !selectedRoom.features.isEmpty() ? selectedRoom.features.size() * 25 : 20);
        g2d.setColor(new Color(139, 69, 19));
        g2d.fillRect(x, y, width, 3);
    }

    private int getRoomHeight() {
        if (selectedRoom == null) return 200;
        int roomTiles = (int) Math.sqrt(selectedRoom.size.numSquares);
        return roomTiles * 40 + 50;
    }

    private void drawRoomLayout(Graphics2D g2d, int startX, int startY, int width) {
        if (selectedRoom == null) return;
        
        int roomTiles = (int) Math.sqrt(selectedRoom.size.numSquares);
        int tileSize = Math.min(40, (width - 20) / roomTiles);
        int roomWidth = roomTiles * tileSize;
        int roomHeight = roomTiles * tileSize;
        
        int offsetX = startX + (width - roomWidth) / 2;
        
        g2d.setColor(new Color(210, 180, 160));
        g2d.fillRect(offsetX, startY, roomWidth, roomHeight);
        
        g2d.setColor(new Color(180, 150, 130));
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i <= roomTiles; i++) {
            g2d.drawLine(offsetX + i * tileSize, startY, offsetX + i * tileSize, startY + roomHeight);
            g2d.drawLine(offsetX, startY + i * tileSize, offsetX + roomWidth, startY + i * tileSize);
        }
        
        // Draw room border
        g2d.setColor(new Color(100, 80, 60));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(offsetX, startY, roomWidth, roomHeight);
        
        // Draw features using cached positions
        if (!selectedRoom.features.isEmpty()) {
            List<RoomFeature> features = selectedRoom.features;
            
            for (int i = 0; i < features.size(); i++) {
                RoomFeature f = features.get(i);
                int[] tilePos = selectedRoom.featurePositions.get(f);
                
                if (tilePos != null) {
                    // Convert tile coordinates to screen coordinates
                    int tileFx = tilePos[0];
                    int tileFy = tilePos[1];
                    int screenFx = offsetX + tileFx * tileSize;
                    int screenFy = startY + tileFy * tileSize;
                    
                    boolean isSelected = f == feature;
                    if (isSelected) {
                        g2d.setColor(new Color(255, 215, 0, 100));
                        g2d.fillRect(screenFx - 2, screenFy - 2, f.getWidth() * tileSize + 4, f.getHeight() * tileSize + 4);
                    }
                    
                    drawFeatureInRoom(g2d, f, screenFx, screenFy, tileSize);
                }
            }
        } else {
            g2d.setColor(new Color(150, 150, 150));
            g2d.setFont(new Font("Serif", Font.ITALIC, 14));
            String msg = "Empty room";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(msg, offsetX + roomWidth / 2 - fm.stringWidth(msg) / 2, startY + roomHeight / 2);
        }
        
        // Draw doors on walls AFTER features so they render on top
        drawDoors(g2d, roomTiles, tileSize, offsetX, startY);
    }
    
    private void drawDoors(Graphics2D g2d, int roomTiles, int tileSize, int offsetX, int startY) {
        if (doors == null || doors.isEmpty()) {
            // No paths in this room
            return;
        }
        
        // Get distribution of doors to walls based on path count
        int doorCount = doors.size();
        int[] wall分配 = distributeDoorsToWalls(doorCount, roomTiles);
        
        int doorIndex = 0;
        int pathIndex = 0;
        
        // Top wall
        for (int i = 0; i < wall分配[0] && doorIndex < doorCount && pathIndex < doorCount; i++) {
            int pos = 1 + i * (roomTiles - 2) / Math.max(1, wall分配[0]);
            boolean hasDoor = pathIndex % 2 == 0; // Alternate between door and break
            if (hasDoor) {
                drawDoorTile(g2d, pos, 0, tileSize, offsetX, startY, "N");
            } else {
                drawWallBreak(g2d, pos, 0, tileSize, offsetX, startY, "N");
            }
            doorIndex++;
            pathIndex++;
        }
        
        // Right wall
        for (int i = 0; i < wall分配[1] && doorIndex < doorCount && pathIndex < doorCount; i++) {
            int pos = 1 + i * (roomTiles - 2) / Math.max(1, wall分配[1]);
            boolean hasDoor = pathIndex % 2 == 0;
            if (hasDoor) {
                drawDoorTile(g2d, roomTiles - 1, pos, tileSize, offsetX, startY, "E");
            } else {
                drawWallBreak(g2d, roomTiles - 1, pos, tileSize, offsetX, startY, "E");
            }
            doorIndex++;
            pathIndex++;
        }
        
        // Bottom wall
        for (int i = 0; i < wall分配[2] && doorIndex < doorCount && pathIndex < doorCount; i++) {
            int pos = 1 + i * (roomTiles - 2) / Math.max(1, wall分配[2]);
            boolean hasDoor = pathIndex % 2 == 0;
            if (hasDoor) {
                drawDoorTile(g2d, pos, roomTiles - 1, tileSize, offsetX, startY, "S");
            } else {
                drawWallBreak(g2d, pos, roomTiles - 1, tileSize, offsetX, startY, "S");
            }
            doorIndex++;
            pathIndex++;
        }
        
        // Left wall
        for (int i = 0; i < wall分配[3] && doorIndex < doorCount && pathIndex < doorCount; i++) {
            int pos = 1 + i * (roomTiles - 2) / Math.max(1, wall分配[3]);
            boolean hasDoor = pathIndex % 2 == 0;
            if (hasDoor) {
                drawDoorTile(g2d, 0, pos, tileSize, offsetX, startY, "W");
            } else {
                drawWallBreak(g2d, 0, pos, tileSize, offsetX, startY, "W");
            }
            doorIndex++;
            pathIndex++;
        }
    }
    
    private int[] distributeDoorsToWalls(int doorCount, int roomTiles) {
        // Distribute doors evenly around the four walls
        int[] walls = {0, 0, 0, 0};
        
        // Simple distribution: put doors on different walls
        for (int i = 0; i < doorCount; i++) {
            walls[i % 4]++;
        }
        
        return walls;
    }
    
    private void drawDoorTile(Graphics2D g2d, int tileX, int tileY, int tileSize, int offsetX, int startY, String wall) {
        int x = offsetX + tileX * tileSize;
        int y = startY + tileY * tileSize;
        
        // Door width: 1/4 of tile size (1/8 on each side of wall)
        int doorWidth = tileSize / 4;
        int doorHalf = doorWidth / 2;
        
        // Door length: 3/4 of tile size, centered on the wall
        int doorLength = (tileSize * 3) / 4;
        int doorOffset = (tileSize - doorLength) / 2;
        
        // Draw door rectangle split by the wall
        g2d.setColor(new Color(101, 67, 33));
        g2d.setStroke(new BasicStroke(2));
        
        switch (wall) {
            case "N":
                // Top wall: door extends above and below the wall, follows the wall horizontally
                int doorXCenter = x + doorOffset;
                int doorYStart = y - doorHalf;
                g2d.fillRect(doorXCenter, doorYStart, doorLength, doorWidth);
                g2d.setColor(new Color(60, 40, 20));
                g2d.drawRect(doorXCenter, doorYStart, doorLength, doorWidth);
                // Draw wall line through middle
                g2d.drawLine(doorXCenter, y, doorXCenter + doorLength, y);
                break;
            case "S":
                // Bottom wall: door extends above and below the wall, follows the wall horizontally
                doorXCenter = x + doorOffset;
                doorYStart = y - doorHalf;
                g2d.fillRect(doorXCenter, doorYStart, doorLength, doorWidth);
                g2d.setColor(new Color(60, 40, 20));
                g2d.drawRect(doorXCenter, doorYStart, doorLength, doorWidth);
                // Draw wall line through middle
                g2d.drawLine(doorXCenter, y + tileSize, doorXCenter + doorLength, y + tileSize);
                break;
            case "E":
                // Right wall: door extends left and right of the wall, follows the wall vertically
                int doorXStart = x - doorHalf;
                int doorYCenter = y + doorOffset;
                g2d.fillRect(doorXStart, doorYCenter, doorWidth, doorLength);
                g2d.setColor(new Color(60, 40, 20));
                g2d.drawRect(doorXStart, doorYCenter, doorWidth, doorLength);
                // Draw wall line through middle
                g2d.drawLine(x + tileSize, doorYCenter, x + tileSize, doorYCenter + doorLength);
                break;
            case "W":
                // Left wall: door extends left and right of the wall, follows the wall vertically
                doorXStart = x - doorHalf;
                doorYCenter = y + doorOffset;
                g2d.fillRect(doorXStart, doorYCenter, doorWidth, doorLength);
                g2d.setColor(new Color(60, 40, 20));
                g2d.drawRect(doorXStart, doorYCenter, doorWidth, doorLength);
                // Draw wall line through middle
                g2d.drawLine(x, doorYCenter, x, doorYCenter + doorLength);
                break;
        }
    }
    
    private void drawWallBreak(Graphics2D g2d, int tileX, int tileY, int tileSize, int offsetX, int startY, String wall) {
        int x = offsetX + tileX * tileSize;
        int y = startY + tileY * tileSize;
        
        // Draw wall break with a visual break/opening
        g2d.setColor(new Color(210, 180, 160)); // Room color
        g2d.fillRect(x + 2, y + 2, tileSize - 4, tileSize - 4);
        
        // Draw opening indicator
        g2d.setColor(new Color(100, 80, 60));
        g2d.setStroke(new BasicStroke(2));
        
        // Draw lines showing an opening
        switch (wall) {
            case "N":
            case "S":
                // Horizontal break
                g2d.drawLine(x + 4, y + tileSize / 2, x + tileSize / 3, y + tileSize / 2);
                g2d.drawLine(x + 2 * tileSize / 3, y + tileSize / 2, x + tileSize - 4, y + tileSize / 2);
                break;
            case "E":
            case "W":
                // Vertical break
                g2d.drawLine(x + tileSize / 2, y + 4, x + tileSize / 2, y + tileSize / 3);
                g2d.drawLine(x + tileSize / 2, y + 2 * tileSize / 3, x + tileSize / 2, y + tileSize - 4);
                break;
        }
    }

    private int[] generateFeaturePositions(List<RoomFeature> features, int roomTiles, int tileSize, int offsetX, int startY) {
        int[] positions = new int[features.size() * 2];
        boolean[][] occupied = new boolean[roomTiles][roomTiles];
        
        // Mark door positions as occupied
        markDoorTilesAsOccupied(occupied, roomTiles);
        
        // Define wall positions (avoid corners - assume 1 tile buffer for doors)
        // 0 = top wall, 1 = right wall, 2 = bottom wall, 3 = left wall
        int[][] wallCandidates = generateWallPositions(roomTiles);
        
        for (int i = 0; i < features.size(); i++) {
            RoomFeature f = features.get(i);
            int fx = -1, fy = -1;
            
            // Shuffle wall candidates for variety
            List<int[]> shuffledWalls = new java.util.ArrayList<>();
            for (int[] w : wallCandidates) {
                shuffledWalls.add(w);
            }
            java.util.Collections.shuffle(shuffledWalls, random);
            
            // Try to place along walls first
            for (int[] wallPos : shuffledWalls) {
                int wallType = wallPos[0]; // 0=top, 1=right, 2=bottom, 3=left
                int start = wallPos[1];
                int end = wallPos[2];
                
                // Try each valid position along this wall segment
                int range = end - f.getWidth() + 1;
                for (int pos = start; pos < start + range; pos++) {
                    int rx, ry;
                    
                    switch (wallType) {
                        case 0: // top wall
                            rx = pos;
                            ry = 0;
                            break;
                        case 1: // right wall
                            rx = roomTiles - f.getWidth();
                            ry = pos;
                            break;
                        case 2: // bottom wall
                            rx = pos;
                            ry = roomTiles - f.getHeight();
                            break;
                        case 3: // left wall
                            rx = 0;
                            ry = pos;
                            break;
                        default:
                            rx = pos;
                            ry = 0;
                    }
                    
                    if (canPlaceFeature(occupied, rx, ry, f.getWidth(), f.getHeight(), roomTiles)) {
                        fx = rx;
                        fy = ry;
                        break;
                    }
                }
                
                if (fx != -1) break;
            }
            
            // If can't place on walls, try anywhere in room
            if (fx == -1) {
                for (int attempt = 0; attempt < 100 && fx == -1; attempt++) {
                    int rx = random.nextInt(roomTiles - f.getWidth() + 1);
                    int ry = random.nextInt(roomTiles - f.getHeight() + 1);
                    
                    if (canPlaceFeature(occupied, rx, ry, f.getWidth(), f.getHeight(), roomTiles)) {
                        fx = rx;
                        fy = ry;
                    }
                }
            }
            
            // Fallback: force placement anywhere
            if (fx == -1 || fy == -1) {
                fx = random.nextInt(Math.max(1, roomTiles - f.getWidth() + 1));
                fy = random.nextInt(Math.max(1, roomTiles - f.getHeight() + 1));
            }
            
            // Mark as occupied
            markOccupied(occupied, fx, fy, f.getWidth(), f.getHeight(), roomTiles);
            
            positions[i * 2] = offsetX + fx * tileSize;
            positions[i * 2 + 1] = startY + fy * tileSize;
        }
        
        return positions;
    }
    
    private int[][] generateWallPositions(int roomTiles) {
        // Avoid 1-tile corners (assume doors go in corners)
        // Format: {wallType, start, end} where start/end are tile positions along the perpendicular axis
        return new int[][] {
            {0, 1, roomTiles - 2},  // top wall (skip corners)
            {1, 1, roomTiles - 2},  // right wall (skip corners)
            {2, 1, roomTiles - 2},  // bottom wall (skip corners)
            {3, 1, roomTiles - 2}   // left wall (skip corners)
        };
    }
    
    private boolean canPlaceFeature(boolean[][] occupied, int rx, int ry, int w, int h, int roomTiles) {
        for (int dx = 0; dx < w; dx++) {
            for (int dy = 0; dy < h; dy++) {
                if (rx + dx >= roomTiles || ry + dy >= roomTiles) {
                    return false;
                }
                if (occupied[rx + dx][ry + dy]) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private void markOccupied(boolean[][] occupied, int rx, int ry, int w, int h, int roomTiles) {
        for (int dx = 0; dx < w; dx++) {
            for (int dy = 0; dy < h; dy++) {
                if (rx + dx < roomTiles && ry + dy < roomTiles) {
                    occupied[rx + dx][ry + dy] = true;
                }
            }
        }
    }

    private void drawFeatureInRoom(Graphics2D g2d, RoomFeature f, int x, int y, int tileSize) {
        int width = f.getWidth() * tileSize;
        int height = f.getHeight() * tileSize;
        
        // Try to load and render SVG
        BufferedImage svgImage = SVGFeatureLoader.loadFeatureSVG(f, width, height);
        
        if (svgImage != null) {
            // Draw SVG directly without any background or additional drawing
            g2d.drawImage(svgImage, x, y, null);
        } else {
            // Fallback to procedural drawing if SVG fails to load
            Color color = getFeatureColor(f);
            g2d.setColor(color);
            g2d.fillRect(x + 2, y + 2, width - 4, height - 4);
            
            g2d.setColor(new Color(60, 40, 20));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(x + 2, y + 2, width - 4, height - 4);
            
            g2d.setColor(new Color(60, 40, 20));
            g2d.setStroke(new BasicStroke(1));
            drawFeatureDetails(g2d, f, x + 4, y + 4, width - 8, height - 8);
        }
    }

    private void drawFeatureList(Graphics2D g2d, int x, int startY, int width) {
        if (selectedRoom == null || selectedRoom.features.isEmpty()) {
            g2d.setColor(new Color(150, 150, 150));
            g2d.setFont(new Font("Serif", Font.ITALIC, 14));
            g2d.drawString("No features in this room", x, startY);
            return;
        }
        
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        for (int i = 0; i < selectedRoom.features.size(); i++) {
            RoomFeature f = selectedRoom.features.get(i);
            int y = startY + i * 25;
            
            if (f == feature) {
                g2d.setColor(new Color(255, 230, 150));
                g2d.fillRect(x, y - 14, width, 18);
            }
            
            g2d.setColor(Color.BLACK);
            String label = String.format("%s (%d ft x %d ft)", f.name(), f.getWidth() * 5, f.getHeight() * 5);
            g2d.drawString(label, x + 5, y);
        }
    }

    private void drawFeatureDetails(Graphics2D g2d, RoomFeature feat, int x, int y, int width, int height) {
        String name = feat.name();
        
        if (name.contains("TABLE") || name.contains("DESK") || name.contains("BENCH")) {
            int margin = Math.min(5, width / 4);
            g2d.drawRect(x + margin, y + margin, width - margin * 2, height - margin * 2);
            g2d.drawLine(x + margin + 3, y + margin, x + margin + 3, y + height - margin);
            g2d.drawLine(x + width - margin - 3, y + margin, x + width - margin - 3, y + height - margin);
        } else if (name.contains("BED")) {
            g2d.fillRect(x + 3, y + 3, width - 6, height / 3);
            g2d.drawLine(x, y + height / 2, x + width, y + height / 2);
        } else if (name.contains("SHELF") || name.contains("CABINET")) {
            int shelfHeight = height / 3;
            for (int i = 1; i < 3; i++) {
                g2d.drawLine(x, y + i * shelfHeight, x + width, y + i * shelfHeight);
            }
        } else if (name.contains("THRONE") || name.contains("CHAIR")) {
            g2d.drawLine(x + width / 4, y, x + width / 4, y + height);
            g2d.drawLine(x, y + height / 3, x + width / 4, y + height / 3);
            g2d.drawLine(x + width - width / 4, y + height / 3, x + width, y + height / 3);
        } else if (name.contains("STATUE")) {
            g2d.drawRect(x + width / 4, y + height - height / 3, width / 2, height / 3);
            g2d.drawLine(x + width / 2, y + height / 3, x + width / 2, y + height - height / 3);
        } else if (name.contains("CHEST")) {
            g2d.drawLine(x, y + height / 3, x + width, y + height / 3);
            g2d.fillRect(x + width / 2 - 3, y + height / 3 - 2, 6, 4);
        } else if (name.contains("ALTAR")) {
            g2d.drawRect(x + 3, y + 3, width - 6, height / 2);
            g2d.drawRect(x + width / 4, y + height / 2 + 2, width / 2, height / 2 - 3);
        } else if (name.contains("FOUNTAIN")) {
            g2d.drawOval(x + 3, y + 3, width - 6, height - 6);
            g2d.drawOval(x + 6, y + 6, width - 12, height - 12);
        } else if (name.contains("PIT")) {
            for (int i = 1; i < 4; i++) {
                int offset = (width / 4) * i / 3;
                g2d.drawLine(x + offset, y, x + width - offset, y + height);
                g2d.drawLine(x + width - offset, y, x + offset, y + height);
            }
        } else if (name.contains("FIRE") || name.contains("BRAZIER")) {
            g2d.drawArc(x + width / 4, y + height / 2, width / 2, height / 2, 0, 180);
        } else if (name.contains("TELEPORTATION")) {
            g2d.drawOval(x + 3, y + 3, width - 6, height - 6);
            g2d.drawOval(x + 6, y + 6, width - 12, height - 12);
            g2d.drawOval(x + width / 4, y + height / 4, width / 2, height / 2);
        } else if (name.contains("TRAP") || name.contains("DOOR")) {
            g2d.drawLine(x, y + height / 2, x + width, y + height / 2);
            g2d.drawLine(x + width / 2, y, x + width / 2, y + height);
        } else if (name.contains("COFFIN") || name.contains("TOMB")) {
            g2d.drawLine(x + width / 2, y, x + width / 2, y + height);
        } else if (name.contains("ANVIL")) {
            g2d.drawRect(x + width / 4, y + height / 2, width / 2, height / 2);
            g2d.drawRect(x + width / 3, y, width / 3, height / 2);
        } else if (name.contains("FORGE")) {
            g2d.drawRect(x + 3, y + height / 2, width - 6, height / 2 - 3);
            g2d.drawRect(x + width / 3, y, width / 3, height / 2);
        } else if (name.contains("STOVE")) {
            int burnerSize = Math.min(width, height) / 3;
            g2d.drawRect(x + 3, y + 3, burnerSize, burnerSize);
            g2d.drawRect(x + width - 3 - burnerSize, y + 3, burnerSize, burnerSize);
            g2d.drawRect(x + 3, y + height - 3 - burnerSize, burnerSize, burnerSize);
            g2d.drawRect(x + width - 3 - burnerSize, y + height - 3 - burnerSize, burnerSize, burnerSize);
        } else if (name.contains("WORK_BENCH")) {
            g2d.drawRect(x + 2, y + 2, width - 4, height - 4);
            g2d.drawLine(x + width / 3, y, x + width / 3, y + height);
        } else if (name.contains("RUBBLE") || name.contains("TREASURE")) {
            for (int i = 0; i < 5; i++) {
                int rx = x + 5 + (width - 10) * (i % 3) / 2;
                int ry = y + 5 + (height - 10) * i / 3;
                int rs = 3 + (i * 2) % 5;
                g2d.fillOval(rx, ry, rs, rs);
            }
        } else {
            int margin = Math.min(4, width / 4);
            g2d.drawRect(x + margin, y + margin, width - margin * 2, height - margin * 2);
        }
    }

    private Color getFeatureColor(RoomFeature feat) {
        String name = feat.name();
        if (name.contains("BED") || name.contains("PILLOW")) {
            return new Color(200, 180, 150);
        } else if (name.contains("TABLE") || name.contains("DESK") || name.contains("BENCH")) {
            return new Color(139, 90, 43);
        } else if (name.contains("SHELF") || name.contains("CABINET")) {
            return new Color(160, 110, 60);
        } else if (name.contains("THRONE") || name.contains("CHAIR")) {
            return new Color(139, 69, 19);
        } else if (name.contains("STATUE") || name.contains("TOMB") || name.contains("COFFIN")) {
            return new Color(150, 150, 150);
        } else if (name.contains("CHEST")) {
            return new Color(160, 82, 45);
        } else if (name.contains("ALTAR") || name.contains("PRAYER")) {
            return new Color(180, 180, 170);
        } else if (name.contains("FOUNTAIN")) {
            return new Color(100, 149, 237);
        } else if (name.contains("PIT")) {
            return new Color(50, 50, 50);
        } else if (name.contains("FIRE") || name.contains("BRAZIER") || name.contains("FORGE") || name.contains("STOVE") || name.contains("ANVIL")) {
            return new Color(80, 80, 80);
        } else if (name.contains("TELEPORTATION")) {
            return new Color(147, 112, 219);
        } else if (name.contains("TRAP") || name.contains("DOOR")) {
            return new Color(101, 67, 33);
        } else if (name.contains("RUBBLE")) {
            return new Color(169, 169, 169);
        } else if (name.contains("TREASURE")) {
            return new Color(218, 165, 32);
        } else if (name.contains("BOOK")) {
            return new Color(34, 139, 34);
        } else {
            return new Color(180, 160, 140);
        }
    }
    
    private void markDoorTilesAsOccupied(boolean[][] occupied, int roomTiles) {
        if (doors == null || doors.isEmpty()) {
            // Mark corners as occupied (door positions)
            if (roomTiles > 0) {
                occupied[0][0] = true;
                occupied[roomTiles - 1][0] = true;
                occupied[0][roomTiles - 1] = true;
                occupied[roomTiles - 1][roomTiles - 1] = true;
            }
            return;
        }
        
        int doorCount = doors.size();
        int[] wall分配 = distributeDoorsToWalls(doorCount, roomTiles);
        
        int doorIndex = 0;
        
        // Top wall
        for (int i = 0; i < wall分配[0] && doorIndex < doorCount; i++) {
            int pos = 1 + i * (roomTiles - 2) / Math.max(1, wall分配[0]);
            occupied[pos][0] = true;
            doorIndex++;
        }
        
        // Right wall
        for (int i = 0; i < wall分配[1] && doorIndex < doorCount; i++) {
            int pos = 1 + i * (roomTiles - 2) / Math.max(1, wall分配[1]);
            occupied[roomTiles - 1][pos] = true;
            doorIndex++;
        }
        
        // Bottom wall
        for (int i = 0; i < wall分配[2] && doorIndex < doorCount; i++) {
            int pos = 1 + i * (roomTiles - 2) / Math.max(1, wall分配[2]);
            occupied[pos][roomTiles - 1] = true;
            doorIndex++;
        }
        
        // Left wall
        for (int i = 0; i < wall分配[3] && doorIndex < doorCount; i++) {
            int pos = 1 + i * (roomTiles - 2) / Math.max(1, wall分配[3]);
            occupied[0][pos] = true;
            doorIndex++;
        }
    }
}
