package edu.smu.smusql;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Engine {
    // Stores the tables in the database
    private Map<String, Table> tables = new HashMap<>();

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

        String columnList = queryBetweenParentheses(tokens, 3); // Get column definitions between parentheses
        List<String> columnDefs = Arrays.asList(columnList.split(","));
        columnDefs.replaceAll(String::trim);

        List<String> columns = new ArrayList<>();
        Map<String, String> columnTypes = new HashMap<>();

        for (String columnDef : columnDefs) {
            String[] parts = columnDef.trim().split("\\s+");
            if (parts.length != 2) {
                return "ERROR: Invalid column definition: " + columnDef;
            }
            String columnName = parts[0];
            String columnType = parts[1].toUpperCase();
            if (!"INTEGER".equals(columnType) && !"STRING".equals(columnType) && !"DOUBLE".equals(columnType)) {
                return "ERROR: Unsupported column type: " + columnType;
            }
            columns.add(columnName);
            columnTypes.put(columnName, columnType);
        }

        Table newTable = new Table(tableName, columns, columnTypes);
        tables.put(tableName, newTable);

        return "Table " + tableName + " created";
    }

    public String insert(String[] tokens) {
        // Check minimum length and syntax
        if (tokens == null || tokens.length < 5 || !tokens[1].equalsIgnoreCase("INTO")) {
            return "ERROR: Invalid INSERT INTO syntax";
        }
    
        // Check table name
        String tableName = tokens[2];
        if (tableName == null || tableName.isEmpty()) {
            return "ERROR: Table name is missing in INSERT statement";
        }
    
        // Retrieve table
        Table table = tables.get(tableName);
        if (table == null) {
            return "ERROR: No such table: " + tableName;
        }
    
        // Get values list between parentheses
        String valueList;
        try {
            valueList = queryBetweenParentheses(tokens, 4);
        } catch (IllegalArgumentException e) {
            return "ERROR: Values list is missing or improperly formatted"; 
        }

        if (valueList == null || valueList.isEmpty()) {
            return "ERROR: Values list is missing or improperly formatted";
        }
    
        // Split values and trim each one
        List<String> values = Arrays.asList(valueList.split(","));
        values.replaceAll(String::trim);
    
        // Retrieve column list from the table
        List<String> columns = table.getColumns();
        if (values.size() != columns.size()) {
            return "ERROR: Column count (" + columns.size() + ") doesn't match value count (" + values.size() + ")";
        }
    
        // Create a new row and validate each value based on column type
        Map<String, String> row = new HashMap<>();
        Map<String, String> columnTypes = table.getColumnTypes();
        
        for (int i = 0; i < columns.size(); i++) {
            String columnName = columns.get(i);
            String columnType = columnTypes.get(columnName);
            String value = values.get(i).replaceAll("['\"]", ""); // Remove surrounding quotes
    
            // Validate data type if specified
            if (columnType != null) {
                switch (columnType.toUpperCase()) {
                    case "INTEGER":
                        try {
                            Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            return "ERROR: Invalid integer value for column " + columnName + ": " + value;
                        }
                        break;
                    case "DOUBLE":
                        try {
                            Double.parseDouble(value);
                        } catch (NumberFormatException e) {
                            return "ERROR: Invalid double value for column " + columnName + ": " + value;
                        }
                        break;
                    case "STRING":
                        // Strings require no conversion; just ensure they're not null
                        if (value == null) {
                            return "ERROR: Null value for STRING column " + columnName;
                        }
                        break;
                    default:
                        return "ERROR: Unsupported data type for column " + columnName;
                }
            }
    
            row.put(columnName, value);
        }
    
        // Add the row to the table
        return table.addRow(row);
    }
    
    public String select(String[] tokens) {
        if (tokens.length < 4 || !tokens[2].equalsIgnoreCase("FROM")) {
            return "ERROR: Invalid SELECT syntax";
        }

        String columnsToSelect = tokens[1];
        String tableName = tokens[3];

        // Retrieve table
        Table table = tables.get(tableName);
        if (table == null) {
            return "ERROR: No such table: " + tableName;
        }

        List<String> columns;
        if (columnsToSelect.equals("*")) {
            columns = table.getColumns();
        } else {
            columns = Arrays.asList(columnsToSelect.split(","));
            columns.replaceAll(String::trim);
        }

        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columns)).append("\n"); // Print column headers

        List<String[]> whereClauseConditions = new ArrayList<>();

        // Parse WHERE clause if present
        if (tokens.length > 4 && tokens[4].equalsIgnoreCase("WHERE")) {
            whereClauseConditions = parseWhereClause(tokens, 5); // Start parsing after "WHERE"
        }

        // Evaluate WHERE conditions
        List<Map<String, String>> filteredRows = evaluateWhereConditions(table, whereClauseConditions);

        // Append filtered rows to the result
        for (Map<String, String> row : filteredRows) {
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
    
        // Retrieve table
        Table table = tables.get(tableName);
        if (table == null) {
            return "ERROR: No such table: " + tableName;
        }
    
        // Parse SET clause
        int whereIndex = -1;
        StringBuilder setClauseBuilder = new StringBuilder();
        for (int i = 3; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("WHERE")) {
                whereIndex = i;
                break;
            }
            setClauseBuilder.append(tokens[i]).append(" ");
        }
        String setClause = setClauseBuilder.toString().trim();
    
        // Parse SET expressions (can have multiple assignments)
        Map<String, String> setExpressions = new HashMap<>();
        String[] assignments = setClause.split(",");
        for (String assignment : assignments) {
            String[] parts = assignment.split("=");
            if (parts.length != 2) {
                return "ERROR: Invalid SET syntax in assignment: " + assignment;
            }
            String column = parts[0].trim();
            String value = parts[1].trim().replaceAll("['\"]", "");
            setExpressions.put(column, value);
        }
    
        // Validate columns in SET expressions
        List<String> validColumns = table.getColumns();
        for (String column : setExpressions.keySet()) {
            if (!validColumns.contains(column)) {
                return "ERROR: Invalid column '" + column + "' in SET clause.";
            }
        }
    
        List<String[]> whereClauseConditions = new ArrayList<>();
    
        // Parse WHERE clause if present
        if (whereIndex != -1) {
            whereClauseConditions = parseWhereClause(tokens, whereIndex + 1); // Start parsing after "WHERE"
        }
    
        // Evaluate WHERE conditions
        List<Map<String, String>> filteredRows = evaluateWhereConditions(table, whereClauseConditions);
        if (filteredRows.isEmpty()) {
            return "No rows match the specified WHERE conditions in table " + tableName + ".";
        }
    
        // Update rows using the updateRow method
        int affectedRows = 0;
        for (Map<String, String> row : filteredRows) {
            table.updateRow(row, setExpressions);
            affectedRows++;
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
            return "ERROR: No such table: " + tableName;
        }

        List<String[]> whereClauseConditions = new ArrayList<>();
        int affectedRows = 0;

        // Parse WHERE clause if present
        if (tokens.length > 3 && tokens[3].equalsIgnoreCase("WHERE")) {
            whereClauseConditions = parseWhereClause(tokens, 4); // Start parsing after "WHERE"
        }

        // Evaluate WHERE conditions
        List<Map<String, String>> rowsToDelete = evaluateWhereConditions(table, whereClauseConditions);

        // Delete rows and update indices
        for (Map<String, String> row : rowsToDelete) {
            table.deleteRow(row);
            affectedRows++;
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

    // Helper method to parse WHERE clause
    private List<String[]> parseWhereClause(String[] tokens, int startIndex) {
        List<String[]> whereClauseConditions = new ArrayList<>();
        String logicalOperator = null; // Track the last logical operator
    
        int i = startIndex;
        while (i < tokens.length) {
            String token = tokens[i];
    
            // Check for logical operators
            if (token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR")) {
                logicalOperator = token.toUpperCase();
                i++;
            } else {
                // Parse condition: column, operator, and value
                if (i + 2 >= tokens.length) {
                    throw new IllegalArgumentException("Invalid WHERE clause syntax near: " + token);
                }
                String column = token;
                String operator = tokens[i + 1];
                String value = tokens[i + 2].replaceAll("['\"]", ""); // Remove quotes from value
    
                // Verify the operator is valid
                if (!isOperator(operator)) {
                    throw new IllegalArgumentException("Invalid operator in WHERE clause: " + operator);
                }
    
                // Add the parsed condition with the logical operator
                whereClauseConditions.add(new String[]{logicalOperator, column, operator, value});
    
                // Reset logical operator for the next condition
                logicalOperator = null;
    
                // Move index past this condition (3 tokens)
                i += 3;
            }
        }
    
        // Handle cases where a logical operator is missing or misplaced
        if (whereClauseConditions.isEmpty()) {
            System.err.println("ERROR: No valid conditions found in WHERE clause.");
        } else if (whereClauseConditions.get(0)[0] == null) {
            whereClauseConditions.get(0)[0] = "AND"; // Default to AND for the first condition if no operator is specified
        }
    
        return whereClauseConditions;
    }
    

    // Helper method to determine if a string is an operator
    private boolean isOperator(String token) {
        return token.equals("=") || token.equals("!=") || token.equals(">") || token.equals("<") || token.equals(">=") || token.equals("<=");
    }
    
    // Helper method to evaluate WHERE conditions and return matching rows
    private List<Map<String, String>> evaluateWhereConditions(Table table, List<String[]> whereClauseConditions) {
        if (whereClauseConditions.isEmpty()) {
            return new ArrayList<>(table.getRows());
        }
    
        // Check if this is a range query
        if (isRangeQuery(whereClauseConditions)) {
            String column = whereClauseConditions.get(0)[1];
            String type = table.getColumnType(column);
            
            if (type.equalsIgnoreCase("INTEGER") || type.equalsIgnoreCase("DOUBLE")) {
                Number min, max;
                String val1 = whereClauseConditions.get(0)[3];
                String val2 = whereClauseConditions.get(1)[3];
                
                if (type.equalsIgnoreCase("INTEGER")) {
                    min = Integer.parseInt(val1);
                    max = Integer.parseInt(val2);
                } else {
                    min = Double.parseDouble(val1);
                    max = Double.parseDouble(val2);
                }
                
                // If operators are reversed, swap min and max
                if (whereClauseConditions.get(0)[2].equals("<") || 
                    whereClauseConditions.get(0)[2].equals("<=")) {
                    Number temp = min;
                    min = max;
                    max = temp;
                }
                
                // Handle inclusive/exclusive bounds
                String op1 = whereClauseConditions.get(0)[2];
                String op2 = whereClauseConditions.get(1)[2];
                
                // Adjust bounds based on operator types
                if (type.equalsIgnoreCase("INTEGER")) {
                    if (op1.equals(">")) min = ((Integer)min) + 1;
                    if (op2.equals("<")) max = ((Integer)max) - 1;
                } else {
                    if (op1.equals(">")) min = ((Double)min) + 0.000001;
                    if (op2.equals("<")) max = ((Double)max) - 0.000001;
                }
                
                return table.getRowsByConditionInRange(column, min, max);
            }
        }
    
        // Original implementation for non-range queries
        List<Map<String, String>> result = null;

        for (int i = 0; i < whereClauseConditions.size(); i++) {
            String[] condition = whereClauseConditions.get(i);
            String logicalOp = condition[0];  // AND/OR
            String column = condition[1];
            String operator = condition[2];
            String value = condition[3];
    
            List<Map<String, String>> currentResult = table.getRowsByCondition(column, operator, value);
    
            if (result == null) {
                result = new ArrayList<>(currentResult);
            } else {
                // Use the logical operator from the current condition
                if ("AND".equals(logicalOp)) {
                    result.retainAll(currentResult);  // Intersection
                } else if ("OR".equals(logicalOp)) {
                    // For OR, we need to add all elements from currentResult that aren't already in result
                    Set<Map<String, String>> resultSet = new HashSet<>(result);
                    for (Map<String, String> row : currentResult) {
                        if (!resultSet.contains(row)) {
                            result.add(row);
                        }
                    }
                }
            }
        }
    
        return result != null ? result : new ArrayList<>();
    }

    private boolean isRangeQuery(List<String[]> conditions) {
        if (conditions.size() != 2) return false;
        
        String column1 = conditions.get(0)[1];
        String column2 = conditions.get(1)[1];
        
        if (!column1.equals(column2)) return false;
        
        String op1 = conditions.get(0)[2];
        String op2 = conditions.get(1)[2];
        
        return (op1.equals(">") && op2.equals("<")) || 
               (op1.equals("<") && op2.equals(">")) ||
               (op1.equals(">=") && op2.equals("<=")) ||
               (op1.equals("<=") && op2.equals(">="));
    }
}
