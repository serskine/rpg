package game.rolltable;

import game.rolltable.persistence.RollTableEntity;
import game.rolltable.persistence.RollTableJpaRepository;
import game.rolltable.persistence.RollTableRecordEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for RollTableService
 */
@DisplayName("Roll Table Service Tests")
public class RollTableServiceTest {

    private RollTableJpaRepository repository;
    private RollTableService service;

    @BeforeEach
    public void setup() {
        repository = mock(RollTableJpaRepository.class);
        service = new RollTableService(repository);
    }

    @Test
    @DisplayName("Should retrieve all tables")
    public void testGetAllTables() {
        final RollTableEntity table1 = new RollTableEntity("Table 1", "1d6");
        final RollTableEntity table2 = new RollTableEntity("Table 2", "2d10");
        
        when(repository.findAll()).thenReturn(List.of(table1, table2));

        final List<RollTable> tables = service.getAllTables();

        assertEquals(2, tables.size());
        assertEquals("Table 1", tables.get(0).getTitle());
        assertEquals("Table 2", tables.get(1).getTitle());
    }

    @Test
    @DisplayName("Should retrieve table by ID")
    public void testGetTableById() {
        final RollTableEntity entity = new RollTableEntity("Test Table", "1d20");
        entity.setId(1);

        when(repository.findById(1)).thenReturn(Optional.of(entity));

        final Optional<RollTable> table = service.getTableById(1);

        assertTrue(table.isPresent());
        assertEquals("Test Table", table.get().getTitle());
        assertEquals("1d20", table.get().getEquation());
    }

    @Test
    @DisplayName("Should return empty when table not found")
    public void testGetTableByIdNotFound() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        final Optional<RollTable> table = service.getTableById(999);

        assertFalse(table.isPresent());
    }

    @Test
    @DisplayName("Should create new table")
    public void testCreateTable() {
        final RollTableEntity entity = new RollTableEntity("New Table", "1d6");
        entity.setId(1);

        when(repository.save(any(RollTableEntity.class))).thenReturn(entity);

        final RollTable created = service.createTable("New Table", "1d6");

        assertEquals("New Table", created.getTitle());
        assertEquals("1d6", created.getEquation());
    }

    @Test
    @DisplayName("Should roll on table and return result")
    public void testRollOnTable() {
        final RollTableEntity tableEntity = new RollTableEntity("Simple Table", "1d3");
        tableEntity.setId(1);

        final RollTableRecordEntity record1 = new RollTableRecordEntity("Result 1", null);
        record1.setId(1);
        record1.setRollTable(tableEntity);

        final RollTableRecordEntity record2 = new RollTableRecordEntity("Result 2", null);
        record2.setId(2);
        record2.setRollTable(tableEntity);

        final RollTableRecordEntity record3 = new RollTableRecordEntity("Result 3", null);
        record3.setId(3);
        record3.setRollTable(tableEntity);

        tableEntity.getRecords().addAll(List.of(record1, record2, record3));

        when(repository.findById(1)).thenReturn(Optional.of(tableEntity));

        final List<RollTableRecord> results = service.rollOnTable(1);

        assertFalse(results.isEmpty());
        assertTrue(results.get(0).getTitle().startsWith("Result"));
    }

    @Test
    @DisplayName("Should follow referenced tables")
    public void testRollOnTableWithReference() {
        // Create two tables
        final RollTableEntity table1 = new RollTableEntity("Main Table", "1d1");
        table1.setId(1);

        final RollTableEntity table2 = new RollTableEntity("Referenced Table", "1d1");
        table2.setId(2);

        // Add records
        final RollTableRecordEntity record1 = new RollTableRecordEntity("Ref to Table 2", 2);
        record1.setId(1);
        record1.setRollTable(table1);
        table1.getRecords().add(record1);

        final RollTableRecordEntity record2 = new RollTableRecordEntity("Final Result", null);
        record2.setId(2);
        record2.setRollTable(table2);
        table2.getRecords().add(record2);

        when(repository.findById(1)).thenReturn(Optional.of(table1));
        when(repository.findById(2)).thenReturn(Optional.of(table2));

        final List<RollTableRecord> results = service.rollOnTable(1);

        assertEquals(2, results.size());
        assertEquals("Ref to Table 2", results.get(0).getTitle());
        assertEquals("Final Result", results.get(1).getTitle());
    }

    @Test
    @DisplayName("Should throw exception when adding record to nonexistent table")
    public void testAddRecordToNonexistentTable() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            service.addRecordToTable(999, "Test", null)
        );
    }
}
