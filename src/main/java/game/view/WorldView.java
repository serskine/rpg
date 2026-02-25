package game.view;

import game.builder.WorldBuilder;
import game.common.World;

import javax.swing.*;
import java.awt.*;

public class WorldView extends JFrame {

    private final DungeonPanel dungeonPanel;
    private final WorldTreePanel worldTreePanel;
    private final ControlsPanel controlsPanel;
    private final WorldBuilder worldBuilder;
    private final JTabbedPane tabbedPane;

    public WorldView(int defaultPartySize, int defaultPartyLevel) {
        super("Dungeon World Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);

        worldBuilder = new WorldBuilder();

        // Initialize Panels
        dungeonPanel = new DungeonPanel();
        worldTreePanel = new WorldTreePanel();
        controlsPanel = new ControlsPanel();

        // Configure Controls
        controlsPanel.setDefaults(defaultPartySize, defaultPartyLevel);
        controlsPanel.setOnGenerateListener(this::generateWorld);

        // Setup Tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Controls", controlsPanel);
        tabbedPane.addTab("Dungeon Map", dungeonPanel);
        tabbedPane.addTab("World Contents", worldTreePanel);

        add(tabbedPane);
        
        // Initial Generation
        generateWorld(defaultPartySize, defaultPartyLevel);
    }

    private void generateWorld(int partySize, int partyLevel) {
        // Run in background to keep UI responsive
        SwingWorker<World, Void> worker = new SwingWorker<World, Void>() {
            @Override
            protected World doInBackground() {
                return worldBuilder.build(partySize, partyLevel);
            }

            @Override
            protected void done() {
                try {
                    World world = get();
                    dungeonPanel.setDungeon(world.dungeon);
                    worldTreePanel.setWorld(world);
                    
                    // Don't switch tabs automatically on initial load
                    // But maybe switching is good after generation?
                    // User requirement: "Add another tab that will be the first tab opened."
                    // This implies the controls tab is the default view.
                    // However, if the user clicks "Generate", they probably want to see the result.
                    
                    // Let's check if this is the initial load or user action.
                    // For now, I'll remove the automatic tab switching to respect the "first tab opened" requirement strictly for the initial state.
                    // But wait, "when done will update the other tabs used to display it's contents."
                    
                    // If I remove the tab switch, the user will have to manually click the tab.
                    // I'll leave it as is for now, but ensure the initial selection is index 0.
                    
                    // Actually, let's remove the auto-switch on initial load but keep it for button press.
                    // The simplest way is to add a boolean flag to generateWorld.
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(WorldView.this, 
                        "Error generating world: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
