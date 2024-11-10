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
        Table table = tables.get(tableName);
        if (table == null) {
            return "Error: no such table: " + tableName;
        }

        String valueList = queryBetweenParentheses(tokens, 4); // Get values list between parentheses
        List<String> values = Arrays.asList(valueList.split(","));
        values.replaceAll(String::trim);

        List<String> columns = table.getColumns();

        if (values.size() != columns.size()) {
            return "ERROR: Column count doesn't match value count";
        }

        // Create a new row using ChainHashMap
        ChainHashMap<String, String> row = new ChainHashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            row.put(columns.get(i), values.get(i));
        }

        // Add the new row using the first column value as the key
        return table.addRow(row);
    }

    

    public String select(String[] tokens) {
        if (!tokens[1].equals("*") || !tokens[2].equalsIgnoreCase("FROM")) {
            return "ERROR: Invalid SELECT syntax";
        }
    
        String tableName = tokens[3];
    
        // Retrieve table from ChainHashMap
        Table table = tables.get(tableName);
        if (table == null) {
            return "Error: no such table: " + tableName;
        }
    
        List<String> columns = table.getColumns();
        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columns)).append("\n"); // Print column headers
    
        List<String[]> whereClauseConditions = new ArrayList<>();
    
        // Parse WHERE clause if present
        if (tokens.length > 4 && tokens[4].equalsIgnoreCase("WHERE")) {
            whereClauseConditions = parseWhereClause(tokens, 5); // Start parsing after "WHERE"
    
            String whereColumn = tokens[5];
            String whereValue = tokens[7];
    
            // Use indexed lookup if condition is on the first column
            if (whereColumn.equals(columns.get(0))) {
                ChainHashMap<String, String> row = table.getRowByFirstColumnValue(whereValue);
                if (row != null) {
                    for (String column : columns) {
                        result.append(row.getOrDefault(column, "NULL")).append("\t");
                    }
                    result.append("\n");
                }
            } else {
                // Fallback to full table scan if WHERE is on a different column
                for (ChainHashMap<String, String> row : table.getRows().values()) {
                    if (evaluateWhereConditions(row, whereClauseConditions)) {
                        for (String column : columns) {
                            result.append(row.getOrDefault(column, "NULL")).append("\t");
                        }
                        result.append("\n");
                    }
                }
            }
        } else {
            // No WHERE clause; retrieve all rows
            for (ChainHashMap<String, String> row : table.getRows().values()) {
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
    
        Table table = tables.get(tableName);
        if (table == null) {
            return "Error: no such table: " + tableName;
        }
    
        String setColumn = tokens[3];
        String newValue = tokens[5];
    
        List<String> columns = table.getColumns();
        List<String[]> whereClauseConditions = new ArrayList<>();
    
        // Parse WHERE clause if present
        if (tokens.length > 6 && tokens[6].equalsIgnoreCase("WHERE")) {
            whereClauseConditions = parseWhereClause(tokens, 7); // Start parsing after "WHERE"
        }
    
        int affectedRows = 0;
    
        // Check if WHERE clause specifies the first column for optimized lookup
        if (!whereClauseConditions.isEmpty() && whereClauseConditions.get(0)[1].equals(columns.get(0))) {
            String key = whereClauseConditions.get(0)[3];
            ChainHashMap<String, String> row = table.getRowByFirstColumnValue(key);
    
            if (row != null) {
                row.put(setColumn, newValue);
                affectedRows++;
            }
        } else {
            // Full table scan if no indexed column specified in WHERE
            for (ChainHashMap<String, String> row : table.getRows().values()) {
                if (evaluateWhereConditions(row, whereClauseConditions)) {
                    row.put(setColumn, newValue);
                    affectedRows++;
                }
            }
        }
    
        return "Table " + tableName + " updated. " + affectedRows + " rows affected.";
    }
    
    

    public String delete(String[] tokens) {
        if (!tokens[1].equalsIgnoreCase("FROM")) {
            return "ERROR: Invalid DELETE syntax";
        }
    
        String tableName = tokens[2];
        Table table = tables.get(tableName);
        if (table == null) {
            return "Error: no such table: " + tableName;
        }
    
        List<String[]> whereClauseConditions = new ArrayList<>();
        int affectedRows = 0;
    
        // Parse WHERE clause if present
        if (tokens.length > 3 && tokens[3].equalsIgnoreCase("WHERE")) {
            whereClauseConditions = parseWhereClause(tokens, 4); // Start parsing after "WHERE"
        }
    
        // Check if WHERE clause specifies the first column for optimized lookup
        if (!whereClauseConditions.isEmpty() && whereClauseConditions.get(0)[1].equals(table.getColumns().get(0))) {
            String key = whereClauseConditions.get(0)[3];
            if (table.getRows().remove(key) != null) {
                affectedRows++;
            }
        } else {
            // Full table scan if no indexed column specified in WHERE
            List<String> keysToRemove = new ArrayList<>();
            for (String key : table.getRows().keySet()) {
                ChainHashMap<String, String> row = table.getRows().get(key);
                if (evaluateWhereConditions(row, whereClauseConditions)) {
                    keysToRemove.add(key);
                    affectedRows++;
                }
            }
            for (String key : keysToRemove) {
                table.getRows().remove(key);
            }
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

    private List<String[]> parseWhereClause(String[] tokens, int startIndex) {
        List<String[]> whereClauseConditions = new ArrayList<>();
    
        for (int i = startIndex; i < tokens.length; i++) {
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
        return whereClauseConditions;
    }
    
}
