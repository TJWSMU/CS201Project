package edu.smu.smusql;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashMap;

public class Table {
    private ChainHashMap<String, HashMap<String, String>> rows; // Keyed by the first column value
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

    public ChainHashMap<String, HashMap<String, String>> getRows() {
        return rows;
    }

    // Adds a row indexed by the first column value
    public String addRow(HashMap<String, String> row) {
        String key = row.get(columns.get(0));
        if (rows.containsKey(key)) {
            return "ERROR: Duplicate key";
        }
        rows.put(key, row);
        return "Row inserted into " + name;
    }

    // Helper method to get the row by the first column value
    public HashMap<String, String> getRowByFirstColumnValue(String key) {
        return rows.get(key);
    }
}
