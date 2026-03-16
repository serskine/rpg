package game.rolltable.persistence;

import javax.persistence.*;

/**
 * JPA entity representing a record within a roll table in the database.
 */
@Entity
@Table(name = "roll_table_records")
public class RollTableRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roll_table_id", nullable = false)
    private RollTableEntity rollTable;

    @Column(nullable = false)
    private String title;

    @Column(name = "referenced_table_id")
    private Integer referencedTableId;

    @Column(nullable = false)
    private int weight = 1;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    public RollTableRecordEntity() {
    }

    public RollTableRecordEntity(final String title, final Integer referencedTableId) {
        this.title = title;
        this.referencedTableId = referencedTableId;
        this.weight = 1;
        this.metadata = null;
    }

    public RollTableRecordEntity(final String title, final Integer referencedTableId, final int weight) {
        this.title = title;
        this.referencedTableId = referencedTableId;
        this.weight = weight;
        this.metadata = null;
    }

    public RollTableRecordEntity(final String title, final Integer referencedTableId, final int weight, final String metadata) {
        this.title = title;
        this.referencedTableId = referencedTableId;
        this.weight = weight;
        this.metadata = metadata;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public RollTableEntity getRollTable() {
        return rollTable;
    }

    public void setRollTable(final RollTableEntity rollTable) {
        this.rollTable = rollTable;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public Integer getReferencedTableId() {
        return referencedTableId;
    }

    public void setReferencedTableId(final Integer referencedTableId) {
        this.referencedTableId = referencedTableId;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(final int weight) {
        this.weight = weight;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(final String metadata) {
        this.metadata = metadata;
    }
}
