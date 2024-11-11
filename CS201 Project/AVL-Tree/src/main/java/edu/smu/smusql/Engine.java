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
        // Get the tree with the correct name
        // Check if second word is TABLE
        if (!tokens[1].equalsIgnoreCase("TABLE")) {
            return "ERROR: Invalid CREATE TABLE syntax";
        }

        // Check if the table already exists
        String tableName = tokens[2];
        if (tableMap.containsKey(tableName)) {
            return "ERROR: Table already exists";
        }

        return "not implemented";

        // INSERT INTO student VALUES (1, John, 20, 3.5, True)
        // INSERT INTO student VALUES (2, Mary, 22, 3.8, True)
        // INSERT INTO student VALUES (3, Peter, 19, 2.9, False)
    }

    public String delete(String[] tokens) {
        //TODO
        return "not implemented";

        // DELETE FROM student WHERE id = 1
        // DELETE FROM student WHERE gpa < 2.0
        // DELETE FROM student WHERE age > 25 AND deans_list = False
    }

    public String select(String[] tokens) {
        //TODO
        return "not implemented";

        // SELECT * FROM student
        // SELECT * FROM student WHERE gpa > 3.5
        // SELECT * FROM student WHERE age < 21 AND gpa > 3.0
        // SELECT * FROM student WHERE gpa < 2.0 OR deans_list = True
    }

    public String update(String[] tokens) {
        //TODO
        return "not implemented";

        // UPDATE student SET age = 21 WHERE id = 1
        // UPDATE student SET deans_list = True WHERE gpa >= 3.5
        // UPDATE student SET gpa = 4.0 WHERE name = John AND age > 20
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

        String columnList = queryBetweenParentheses(tokens, 3); // Get column list between parentheses
        List<String> columns = Arrays.asList(columnList.split(","));

        // Trim each column name to avoid spaces around them
        columns.replaceAll(String::trim);

        // Map the table name to the tree
        tableMap.put(tableName, new AVLTree(columns));

        return "Table " + tableName + " created";

        // CREATE TABLE student (id, name, age, gpa, deans_list)
        // CREATE TABLE employee (id, name, salary, department, hire_date)
        // CREATE TABLE course (code, name, credits, instructor, capacity)
    }

    // Helper method to extract content inside parentheses
    private String queryBetweenParentheses(String[] tokens, int startIndex) {
        StringBuilder result = new StringBuilder();
        for (int i = startIndex; i < tokens.length; i++) {
            result.append(tokens[i]).append(" ");
        }
        return result.toString().trim().replaceAll("\\(", "").replaceAll("\\)", "");
    }

}
