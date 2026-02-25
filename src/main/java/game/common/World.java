package game.common;

import game.util.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class World {
    public Graph<Room, Path> dungeon = new Graph<>();
    public List<Party> parties = new ArrayList<>();

    public World() {

    }
}
