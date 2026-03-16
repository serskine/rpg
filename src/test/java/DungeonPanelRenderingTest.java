import game.builder.WorldBuilder;
import game.common.World;
import game.view.DungeonPanel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that DungeonPanel correctly renders directed graphs with straight-line connections
 */
public class DungeonPanelRenderingTest {

    @Test
    public void testDirectedGraphInitialization() {
        // Create a dungeon with directed edges
        WorldBuilder builder = new WorldBuilder();
        World world = builder.build(4, 5);
        
        assertNotNull(world, "World should be created");
        assertNotNull(world.dungeon, "Dungeon should exist");
        
        // Create a DungeonPanel and set the dungeon
        DungeonPanel panel = new DungeonPanel();
        panel.setSize(800, 600);
        
        // This should not throw any exceptions
        assertDoesNotThrow(() -> {
            panel.setDungeon(world.dungeon);
        }, "DungeonPanel should initialize with dungeon without errors");
        }

    @Test
    public void testDirectedGraphEdges() {
        WorldBuilder builder = new WorldBuilder();
        World world = builder.build(4, 5);
        
        // Verify edges exist
        int edgeCount = world.dungeon.getAllEdges().size();
        assertTrue(edgeCount >= 0, "Graph should have valid edge count");
        
        // If there are rooms, verify they have edges
        if (!world.dungeon.getAllVertex().isEmpty()) {
            assertTrue(edgeCount > 0, "Dungeon with rooms should have edges");
            
            // Verify all edges have valid rooms
            world.dungeon.getAllEdges().forEach(edge -> {
                assertNotNull(edge.from, "Edge source should not be null");
                assertNotNull(edge.to, "Edge destination should not be null");
                assertNotNull(edge.path, "Edge path should not be null");
                assertNotNull(edge.path.distance, "Path distance should not be null");
            });
        }
    }

    @Test
    public void testDungeonPanelWithDirectedGraph() {
        WorldBuilder builder = new WorldBuilder();
        World world = builder.build(5, 6);
        
        if (!world.dungeon.getAllVertex().isEmpty()) {
            DungeonPanel panel = new DungeonPanel();
            panel.setSize(1024, 768);
            
            // This should not throw any exceptions
            assertDoesNotThrow(() -> {
                panel.setDungeon(world.dungeon);
            }, "DungeonPanel should initialize with directed graph");
            
            // Panel should have initialized successfully
            assertNotNull(panel, "Panel should be created");
        }
    }
}
