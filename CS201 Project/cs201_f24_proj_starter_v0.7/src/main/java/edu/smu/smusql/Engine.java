package edu.smu.smusql;

import java.util.HashMap;
import java.util.Map;

public class Engine {
    //Stores all the tables in the database with the table name as the key
    private Map<String, BinarySearchTree> tableList = new HashMap<>();

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

    public String insert(String[] tokens) {
        //Check for the correct syntax
        if (!tokens[1].toUpperCase().equals("INTO") || 
            !tokens[3].toUpperCase().equals("VALUES")) {
            return "ERROR: Invalid INSERT INTO syntax";
        }

        //Get the table
        String tableName = tokens[2];
        BinarySearchTree table = tableList.get(tableName);
        if (table == null) {
            return "ERROR: Table does not exist";
        }

        //Ensure the input has the correct number of columns
        String values = Parser.queryBetweenParentheses(tokens, 4);
        String[] row = values.split(",");
        if (row.length != table.getColumns().size()) {
            return "ERROR: Incorrect number of columns";
        }
        //Remove leading and trailing whitespace and single quotes from each value
        for (int i = 0; i < row.length; i++) {
            row[i] = row[i].trim();
            row[i] = removeSingleQuotes(row[i]);
        }
        
        //Insert the row into table
        table.insert(row);

        return "Row inserted into " + tableName;
    }

    private String removeSingleQuotes(String value) {
        if (value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
    //Delete from table where column = value || && column = value
    public String delete(String[] tokens) {
        //Check for correct syntax
        if (!tokens[1].toUpperCase().equals("FROM") || 
            !tokens[3].toUpperCase().equals("WHERE")) {
            return "ERROR: Invalid DELETE syntax";
        }

        //Get the table
        String tableName = tokens[2];
        BinarySearchTree table = tableList.get(tableName);
        if (table == null) {
            return "ERROR: Table does not exist";
        }

        //Get the column name and value
        String column = tokens[4];
        column = removeSingleQuotes(column);
        String operator = tokens[5];
        String value = tokens[6];
        value = removeSingleQuotes(value);
        //Get the index of the column name
        int index = table.getColumns().getOrDefault(column, -1);
        if (index == -1) {
            return "ERROR: Column does not exist";
        }

        //Store the number of rows deleted
        int rowsDeleted = 0;

        if (tokens.length == 7) {
            rowsDeleted = table.delete(index, value, operator, null, -1, null, null);
        }

        if (tokens.length == 11) {
            String logic = tokens[7].toUpperCase();
            String column2 = tokens[8];
            column2 = removeSingleQuotes(column2);
            String operator2 = tokens[9];
            String value2 = tokens[10];
            value2 = removeSingleQuotes(value2);
            //Get the index of the column name
            int index2 = table.getColumns().getOrDefault(column2, -1);
            if (index2 == -1) {
                return "ERROR: Column does not exist";
            }

            switch (logic) {
                case "AND":
                    rowsDeleted = table.delete(index, value, operator, "AND", index2, value2, operator2);
                    break;
                case "OR":
                    rowsDeleted = table.delete(index, value, operator,"OR", index2, value2, operator2);
                    break;
                default:
                    return "ERROR: Invalid logic operator";
            }
        }

        return "Rows deleted from " + tableName + ". " + rowsDeleted + " rows affected.";

    }

    public String select(String[] tokens) {
        //Check for correct syntax
        if (tokens.length < 4 ||
            !tokens[1].equals("*") ||
            !tokens[2].toUpperCase().equals("FROM") ||
            ((tokens.length == 8 || tokens.length == 12) &&
            !tokens[4].toUpperCase().equals("WHERE"))) {
            return "ERROR: Invalid SELECT syntax";
        }

        //Get the table
        String tableName = tokens[3];
        BinarySearchTree table = tableList.get(tableName);

        //Return error message if table does not exist
        if (table == null) {
            return "ERROR: Table does not exist";
        }

        //Return the whole table if no WHERE clause
        if (tokens.length == 4) {
            return table.toString();

        //If token.length == 8, then there is 1 conditional
        } else if (tokens.length == 8) {
            String column = tokens[5];
            column = removeSingleQuotes(column);
            String operator = tokens[6];
            String value = tokens[7];
            value = removeSingleQuotes(value);
            //Get the index of the column name
            int index = table.getColumns().getOrDefault(column, -1);
            if (index == -1) {
                return "ERROR: Column does not exist";
            }

            switch (operator) {
                case "=":
                    return table.searchAndPrintEquals(index, value);
                case "<":
                    return table.searchAndPrintLessThan(index, value);
                case ">":
                    return table.searchAndPrintMoreThan(index, value);
                case "<=":
                    return table.searchAndPrintLessThanOrEqual(index, value);
                case ">=":
                    return table.searchAndPrintMoreThanOrEqual(index, value);
            
                default:
                    return "ERROR: Invalid operator";
            }
        } else if (tokens.length == 12) {
            String column1 = tokens[5];
            column1 = removeSingleQuotes(column1);
            String operator1 = tokens[6];
            String value1 = tokens[7];
            value1 = removeSingleQuotes(value1);
            String logic = tokens[8].toUpperCase();
            String column2 = tokens[9];
            column2 = removeSingleQuotes(column2);
            String operator2 = tokens[10];
            String value2 = tokens[11];
            value2 = removeSingleQuotes(value2);
            //Get the index of the column name
            int index1 = table.getColumns().getOrDefault(column1, -1);
            int index2 = table.getColumns().getOrDefault(column2, -1);
            if (index1 == -1 || index2 == -1) {
                return "ERROR: Column does not exist";
            }

            switch (logic) {
                case "AND":
                    return table.searchAndPrintAnd(index1, operator1, value1, index2, operator2, value2);
                case "OR":
                    return table.searchAndPrintOr(index1, operator1, value1, index2, operator2, value2);
                default:
                    return "ERROR: Invalid logic operator";
            }
        }

        return "ERROR: Invalid SELECT syntax";
    }

    public String update(String[] tokens) {
        //TODO
        //Check for correct syntax
        if (!tokens[2].toUpperCase().equals("SET") ||
            !tokens[4].equals("=") || 
            !tokens[6].toUpperCase().equals("WHERE") ||
            tokens.length < 10) {
            return "ERROR: Invalid UPDATE syntax";
        }

        //Get the table
        String tableName = tokens[1];
        BinarySearchTree table = tableList.get(tableName);
        if (table == null) {
            return "ERROR: Table does not exist";
        }

        //Check the column to be updated
        String updateColumn = tokens[3];
        int index = table.getColumns().getOrDefault(updateColumn, -1);
        if (index == -1) {
            return "ERROR: Column does not exist";
        }
        //Get the new value
        String newValue = tokens[5];
        newValue = removeSingleQuotes(newValue);

        //Get the column name and value that needs to be checked
        String column = tokens[7];
        column = removeSingleQuotes(column);
        String operator = tokens[8];
        String value = tokens[9];
        value = removeSingleQuotes(value);

        //Get the index of the column name
        int index1 = table.getColumns().getOrDefault(column, -1);
        if (index1 == -1) {
            return "ERROR: Column does not exist";
        }

        //Store the number of rows updated
        int rowsUpdated = 0;

        if (tokens.length == 10) {
            rowsUpdated = table.update(index, newValue, index1, value, operator, null, -1, null, null);
        } else if (tokens.length == 14) {
            String logic = tokens[10].toUpperCase();
            String column2 = tokens[11];
            column2 = removeSingleQuotes(column2);
            String operator2 = tokens[12];
            String value2 = tokens[13];
            value2 = removeSingleQuotes(value2);
            //Get the index of the column name
            int index2 = table.getColumns().getOrDefault(column2, -1);
            if (index2 == -1) {
                return "ERROR: Column does not exist";
            }

            switch (logic) {
                case "AND":
                    rowsUpdated = table.update(index, newValue, index1, value, operator, "AND", index2, value2, operator2);
                    break;
                case "OR":
                    rowsUpdated = table.update(index, newValue, index1, value, operator, "OR", index2, value2, operator2);
                    break;
                default:
                    return "ERROR: Invalid logic operator";
            }
        }

        
        return "Table " + tableName + " updated. " + rowsUpdated + " rows affected.";
    }
    public String create(String[] tokens) {

        //Check for correct syntax
        if (!tokens[1].toUpperCase().equals("TABLE")) {
            return "ERROR: Invalid CREATE TABLE syntax";
        }

        //Check if this table already exists
        String tableName = tokens[2];
        if (tableList.containsKey(tableName)) {
            return "ERROR: Table already exists";
        }

        // Get the columns and trim whitespace
        String columns = Parser.queryBetweenParentheses(tokens, 3);
        String[] columnNames = columns.split(",");
        for (int i = 0; i < columnNames.length; i++) {
            columnNames[i] = columnNames[i].trim();
        }

        // Add the table to the database
        BinarySearchTree newTable = new BinarySearchTree(columnNames);
        tableList.put(tableName, newTable);

        return "Table " + tableName + " created";
    }

}
