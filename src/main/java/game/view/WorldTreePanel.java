package game.view;

import game.common.Creature;
import game.common.Party;
import game.common.Path;
import game.common.Room;
import game.common.World;
import game.util.Graph;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class WorldTreePanel extends JPanel {

    private final JTree tree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;

    public WorldTreePanel() {
        setLayout(new BorderLayout());

        rootNode = new DefaultMutableTreeNode("World");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);

        JScrollPane scrollPane = new JScrollPane(tree);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setWorld(World world) {
        rootNode.removeAllChildren();

        if (world != null) {
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
        
        // Expand the first level (Dungeon and Parties)
        tree.expandRow(0);
        tree.expandRow(1);
    }

    private void buildDungeonNodes(DefaultMutableTreeNode parent, Graph<Room, Path> dungeon) {
        for (Room room : dungeon.getAllVertex()) {
            DefaultMutableTreeNode roomNode = new DefaultMutableTreeNode(room.title);
            parent.add(roomNode);

            roomNode.add(new DefaultMutableTreeNode("Type: " + room.type));
            roomNode.add(new DefaultMutableTreeNode("Size: " + room.size));

            Map<Room, Path> children = dungeon.getChildrenOf(room);
            if (!children.isEmpty()) {
                DefaultMutableTreeNode connectionsNode = new DefaultMutableTreeNode("Connections");
                roomNode.add(connectionsNode);
                for (Map.Entry<Room, Path> entry : children.entrySet()) {
                    Room target = entry.getKey();
                    Path path = entry.getValue();
                    connectionsNode.add(new DefaultMutableTreeNode("To: " + target.title + " (" + path.distance + " ft) - " + path.title));
                }
            }
        }
    }

    private void buildPartiesNodes(DefaultMutableTreeNode parent, List<Party> parties) {
        for (Party party : parties) {
            DefaultMutableTreeNode partyNode = new DefaultMutableTreeNode(party.title);
            parent.add(partyNode);

            for (Creature creature : party.creatures) {
                DefaultMutableTreeNode creatureNode = new DefaultMutableTreeNode(creature.title);
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
