package game.rolltable.persistence;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a roll table in the database.
 */
@Entity
@Table(name = "roll_tables")
public class RollTableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String equation;

    @OneToMany(mappedBy = "rollTable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RollTableRecordEntity> records = new ArrayList<>();

    public RollTableEntity() {
    }

    public RollTableEntity(final String title, final String equation) {
        this.title = title;
        this.equation = equation;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getEquation() {
        return equation;
    }

    public void setEquation(final String equation) {
        this.equation = equation;
    }

    public List<RollTableRecordEntity> getRecords() {
        return records;
    }

    public void setRecords(final List<RollTableRecordEntity> records) {
        this.records = records;
    }
}
