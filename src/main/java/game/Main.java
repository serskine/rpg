package game;

import game.builder.DungeonBuilder;
import game.builder.WorldBuilder;
import game.common.Party;
import game.common.Path;
import game.common.Room;
import game.common.World;
import game.util.Edge;
import game.util.Graph;
import game.util.Logger;

import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {

        if (args.length != 2) {
            Logger.warn("Expected 2 arguments: <party size>, <avg party level>");
        }

        final int partySize = Integer.parseInt(args[0]);
        final int partyLevel = Integer.parseInt(args[1]);

        Logger.info("Generating world for party size: " + partySize + ", party level: " + partyLevel);

        final WorldBuilder worldBuilder = new WorldBuilder();

        final World world = worldBuilder.build(partySize, partyLevel);

        Logger.info(describeWorld(world));

    }

    private static String indent(final String s) {
        final String pad = " ";
        return pad + s.replaceAll("\n", "\n" + pad);
    }

    private static String property(final String name, final String... items) {

        final String prefix = (name==null) ? "" : name + ": ";
        final StringBuilder sb = new StringBuilder();
        for(int i=0; i<items.length; i++) {
            if (i>0) {
                sb.append(",\n");
            }
            sb.append(items[i]);
        }
        return prefix + "{\n" + indent(sb.toString()) + "\n}";
    }

    private static String describeWorld(final World world) {
        final String dungeonDesc = property("dungeon", describeDungeon(world.dungeon));
        final String partiesDesc = property("parties", describeParties(world.parties));
        return property("world", dungeonDesc, partiesDesc);
    }

    private static String describeDungeon(final Graph<Room, Path> dungeon) {
        final Set<Room> rooms = dungeon.getAllVertex();
        final String roomsDesc = property("rooms", rooms.stream().map(r -> r.title).toArray(String[]::new));
        final String edgesDesc = property("edges",
                dungeon.getAllEdges().stream()
                        .map(e -> e.from.title + " -> " + e.to.title).toArray(String[]::new));
        return roomsDesc + ",\n" + edgesDesc;
    }

    private static String describeParties(final List<Party> partyList) {
        final StringBuilder sb = new StringBuilder();
        partyList.forEach(p -> sb.append(describeParty(p)).append("\n"));
        return sb.toString();
    }

    private static String describeParty(final Party party) {
        return property(party.title, party.creatures.stream().map(c -> c.title).toArray(String[]::new));
    }

}