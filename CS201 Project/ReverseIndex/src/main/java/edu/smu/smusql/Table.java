package edu.smu.smusql;

import java.util.*;

public class Table {
    private String name;
    private List<String> columns;
    private Map<String, String> columnTypes; // Map of column names to their data types ("INTEGER", "STRING", "DOUBLE")
    private List<Map<String, String>> rows; // List of rows (each row is a Map of column name to value)

    // Indices: Map of column name to its index (AVLTree or ChainHashMap)
    private Map<String, Object> indices;

    public Table(String name, List<String> columns, Map<String, String> columnTypes) {
        this.name = name;
        this.columns = columns;
        this.columnTypes = columnTypes;
        this.rows = new ArrayList<>();
        this.indices = new HashMap<>();

        // Initialize indices for each column
        for (String column : columns) {
            String type = columnTypes.get(column).toUpperCase();
            switch (type) {
                case "INTEGER":
                    indices.put(column, new AVLTree<Integer, Map<String, String>>());
                    break;
                case "DOUBLE":
                    indices.put(column, new AVLTree<Double, Map<String, String>>());
                    break;
                case "STRING":
                    indices.put(column, new ChainHashMap<String, List<Map<String, String>>>());
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported data type: " + type);
            }
        }
    }

    public List<String> getColumns() {
        return columns;
    }

    public String getColumnType(String column) {
        return columnTypes.get(column);
    }
    
    public List<Map<String, String>> getRows() {
        return rows;
    }

    // Adds a row to the table and updates indices
    public String addRow(Map<String, String> row) {
        // Validate row
        for (String column : columns) {
            if (!row.containsKey(column)) {
                return "ERROR: Missing value for column " + column;
            }
        }

        // Add the row to the main storage
        rows.add(row);

        // Update indices
        addRowToIndices(row);

        return "Row inserted into " + name;
    }

    // Helper method to add a row to indices
    private void addRowToIndices(Map<String, String> row) {
        for (String column : columns) {
            String type = columnTypes.get(column).toUpperCase();
            String value = row.get(column);

            switch (type) {
                case "INTEGER":
                    AVLTree<Integer, Map<String, String>> intIndex = (AVLTree<Integer, Map<String, String>>) indices.get(column);
                    if (intIndex != null) {
                        int intValue = Integer.parseInt(value);
                        intIndex.insert(intValue, row);
                    }
                    break;
                case "DOUBLE":
                    AVLTree<Double, Map<String, String>> doubleIndex = (AVLTree<Double, Map<String, String>>) indices.get(column);
                    if (doubleIndex != null) {
                        double doubleValue = Double.parseDouble(value);
                        doubleIndex.insert(doubleValue, row);
                    }
                    break;
                case "STRING":
                    ChainHashMap<String, List<Map<String, String>>> strIndex = (ChainHashMap<String, List<Map<String, String>>>) indices.get(column);
                    if (strIndex != null) {
                        List<Map<String, String>> list = strIndex.get(value);
                        if (list == null) {
                            list = new ArrayList<>();
                            strIndex.put(value, list);
                        }
                        list.add(row);
                    }
                    break;
            }
        }
    }

    // Helper method to remove a row from indices
    private void removeRowFromIndices(Map<String, String> row) {
        for (String column : columns) {
            String type = columnTypes.get(column).toUpperCase();
            String value = row.get(column);

            switch (type) {
                case "INTEGER":
                    AVLTree<Integer, Map<String, String>> intIndex = (AVLTree<Integer, Map<String, String>>) indices.get(column);
                    if (intIndex != null) {
                        int intValue = Integer.parseInt(value);
                        intIndex.delete(intValue, row);
                    }
                    break;
                case "DOUBLE":
                    AVLTree<Double, Map<String, String>> doubleIndex = (AVLTree<Double, Map<String, String>>) indices.get(column);
                    if (doubleIndex != null) {
                        double doubleValue = Double.parseDouble(value);
                        doubleIndex.delete(doubleValue, row);
                    }
                    break;
                case "STRING":
                    ChainHashMap<String, List<Map<String, String>>> strIndex = (ChainHashMap<String, List<Map<String, String>>>) indices.get(column);
                    if (strIndex != null) {
                        List<Map<String, String>> list = strIndex.get(value);
                        if (list != null) {
                            list.remove(row);
                            if (list.isEmpty()) {
                                strIndex.remove(value);
                            }
                        }
                    }
                    break;
            }
        }
    }

    // Method to get rows matching a condition on a column
    public List<Map<String, String>> getRowsByCondition(String column, String operator, String value) {
        List<Map<String, String>> result = new ArrayList<>();
        String type = columnTypes.get(column);

        if (type == null) {
            System.err.println("ERROR: Column '" + column + "' does not exist in table '" + name + "'.");
            return result;
        }

        type = type.toUpperCase();

        switch (type) {
            case "INTEGER":
                return getRowsByConditionForInteger(column, operator, Integer.parseInt(value));
            case "DOUBLE":
                return getRowsByConditionForDouble(column, operator, Double.parseDouble(value));
            case "STRING":
                return getRowsByConditionForString(column, operator, value);
            default:
                System.err.println("ERROR: Unsupported data type '" + type + "' for column '" + column + "'.");
        }
        return result;
    }

    private List<Map<String, String>> getRowsByConditionForInteger(String column, String operator, int intValue) {
        List<Map<String, String>> result = new ArrayList<>();
        AVLTree<Integer, Map<String, String>> index = (AVLTree<Integer, Map<String, String>>) indices.get(column);

        switch (operator) {
            case "=":
                List<Map<String, String>> list = index.search(intValue);
                if (list != null) result.addAll(list);
                break;
            case "!=":
                List<Map<String, String>> allRowsInt = new ArrayList<>(rows);
                List<Map<String, String>> equalRowsInt = index.search(intValue);
                if (equalRowsInt != null) {
                    allRowsInt.removeAll(equalRowsInt);
                }
                result.addAll(allRowsInt);
                break;
            case ">":
                result.addAll(index.getValuesGreaterThan(intValue));
                break;
            case "<":
                result.addAll(index.getValuesLessThan(intValue));
                break;
            case ">=":
                result.addAll(index.getValuesGreaterThanOrEqual(intValue));
                break;
            case "<=":
                result.addAll(index.getValuesLessThanOrEqual(intValue));
                break;
        }
        return result;
    }

    private List<Map<String, String>> getRowsByConditionForDouble(String column, String operator, double doubleValue) {
        List<Map<String, String>> result = new ArrayList<>();
        AVLTree<Double, Map<String, String>> index = (AVLTree<Double, Map<String, String>>) indices.get(column);

        switch (operator) {
            case "=":
                List<Map<String, String>> list = index.search(doubleValue);
                if (list != null) result.addAll(list);
                break;
            case "!=":
                List<Map<String, String>> allRowsDbl = new ArrayList<>(rows);
                List<Map<String, String>> equalRowsDbl = index.search(doubleValue);
                if (equalRowsDbl != null) {
                    allRowsDbl.removeAll(equalRowsDbl);
                }
                result.addAll(allRowsDbl);
                break;
            case ">":
                result.addAll(index.getValuesGreaterThan(doubleValue));
                break;
            case "<":
                result.addAll(index.getValuesLessThan(doubleValue));
                break;
            case ">=":
                result.addAll(index.getValuesGreaterThanOrEqual(doubleValue));
                break;
            case "<=":
                result.addAll(index.getValuesLessThanOrEqual(doubleValue));
                break;
        }
        return result;
    }

    private List<Map<String, String>> getRowsByConditionForString(String column, String operator, String value) {
        List<Map<String, String>> result = new ArrayList<>();
        ChainHashMap<String, List<Map<String, String>>> index = (ChainHashMap<String, List<Map<String, String>>>) indices.get(column);

        switch (operator) {
            case "=":
                List<Map<String, String>> list = index.get(value);
                if (list != null) result.addAll(list);
                break;
            case "!=":
                List<Map<String, String>> allRowsStr = new ArrayList<>(rows);
                List<Map<String, String>> equalRowsStr = index.get(value);
                if (equalRowsStr != null) {
                    allRowsStr.removeAll(equalRowsStr);
                }
                result.addAll(allRowsStr);
                break;
            // Additional range handling for strings can be implemented if needed
        }
        return result;
    }

    public List<Map<String, String>> getRowsByConditionInRange(String column, Number min, Number max) {
        List<Map<String, String>> result = new ArrayList<>();
        Object index = indices.get(column);
    
        if (index instanceof AVLTree) {
            if (min instanceof Integer && max instanceof Integer) {
                AVLTree<Integer, Map<String, String>> intIndex = (AVLTree<Integer, Map<String, String>>) index;
                result.addAll(intIndex.getValuesInRange((Integer) min, (Integer) max));
            } else if (min instanceof Double && max instanceof Double) {
                AVLTree<Double, Map<String, String>> doubleIndex = (AVLTree<Double, Map<String, String>>) index;
                result.addAll(doubleIndex.getValuesInRange((Double) min, (Double) max));
            } else {
                throw new IllegalArgumentException("Incompatible range types for column " + column);
            }
        }
        return result;
    }
    
    

    
    // Method to update a row and update indices
    public void updateRow(Map<String, String> row, Map<String, String> newValues) {
        // Only remove from indices that are being updated
        for (String column : newValues.keySet()) {
            removeRowFromIndex(row, column);
        }
    
        // Update the row values
        for (Map.Entry<String, String> entry : newValues.entrySet()) {
            row.put(entry.getKey(), entry.getValue());
        }
    
        // Only add back to indices that were updated
        for (String column : newValues.keySet()) {
            addRowToIndex(row, column);
        }
    }
    
    private void removeRowFromIndex(Map<String, String> row, String column) {
        String type = columnTypes.get(column).toUpperCase();
        String value = row.get(column);
        
        switch (type) {
            case "INTEGER":
                AVLTree<Integer, Map<String, String>> intIndex = (AVLTree<Integer, Map<String, String>>) indices.get(column);
                intIndex.delete(Integer.parseInt(value), row);
                break;
            case "DOUBLE":
                AVLTree<Double, Map<String, String>> doubleIndex = (AVLTree<Double, Map<String, String>>) indices.get(column);
                doubleIndex.delete(Double.parseDouble(value), row);
                break;
            case "STRING":
                ChainHashMap<String, List<Map<String, String>>> stringIndex = (ChainHashMap<String, List<Map<String, String>>>) indices.get(column);
                List<Map<String, String>> rows = stringIndex.get(value);
                if (rows != null) {
                    rows.remove(row);
                    if (rows.isEmpty()) stringIndex.remove(value);
                }
                break;
        }
    }

    private void addRowToIndex(Map<String, String> row, String column) {
        String type = columnTypes.get(column).toUpperCase();
        String value = row.get(column);
        
        switch (type) {
            case "INTEGER":
                AVLTree<Integer, Map<String, String>> intIndex = (AVLTree<Integer, Map<String, String>>) indices.get(column);
                intIndex.insert(Integer.parseInt(value), row);
                break;
            case "DOUBLE":
                AVLTree<Double, Map<String, String>> doubleIndex = (AVLTree<Double, Map<String, String>>) indices.get(column);
                doubleIndex.insert(Double.parseDouble(value), row);
                break;
            case "STRING":
                ChainHashMap<String, List<Map<String, String>>> stringIndex = (ChainHashMap<String, List<Map<String, String>>>) indices.get(column);
                List<Map<String, String>> rows = stringIndex.get(value);
                if (rows == null) {
                    rows = new ArrayList<>();
                    stringIndex.put(value, rows);
                }
                rows.add(row);
                break;
        }
    }

    // Method to delete a row and update indices
    public void deleteRow(Map<String, String> row) {
        rows.remove(row);
        
        // Remove the row from indices
        removeRowFromIndices(row);
    }
    
}
