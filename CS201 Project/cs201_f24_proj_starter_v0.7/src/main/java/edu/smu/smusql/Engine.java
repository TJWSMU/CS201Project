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
        for (int i = 0; i < row.length; i++) {
            row[i] = row[i].trim();
        }
        if (row.length != table.getColumns().size()) {
            return "ERROR: Incorrect number of columns";
        }

        //Insert the row into table
        table.insert(row);

        return "Row inserted into table " + tableName;
    }
    public String delete(String[] tokens) {
        //TODO
        return "not implemented";
    }

    public String select(String[] tokens) {
        //TODO
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
            String operator = tokens[6];
            String value = tokens[7];
            //Get the index of the column name
            int index = table.getColumns().get(column);
            if (value == null) {
                // Handle the null case, e.g., throw an exception or use a default value
                throw new NullPointerException("Column" + column + " does not exist.");
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
            String operator1 = tokens[6];
            String value1 = tokens[7];
            String logic = tokens[8].toUpperCase();
            String column2 = tokens[9];
            String operator2 = tokens[10];
            String value2 = tokens[11];
            //Get the index of the column name
            int index1 = table.getColumns().get(column1);
            int index2 = table.getColumns().get(column2);

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
        return "not implemented";
    }
    public String create(String[] tokens) {
        //Check if this table already exists
        String tableName = tokens[2];
        if (tableList.containsKey(tableName)) {
            return "ERROR: Table already exists";
        }

        //Check for correct syntax
        if (!tokens[1].toUpperCase().equals("TABLE")) {
            return "ERROR: Invalid CREATE TABLE syntax";
        }

        // Get the columns
        String columns = Parser.queryBetweenParentheses(tokens, 3);
        String[] columnNames = columns.split(",");

        // Add the table to the database
        BinarySearchTree newTable = new BinarySearchTree(columnNames);
        tableList.put(tableName, newTable);

        return "Table " + tableName + " created";
    }

}
