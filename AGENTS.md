# Agent Instructions for RPG Project

This document provides essential instructions for AI agents working on the **Rpg** project. It outlines build commands, code style, and project conventions to ensure consistency and correctness.

## 1. Project Overview & Environment

*   **Language:** Java
*   **Version:** Java 16 (Source/Target compatibility)
*   **Build System:** Maven
*   **Frameworks:** Java Swing (GUI), Standard Java Libraries.

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

### Test
**Current Status:** There are currently **NO** configured unit tests or test dependencies (like JUnit) in `pom.xml`.
*   If instructed to add tests, you must first add the JUnit 5 dependency to `pom.xml`.
*   Once configured, run all tests with:
    ```bash
    mvn test
    ```
*   To run a single test class (once configured):
    ```bash
    mvn -Dtest=TestClassName test
    ```

### Linting
There are no automated linting plugins (Checkstyle/PMD) configured. strictly follow the **Code Style** section below.

## 3. Code Style & Conventions

Adhere to the existing style found in `src/main/java`.

### Formatting
*   **Indentation:** 4 spaces.
*   **Braces:** Open braces on the same line (K&R / One True Brace style).
*   **Line Endings:** Unix style (`\n`).
*   **Encoding:** UTF-8.

### Naming
*   **Classes/Interfaces:** PascalCase (e.g., `DungeonPanel`, `WorldBuilder`).
*   **Methods:** camelCase (e.g., `generateOrthogonalPaths`, `rollRarity`).
*   **Variables:** camelCase (e.g., `roomPositions`, `partySize`).
*   **Constants:** UPPER_SNAKE_CASE (e.g., `MIN_ZOOM`, `MAX_ZOOM`).
*   **Generics:** Use descriptive names where helpful (e.g., `Graph<Vertex, Path>`) or standard single letters (`T`, `E`) for simple utilities.

### Coding Patterns (Important)
*   **`final` Keyword:** The project uses `final` **extensively** for method parameters and immutable local variables.
    *   *Example:* `public void setPath(final Vertex from, final Vertex to, final Path path) { ... }`
    *   **Rule:** Default to declaring parameters and unchanging locals as `final`.
*   **Java Version Features (Java 16):**
    *   Use **Switch Expressions**: `case UNCOMMON -> { ... }`.
    *   Use `var` sparingly; explicit types are preferred in this codebase (e.g., `Map<Room, Point> roomPositions = ...`).
*   **Collections:**
    *   Use `java.util.stream` for processing collections where appropriate.
    *   Use `Optional<T>` for return values that may be null (e.g., `Optional<Integer> lockDc`).
*   **Imports:**
    *   Explicit imports are preferred, but wildcard imports (e.g., `java.util.*`, `java.awt.*`) are present in legacy/GUI code.
    *   Keep standard Java imports separate from project imports (`game.*`).

### Package Structure
*   `game.common`: Core domain objects (Room, Path, Party, Creature).
*   `game.builder`: Logic for procedurally generating content.
*   `game.util`: Generic utilities (Graph, Math/Func, Logging).
*   `game.view`: Swing UI components.

## 4. Error Handling
*   Use `try-catch` blocks for checked exceptions.
*   Use `Optional` to handle missing data instead of returning `null`.
*   Use `game.util.Logger` for logging (e.g., `Logger.warn(...)`) instead of `System.out.println`.

## 5. Agent Workflow
1.  **Read First:** Always read relevant files before editing to understand context and existing patterns.
2.  **Verify:** After editing, run `mvn clean compile` to ensure no build errors were introduced.
3.  **Consistency:** Match the style of the file you are editing. If the file uses `final` everywhere, you must too.
