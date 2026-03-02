package game.rolltable;

import java.util.Optional;

/**
 * Represents a record within a roll table.
 * Each record has a title describing the result and optionally references another table to roll on.
 */
public class RollTableRecord {
    private final int id;
    private final int rollTableId;
    private final String title;
    private final Optional<Integer> referencedTableId;
    private final int weight;

    public RollTableRecord(final int id, final int rollTableId, final String title, final Integer referencedTableId) {
        this(id, rollTableId, title, referencedTableId, 1);
    }

    public RollTableRecord(final int id, final int rollTableId, final String title, final Integer referencedTableId, final int weight) {
        this.id = id;
        this.rollTableId = rollTableId;
        this.title = title;
        this.referencedTableId = Optional.ofNullable(referencedTableId);
        this.weight = weight;
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

    @Override
    public String toString() {
        return title;
    }
}
