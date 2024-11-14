package edu.smu.smusql;

import java.util.*;

/*
 * Updated implementation of a database table for smuSQL.
 * author: ziyuanliu@smu.edu.sg
 */
public class Table {

    private DynamicArray<Map<String, String>> dataList; // Stores rows as a DynamicArray of maps
    private String name;                                // Name of the table
    private List<String> columns;                       // List of column names

    // Constructor for the Table class
    public Table(String name, List<String> columns) {
        this.name = name;
        this.columns = columns;
        this.dataList = new DynamicArray<>(); // Initialize the DynamicArray for rows
    }

    // Gets the name of the table
    public String getName() {
        return name;
    }

    // Gets the list of columns in the table
    public List<String> getColumns() {
        return columns;
    }

    // Gets the DynamicArray containing all rows
    public DynamicArray<Map<String, String>> getDataList() {
        return dataList;
    }

    // Sets the DynamicArray for the table's rows (useful for filtering results)
    public void setDataList(DynamicArray<Map<String, String>> dataList) {
        this.dataList = dataList;
    }

    // Adds a new row to the table
    public void addRow(Map<String, String> newRow) {
        dataList.add(newRow); // Use DynamicArray's add method
    }

    // Returns a string representation of the table's rows for easy display
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Table: ").append(name).append("\n");
        sb.append("Columns: ").append(columns).append("\n");

        for (int i = 0; i < dataList.size(); i++) {
            sb.append(dataList.get(i)).append("\n");
        }
        return sb.toString();
    }
}
