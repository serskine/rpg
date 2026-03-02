package game.rolltable;

import game.rolltable.persistence.RollTableEntity;
import game.rolltable.persistence.RollTableJpaRepository;
import game.rolltable.persistence.RollTableRecordEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing and rolling on roll tables.
 * Takes a table ID and returns a list of results based on the roll outcome.
 */
@Service
public class RollTableService {

    private final RollTableJpaRepository repository;

    @Autowired
    public RollTableService(final RollTableJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieves all roll tables.
     *
     * @return a list of all RollTable objects
     */
    public List<RollTable> getAllTables() {
        final List<RollTable> tables = new ArrayList<>();
        for (final RollTableEntity entity : repository.findAll()) {
            tables.add(convertToRollTable(entity));
        }
        return tables;
    }

    /**
     * Retrieves a specific roll table by ID.
     *
     * @param tableId the ID of the table to retrieve
     * @return an Optional containing the RollTable if found, empty otherwise
     */
    public Optional<RollTable> getTableById(final int tableId) {
        return repository.findById(tableId).map(this::convertToRollTable);
    }

    /**
     * Rolls on a table and returns the result.
     * If a record references another table, that table is also rolled on.
     *
     * @param tableId the ID of the table to roll on
     * @return a list of RollTableRecord results (may include results from referenced tables)
     */
    public List<RollTableRecord> rollOnTable(final int tableId) {
        final List<RollTableRecord> results = new ArrayList<>();
        rollOnTableRecursive(tableId, results);
        return results;
    }

    /**
     * Internal recursive method to roll on a table and follow references.
     * Uses weighted selection if weights are present.
     */
    private void rollOnTableRecursive(final int tableId, final List<RollTableRecord> results) {
        final Optional<RollTableEntity> tableOptional = repository.findById(tableId);
        if (!tableOptional.isPresent()) {
            return;
        }

        final RollTableEntity tableEntity = tableOptional.get();
        final List<RollTableRecordEntity> recordEntities = tableEntity.getRecords();
        
        if (recordEntities.isEmpty()) {
            return;
        }

        // Determine selection index: use weighted selection if all weights > 0, otherwise use equation
        final int recordIndex = selectRecordIndex(tableEntity, recordEntities);
        
        if (recordIndex >= 0 && recordIndex < recordEntities.size()) {
            final RollTableRecordEntity recordEntity = recordEntities.get(recordIndex);
            final RollTableRecord record = new RollTableRecord(
                recordEntity.getId(),
                recordEntity.getRollTable().getId(),
                recordEntity.getTitle(),
                recordEntity.getReferencedTableId(),
                recordEntity.getWeight(),
                recordEntity.getMetadata()
            );
            results.add(record);
            
            // If this record references another table, roll on that table too
            if (record.getReferencedTableId().isPresent()) {
                rollOnTableRecursive(record.getReferencedTableId().get(), results);
            }
        }
    }

    /**
     * Determines which record to select from a table.
     * If any weight is set, uses weighted selection. Otherwise uses equation-based selection.
     */
    private int selectRecordIndex(final RollTableEntity tableEntity, final List<RollTableRecordEntity> recordEntities) {
        // Check if any weight is set (non-default)
        boolean hasWeights = false;
        for (final RollTableRecordEntity record : recordEntities) {
            if (record.getWeight() != 1) {
                hasWeights = true;
                break;
            }
        }

        if (hasWeights) {
            // Use weighted selection
            final int[] weights = new int[recordEntities.size()];
            for (int i = 0; i < recordEntities.size(); i++) {
                weights[i] = recordEntities.get(i).getWeight();
            }
            return DiceEquationEvaluator.selectWeightedIndex(weights);
        } else {
            // Use equation-based selection
            final int rollResult = DiceEquationEvaluator.evaluate(tableEntity.getEquation());
            return rollResult - 1; // Convert from 1-indexed to 0-indexed
        }
    }

    /**
     * Creates a new roll table.
     *
     * @param title the title of the table
     * @param equation the dice equation (e.g., "1d20 + 5")
     * @return the created RollTable
     */
    public RollTable createTable(final String title, final String equation) {
        final RollTableEntity entity = new RollTableEntity(title, equation);
        final RollTableEntity saved = repository.save(entity);
        return convertToRollTable(saved);
    }

    /**
     * Adds a record to a roll table.
     *
     * @param tableId the ID of the table to add the record to
     * @param title the title of the record
     * @param referencedTableId optional ID of another table to reference (null if none)
     * @return the created RollTableRecord
     */
    public RollTableRecord addRecordToTable(final int tableId, final String title, final Integer referencedTableId) {
        return addRecordToTable(tableId, title, referencedTableId, 1);
    }

    /**
     * Adds a record to a roll table with a specified weight.
     *
     * @param tableId the ID of the table to add the record to
     * @param title the title of the record
     * @param referencedTableId optional ID of another table to reference (null if none)
     * @param weight the weight/probability of this record
     * @return the created RollTableRecord
     */
    public RollTableRecord addRecordToTable(final int tableId, final String title, final Integer referencedTableId, final int weight) {
        return addRecordToTable(tableId, title, referencedTableId, weight, null);
    }

    /**
     * Adds a record to a roll table with weight and metadata.
     *
     * @param tableId the ID of the table to add the record to
     * @param title the title of the record
     * @param referencedTableId optional ID of another table to reference (null if none)
     * @param weight the weight/probability of this record
     * @param metadata optional JSON metadata (null if none)
     * @return the created RollTableRecord
     */
    public RollTableRecord addRecordToTable(final int tableId, final String title, final Integer referencedTableId, final int weight, final String metadata) {
        final Optional<RollTableEntity> tableOptional = repository.findById(tableId);
        if (!tableOptional.isPresent()) {
            throw new IllegalArgumentException("Roll table with ID " + tableId + " not found");
        }

        final RollTableEntity tableEntity = tableOptional.get();
        final RollTableRecordEntity recordEntity = new RollTableRecordEntity(title, referencedTableId, weight);
        recordEntity.setMetadata(metadata);
        recordEntity.setRollTable(tableEntity);
        tableEntity.getRecords().add(recordEntity);
        
        final RollTableEntity updated = repository.save(tableEntity);
        final RollTableRecordEntity savedRecord = updated.getRecords().get(updated.getRecords().size() - 1);
        
        return new RollTableRecord(
            savedRecord.getId(),
            savedRecord.getRollTable().getId(),
            savedRecord.getTitle(),
            savedRecord.getReferencedTableId(),
            savedRecord.getWeight(),
            savedRecord.getMetadata()
        );
    }

    /**
     * Converts a RollTableEntity to a RollTable domain object.
     */
    private RollTable convertToRollTable(final RollTableEntity entity) {
        final RollTable table = new RollTable(entity.getId(), entity.getTitle(), entity.getEquation());
        for (final RollTableRecordEntity recordEntity : entity.getRecords()) {
            final RollTableRecord record = new RollTableRecord(
                recordEntity.getId(),
                recordEntity.getRollTable().getId(),
                recordEntity.getTitle(),
                recordEntity.getReferencedTableId(),
                recordEntity.getWeight(),
                recordEntity.getMetadata()
            );
            table.addRecord(record);
        }
        return table;
    }
}
