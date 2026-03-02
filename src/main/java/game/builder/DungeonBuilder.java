package game.builder;

import game.common.Path;
import game.common.Rarity;
import game.common.Room;
import game.util.Graph;

import static game.util.Func.*;

public class DungeonBuilder {

    final RoomBuilder roomBuilder = new RoomBuilder();
    final PathBuilder pathBuilder = new PathBuilder();

    private int expectedNumberOfRooms(Rarity dungeonRarity) {
        switch(dungeonRarity) {
            case UNCOMMON -> {
                return 5;        // 1-4 rooms
            }
            case RARE -> {
                return 11;    // 5 - 10 rooms
            }
            default -> {
                return 7;   // 11 - 18 rooms
            }
        }
    }

    private Room[] buildRooms(int numberOfRooms) {
        final Room[] rooms = new Room[numberOfRooms];
        for(int i=0; i<numberOfRooms; i++) {
            rooms[i] = roomBuilder.build();
        }
        return rooms;
    }

    public Graph<Room, Path> build() {
        final int numberOfRooms = expectedNumberOfRooms(rollRarity());
        final Room[] rooms = buildRooms(numberOfRooms);

        final Graph<Room, Path> dungeonGraph = new Graph<>();
        for(int i=0; i<numberOfRooms; i++) {
            for(int j=i+1; j<numberOfRooms; j++) {
                final Rarity chanceOfPath = rollRarity();
                if(chanceOfPath == Rarity.UNCOMMON) {
                    final String title = rooms[i].title + " -> " + rooms[j].title;
                    final Path path = pathBuilder.build(title);
                    dungeonGraph.setPathsBetween(rooms[i], rooms[j], path);
                }
            }
        }
        return dungeonGraph;
    }

}
