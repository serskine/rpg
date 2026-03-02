package game.web;

import game.rolltable.RollTable;
import game.rolltable.RollTableRecord;
import game.rolltable.RollTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST API controller for roll table operations.
 * Provides endpoints for retrieving tables and rolling on them.
 */
@RestController
@RequestMapping("/api/rolltables")
public class RollTableController {

    private final RollTableService rollTableService;

    @Autowired
    public RollTableController(final RollTableService rollTableService) {
        this.rollTableService = rollTableService;
    }

    /**
     * GET /api/rolltables
     * Retrieves all available roll tables.
     */
    @GetMapping
    public ResponseEntity<List<RollTable>> getAllTables() {
        final List<RollTable> tables = rollTableService.getAllTables();
        return ResponseEntity.ok(tables);
    }

    /**
     * GET /api/rolltables/{id}
     * Retrieves a specific roll table by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RollTable> getTable(@PathVariable final int id) {
        final Optional<RollTable> table = rollTableService.getTableById(id);
        if (table.isPresent()) {
            return ResponseEntity.ok(table.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * POST /api/rolltables/{id}/roll
     * Rolls on a specific table and returns the results.
     */
    @PostMapping("/{id}/roll")
    public ResponseEntity<List<RollTableRecord>> rollOnTable(@PathVariable final int id) {
        final Optional<RollTable> table = rollTableService.getTableById(id);
        if (!table.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        final List<RollTableRecord> results = rollTableService.rollOnTable(id);
        return ResponseEntity.ok(results);
    }

    /**
     * POST /api/rolltables
     * Creates a new roll table.
     *
     * @param request containing title and equation
     */
    @PostMapping
    public ResponseEntity<RollTable> createTable(@RequestBody final CreateTableRequest request) {
        final RollTable table = rollTableService.createTable(request.title, request.equation);
        return ResponseEntity.ok(table);
    }

    /**
     * POST /api/rolltables/{id}/records
     * Adds a record to a roll table.
     *
     * @param id the table ID
     * @param request containing title, optional referencedTableId, and optional weight
     */
    @PostMapping("/{id}/records")
    public ResponseEntity<RollTableRecord> addRecord(
            @PathVariable final int id,
            @RequestBody final AddRecordRequest request) {
        try {
            final RollTableRecord record = rollTableService.addRecordToTable(id, request.title, request.referencedTableId, request.weight);
            return ResponseEntity.ok(record);
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Request body for creating a new table.
     */
    public static class CreateTableRequest {
        public String title;
        public String equation;
    }

    /**
     * Request body for adding a record to a table.
     */
    public static class AddRecordRequest {
        public String title;
        public Integer referencedTableId;
        public int weight = 1;
    }
}
