package game.view;

import game.common.Creature;
import game.common.Party;
import game.common.Path;
import game.common.Room;
import game.common.RoomFeature;
import game.common.World;
import game.util.Graph;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

public class WorldTreePanel extends JPanel {

    private final JTree tree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;
    private final CreaturePreviewPanel creaturePreviewPanel;
    private final FeaturePreviewPanel featurePreviewPanel;
    private JComponent currentPreview;
    private Graph<Room, Path> dungeonGraph;
    private Map<RoomFeature, Room> featureToRoomMap;

    public WorldTreePanel() {
        setLayout(new BorderLayout());

        rootNode = new DefaultMutableTreeNode("World");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);

        JScrollPane scrollPane = new JScrollPane(tree);
        tree.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        creaturePreviewPanel = new CreaturePreviewPanel();
        featurePreviewPanel = new FeaturePreviewPanel();
        
        currentPreview = creaturePreviewPanel;
        JScrollPane previewScroll = new JScrollPane(currentPreview);
        previewScroll.setPreferredSize(new Dimension(420, 0));
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, previewScroll);
        splitPane.setResizeWeight(0.4);
        add(splitPane, BorderLayout.CENTER);

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = tree.getRowForLocation(e.getX(), e.getY());
                if (row >= 0) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getPathForRow(row).getLastPathComponent();
                    Object userObject = node.getUserObject();
                    
                    if (userObject instanceof RoomFeature) {
                        showFeaturePreview((RoomFeature) userObject);
                    } else if (userObject instanceof Creature) {
                        showCreaturePreview((Creature) userObject);
                    } else if (node.getUserObject() instanceof String && ((String) node.getUserObject()).startsWith("Job:")) {
                        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
                        if (parent != null && parent.getUserObject() instanceof Creature) {
                            showCreaturePreview((Creature) parent.getUserObject());
                        }
                    } else if (node.getUserObject() instanceof String && ((String) node.getUserObject()).startsWith("Feature:")) {
                        // Find feature from parent
                        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
                        if (parent != null && parent.getUserObject() instanceof RoomFeature) {
                            showFeaturePreview((RoomFeature) parent.getUserObject());
                        }
                    } else if (userObject instanceof String) {
                        // Check if this is a room node (contains features or connections)
                        Room room = findRoomFromNode(node);
                        if (room != null) {
                            showRoomPreview(room);
                        } else {
                            showCreaturePreview(null);
                            featurePreviewPanel.setFeature(null, null, null);
                        }
                    } else {
                        showCreaturePreview(null);
                        featurePreviewPanel.setFeature(null, null, null);
                    }
                }
            }
        });
    }

    private void showCreaturePreview(Creature creature) {
        if (currentPreview != creaturePreviewPanel) {
            JScrollPane parent = (JScrollPane) ((JSplitPane) getComponent(0)).getRightComponent();
            parent.setViewportView(creaturePreviewPanel);
            currentPreview = creaturePreviewPanel;
        }
        creaturePreviewPanel.setCreature(creature);
    }

    private void showFeaturePreview(RoomFeature feature) {
        if (currentPreview != featurePreviewPanel) {
            JScrollPane parent = (JScrollPane) ((JSplitPane) getComponent(0)).getRightComponent();
            parent.setViewportView(featurePreviewPanel);
            currentPreview = featurePreviewPanel;
        }
        
        Room containingRoom = findRoomForFeature(feature);
        Map<Room, Path> doors = (containingRoom != null && dungeonGraph != null) 
            ? dungeonGraph.getChildrenOf(containingRoom) 
            : null;
        featurePreviewPanel.setFeature(feature, containingRoom, doors);
    }
    
    private void showRoomPreview(Room room) {
        if (currentPreview != featurePreviewPanel) {
            JScrollPane parent = (JScrollPane) ((JSplitPane) getComponent(0)).getRightComponent();
            parent.setViewportView(featurePreviewPanel);
            currentPreview = featurePreviewPanel;
        }
        
        Map<Room, Path> doors = (room != null && dungeonGraph != null) 
            ? dungeonGraph.getChildrenOf(room) 
            : null;
        featurePreviewPanel.setRoom(room, doors);
    }
    
    public void showRoomSelected(Room room) {
        showRoomPreview(room);
    }
    
    private Room findRoomFromNode(DefaultMutableTreeNode node) {
        // Walk up the tree to find the room node
        DefaultMutableTreeNode current = node;
        while (current != null) {
            Object obj = current.getUserObject();
            if (obj instanceof String) {
                String title = (String) obj;
                // Check if this is a room node (find it in the dungeon graph)
                if (dungeonGraph != null) {
                    for (Room r : dungeonGraph.getAllVertex()) {
                        if (r.title.equals(title)) {
                            return r;
                        }
                    }
                }
            }
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) current.getParent();
            // Stop at dungeon node
            if (parent != null && parent.getUserObject().equals("Dungeon")) {
                break;
            }
            current = parent;
        }
        return null;
    }
    
    private Room findRoomForFeature(RoomFeature feature) {
        return featureToRoomMap.get(feature);
    }

    public void setWorld(World world) {
        rootNode.removeAllChildren();
        featureToRoomMap = new java.util.HashMap<>();

        if (world != null) {
            dungeonGraph = world.dungeon;
            
            // Build Dungeon Node
            DefaultMutableTreeNode dungeonNode = new DefaultMutableTreeNode("Dungeon");
            rootNode.add(dungeonNode);
            buildDungeonNodes(dungeonNode, world.dungeon);

            // Build Parties Node
            DefaultMutableTreeNode partiesNode = new DefaultMutableTreeNode("Parties");
            rootNode.add(partiesNode);
            buildPartiesNodes(partiesNode, world.parties);
        }

        treeModel.reload();
        
        showCreaturePreview(null);
    }

    private void buildDungeonNodes(DefaultMutableTreeNode parent, Graph<Room, Path> dungeon) {
        for (Room room : dungeon.getAllVertex()) {
            DefaultMutableTreeNode roomNode = new DefaultMutableTreeNode(room.title);
            parent.add(roomNode);

            roomNode.add(new DefaultMutableTreeNode("Type: " + room.type));
            roomNode.add(new DefaultMutableTreeNode("Size: " + room.size));

            // Add features
            if (!room.features.isEmpty()) {
                DefaultMutableTreeNode featuresNode = new DefaultMutableTreeNode("Features");
                roomNode.add(featuresNode);
                for (RoomFeature feature : room.features) {
                    DefaultMutableTreeNode featureNode = new DefaultMutableTreeNode(feature);
                    featuresNode.add(featureNode);
                    featureToRoomMap.put(feature, room);
                }
            }

            Map<Room, Path> children = dungeon.getChildrenOf(room);
            if (!children.isEmpty()) {
                DefaultMutableTreeNode connectionsNode = new DefaultMutableTreeNode("Connections");
                roomNode.add(connectionsNode);
                for (Map.Entry<Room, Path> entry : children.entrySet()) {
                    Room target = entry.getKey();
                    Path path = entry.getValue();
                    connectionsNode.add(new DefaultMutableTreeNode("To: " + target.title + " (" + path.distance.name() + ") - " + path.title));
                }
            }
        }
    }

    private void buildPartiesNodes(DefaultMutableTreeNode parent, List<Party> parties) {
        for (Party party : parties) {
            DefaultMutableTreeNode partyNode = new DefaultMutableTreeNode(party.title);
            parent.add(partyNode);

            for (Creature creature : party.creatures) {
                DefaultMutableTreeNode creatureNode = new DefaultMutableTreeNode(creature);
                partyNode.add(creatureNode);

                creatureNode.add(new DefaultMutableTreeNode("Job: " + creature.job));
                creatureNode.add(new DefaultMutableTreeNode("Level: " + creature.level));
                creatureNode.add(new DefaultMutableTreeNode("HP: " + creature.hp() + " / " + creature.maxHp()));
                creatureNode.add(new DefaultMutableTreeNode("Alignment: " + creature.alignment));
                creatureNode.add(new DefaultMutableTreeNode("Size: " + creature.size));
                
                // Add stats
                DefaultMutableTreeNode statsNode = new DefaultMutableTreeNode("Stats");
                creatureNode.add(statsNode);
                statsNode.add(new DefaultMutableTreeNode("STR: " + creature.strength()));
                statsNode.add(new DefaultMutableTreeNode("DEX: " + creature.dexterity()));
                statsNode.add(new DefaultMutableTreeNode("CON: " + creature.constitution()));
                statsNode.add(new DefaultMutableTreeNode("INT: " + creature.intelligence()));
                statsNode.add(new DefaultMutableTreeNode("WIS: " + creature.wisdom()));
                statsNode.add(new DefaultMutableTreeNode("CHA: " + creature.charisma()));
            }
        }
    }
}
