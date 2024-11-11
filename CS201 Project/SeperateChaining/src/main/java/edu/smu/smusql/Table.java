package edu.smu.smusql;

import java.util.LinkedHashMap;
import java.util.List;

public class Table {
    private ChainHashMap<String, ChainHashMap<String, String>> rows; // Keyed by the first column value
    private List<String> columns;
    private String name;
    //private LinkedHashMap<String, ChainHashMap<String, String>> rows;

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
        String key = row.get(columns.get(0));
        if (rows.containsKey(key)) {
            return "ERROR: Duplicate key";
        }
        rows.put(key, row);
        return "Row inserted into " + name;
    }

    // Helper method to get the row by the first column value
    public ChainHashMap<String, String> getRowByFirstColumnValue(String key) {
        return rows.get(key);
    }
}
