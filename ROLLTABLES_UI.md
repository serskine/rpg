# Roll Tables UI Feature

## Overview

A new "Roll Tables" tab has been added to both the GUI (Swing) and web interfaces, allowing users to:
- Select a roll table from the database
- View the table's dice equation
- Roll on the table and see results
- Follow cascading table references

## GUI Implementation (Swing)

### RollTablePanel

Located in `game.view.RollTablePanel`, this panel provides:

**Components:**
- **Table Selector** (ComboBox): Dropdown to select from available roll tables
- **Refresh Button**: Reloads tables from the database
- **Roll Button**: Executes a roll on the selected table
- **Equation Display** (TextArea): Shows the selected table's dice equation
- **Results Display** (TextArea): Shows detailed roll results
- **Status Label**: Displays current operation status

**Layout:**
```
┌─ Top Panel ─────────────────────────────────────┐
│ "Select Table:" [ComboBox] [Roll] [Refresh]    │
└─────────────────────────────────────────────────┘
┌─ Middle Panel (Split) ──────────────────────────┐
│ Left (30%) │ Right (70%)                        │
│ Equation   │ Roll Results                       │
│ Display    │                                    │
├────────────┼──────────────────────────────────┤
│ [Equation] │ [Results formatted with steps]   │
└────────────┴──────────────────────────────────┘
┌─ Bottom Panel ──────────────────────────────────┐
│ Status: Ready                                   │
└─────────────────────────────────────────────────┘
```

### Integration with WorldView

The RollTablePanel is added as the third tab in `game.view.WorldView`:

```
Tabs:
1. Dungeon Map     → DungeonPanel
2. World Contents  → WorldTreePanel
3. Roll Tables     → RollTablePanel  ← NEW
```

The panel is loaded automatically when the application starts via `RollTableServiceLocator`.

### Service Initialization

The `RollTableServiceLocator` class (in `game.rolltable.RollTableServiceLocator`) handles Spring context initialization in GUI mode:

```java
// Called automatically by RollTablePanel constructor
RollTableService service = RollTableServiceLocator.getService();
```

This creates a minimal Spring Boot context to:
- Initialize the H2 database
- Load JPA repositories
- Create the RollTableService bean

## Web Implementation (REST API)

### RollTableController

Located in `game.web.RollTableController`, provides REST endpoints:

#### GET /api/rolltables
Retrieve all available roll tables
```json
Response: [
  {
    "id": 1,
    "title": "Simple Encounter",
    "equation": "1d6",
    "records": [...]
  },
  ...
]
```

#### GET /api/rolltables/{id}
Retrieve a specific roll table
```
GET /api/rolltables/1
Response: { id: 1, title: "Simple Encounter", ... }
```

#### POST /api/rolltables/{id}/roll
Roll on a specific table
```
POST /api/rolltables/1/roll
Response: [
  {
    "id": 1,
    "rollTableId": 1,
    "title": "Peaceful",
    "referencedTableId": null
  }
]
```

#### POST /api/rolltables
Create a new roll table
```json
Request: {
  "title": "My Table",
  "equation": "2d10"
}
Response: { id: 3, title: "My Table", equation: "2d10", records: [] }
```

#### POST /api/rolltables/{id}/records
Add a record to a table
```json
Request: {
  "title": "Peaceful Encounter",
  "referencedTableId": null
}
Response: { id: 10, rollTableId: 1, title: "Peaceful Encounter", ... }
```

## Usage Examples

### GUI Usage

1. **Launch Application**
   ```bash
   java -cp target/classes game.Main
   ```

2. **Navigate to Roll Tables Tab**
   - Click on "Roll Tables" tab

3. **Select a Table**
   - Dropdown automatically populated with available tables
   - View equation in left panel

4. **Roll on Table**
   - Click "Roll" button
   - Results displayed in right panel with step-by-step outcomes

5. **Refresh Tables**
   - Click "Refresh Tables" to reload from database
   - Useful if tables were added programmatically

### Web Usage

1. **Start Web Server**
   ```bash
   java -cp target/classes game.Main --web
   ```

2. **Get Tables**
   ```bash
   curl http://localhost:8080/api/rolltables
   ```

3. **Roll on Table**
   ```bash
   curl -X POST http://localhost:8080/api/rolltables/1/roll
   ```

## Result Formatting

Roll results are displayed with clear formatting showing:
- Step number in the roll chain
- Outcome title
- Reference to next table (if applicable)

Example output:
```
═══════════════════════════════════════
Roll Results
═══════════════════════════════════════

Step 1: Very Hostile
  └─ References Table #2

Step 2: Magical Item

═══════════════════════════════════════
```

## Sample Tables

Two sample tables come pre-populated:

### Table 1: Simple Encounter (1d6)
1. Peaceful
2. Wary
3. Hostile
4. Very Hostile
5. Aggressive Combat
6. Deadly Encounter

### Table 2: Treasure Chest (2d10)
20 different treasure outcomes ranging from Empty to Ultimate Prize

## Error Handling

The UI gracefully handles:
- **No tables loaded**: Status shows "Please select a table"
- **Database errors**: Error message in status bar
- **Missing tables**: 404 response in REST API
- **Invalid input**: Validation on table selection

## File Locations

**GUI Components:**
- `src/main/java/game/view/RollTablePanel.java` - UI panel (228 lines)
- `src/main/java/game/view/WorldView.java` - Updated with new tab

**Service Layer:**
- `src/main/java/game/rolltable/RollTableServiceLocator.java` - Spring context for GUI (42 lines)
- `src/main/java/game/rolltable/RollTableService.java` - Core service

**Web API:**
- `src/main/java/game/web/RollTableController.java` - REST endpoints (86 lines)

**Database:**
- `src/main/resources/data.sql` - Sample data initialization
- `src/main/resources/application.properties` - Configuration

## Testing

All existing tests pass:
- ✅ 9 DiceEquationEvaluator tests
- ✅ 7 RollTableService tests  
- ✅ 7 Integration tests

Total: **23 tests** - All passing

## Build Status

```
Build: SUCCESS
Compilation: 49 Java files compiled without errors
Tests: 23/23 passing
```

## Notes

- The RollTableServiceLocator is singleton-based and thread-safe
- Spring context is lazily initialized on first use
- All database queries are managed by JPA
- The combo box displays table titles for user-friendly selection
- Results display supports tables that reference other tables recursively
