package edu.smu.smusql;

import java.util.List;

public class Table {
    private ChainHashMap<Integer, ChainHashMap<String, String>> rows;
    private String name;
    private List<String> columns;
    private int nextRowId;

    // Constructor
    public Table(String name, List<String> columns) {
        rows = new ChainHashMap<>();
        this.name = name;
        this.columns = columns;
        this.nextRowId = 1;
    }

    public String getName() {
        return name;
    }

    public List<String> getColumns() {
        return columns;
    }

    public ChainHashMap<Integer, ChainHashMap<String, String>> getRows() {
        return rows;
    }

    public void addRow(ChainHashMap<String, String> newRow) {
        rows.put(nextRowId, newRow);
        nextRowId++;
    }

    // Other methods...
}
