package game.builder;

import game.common.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static game.common.RoomFeature.*;
import static game.common.RoomSize.*;
import static game.util.Func.*;

public class RoomBuilder {
    private final Random random;
    
    public RoomBuilder() {
        this.random = new Random();
    }
    
    public RoomBuilder(final long seed) {
        this.random = new Random(seed);
    }
    
    public RoomSize expectedRoomSize(final Rarity rarity) {
        switch(rarity) {
            case UNCOMMON -> {
                return CRAMPED;
            }
            case RARE -> {
                return VAST;
            }
            default -> {
                return ROOMY;
            }
        }
    }

    public List<RoomFeature> expectedRoomFeature(final Rarity rarity, final RoomType roomType, final RoomSize roomSize) {
        final List<RoomFeature> features = new ArrayList<>();
        
        int maxArea = roomSize.numSquares / 2;
        int currentArea = 0;
        
        RoomFeature[] availableFeatures = getFeaturesForRoomType(roomType);
        
        int numFeatures = switch (rarity) {
            case RARE -> 3;
            case UNCOMMON -> 2;
            default -> 1;
        };
        
        for (int i = 0; i < numFeatures && currentArea < maxArea; i++) {
            RoomFeature feature = chooseFromRandomly(availableFeatures);
            int featureArea = feature.getWidth() * feature.getHeight();
            
            if (currentArea + featureArea <= maxArea) {
                features.add(feature);
                currentArea += featureArea;
            }
        }

        return features;
    }
    
    private RoomFeature[] getFeaturesForRoomType(final RoomType roomType) {
        return switch (roomType) {
            case KITCHEN -> new RoomFeature[]{STOVE, SMALL_TABLE, BIG_TABLE, STORAGE_CABINET, PILE_OF_RUBBLE};
            case LIBRARY -> new RoomFeature[]{BOOK_SHELF, SMALL_SHELF, DESK, STATUE, LARGE_STATUE};
            case THRONE_ROOM -> new RoomFeature[]{THRONE, STATUE, LARGE_STATUE, HUGE_STATUE, ALTAR};
            case MESS_HALL -> new RoomFeature[]{SMALL_TABLE, BIG_TABLE, TWIN_BED, DOUBLE_BED, BEDROLL, STOVE};
            case STORAGE_ROOM -> new RoomFeature[]{STORAGE_CABINET, SMALL_SHELF, CHEST, PILE_OF_RUBBLE};
            case SLEEPING_QUARTERS -> new RoomFeature[]{TWIN_BED, DOUBLE_BED, BEDROLL, CHEST, DESK};
            case ARMORY -> new RoomFeature[]{SMALL_TABLE, BIG_TABLE, CHEST, PILE_OF_RUBBLE};
            case GARDEN -> new RoomFeature[]{FOUNTAIN, STATUE, LARGE_STATUE, PILE_OF_RUBBLE};
            case CHAPEL -> new RoomFeature[]{ALTAR, STATUE, BRAZIER, COFFIN};
            case WORKSHOP -> new RoomFeature[]{WORK_BENCH, ANVIL, FORGE, STORAGE_CABINET};
            case LABORATORY -> new RoomFeature[]{DESK, SPELL_ALTAR, BOOK_SHELF};
            case VAULT -> new RoomFeature[]{CHEST, PILE_OF_TREASURE, STATUE, TELEPORTATION_CIRCLE, TRAP_DOOR};
            case CRYPT -> new RoomFeature[]{TOMB, COFFIN, STATUE, PILE_OF_RUBBLE, TRAP_DOOR};
            case TORTURE_CHANBER -> new RoomFeature[]{SMALL_TABLE, STATUE, PILE_OF_RUBBLE, CHEST};
            case RITUAL_ROOM -> new RoomFeature[]{SPELL_ALTAR, TELEPORTATION_CIRCLE, BRAZIER, STATUE, PILE_OF_TREASURE};
            case TOWER, STAIRWELL, COURTYARD, DUNGEON -> new RoomFeature[]{SMALL_TABLE, STATUE, PILE_OF_RUBBLE, CHEST, TRAP_DOOR};
        };
    }

    public Room build() {
        final RoomSize roomSize = expectedRoomSize(rollRarity());
        final RoomType roomType = chooseFromRandomly(RoomType.values());

        final Room room = new Room(roomType, roomSize);
        room.features.addAll(expectedRoomFeature(rollRarity(), roomType, roomSize));
        
        // Generate and cache feature positions
        generateFeaturePositions(room);
        
        return room;
    }
    
    public void generateFeaturePositions(final Room room) {
        if (room.features.isEmpty()) return;
        
        int roomTiles = (int) Math.sqrt(room.size.numSquares);
        
        // Track occupied tiles
        boolean[][] occupied = new boolean[roomTiles][roomTiles];
        
        // Place features
        for (final RoomFeature feature : room.features) {
            int fx = -1, fy = -1;
            
            // Try to place along walls first
            for (int attempt = 0; attempt < 100; attempt++) {
                int candidateX = random.nextInt(roomTiles - feature.getWidth() + 1);
                int candidateY = random.nextInt(roomTiles - feature.getHeight() + 1);
                
                if (canPlaceFeature(occupied, candidateX, candidateY, feature.getWidth(), feature.getHeight(), roomTiles)) {
                    fx = candidateX;
                    fy = candidateY;
                    break;
                }
            }
            
            // Fallback: force placement
            if (fx == -1 || fy == -1) {
                fx = Math.max(0, roomTiles - feature.getWidth());
                fy = Math.max(0, roomTiles - feature.getHeight());
            }
            
            // Store tile coordinates (will be converted to screen coordinates during rendering)
            room.featurePositions.put(feature, new int[]{fx, fy});
            
            // Mark as occupied
            markOccupied(occupied, fx, fy, feature.getWidth(), feature.getHeight(), roomTiles);
        }
    }
    
    private boolean canPlaceFeature(final boolean[][] occupied, final int rx, final int ry, final int w, final int h, final int roomTiles) {
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
    
    private void markOccupied(final boolean[][] occupied, final int rx, final int ry, final int w, final int h, final int roomTiles) {
        for (int dx = 0; dx < w; dx++) {
            for (int dy = 0; dy < h; dy++) {
                if (rx + dx < roomTiles && ry + dy < roomTiles) {
                    occupied[rx + dx][ry + dy] = true;
                }
            }
        }
    }
}
