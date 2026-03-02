package game.rolltable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a roll table that defines an equation for rolling dice
 * and contains records that map roll results to outcomes.
 */
public class RollTable {
    private final int id;
    private final String title;
    private final String equation;
    private final List<RollTableRecord> records;

    public RollTable(final int id, final String title, final String equation) {
        this.id = id;
        this.title = title;
        this.equation = equation;
        this.records = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getEquation() {
        return equation;
    }

    public List<RollTableRecord> getRecords() {
        return records;
    }

    public void addRecord(final RollTableRecord record) {
        records.add(record);
    }

    @Override
    public String toString() {
        return title;
    }
}
