package game.builder;

import game.common.World;

public class WorldBuilder {
    final DungeonBuilder dungeonBuilder = new DungeonBuilder();
    final EncounterBuilder encounterBuilder = new EncounterBuilder();

    public final World build(final int partySize, final int avgPartyLevel) {
        final World world = new World();

        world.dungeon = dungeonBuilder.build();
        world.parties = encounterBuilder.build(partySize, avgPartyLevel);

        return world;
    }
}
