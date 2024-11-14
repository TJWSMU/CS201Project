package edu.smu.smusql;

import java.util.*;

public class Engine {
    // stores the contents of database tables in-memory
    private HashMap<String, Table> dataList = new HashMap<>();

    public String executeSQL(String query) {
        String[] tokens = query.trim().split("\\s+");
        String command = tokens[0].toUpperCase();

        switch (command) {
            case "CREATE":
                return create(tokens);
            case "INSERT":
                return insert(tokens);
            case "SELECT":
                return select(tokens);
            case "UPDATE":
                return update(tokens);
            case "DELETE":
                return delete(tokens);
            default:
                return "ERROR: Unknown command";
        }
    }

    // implementation of the INSERT command
    public String insert(String[] tokens) {
        if (!tokens[1].toUpperCase().equals("INTO")) {
            return "ERROR: Invalid INSERT INTO syntax";
        }

        String tableName = tokens[2];
        Table tbl = dataList.get(tableName); // Access table directly by name
        if (tbl == null) {
            return "Error: no such table: " + tableName;
        }

        String valueList = queryBetweenParentheses(tokens, 4); // Get values list between parentheses
        List<String> values = Arrays.asList(valueList.split(","));

        // Trim each value to avoid spaces around them
        values.replaceAll(String::trim);

        List<String> columns = tbl.getColumns();

        if (values.size() != columns.size()) {
            return "ERROR: Column count doesn't match value count";
        }

        Map<String, String> row = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            row.put(columns.get(i), values.get(i));
        }

        tbl.addRow(row); // Add the new row to the table

        return "Row inserted into " + tableName;
    }
    // implementation of the DELETE command
    public String delete(String[] tokens) {
        if (!tokens[1].toUpperCase().equals("FROM")) {
            return "ERROR: Invalid DELETE syntax";
        }

        String tableName = tokens[2];
        Table tbl = dataList.get(tableName); // Access table directly by name
        if (tbl == null) {
            return "Error: no such table: " + tableName;
        }

        DynamicArray<Map<String, String>> tableData = tbl.getDataList();
        List<String> columns = tbl.getColumns();

        // Initialize whereClauseConditions list
        List<String[]> whereClauseConditions = new ArrayList<>();

        // Parse WHERE clause conditions
        if (tokens.length > 3 && tokens[3].toUpperCase().equals("WHERE")) {
            for (int i = 4; i < tokens.length; i++) {
                if (tokens[i].toUpperCase().equals("AND") || tokens[i].toUpperCase().equals("OR")) {
                    // Add AND/OR conditions
                    whereClauseConditions.add(new String[] {tokens[i].toUpperCase(), null, null, null});
                } else if (isOperator(tokens[i])) {
                    // Add condition with operator (column, operator, value)
                    String column = tokens[i - 1];
                    String operator = tokens[i];
                    String value = tokens[i + 1];
                    whereClauseConditions.add(new String[] {null, column, operator, value});
                    i += 1; // Skip the value since it has been processed
                }
            }
        }

        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columns)).append("\n"); // Print column headers

        DynamicArray<Map<String, String>> tempList = new DynamicArray<>();

        int i = 0;
        int ct = 0; // count number of rows affected.
        while (i < tableData.size()){
            Map<String, String> row = tableData.get(i);
            boolean match = evaluateWhereConditions(row, whereClauseConditions);

            if (!match) {
                tempList.add(row); // Retain row if condition matches
            } else {
                ct++;
            }
            i++;
        }
        tbl.setDataList(tempList);

        return "Rows deleted from " + tableName + ". " + Integer.toString(ct) + " rows affected.";
    }

    // SELECT command
    public String select(String[] tokens) {
        if (!tokens[1].equals("*") || !tokens[2].toUpperCase().equals("FROM")) {
            return "ERROR: Invalid SELECT syntax";
        }

        String tableName = tokens[3];
        Table tbl = dataList.get(tableName);
        if (tbl == null) {
            return "Error: no such table: " + tableName;
        }

        DynamicArray<Map<String, String>> tableData = tbl.getDataList();
        List<String> columns = tbl.getColumns();

        // Initialize whereClauseConditions list
        List<String[]> whereClauseConditions = new ArrayList<>();

        // Parse WHERE clause conditions
        if (tokens.length > 4 && tokens[4].equalsIgnoreCase("WHERE")) {
            for (int i = 5; i < tokens.length; i++) {
                if (tokens[i].equalsIgnoreCase("AND") || tokens[i].equalsIgnoreCase("OR")) {
                    // Add AND/OR conditions
                    whereClauseConditions.add(new String[] {tokens[i].toUpperCase(), null, null, null});
                } else if (isOperator(tokens[i])) {
                    // Add condition with operator (column, operator, value)
                    String column = tokens[i - 1];
                    String operator = tokens[i];
                    String value = tokens[i + 1];
                    whereClauseConditions.add(new String[] {null, column, operator, value});
                    i += 1; // Skip the value since it has been processed
                }
            }
        }

        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columns)).append("\n"); // Print column headers

        // Filter rows based on WHERE clause


        int i = 0;
        while (i < tableData.size()){
            Map<String, String> row = tableData.get(i);
            boolean match = evaluateWhereConditions(row, whereClauseConditions);

            if (match) {
                for (String column : columns) {
                    result.append(row.getOrDefault(column, "NULL")).append("\t");
                }
                result.append("\n");
            }
            i++;
        }

        return result.toString();
    }
    // UPDATE command
    public String update(String[] tokens) {
        String tableName = tokens[1];
        Table tbl = dataList.get(tableName);
        if (tbl == null) {
            return "Error: no such table: " + tableName;
        }

        String setColumn = tokens[3]; // column to be updated
        String newValue = tokens[5]; // new value for above column

        List<String> columns = tbl.getColumns();

        // Retrieve table data
        DynamicArray<Map<String, String>> tableData = tbl.getDataList();
        // Initialize whereClauseConditions list
        List<String[]> whereClauseConditions = new ArrayList<>();

        // Parse WHERE clause conditions
        if (tokens.length > 6 && tokens[6].equalsIgnoreCase("WHERE")) {
            for (int i = 5; i < tokens.length; i++) {
                if (tokens[i].equalsIgnoreCase("AND") || tokens[i].equalsIgnoreCase("OR")) {
                    // Add AND/OR conditions
                    whereClauseConditions.add(new String[] {tokens[i].toUpperCase(), null, null, null});
                } else if (isOperator(tokens[i])) {
                    // Add condition with operator (column, operator, value)
                    String column = tokens[i - 1];
                    String operator = tokens[i];
                    String value = tokens[i + 1];
                    whereClauseConditions.add(new String[] {null, column, operator, value});
                    i += 1; // Skip the value since it has been processed
                }
            }
        }

        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columns)).append("\n"); // Print column headers

        // Update rows based on WHERE clause

        int i = 0;
        int ct = 0; // count number of affected rows
        while (i < tableData.size()){
            Map<String, String> row = tableData.get(i);
            boolean match = evaluateWhereConditions(row, whereClauseConditions);
            if (match) {
                row.put(setColumn, newValue);
                ct++;
            }
            i++;
        }

        return "Table " + tableName + " updated. " + Integer.toString(ct) + " rows affected.";
    }

    // CREATE TABLE command
    public String create(String[] tokens) {
        if (!tokens[1].equalsIgnoreCase("TABLE")) {
            return "ERROR: Invalid CREATE TABLE syntax";
        }

        String tableName = tokens[2];
        if (dataList.containsKey(tableName)) {
            return "ERROR: Table already exists";
        }

        String columnList = queryBetweenParentheses(tokens, 3); // Get column list between parentheses
        List<String> columns = Arrays.asList(columnList.split(","));

        // Trim each column name to avoid spaces around them
        columns.replaceAll(String::trim);

        Table new_table = new Table(tableName, columns);

        dataList.put(tableName, new_table);

        return "Table " + tableName + " created";
    }

    /*
     *  HELPER METHODS
     *  Below are some helper methods which you may wish to use in your own
     *  implementations.
     */

    // Helper method to extract content inside parentheses
    private String queryBetweenParentheses(String[] tokens, int startIndex) {
        StringBuilder result = new StringBuilder();
        for (int i = startIndex; i < tokens.length; i++) {
            result.append(tokens[i]).append(" ");
        }
        return result.toString().trim().replaceAll("\\(", "").replaceAll("\\)", "");
    }

    // Helper method to evaluate a single condition
    private boolean evaluateCondition(String columnValue, String operator, String value) {
        if (columnValue == null) return false;

        // Compare strings as numbers if possible
        boolean isNumeric = isNumeric(columnValue) && isNumeric(value);
        if (isNumeric) {
            double columnNumber = Double.parseDouble(columnValue);
            double valueNumber = Double.parseDouble(value);

            switch (operator) {
                case "=": return columnNumber == valueNumber;
                case ">": return columnNumber > valueNumber;
                case "<": return columnNumber < valueNumber;
                case ">=": return columnNumber >= valueNumber;
                case "<=": return columnNumber <= valueNumber;
            }
        } else {
            switch (operator) {
                case "=": return columnValue.equals(value);
                case ">": return columnValue.compareTo(value) > 0;
                case "<": return columnValue.compareTo(value) < 0;
                case ">=": return columnValue.compareTo(value) >= 0;
                case "<=": return columnValue.compareTo(value) <= 0;
            }
        }

        return false;
    }

    // Helper method to determine if a string is an operator
    private boolean isOperator(String token) {
        return token.equals("=") || token.equals(">") || token.equals("<") || token.equals(">=") || token.equals("<=");
    }

    // Helper method to determine if a string is numeric
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Method to evaluate where conditions
    private boolean evaluateWhereConditions(Map<String, String> row, List<String[]> conditions) {
        boolean overallMatch = true;
        boolean nextConditionShouldMatch = true; // Default behavior for AND

        for (String[] condition : conditions) {
            if (condition[0] != null) { // AND/OR operator
                nextConditionShouldMatch = condition[0].equals("AND");
            } else {
                // Parse column, operator, and value
                String column = condition[1];
                String operator = condition[2];
                String value = condition[3];

                boolean currentMatch = evaluateCondition(row.get(column), operator, value);

                if (nextConditionShouldMatch) {
                    overallMatch = overallMatch && currentMatch;
                } else {
                    overallMatch = overallMatch || currentMatch;
                }
            }
        }

        return overallMatch;
    }

}
