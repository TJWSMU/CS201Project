package edu.smu.smusql;

import java.util.List;

public class Table {
    private LinearProbingHashMap<String, LinearProbingHashMap<String, String>> rows;
    private List<String> columns;
    private String name;

    public Table(String name, List<String> columns) {
        this.name = name;
        this.columns = columns;
        this.rows = new LinearProbingHashMap<>();
    }

    public List<String> getColumns() {
        return columns;
    }

    public LinearProbingHashMap<String, LinearProbingHashMap<String, String>> getRows() {
        return rows;
    }

    public String addRow(LinearProbingHashMap<String, String> row) {
        String key = row.get(columns.get(0));
        if (rows.containsKey(key)) {
            return "ERROR: Duplicate key";
        }
        rows.put(key, row);
        return "Row inserted into " + name;
    }

    public LinearProbingHashMap<String, String> getRowByFirstColumnValue(String key) {
        return rows.get(key);
    }
}