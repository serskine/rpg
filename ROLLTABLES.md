# Roll Tables System

The Roll Tables system provides a database-backed mechanism for managing and executing dice roll tables in the RPG application. Each roll table contains an equation defining the dice to roll and a list of records mapping roll results to outcomes.

## Architecture Overview

### Core Classes

#### Domain Models
- **RollTable** (`game.rolltable.RollTable`): Represents a roll table with title, equation, and records
- **RollTableRecord** (`game.rolltable.RollTableRecord`): Represents a single outcome record with optional reference to another table

#### Database Layer
- **RollTableEntity** (`game.rolltable.persistence.RollTableEntity`): JPA entity for `roll_tables` table
- **RollTableRecordEntity** (`game.rolltable.persistence.RollTableRecordEntity`): JPA entity for `roll_table_records` table
- **RollTableJpaRepository** (`game.rolltable.persistence.RollTableJpaRepository`): Spring Data JPA repository for database operations

#### Service & Utilities
- **RollTableService** (`game.rolltable.RollTableService`): Main service for managing and rolling on tables
- **DiceEquationEvaluator** (`game.rolltable.DiceEquationEvaluator`): Parses and evaluates dice equations

## Usage

### Basic Setup

The system is configured to use an in-memory H2 database that is automatically initialized with sample data on startup.

### Rolling on a Table

```java
@Autowired
private RollTableService rollTableService;

// Roll on a table and get results
List<RollTableRecord> results = rollTableService.rollOnTable(1);
// results may contain multiple entries if records reference other tables
```

### Creating Tables Programmatically

```java
// Create a new table
RollTable table = rollTableService.createTable("Encounter Table", "1d10");

// Add records to the table
rollTableService.addRecordToTable(table.getId(), "Peaceful", null);
rollTableService.addRecordToTable(table.getId(), "Combat", null);
rollTableService.addRecordToTable(table.getId(), "Deadly", 2); // References table 2
```

### Retrieving Tables

```java
// Get all tables
List<RollTable> allTables = rollTableService.getAllTables();

// Get a specific table
Optional<RollTable> table = rollTableService.getTableById(1);
```

## Dice Equations

Equations use the format: `NdS ± modifier ± modifier ...`

Where:
- `N` = number of dice to roll
- `S` = number of sides per die
- Modifiers are optional and can be added or subtracted

### Examples

| Equation | Description |
|----------|-------------|
| `1d20` | Roll one 20-sided die |
| `2d6` | Roll two 6-sided dice |
| `1d20 + 5` | Roll 1d20 and add 5 |
| `2d20 + 1d6 + 2` | Roll 2d20, add 1d6, add 2 |
| `3d6 - 1` | Roll 3d6 and subtract 1 |

## Database Schema

### roll_tables
```sql
CREATE TABLE roll_tables (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    equation VARCHAR(255) NOT NULL
);
```

### roll_table_records
```sql
CREATE TABLE roll_table_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    roll_table_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    referenced_table_id INT,
    FOREIGN KEY (roll_table_id) REFERENCES roll_tables(id),
    FOREIGN KEY (referenced_table_id) REFERENCES roll_tables(id)
);
```

## Sample Data

The application comes with two sample tables:

### Simple Encounter (ID: 1)
- Equation: `1d6`
- Records:
  1. Peaceful
  2. Wary
  3. Hostile
  4. Very Hostile
  5. Aggressive Combat
  6. Deadly Encounter

### Treasure Chest (ID: 2)
- Equation: `2d10`
- Records: 20 outcomes ranging from Empty to Ultimate Prize

## Roll Resolution

When rolling on a table:

1. Evaluate the dice equation to get a result (1-indexed)
2. Use result as index into the records list
3. Return the record at that index
4. If the record references another table, recursively roll on that table
5. Return all results in order (original record followed by referenced table results)

### Example

Rolling on Treasure Chest (1d6, actually 2d10 = 1-20):
- Roll result: 15
- Returns: `records[14]` = "Kings Ransom"
- If "Kings Ransom" referenced another table, that would be rolled next

## Testing

The system includes comprehensive tests:

### DiceEquationEvaluatorTest
Tests for dice equation parsing and evaluation:
- Simple dice rolls
- Multiple dice
- Modifiers (positive and negative)
- Complex equations
- Edge cases

### RollTableServiceTest
Tests for the service layer:
- CRUD operations
- Rolling on tables
- Following table references
- Error handling

Run tests with:
```bash
mvn test
```

All 23 tests should pass.

## Configuration

Configuration is in `src/main/resources/application.properties`:

```properties
# H2 Database (in-memory)
spring.datasource.url=jdbc:h2:mem:rpg
spring.jpa.hibernate.ddl-auto=create-drop

# Initialize with sample data
spring.sql.init.mode=always
spring.sql.init.data-locations=classpath:data.sql
```

## Integration Points

The RollTableService can be injected into any Spring component:

```java
@Service
public class MyService {
    @Autowired
    private RollTableService rollTableService;
    
    public void doSomething() {
        List<RollTableRecord> results = rollTableService.rollOnTable(1);
        // Use results...
    }
}
```

## Future Enhancements

Potential improvements:
- Web UI for managing roll tables
- Support for weighted outcomes (if roll 5 appears twice, 5 is more likely)
- Complex conditions and branching logic
- Equation validation and error messages
- Export/import tables in JSON/CSV format
- Category tags for organizing tables
