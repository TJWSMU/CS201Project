package edu.smu.smusql;

import java.util.*;

/*
 * A hilariously bad implementation of a database table for smuSQL.
 * author: ziyuanliu@smu.edu.sg
 */
public class Table {

    private SinglyLinkedList<Map<String, String>> dataList;

    public String getName() {
        return name;
    }

    public SinglyLinkedList<Map<String, String>> getDataList() {
        return dataList;
    }

    public void setDataList(SinglyLinkedList<Map<String, String>> dataList) {
        this.dataList = dataList;
    }

    private String name;

    public List<String> getColumns() {
        return columns;
    }

    private List<String> columns;

    public void addRow(Map<String, String> new_row){
        dataList.addLast(new_row);
    }

    public Table(String name, List<String> columns) {
        dataList = new SinglyLinkedList<>();
        this.name = name;
        this.columns = columns;
    }
}
