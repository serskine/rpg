package game.view;

import game.common.Room;
import game.common.RoomFeature;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

/**
 * Utility class for rendering room layouts consistently across different UI panels.
 * Handles tile grid rendering, features, and doors to ensure consistent visual representation.
 */
public class RoomRenderer {

    /**
     * Draws a complete room layout with grid, borders, features, and doors.
     *
     * @param g2d the Graphics2D context
     * @param room the room to render
     * @param startX screen X coordinate (top-left of room)
     * @param startY screen Y coordinate (top-left of room)
     * @param width available width in pixels
     * @param roomTiles number of tiles in the room (width and height)
     * @param tileSize size of each tile in pixels
     * @param doors map of adjacent room doors (or empty if none)
     */
    public static void drawRoomLayout(Graphics2D g2d, Room room, int startX, int startY, 
                                     int width, int roomTiles, int tileSize, Map<Room, ?> doors) {
        int roomWidth = roomTiles * tileSize;
        int roomHeight = roomTiles * tileSize;
        int offsetX = startX + (width - roomWidth) / 2;
        
        // Draw room background
        g2d.setColor(new Color(210, 180, 160));
        g2d.fillRect(offsetX, startY, roomWidth, roomHeight);
        
        // Draw grid
        drawGrid(g2d, offsetX, startY, roomTiles, tileSize);
        
        // Draw room border
        g2d.setColor(new Color(100, 80, 60));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(offsetX, startY, roomWidth, roomHeight);
        
        // Draw features first
        if (!room.features.isEmpty()) {
            drawFeatures(g2d, room, offsetX, startY, roomTiles, tileSize);
        } else {
            drawEmptyRoom(g2d, offsetX, startY, roomWidth, roomHeight);
        }
        
        // Draw doors/wall breaks on walls AFTER features so they render on top
        drawDoors(g2d, roomTiles, tileSize, offsetX, startY, doors != null ? doors.size() : 0);
    }

    /**
     * Draws the grid lines within a room.
     */
    private static void drawGrid(Graphics2D g2d, int offsetX, int startY, int roomTiles, int tileSize) {
        g2d.setColor(new Color(180, 150, 130));
        g2d.setStroke(new BasicStroke(1));
        
        for (int i = 0; i <= roomTiles; i++) {
            int x = offsetX + i * tileSize;
            g2d.drawLine(x, startY, x, startY + roomTiles * tileSize);
            
            int y = startY + i * tileSize;
            g2d.drawLine(offsetX, y, offsetX + roomTiles * tileSize, y);
        }
    }

    /**
     * Draws doors and wall breaks around the room perimeter.
     */
    private static void drawDoors(Graphics2D g2d, int roomTiles, int tileSize, int offsetX, int startY, int doorCount) {
        if (doorCount == 0) {
            return;
        }
        
        int[] wallDistribution = distributeDoorsToWalls(doorCount, roomTiles);
        int doorIndex = 0;
        int pathIndex = 0;
        
        // Top wall
        for (int i = 0; i < wallDistribution[0] && doorIndex < doorCount && pathIndex < doorCount; i++) {
            int pos = 1 + i * (roomTiles - 2) / Math.max(1, wallDistribution[0]);
            boolean hasDoor = pathIndex % 2 == 0;
            if (hasDoor) {
                drawDoorTile(g2d, pos, 0, tileSize, offsetX, startY, "N");
            } else {
                drawWallBreak(g2d, pos, 0, tileSize, offsetX, startY, "N");
            }
            doorIndex++;
            pathIndex++;
        }
        
        // Right wall
        for (int i = 0; i < wallDistribution[1] && doorIndex < doorCount && pathIndex < doorCount; i++) {
            int pos = 1 + i * (roomTiles - 2) / Math.max(1, wallDistribution[1]);
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
        for (int i = 0; i < wallDistribution[2] && doorIndex < doorCount && pathIndex < doorCount; i++) {
            int pos = 1 + i * (roomTiles - 2) / Math.max(1, wallDistribution[2]);
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
        for (int i = 0; i < wallDistribution[3] && doorIndex < doorCount && pathIndex < doorCount; i++) {
            int pos = 1 + i * (roomTiles - 2) / Math.max(1, wallDistribution[3]);
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

    /**
     * Draws a door tile on a room wall.
     */
    private static void drawDoorTile(Graphics2D g2d, int tileX, int tileY, int tileSize, 
                                   int offsetX, int startY, String wall) {
        int x = offsetX + tileX * tileSize;
        int y = startY + tileY * tileSize;
        
        int doorWidth = tileSize / 4;
        int doorHalf = doorWidth / 2;
        int doorLength = (tileSize * 3) / 4;
        int doorOffset = (tileSize - doorLength) / 2;
        
        g2d.setColor(new Color(101, 67, 33));
        g2d.setStroke(new BasicStroke(2));
        
        switch (wall) {
            case "N":
                int doorXCenter = x + doorOffset;
                int doorYStart = y - doorHalf;
                g2d.fillRect(doorXCenter, doorYStart, doorLength, doorWidth);
                g2d.setColor(new Color(60, 40, 20));
                g2d.drawRect(doorXCenter, doorYStart, doorLength, doorWidth);
                g2d.drawLine(doorXCenter, y, doorXCenter + doorLength, y);
                break;
            case "S":
                doorXCenter = x + doorOffset;
                doorYStart = y - doorHalf;
                g2d.fillRect(doorXCenter, doorYStart, doorLength, doorWidth);
                g2d.setColor(new Color(60, 40, 20));
                g2d.drawRect(doorXCenter, doorYStart, doorLength, doorWidth);
                g2d.drawLine(doorXCenter, y + tileSize, doorXCenter + doorLength, y + tileSize);
                break;
            case "E":
                int doorXStart = x - doorHalf;
                int doorYCenter = y + doorOffset;
                g2d.fillRect(doorXStart, doorYCenter, doorWidth, doorLength);
                g2d.setColor(new Color(60, 40, 20));
                g2d.drawRect(doorXStart, doorYCenter, doorWidth, doorLength);
                g2d.drawLine(x + tileSize, doorYCenter, x + tileSize, doorYCenter + doorLength);
                break;
            case "W":
                doorXStart = x - doorHalf;
                doorYCenter = y + doorOffset;
                g2d.fillRect(doorXStart, doorYCenter, doorWidth, doorLength);
                g2d.setColor(new Color(60, 40, 20));
                g2d.drawRect(doorXStart, doorYCenter, doorWidth, doorLength);
                g2d.drawLine(x, doorYCenter, x, doorYCenter + doorLength);
                break;
        }
    }

    /**
     * Draws a wall break (opening without a door) on a room wall.
     */
    private static void drawWallBreak(Graphics2D g2d, int tileX, int tileY, int tileSize, 
                                    int offsetX, int startY, String wall) {
        int x = offsetX + tileX * tileSize;
        int y = startY + tileY * tileSize;
        
        g2d.setColor(new Color(210, 180, 160));
        g2d.fillRect(x + 2, y + 2, tileSize - 4, tileSize - 4);
        
        g2d.setColor(new Color(100, 80, 60));
        g2d.setStroke(new BasicStroke(2));
        
        switch (wall) {
            case "N":
            case "S":
                g2d.drawLine(x + 4, y + tileSize / 2, x + tileSize / 3, y + tileSize / 2);
                g2d.drawLine(x + 2 * tileSize / 3, y + tileSize / 2, x + tileSize - 4, y + tileSize / 2);
                break;
            case "E":
            case "W":
                g2d.drawLine(x + tileSize / 2, y + 4, x + tileSize / 2, y + tileSize / 3);
                g2d.drawLine(x + tileSize / 2, y + 2 * tileSize / 3, x + tileSize / 2, y + tileSize - 4);
                break;
        }
    }

    /**
     * Draws room features within the room layout.
     */
    private static void drawFeatures(Graphics2D g2d, Room room, int offsetX, int startY, 
                                    int roomTiles, int tileSize) {
        if (room.features.isEmpty() || room.featurePositions.isEmpty()) {
            return;
        }
        
        for (RoomFeature feature : room.features) {
            int[] tilePos = room.featurePositions.get(feature);
            if (tilePos == null) continue;
            
            int tileFx = tilePos[0];
            int tileFy = tilePos[1];
            
            int screenX = offsetX + tileFx * tileSize;
            int screenY = startY + tileFy * tileSize;
            int featureWidth = feature.getWidth() * tileSize;
            int featureHeight = feature.getHeight() * tileSize;
            
            drawFeatureInRoom(g2d, feature, screenX, screenY, featureWidth, featureHeight);
        }
    }

    /**
     * Draws a single feature within a room using SVG if available, falls back to procedural drawing.
     */
    private static void drawFeatureInRoom(Graphics2D g2d, RoomFeature feature, 
                                        int x, int y, int width, int height) {
        // Try to load and render SVG
        BufferedImage svgImage = SVGFeatureLoader.loadFeatureSVG(feature, width, height);
        
        if (svgImage != null) {
            // Draw SVG directly without any background or additional drawing
            g2d.drawImage(svgImage, x, y, null);
        } else {
            // Fallback to procedural drawing if SVG fails to load
            Color color = getFeatureColor(feature);
            g2d.setColor(color);
            g2d.fillRect(x + 2, y + 2, width - 4, height - 4);
            
            g2d.setColor(new Color(60, 40, 20));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(x + 2, y + 2, width - 4, height - 4);
            
            g2d.setStroke(new BasicStroke(1));
            drawFeatureDetails(g2d, feature, x + 4, y + 4, width - 8, height - 8);
        }
    }

    /**
     * Draws empty room message.
     */
     private static void drawEmptyRoom(Graphics2D g2d, int offsetX, int startY, int roomWidth, int roomHeight) {
         g2d.setColor(new Color(150, 150, 150));
         g2d.setFont(new Font("Serif", Font.ITALIC, 10));
         String msg = "Empty room";
         FontMetrics fm = g2d.getFontMetrics();
         g2d.drawString(msg, offsetX + roomWidth / 2 - fm.stringWidth(msg) / 2, 
                       startY + roomHeight / 2);
     }

    /**
     * Distributes doors evenly around the four walls.
     */
    private static int[] distributeDoorsToWalls(int doorCount, int roomTiles) {
        int[] walls = {0, 0, 0, 0};
        for (int i = 0; i < doorCount; i++) {
            walls[i % 4]++;
        }
        return walls;
    }

    /**
     * Draws feature-specific details (furniture pattern, etc.).
     */
    private static void drawFeatureDetails(Graphics2D g2d, RoomFeature feature, 
                                         int x, int y, int width, int height) {
        String name = feature.name();
        
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

    /**
     * Gets the color for a specific feature type.
     */
    private static Color getFeatureColor(RoomFeature feature) {
        String name = feature.name();
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
        } else if (name.contains("FIRE") || name.contains("BRAZIER") || name.contains("FORGE") || 
                   name.contains("STOVE") || name.contains("ANVIL")) {
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
}
