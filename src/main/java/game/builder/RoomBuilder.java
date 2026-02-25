package game.builder;

import game.common.Rarity;
import game.common.Room;
import game.common.RoomType;
import game.common.RoomSize;

import static game.common.RoomSize.*;
import static game.util.Func.chooseFromRandomly;
import static game.util.Func.rollRarity;

public class RoomBuilder {
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

    public Room build() {
        final RoomSize roomSize = expectedRoomSize(rollRarity());
        final RoomType roomType = chooseFromRandomly(RoomType.values());

        return new Room(roomType, roomSize);
    }
}
