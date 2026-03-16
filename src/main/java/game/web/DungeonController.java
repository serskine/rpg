package game.web;

import game.builder.WorldBuilder;
import game.common.Creature;
import game.common.Path;
import game.common.Party;
import game.common.Room;
import game.common.RoomFeature;
import game.common.World;
import game.util.Edge;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for the RPG dungeon viewer.
 */
@RestController
@RequestMapping("/api")
public class DungeonController {
    private World world;
    private Map<Room, Integer> roomIndexMap;

    public DungeonController() {
        // Generate world with party size and level from WebServer
        final int partySize = WebServer.getPartySize();
        final int partyLevel = WebServer.getPartyLevel();
        this.world = new WorldBuilder().build(partySize, partyLevel);
        buildRoomIndexMap();
    }

    private void buildRoomIndexMap() {
        roomIndexMap = new HashMap<>();
        int index = 0;
        for (Room room : world.dungeon.getAllVertex()) {
            roomIndexMap.put(room, index++);
        }
    }

    /**
     * Get complete world data including dungeon and parties.
     */
    @GetMapping("/world")
    public ResponseEntity<WorldData> getWorld() {
        WorldData data = new WorldData();
        data.stats = new StatsData();
        data.stats.totalRooms = world.dungeon.getAllVertex().size();
        data.stats.totalPaths = world.dungeon.getAllEdges().size();
        data.stats.partySize = WebServer.getPartySize();
        data.stats.partyLevel = WebServer.getPartyLevel();

        // Build room data with positions
        data.rooms = new ArrayList<>();
        int roomId = 0;
        for (Room room : world.dungeon.getAllVertex()) {
            RoomData roomData = new RoomData();
            roomData.id = roomId;
            roomData.title = room.title;
            roomData.type = room.type.name();
            roomData.size = room.size.name();
            roomData.numSquares = room.size.numSquares;
            roomData.features = new ArrayList<>();
            
            for (RoomFeature feature : room.features) {
                FeatureData featureData = new FeatureData();
                featureData.name = feature.name();
                featureData.width = feature.getWidth();
                featureData.height = feature.getHeight();
                
                int[] position = room.featurePositions.get(feature);
                if (position != null) {
                    featureData.tileX = position[0];
                    featureData.tileY = position[1];
                }
                
                roomData.features.add(featureData);
            }
            
            // Add creatures in room
            roomData.creatures = new ArrayList<>();
            for (Creature creature : room.creatures) {
                CreatureData creatureData = new CreatureData();
                creatureData.name = creature.title;
                creatureData.job = creature.job.name();
                creatureData.level = creature.level;
                creatureData.hp = creature.hp();
                creatureData.maxHp = creature.maxHp();
                creatureData.alignment = creature.alignment.name();
                creatureData.size = creature.size.name();
                creatureData.strength = creature.strength();
                creatureData.dexterity = creature.dexterity();
                creatureData.constitution = creature.constitution();
                creatureData.intelligence = creature.intelligence();
                creatureData.wisdom = creature.wisdom();
                creatureData.charisma = creature.charisma();
                
                roomData.creatures.add(creatureData);
            }
            
            data.rooms.add(roomData);
            roomId++;
        }

        // Build path/connection data
        data.paths = new ArrayList<>();
        for (Edge<Room, Path> edge : world.dungeon.getAllEdges()) {
            PathData pathData = new PathData();
            pathData.from = roomIndexMap.get(edge.from);
            pathData.to = roomIndexMap.get(edge.to);
            pathData.title = edge.path.title;
            pathData.distance = edge.path.distance.name();
            if (edge.path.stealthDc.isPresent()) {
                pathData.stealthDc = edge.path.stealthDc.get();
            }
            if (edge.path.lockDc.isPresent()) {
                pathData.lockDc = edge.path.lockDc.get();
            }
            
            data.paths.add(pathData);
        }

        // Build party data
        data.parties = new ArrayList<>();
        for (Party party : world.parties) {
            PartyData partyData = new PartyData();
            partyData.title = party.title;
            partyData.creatures = new ArrayList<>();
            
            for (Creature creature : party.creatures) {
                CreatureData creatureData = new CreatureData();
                creatureData.name = creature.title;
                creatureData.job = creature.job.name();
                creatureData.level = creature.level;
                creatureData.hp = creature.hp();
                creatureData.maxHp = creature.maxHp();
                creatureData.alignment = creature.alignment.name();
                creatureData.size = creature.size.name();
                creatureData.strength = creature.strength();
                creatureData.dexterity = creature.dexterity();
                creatureData.constitution = creature.constitution();
                creatureData.intelligence = creature.intelligence();
                creatureData.wisdom = creature.wisdom();
                creatureData.charisma = creature.charisma();
                
                partyData.creatures.add(creatureData);
            }
            
            data.parties.add(partyData);
        }

        return ResponseEntity.ok(data);
    }

    /**
     * Generate a new world with given parameters.
     */
    @GetMapping("/world/generate")
    public ResponseEntity<WorldData> generateWorld(
            @RequestParam(defaultValue = "4") int partySize,
            @RequestParam(defaultValue = "5") int partyLevel) {
        WebServer.setPartySize(partySize);
        WebServer.setPartyLevel(partyLevel);
        this.world = new WorldBuilder().build(partySize, partyLevel);
        buildRoomIndexMap();
        return getWorld();
    }

    /**
     * Get all rooms in the world.
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<RoomData>> getRooms() {
        List<RoomData> roomDataList = new ArrayList<>();
        int roomId = 0;

        for (Room room : world.dungeon.getAllVertex()) {
            RoomData data = new RoomData();
            data.id = roomId++;
            data.title = room.title;
            data.type = room.type.name();
            data.size = room.size.name();
            data.numSquares = room.size.numSquares;
            data.features = new ArrayList<>();
            
            for (RoomFeature feature : room.features) {
                FeatureData featureData = new FeatureData();
                featureData.name = feature.name();
                data.features.add(featureData);
            }

            roomDataList.add(data);
        }

        return ResponseEntity.ok(roomDataList);
    }

    /**
     * Get detailed information about a specific room.
     */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<RoomDetailData> getRoomDetail(@PathVariable final int roomId) {
        Room room = null;
        int currentId = 0;
        for (Room r : world.dungeon.getAllVertex()) {
            if (currentId == roomId) {
                room = r;
                break;
            }
            currentId++;
        }

        if (room == null) {
            return ResponseEntity.notFound().build();
        }

        RoomDetailData detail = new RoomDetailData();
        detail.id = roomId;
        detail.title = room.title;
        detail.type = room.type.name();
        detail.size = room.size.name();
        detail.numSquares = room.size.numSquares;
        detail.numTiles = (int) Math.sqrt(room.size.numSquares);

        // Get feature data with positions
        detail.features = new ArrayList<>();
        for (RoomFeature feature : room.features) {
            FeatureData featureData = new FeatureData();
            featureData.name = feature.name();
            featureData.width = feature.getWidth();
            featureData.height = feature.getHeight();

            int[] position = room.featurePositions.get(feature);
            if (position != null) {
                featureData.tileX = position[0];
                featureData.tileY = position[1];
            }

            detail.features.add(featureData);
        }

        // Get connected rooms (doors)
        detail.connections = new ArrayList<>();
        Map<Room, ?> adjacent = world.dungeon.getChildrenOf(room);
        for (Room r : world.dungeon.getAllVertex()) {
            if (adjacent.containsKey(r)) {
                detail.connections.add(roomIndexMap.get(r));
            }
        }

        return ResponseEntity.ok(detail);
    }

    /**
     * Get the total number of rooms.
     */
    @GetMapping("/stats")
    public ResponseEntity<StatsData> getStats() {
        StatsData stats = new StatsData();
        stats.totalRooms = world.dungeon.getAllVertex().size();
        stats.totalPaths = world.dungeon.getAllEdges().size();
        stats.partySize = WebServer.getPartySize();
        stats.partyLevel = WebServer.getPartyLevel();
        return ResponseEntity.ok(stats);
    }

    // =============== Data Transfer Objects ===============

    public static class WorldData {
        public StatsData stats;
        public List<RoomData> rooms;
        public List<PathData> paths;
        public List<PartyData> parties;
    }

    public static class StatsData {
        public int totalRooms;
        public int totalPaths;
        public int partySize;
        public int partyLevel;
    }

    public static class RoomData {
        public int id;
        public String title;
        public String type;
        public String size;
        public int numSquares;
        public List<FeatureData> features;
        public List<CreatureData> creatures;
    }

    public static class RoomDetailData {
        public int id;
        public String title;
        public String type;
        public String size;
        public int numSquares;
        public int numTiles;
        public List<FeatureData> features;
        public List<Integer> connections;
    }

    public static class FeatureData {
        public String name;
        public int width;
        public int height;
        public int tileX;
        public int tileY;
    }

    public static class CreatureData {
        public String name;
        public String job;
        public int level;
        public int hp;
        public int maxHp;
        public String alignment;
        public String size;
        public int strength;
        public int dexterity;
        public int constitution;
        public int intelligence;
        public int wisdom;
        public int charisma;
    }

    public static class PathData {
        public int from;
        public int to;
        public String title;
        public String distance;
        public Integer stealthDc;
        public Integer lockDc;
    }

    public static class PartyData {
        public String title;
        public List<CreatureData> creatures;
    }
}
