package edu.smu.smusql;

import java.util.List;

public class Table {
    private ChainHashMap<String, ChainHashMap<String, String>> rows; // Keyed by the first column value
    private List<String> columns;
    private String name;

    public Table(String name, List<String> columns) {
        this.name = name;
        this.columns = columns;
        this.rows = new ChainHashMap<>();
    }

    public List<String> getColumns() {
        return columns;
    }

    public ChainHashMap<String, ChainHashMap<String, String>> getRows() {
        return rows;
    }

    // Adds a row indexed by the first column value
    public String addRow(ChainHashMap<String, String> row) {
        String key = row.get(columns.get(0)); // Use the first column's value as the key
        if (rows.containsKey(key)) {
            return "ERROR: Duplicate entry for key '" + key + "'";
        }
        rows.put(key, row); // Insert row
        return "Row inserted.";
    }

    // Helper method to get the row by the first column value
    public ChainHashMap<String, String> getRowByFirstColumnValue(String key) {
        return rows.get(key);
    }
}
