package edu.smu.smusql;

import java.util.*;

public class Table {
    private String name;
    private List<String> columns;
    private SkipList data;

    public Table(String name, List<String> columns) {
        this.name = name;
        this.columns = columns;
        this.data = new SkipList(columns);
    }

    public String getName() {
        return name;
    }

    public List<String> getColumns() {
        return columns;
    }

    public SkipList getData() {
        return data;
    }
} 