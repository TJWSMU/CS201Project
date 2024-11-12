package edu.smu.smusql;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Engine {
    // Stores the tables in the database
    private ChainHashMap<String, Table> tables = new ChainHashMap<>();

    public String executeSQL(String query) {
        String[] tokens = tokenizeQuery(query);
        if (tokens.length == 0) {
            return "ERROR: Empty query";
        }
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

    // Tokenizer that handles quoted strings and spaces within values
    private String[] tokenizeQuery(String query) {
        List<String> tokensList = new ArrayList<>();
        Matcher matcher = Pattern.compile("\"([^\"]*)\"|'([^']*)'|\\S+").matcher(query);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                tokensList.add(matcher.group(1)); // Double-quoted string
            } else if (matcher.group(2) != null) {
                tokensList.add(matcher.group(2)); // Single-quoted string
            } else {
                tokensList.add(matcher.group());
            }
        }
        return tokensList.toArray(new String[0]);
    }

    public String create(String[] tokens) {
        if (tokens.length < 4 || !tokens[1].equalsIgnoreCase("TABLE")) {
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
        return "Table " + tableName + " created";
    }

    public String insert(String[] tokens) {
        if (tokens.length < 5 || !tokens[1].equalsIgnoreCase("INTO")) {
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
        if (tokens.length < 4 || !tokens[1].equals("*") || !tokens[2].equalsIgnoreCase("FROM")) {
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
        }
    
        boolean usesFirstColumn = false;
        String firstColumnValue = null;
    
        // Determine the name of the first column
        String firstColumn = columns.get(0);
    
        // Check if any condition is on the first column with '=' operator
        for (String[] condition : whereClauseConditions) {
            if (condition[1] != null && condition[1].equals(firstColumn) && condition[2].equals("=")) {
                usesFirstColumn = true;
                firstColumnValue = condition[3];
                break;
            }
        }
    
        List<ChainHashMap<String, String>> filteredRows = new ArrayList<>();
        if (usesFirstColumn) {
            ChainHashMap<String, String> row = table.getRowByFirstColumnValue(firstColumnValue);
            if (row != null && evaluateWhereConditions(row, whereClauseConditions)) {
                filteredRows.add(row);
            }
        } else {
            for (ChainHashMap<String, String> row : table.getRows().values()) {
                if (evaluateWhereConditions(row, whereClauseConditions)) {
                    filteredRows.add(row);
                }
            }
        }
    
        // Sort the filtered rows by the value in the first column
        // filteredRows.sort((row1, row2) -> {
        //     String value1 = row1.getOrDefault(firstColumn, "0");
        //     String value2 = row2.getOrDefault(firstColumn, "0");
    
        //     try {
        //         // Attempt to parse as integers for numerical sorting
        //         int intVal1 = Integer.parseInt(value1);
        //         int intVal2 = Integer.parseInt(value2);
        //         return Integer.compare(intVal1, intVal2);
        //     } catch (NumberFormatException e) {
        //         // Fallback to string comparison if values are not integers
        //         return value1.compareTo(value2);
        //     }
        // });
    
        // Append sorted rows to the result
        for (ChainHashMap<String, String> row : filteredRows) {
            for (String column : columns) {
                result.append(row.getOrDefault(column, "NULL")).append("\t");
            }
            result.append("\n");
        }
        return result.toString();
    }
    

    public String update(String[] tokens) {
        if (tokens.length < 6 || !tokens[2].equalsIgnoreCase("SET")) {
            return "ERROR: Invalid UPDATE syntax";
        }

        String tableName = tokens[1];

        Table table = tables.get(tableName);
        if (table == null) {
            return "Error: no such table: " + tableName;
        }

        // Parse SET clause
        String setClause = "";
        int whereIndex = -1;
        for (int i = 3; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("WHERE")) {
                whereIndex = i;
                break;
            }
            setClause += tokens[i] + " ";
        }
        setClause = setClause.trim();

        String[] setTokens = setClause.split("\\s*=\\s*");
        if (setTokens.length != 2) {
            return "ERROR: Invalid SET syntax";
        }
        String setColumn = setTokens[0];
        String newValue = setTokens[1];

        List<String[]> whereClauseConditions = new ArrayList<>();

        // Parse WHERE clause if present
        if (whereIndex != -1) {
            whereClauseConditions = parseWhereClause(tokens, whereIndex + 1); // Start parsing after "WHERE"
        }

        List<String> columns = table.getColumns();
        int affectedRows = 0;

        // Check if any condition is on the first column with '=' operator
        boolean usesFirstColumn = false;
        String firstColumnValue = null;

        for (String[] condition : whereClauseConditions) {
            if (condition[1] != null && condition[1].equals(columns.get(0)) && condition[2].equals("=")) {
                usesFirstColumn = true;
                firstColumnValue = condition[3];
                break;
            }
        }

        if (usesFirstColumn) {
            ChainHashMap<String, String> row = table.getRowByFirstColumnValue(firstColumnValue);
            if (row != null && evaluateWhereConditions(row, whereClauseConditions)) {
                row.put(setColumn, newValue);
                affectedRows++;
            }
        } else {
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
        if (tokens.length < 3 || !tokens[1].equalsIgnoreCase("FROM")) {
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

        List<String> columns = table.getColumns();

        // Check if any condition is on the first column with '=' operator
        boolean usesFirstColumn = false;
        String firstColumnValue = null;

        for (String[] condition : whereClauseConditions) {
            if (condition[1] != null && condition[1].equals(columns.get(0)) && condition[2].equals("=")) {
                usesFirstColumn = true;
                firstColumnValue = condition[3];
                break;
            }
        }

        if (usesFirstColumn) {
            if (table.getRows().remove(firstColumnValue) != null) {
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
        boolean openParenthesisFound = false;

        for (int i = startIndex; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.startsWith("(")) {
                openParenthesisFound = true;
                token = token.substring(1);
            }
            if (token.endsWith(")")) {
                token = token.substring(0, token.length() - 1);
                result.append(token);
                break;
            }
            result.append(token).append(" ");
        }

        if (!openParenthesisFound) {
            throw new IllegalArgumentException("Parentheses not found in query.");
        }

        return result.toString().trim();
    }

    // Helper method to evaluate a single condition
    private boolean evaluateCondition(String columnValue, String operator, String value) {
        if (columnValue == null) return false;

        // Remove quotes from value if present
        if ((value.startsWith("'") && value.endsWith("'")) || (value.startsWith("\"") && value.endsWith("\""))) {
            value = value.substring(1, value.length() - 1);
        }

        // Compare strings as numbers if possible
        boolean isNumeric = isNumeric(columnValue) && isNumeric(value);
        if (isNumeric) {
            double columnNumber = Double.parseDouble(columnValue);
            double valueNumber = Double.parseDouble(value);

            switch (operator) {
                case "=":
                    return columnNumber == valueNumber;
                case ">":
                    return columnNumber > valueNumber;
                case "<":
                    return columnNumber < valueNumber;
                case ">=":
                    return columnNumber >= valueNumber;
                case "<=":
                    return columnNumber <= valueNumber;
                default:
                    return false;
            }
        } else {
            switch (operator) {
                case "=":
                    return columnValue.equals(value);
                case ">":
                    return columnValue.compareTo(value) > 0;
                case "<":
                    return columnValue.compareTo(value) < 0;
                case ">=":
                    return columnValue.compareTo(value) >= 0;
                case "<=":
                    return columnValue.compareTo(value) <= 0;
                default:
                    return false;
            }
        }
    }

    // Helper method to determine if a string is an operator
    private boolean isOperator(String token) {
        return token.equals("=") || token.equals(">") || token.equals("<") || token.equals(">=") || token.equals("<=");
    }

    // Helper method to determine if a string is numeric
    private boolean isNumeric(String str) {
        if (str == null) return false;
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
        int i = startIndex;

        while (i < tokens.length) {
            String token = tokens[i];

            if (token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR")) {
                whereClauseConditions.add(new String[]{token.toUpperCase(), null, null, null});
                i++;
            } else {
                String column = token;
                i++;
                if (i >= tokens.length || !isOperator(tokens[i])) {
                    throw new IllegalArgumentException("Invalid WHERE clause syntax near: " + column);
                }
                String operator = tokens[i];
                i++;
                if (i >= tokens.length) {
                    throw new IllegalArgumentException("Missing value after operator in WHERE clause.");
                }
                String value = tokens[i];
                whereClauseConditions.add(new String[]{null, column, operator, value});
                i++;
            }
        }
        return whereClauseConditions;
    }
}
