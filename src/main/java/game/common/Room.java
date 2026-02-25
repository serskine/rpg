package game.common;

public class Room {
    public String title;
    public RoomType type;
    public RoomSize size;

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
