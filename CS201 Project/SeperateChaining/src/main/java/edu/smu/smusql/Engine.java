package edu.smu.smusql;

import java.util.*;

public class Engine {
    // Stores the tables in the database
    private ChainHashMap<String, Table> tables = new ChainHashMap<>();

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

    public String create(String[] tokens) {
        if (!tokens[1].equalsIgnoreCase("TABLE")) {
            return "ERROR: Invalid CREATE TABLE syntax";
        }
    
        String tableName = tokens[2];
    
        // Check if table exists
        if (tables.containsKey(tableName)) {
            return "ERROR: Table already exists";
        }
    
        String columnList = queryBetweenParentheses(tokens, 3); // Get column list between parentheses
        List<String> columns = Arrays.asList(columnList.split(","));
        columns.replaceAll(String::trim);
    
        Table newTable = new Table(tableName, columns);
    
        tables.put(tableName, newTable); // Use ChainHashMap to store the table
        System.out.println(columns);
        return "Table " + tableName + " created";
    }
    

    public String insert(String[] tokens) {
        if (!tokens[1].equalsIgnoreCase("INTO")) {
            return "ERROR: Invalid INSERT INTO syntax";
        }
    
        String tableName = tokens[2];
    
        // Retrieve table from ChainHashMap
        Table tbl = tables.get(tableName);
        if (tbl == null) {
            return "Error: no such table: " + tableName;
        }
    
        String valueList = queryBetweenParentheses(tokens, 4); // Get values list between parentheses
        System.out.println(valueList);
        List<String> values = Arrays.asList(valueList.split(","));
        values.replaceAll(String::trim);
    
        List<String> columns = tbl.getColumns();
        System.out.println(columns);

        if (values.size() != columns.size()) {
            return "ERROR: Column count doesn't match value count";
        }
    
        // Create a new row using ChainHashMap
        ChainHashMap<String, String> row = new ChainHashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            row.put(columns.get(i), values.get(i));
        }
    
        tbl.addRow(row); // Add the new row to the table
    
        return "Row inserted into " + tableName;
    }
    

    public String select(String[] tokens) {
        if (!tokens[1].equals("*") || !tokens[2].equalsIgnoreCase("FROM")) {
            return "ERROR: Invalid SELECT syntax";
        }
    
        String tableName = tokens[3];
    
        // Retrieve table from ChainHashMap
        Table tbl = tables.get(tableName);
        if (tbl == null) {
            return "Error: no such table: " + tableName;
        }
    
        ChainHashMap<Integer, ChainHashMap<String, String>> tableData = tbl.getRows();
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
    
        // Iterate over rows using ChainHashMap
        for (Integer rowId : tableData.keySet()) {
            ChainHashMap<String, String> row = tableData.get(rowId);
            boolean match = evaluateWhereConditions(row, whereClauseConditions);
    
            if (match) {
                for (String column : columns) {
                    result.append(row.getOrDefault(column, "NULL")).append("\t");
                }
                result.append("\n");
            }
        }
    
        return result.toString();
    }
    

    public String update(String[] tokens) {
        String tableName = tokens[1];
    
        // Retrieve table from ChainHashMap
        Table tbl = tables.get(tableName);
        if (tbl == null) {
            return "Error: no such table: " + tableName;
        }
    
        String setColumn = tokens[3]; // column to be updated
        String newValue = tokens[5]; // new value for above column
    
        List<String> columns = tbl.getColumns();
        ChainHashMap<Integer, ChainHashMap<String, String>> tableData = tbl.getRows();
    
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
    
        int affectedRows = 0;
    
        for (Integer rowId : tableData.keySet()) {
            ChainHashMap<String, String> row = tableData.get(rowId);
            boolean match = evaluateWhereConditions(row, whereClauseConditions);
    
            if (match) {
                row.put(setColumn, newValue);
                affectedRows++;
            }
        }
    
        return "Table " + tableName + " updated. " + affectedRows + " rows affected.";
    }
    

    public String delete(String[] tokens) {
        if (!tokens[1].equalsIgnoreCase("FROM")) {
            return "ERROR: Invalid DELETE syntax";
        }
    
        String tableName = tokens[2];
    
        // Retrieve table from ChainHashMap
        Table tbl = tables.get(tableName);
        if (tbl == null) {
            return "Error: no such table: " + tableName;
        }
    
        ChainHashMap<Integer, ChainHashMap<String, String>> tableData = tbl.getRows();
        
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
    
        int affectedRows = 0;
        List<Integer> rowsToDelete = new ArrayList<>();
    
        for (Integer rowId : tableData.keySet()) {
            ChainHashMap<String, String> row = tableData.get(rowId);
            boolean match = evaluateWhereConditions(row, whereClauseConditions);
    
            if (match) {
                rowsToDelete.add(rowId);
                affectedRows++;
            }
        }
    
        // Remove matched rows
        for (Integer rowId : rowsToDelete) {
            tableData.remove(rowId);
        }
    
        return "Rows deleted from " + tableName + ". " + affectedRows + " rows affected.";
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
