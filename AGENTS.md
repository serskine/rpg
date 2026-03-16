# Agent Instructions for RPG Project

This document provides essential instructions for AI agents working on the **Rpg** project. It outlines build commands, code style, and project conventions to ensure consistency and correctness.

## 1. Project Overview & Environment

*   **Language:** Java
*   **Version:** Java 16 (Source/Target compatibility)
*   **Build System:** Maven
*   **Frameworks:** Java Swing (GUI), Standard Java Libraries.
*   **Testing:** JUnit 5 (Jupiter)

## 2. Build & Test Commands

Since this is a Maven project, use the standard lifecycle phases.

### Build
To compile the project and check for errors:
```bash
mvn clean compile
```

### Run
To run the application (Main class: `game.Main`):
*Note: This requires the project to be compiled first.*
```bash
java -cp target/classes game.Main
```
*   **Arguments:** The application accepts optional arguments for `<party size>` and `<avg party level>`.
    *   Example: `java -cp target/classes game.Main 4 5`

Alternatively, use Maven exec plugin:
```bash
mvn exec:java -Dexec.mainClass="game.Main"
```

### Test
Run all unit tests:
```bash
mvn test
```

To run a single test class:
```bash
mvn -Dtest=TestClassName test
```

To run a specific test method within a class:
```bash
mvn -Dtest=TestClassName#testMethodName test
```

### Linting
There are no automated linting plugins (Checkstyle/PMD) configured. Strictly follow the **Code Style** section below.

## 3. Code Style & Conventions

Adhere to the existing style found in `src/main/java`.

### Formatting
*   **Indentation:** 4 spaces.
*   **Braces:** Open braces on the same line (K&R / One True Brace style).
*   **Line Endings:** Unix style (`\n`).
*   **Encoding:** UTF-8.
*   **Line Length:** Keep lines under 120 characters when practical.

### Naming
*   **Classes/Interfaces:** PascalCase (e.g., `DungeonPanel`, `WorldBuilder`).
*   **Methods:** camelCase (e.g., `generateOrthogonalPaths`, `rollRarity`).
*   **Variables:** camelCase (e.g., `roomPositions`, `partySize`).
*   **Constants:** UPPER_SNAKE_CASE (e.g., `MIN_ZOOM`, `MAX_ZOOM`).
*   **Enums:** PascalCase (e.g., `COMMON`, `UNCOMMON`) - see `Rarity.java`.
*   **Generics:** Use descriptive names where helpful (e.g., `Graph<Vertex, Path>`) or standard single letters (`T`, `E`) for simple utilities.

### Coding Patterns (Important)

#### `final` Keyword Usage
The project uses `final` **extensively** for method parameters and immutable local variables:
*   **Method parameters:** Always use `final`
    ```java
    public void setPath(final Vertex from, final Vertex to, final Path path) { ... }
    ```
*   **Local variables:** Use `final` for variables that are not reassigned
    ```java
    final Room[] rooms = new Room[numberOfRooms];
    final Graph<Room, Path> dungeonGraph = new Graph<>();
    ```
*   **Instance fields:** Do NOT use `final` on mutable instance fields (see `Room.java`)

#### Static Imports
Use static imports for utility functions to improve readability:
```java
import static game.util.Func.*;
// Allows: d(6), rollRarity() instead of Func.d(6), Func.rollRarity()
```

#### Java Version Features (Java 16)
*   Use **Switch Expressions**: `case UNCOMMON -> { ... }`.
*   Use `var` sparingly; explicit types are preferred (e.g., `Map<Room, Point> roomPositions = ...`).

#### Records (Java 16+)
Use records for immutable data carriers:
```java
public record Edge(V source, V target, E data) { }
```

#### Enums
Keep enums simple and concise:
```java
public enum Rarity {
    COMMON, UNCOMMON, RARE;
}
```

### Collections
*   Use `java.util.stream` for processing collections where appropriate.
*   Use `Optional<T>` for return values that may be null (e.g., `Optional<Integer> lockDc`).
*   Prefer immutable collection patterns where possible.

### Imports
*   Explicit imports are preferred, but wildcard imports (e.g., `java.util.*`, `java.awt.*`) are present in legacy/GUI code.
*   Keep standard Java imports separate from project imports (`game.*`).
*   Group imports in this order:
    1.  Java standard library (`java.*`)
    2.  Third-party libraries
    3.  Project imports (`game.*`)
    4.  Static imports

### Package Structure
```
game/
├── Main.java              # Application entry point
├── common/                # Core domain objects
│   ├── Room.java
│   ├── Path.java
│   ├── Party.java
│   ├── Creature.java
│   └── (enums)
├── builder/               # Procedural content generation
│   ├── WorldBuilder.java
│   ├── DungeonBuilder.java
│   └── (other builders)
├── util/                  # Generic utilities
│   ├── Graph.java
│   ├── Func.java
│   └── Logger.java
└── view/                  # Swing UI components
    ├── WorldView.java
    └── (UI panels)
```

## 4. Error Handling
*   Use `try-catch` blocks for checked exceptions.
*   Use `Optional` to handle missing data instead of returning `null`.
*   Use `game.util.Logger` for logging:
    ```java
    Logger.info("Generated " + roomCount + " rooms");
    Logger.warn("Path already exists between rooms");
    Logger.error("Failed to load configuration", e);
    ```
*   Avoid silently catching exceptions - always log or handle appropriately.

## 5. Documentation
*   Add Javadoc comments for public APIs, especially in `game.common` and `game.builder` packages.
*   Keep comments concise and descriptive; avoid stating the obvious.
*   Example Javadoc:
    ```java
    /**
     * Builds a dungeon graph with rooms and paths based on rarity.
     *
     * @return a Graph containing rooms as vertices and paths as edges
     */
    public Graph<Room, Path> build() { ... }
    ```

## 6. Agent Workflow
1.  **Read First:** Always read relevant files before editing to understand context and existing patterns.
2.  **Verify:** After editing, run `mvn clean compile` to ensure no build errors were introduced.
3.  **Consistency:** Match the style of the file you are editing. If the file uses `final` everywhere, you must too.
4.  **Test:** If adding new functionality, write JUnit 5 tests in `src/test/java`.
