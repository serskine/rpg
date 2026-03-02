package game.builder;

import game.common.Creature;
import game.common.Party;
import game.common.Room;
import game.common.World;
import game.util.Graph;

import java.util.List;
import java.util.Random;

public class WorldBuilder {
    final DungeonBuilder dungeonBuilder = new DungeonBuilder();
    final EncounterBuilder encounterBuilder = new EncounterBuilder();

    public final World build(final int partySize, final int avgPartyLevel) {
        final World world = new World();

        world.dungeon = dungeonBuilder.build();
        world.parties = encounterBuilder.build(partySize, avgPartyLevel);

        assignPartiesToRooms(world.dungeon, world.parties);

        return world;
    }

    private void assignPartiesToRooms(Graph<Room, ?> dungeon, List<Party> parties) {
        if (parties.isEmpty()) return;

        List<Room> rooms = new java.util.ArrayList<>(dungeon.getAllVertex());
        if (rooms.isEmpty()) return;

        Random rand = new Random(42);
        
        // Assign each party to a random room
        for (Party party : parties) {
            if (!party.creatures.isEmpty()) {
                Room targetRoom = rooms.get(rand.nextInt(rooms.size()));
                targetRoom.creatures.addAll(party.creatures);
            }
        }
    }
}
