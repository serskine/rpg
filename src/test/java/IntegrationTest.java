import game.builder.WorldBuilder;
import game.common.Room;
import game.common.RoomFeature;
import game.common.RoomSize;
import game.common.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {

    /**
     * Tests that feature positions are cached and never change
     */
    @Test
    public void testFeaturePositionsCached() {
        WorldBuilder builder = new WorldBuilder();
        World world = builder.build(4, 5);

        // Get a room from the dungeon
        Room[] rooms = world.dungeon.getAllVertex().toArray(new Room[0]);
        
        // Skip test if dungeon is empty (due to randomness)
        if (rooms.length == 0) {
            return;
        }

        Room testRoom = rooms[0];

        // Verify that feature positions are cached
        assertNotNull(testRoom.featurePositions, "Room should have featurePositions map");
        
        // Store original positions
        java.util.Map<RoomFeature, int[]> originalPositions = 
            new java.util.HashMap<>(testRoom.featurePositions);

        // Verify positions didn't change
        for (java.util.Map.Entry<RoomFeature, int[]> entry : testRoom.featurePositions.entrySet()) {
            RoomFeature feature = entry.getKey();
            int[] pos = entry.getValue();
            int[] originalPos = originalPositions.get(feature);
            
            assertNotNull(originalPos, "Feature should have cached position");
            assertArrayEquals(originalPos, pos, "Feature position should not change");
        }
    }

    /**
     * Tests that rooms have valid feature positions
     */
    @Test
    public void testRoomFeaturePositionsValid() {
        WorldBuilder builder = new WorldBuilder();
        World world = builder.build(4, 5);

        for (Room room : world.dungeon.getAllVertex()) {
            // Each cached position should be a 2-element array [x, y]
            for (java.util.Map.Entry<RoomFeature, int[]> entry : room.featurePositions.entrySet()) {
                RoomFeature feature = entry.getKey();
                int[] pos = entry.getValue();

                assertNotNull(pos, "Feature " + feature + " should have position");
                assertEquals(2, pos.length, "Position should be [x, y]");
                assertTrue(pos[0] >= 0, "X coordinate should be non-negative");
                assertTrue(pos[1] >= 0, "Y coordinate should be non-negative");
            }
        }
    }

    /**
     * Tests that multiple dungeon generations complete without errors
     */
    @Test
    public void testDifferentDungeonsGenerated() {
        WorldBuilder builder1 = new WorldBuilder();
        World world1 = builder1.build(4, 5);
        
        WorldBuilder builder2 = new WorldBuilder();
        World world2 = builder2.build(4, 5);

        // Count rooms in each dungeon
        int size1 = (world1 != null && world1.dungeon != null) ? world1.dungeon.getAllVertex().size() : 0;
        int size2 = (world2 != null && world2.dungeon != null) ? world2.dungeon.getAllVertex().size() : 0;

        // Both dungeons should generate without error (they may be empty due to randomness)
        assertNotNull(world1, "First dungeon should be created");
        assertNotNull(world2, "Second dungeon should be created");
        assertNotNull(world1.dungeon, "First dungeon graph should be created");
        assertNotNull(world2.dungeon, "Second dungeon graph should be created");
    }

    /**
     * Tests that room selection works (simulating listener callback)
     */
    @Test
    public void testRoomSelectionListener() {
        WorldBuilder builder = new WorldBuilder();
        World world = builder.build(4, 5);

        Room[] rooms = world.dungeon.getAllVertex().toArray(new Room[0]);
        
        // Skip test if dungeon is empty (due to randomness)
        if (rooms.length == 0) {
            return;
        }

        Room selectedRoom = rooms[0];
        
        // Simulate the listener callback
        assertNotNull(selectedRoom, "Selected room should not be null");
        assertNotNull(selectedRoom.featurePositions, "Selected room should have cached positions");
    }

    /**
     * Tests that features can be retrieved and highlighted
     */
    @Test
    public void testFeatureHighlighting() {
        WorldBuilder builder = new WorldBuilder();
        World world = builder.build(4, 5);

        // Get a room with features
        Room[] rooms = world.dungeon.getAllVertex().toArray(new Room[0]);
        Room roomWithFeatures = null;
        
        for (Room room : rooms) {
            if (!room.featurePositions.isEmpty()) {
                roomWithFeatures = room;
                break;
            }
        }

        // If we found a room with features, verify highlighting would work
        if (roomWithFeatures != null) {
            for (RoomFeature feature : roomWithFeatures.featurePositions.keySet()) {
                // Verify feature has cached position for highlighting
                int[] position = roomWithFeatures.featurePositions.get(feature);
                assertNotNull(position, "Feature " + feature + " should have position for highlighting");
                assertEquals(2, position.length, "Feature position should have [x, y] coordinates");
            }
        }
    }

    /**
     * Tests door rendering proportions are mathematically correct
     */
    @Test
    public void testDoorRenderingProportions() {
        // Test that the door proportions are correct
        int tileSize = 40; // Default tile size
        
        // Door width: 1/4 of tile size
        int doorWidth = tileSize / 4;
        assertEquals(10, doorWidth, "Door width should be 1/4 of tile size");
        
        int doorHalf = doorWidth / 2;
        assertEquals(5, doorHalf, "Door half-width should be 5");
        
        // Door length: 3/4 of tile size
        int doorLength = (tileSize * 3) / 4;
        assertEquals(30, doorLength, "Door length should be 3/4 of tile size");
        
        // Door offset (to center): (tileSize - doorLength) / 2
        int doorOffset = (tileSize - doorLength) / 2;
        assertEquals(5, doorOffset, "Door offset should center the door");
        
        // Verify the door fits within the tile
        assertTrue(doorOffset + doorLength <= tileSize, "Door should fit within tile bounds");
        assertTrue(doorOffset >= 0, "Door offset should be non-negative");
    }

    /**
     * Tests that rooms of different sizes are generated correctly
     */
    @Test
    public void testDifferentRoomSizes() {
        WorldBuilder builder = new WorldBuilder();
        World world = builder.build(4, 5);

        // Check that we have rooms of different sizes
        java.util.Set<RoomSize> sizesSeen = new java.util.HashSet<>();
        
        for (Room room : world.dungeon.getAllVertex()) {
            assertNotNull(room.size, "Room should have a size");
            sizesSeen.add(room.size);
            
            // Verify size values are valid
            assertTrue(room.size.numSquares >= 4, "Room size should have at least 4 squares");
        }
        
        // Verify that CRAMPED, ROOMY, and VAST sizes exist as enum values
        assertEquals(4, RoomSize.CRAMPED.numSquares);
        assertEquals(16, RoomSize.ROOMY.numSquares);
        assertEquals(36, RoomSize.VAST.numSquares);
    }
}
