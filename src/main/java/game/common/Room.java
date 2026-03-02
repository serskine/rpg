package game.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Room {
    public String title;
    public RoomType type;
    public RoomSize size;
    public List<Creature> creatures = new ArrayList<>();
    public List<RoomFeature> features = new ArrayList<>();
    
    // Cached feature positions: feature -> [pixelX, pixelY]
    public Map<RoomFeature, int[]> featurePositions = new HashMap<>();
    
    // Cached room layout parameters
    public int cachedRoomTiles;
    public int cachedTileSize;
    public int cachedOffsetX;
    public int cachedStartY;

    public Room() {
        this(RoomType.STORAGE_ROOM, RoomSize.ROOMY);
    }

    public Room(final RoomType type, final RoomSize size) {
        this(size.name() + " " + type.name(), type, size);
    }

    public Room(final String title, final RoomType type, final RoomSize size) {
        this.title = title;
        this.type = type;
        this.size = size;
    }
}
