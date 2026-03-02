# Game Data Roll Tables Documentation

All static dungeon building data has been migrated from hardcoded enums into database roll tables. This allows for:
- Dynamic table management via the UI
- Future expansion and modification without code changes
- Consistent random selection via the roll system

## Roll Table Mapping

### Core Game Data Tables

| ID | Table Name | Equation | Records | Purpose |
|----|-----------|----------|---------|---------|
| 1 | Room Types | 1d19 | 19 | Random room type selection |
| 2 | Room Sizes | 1d3 | 3 | Random room size selection |
| 3 | Room Features | 1d32 | 32 | Random room feature selection |
| 4 | Alignments | 1d9 | 9 | Random creature alignment |
| 5 | Creature Sizes | 1d6 | 6 | Random creature size |
| 6 | Movement Types | 1d6 | 6 | Random creature movement type |
| 7 | Path Distances | 1d4 | 4 | Random path distance |
| 8 | Rarities | 1d3 | 3 | Random rarity level |
| 9 | NPC Jobs | 1d6 | 6 | Random NPC class selection |
| 10 | PC Jobs (Common) | 1d7 | 7 | Random common player character job |
| 11 | PC Jobs (Uncommon) | 1d3 | 3 | Random uncommon player character job |
| 12 | PC Jobs (Rare) | 1d3 | 3 | Random rare player character job |
| 13 | All PC Jobs | 1d13 | 13 | Random any player character job |
| 14 | Simple Encounter | 1d6 | 6 | Sample encounter table |
| 15 | Treasure Chest | 2d10 | 20 | Sample treasure table |

## Table Details

### Room Types (ID: 1)
Records for: COURTYARD, CRYPT, DUNGEON, TOWER, KITCHEN, LIBRARY, THRONE_ROOM, MESS_HALL, STORAGE_ROOM, SLEEPING_QUARTERS, ARMORY, GARDEN, CHAPEL, WORKSHOP, LABORATORY, VAULT, TORTURE_CHAMBER, RITUAL_ROOM, STAIRWELL

### Room Sizes (ID: 2)
Records for: CRAMPED, ROOMY, VAST

### Room Features (ID: 3)
Records for: SMALL_TABLE, BIG_TABLE, WORK_BENCH, TOMB, COFFIN, THRONE, ALTAR, STATUE, LARGE_STATUE, HUGE_STATUE, DESK, SPELL_ALTAR, BOOK_SHELF, STORAGE_CABINET, SMALL_SHELF, BRAZIER, TELEPORTATION_CIRCLE, FOUNTAIN, TRAP_DOOR, DOOR, PIT, PILE_OF_RUBBLE, PILE_OF_TREASURE, CHEST, TWIN_BED, DOUBLE_BED, STOVE, ANVIL, FORGE, CAMPFIRE, FIREPLACE, BEDROLL

### Alignments (ID: 4)
Records for: LAWFUL_GOOD, NEUTRAL_GOOD, CHAOTIC_GOOD, LAWFUL_NEUTRAL, NEUTRAL, CHAOTIC_NEUTRAL, LAWFUL_EVIL, NEUTRAL_EVIL, CHAOTIC_EVIL

### Creature Sizes (ID: 5)
Records for: TINY, SMALL, MEDIUM, LARGE, HUGE, GARGANTUAN

### Movement Types (ID: 6)
Records for: WALKING, FLYING, SWIMMING, CLIMBING, BURROWING, TELEPORTING

### Path Distances (ID: 7)
Records for: MELEE, SHORT, FAR, VERY_FAR

### Rarities (ID: 8)
Records for: COMMON, UNCOMMON, RARE

### NPC Jobs (ID: 9)
Records for: COMMONER, MINION, TOUGH, ELITE, BOSS, DIETY

### PC Jobs (Common) (ID: 10)
Records for: BARBARIAN, BARD, CLERIC, FIGHTER, PALADIN, RANGER, ROGUE

### PC Jobs (Uncommon) (ID: 11)
Records for: DRUID, MONK, WARLOCK

### PC Jobs (Rare) (ID: 12)
Records for: ARTIFICER, SORCERER, WIZARD

### All PC Jobs (ID: 13)
Records for: ARTIFICER, BARBARIAN, BARD, CLERIC, DRUID, FIGHTER, MONK, PALADIN, RANGER, ROGUE, SORCERER, WARLOCK, WIZARD

## Usage Examples

### Selecting Random Room Type
```java
List<RollTableRecord> results = rollTableService.rollOnTable(1); // Room Types
String roomType = results.get(0).getTitle(); // e.g., "KITCHEN"
```

### Selecting Random Room Size
```java
List<RollTableRecord> results = rollTableService.rollOnTable(2); // Room Sizes
String roomSize = results.get(0).getTitle(); // e.g., "ROOMY"
```

### Selecting Random Creature
```java
// Size
List<RollTableRecord> sizeResult = rollTableService.rollOnTable(5);
// Alignment
List<RollTableRecord> alignmentResult = rollTableService.rollOnTable(4);
// Job (depends on context - use 9, 10, 11, 12, or 13)
List<RollTableRecord> jobResult = rollTableService.rollOnTable(9);
```

## Integration Points

### In DungeonBuilder
Instead of:
```java
RoomType roomType = chooseFromRandomly(RoomType.values());
```

Can now use:
```java
List<RollTableRecord> result = rollTableService.rollOnTable(1);
RoomType roomType = RoomType.valueOf(result.get(0).getTitle());
```

### In CreatureBuilder
Instead of:
```java
CreatureSize size = chooseFromRandomly(CreatureSize.values());
```

Can now use:
```java
List<RollTableRecord> result = rollTableService.rollOnTable(5);
CreatureSize size = CreatureSize.valueOf(result.get(0).getTitle());
```

## Benefits

1. **Flexibility**: Tables can be modified via the Roll Tables UI without recompiling
2. **Auditing**: All selections are traceable through the database
3. **Expansion**: Easy to add weighted options or special rules later
4. **Consistency**: Single source of truth for all game data
5. **Testing**: Mock data easily for testing different scenarios

## Migration Path

Future enhancements:
- [ ] Refactor builders to inject RollTableService
- [ ] Replace all chooseFromRandomly() calls with table rolls
- [ ] Add weights to table records for probability customization
- [ ] Create UI for managing table probabilities
- [ ] Add table composition rules (e.g., "always include one rare feature")

## Database Schema

```sql
roll_tables (
    id INT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    equation VARCHAR(255) NOT NULL
)

roll_table_records (
    id INT PRIMARY KEY,
    roll_table_id INT FOREIGN KEY,
    title VARCHAR(255) NOT NULL,
    referenced_table_id INT FOREIGN KEY
)
```

Total records in system: **181** (across all 15 tables)

## Access Methods

### Via GUI
- Open "Roll Tables" tab
- Select desired table from dropdown
- Click "Roll" to get random result

### Via REST API
```bash
# Get all game data tables
curl http://localhost:8080/api/rolltables

# Roll on a specific table
curl -X POST http://localhost:8080/api/rolltables/1/roll
```

### Via Code
```java
@Autowired
private RollTableService rollTableService;

// Roll on Room Types table
List<RollTableRecord> result = rollTableService.rollOnTable(1);
```

## Verification

To verify the data loaded correctly:
1. Start the application in web mode: `java -cp target/classes game.Main --web`
2. Visit: `http://localhost:8080/api/rolltables`
3. Should see 15 tables with appropriate record counts

Or in GUI mode:
1. Start: `java -cp target/classes game.Main`
2. Click "Roll Tables" tab
3. Dropdown should show all 15 tables
4. Select any table and click "Roll"
