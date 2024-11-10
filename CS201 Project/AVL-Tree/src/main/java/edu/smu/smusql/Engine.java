package edu.smu.smusql;

import java.util.*;

public class Engine {

    // stores the contents of database tables in-memory
    private Map<String, AVLTree> tableMap = new HashMap<>();

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
        // String tableName = tokens[2]; // The name of the table to be inserted into.
        // String valueList = queryBetweenParentheses(tokens, 4); // Get values list between parentheses
        // List<String> values = Arrays.asList(valueList.split(",")); // These are the values in the row to be inserted.

        // System.out.println(valueList);
        return "not implemented";
    }
    public String delete(String[] tokens) {
        //TODO
        return "not implemented";
    }

    public String select(String[] tokens) {
        //TODO
        return "not implemented";
    }
    public String update(String[] tokens) {
        //TODO
        return "not implemented";
    }
    public String create(String[] tokens) {
        // Check if second word is TABLE
        if (!tokens[1].equalsIgnoreCase("TABLE")) {
            return "ERROR: Invalid CREATE TABLE syntax";
        }

        // Check if the table already exists
        String tableName = tokens[2];
        if (tableMap.containsKey(tableName)) {
            return "ERROR: Table already exists";
        }
        
        // Map the table name to the tree
        tableMap.put(tableName, new AVLTree());

        // CREATE TABLE users (id, name, age, city)
        // CREATE TABLE products (id, name, price, category)
        // CREATE TABLE orders (id, user_id, product_id, quantity)

        System.out.println(tableMap);

        return "Table " + tableName + " created";
    }

}
