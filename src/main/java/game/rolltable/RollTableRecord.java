package game.rolltable;

import java.util.Optional;

/**
 * Represents a record within a roll table.
 * Each record has a title describing the result and optionally references another table to roll on.
 * Records can also contain JSON metadata for storing enum properties.
 */
public class RollTableRecord {
    private final int id;
    private final int rollTableId;
    private final String title;
    private final Optional<Integer> referencedTableId;
    private final int weight;
    private final Optional<String> metadata;

    public RollTableRecord(final int id, final int rollTableId, final String title, final Integer referencedTableId) {
        this(id, rollTableId, title, referencedTableId, 1, null);
    }

    public RollTableRecord(final int id, final int rollTableId, final String title, final Integer referencedTableId, final int weight) {
        this(id, rollTableId, title, referencedTableId, weight, null);
    }

    public RollTableRecord(final int id, final int rollTableId, final String title, final Integer referencedTableId, final int weight, final String metadata) {
        this.id = id;
        this.rollTableId = rollTableId;
        this.title = title;
        this.referencedTableId = Optional.ofNullable(referencedTableId);
        this.weight = weight;
        this.metadata = Optional.ofNullable(metadata);
    }

    public int getId() {
        return id;
    }

    public int getRollTableId() {
        return rollTableId;
    }

    public String getTitle() {
        return title;
    }

    public Optional<Integer> getReferencedTableId() {
        return referencedTableId;
    }

    public int getWeight() {
        return weight;
    }

    public Optional<String> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return title;
    }
}
