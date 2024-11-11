package edu.smu.smusql;

import java.util.*;

import edu.smu.smusql.AVLTree.*;


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
        if (!tokens[1].equalsIgnoreCase("into")) {
            return "ERROR: Invalid INSERT INTO syntax";
        }

        // Check if the table already exists
        String tableName = tokens[2];
        if (!tableMap.containsKey(tableName)) {
            return "ERROR: Table does not exists";
        }

        // Split the input
        String column = queryBetweenParentheses(tokens, 3); // Get column list between parentheses
        List<String> columnList = Arrays.asList(column.split(","));
        
        // Trim each column name and remove "VALUES" if present
        columnList.replaceAll(s -> s.trim().replace("VALUES", "").trim());

        // Get the table
        AVLTree table = tableMap.get(tableName);
        List<String> colKeys = table.getColumns();

        // Create a Map of data and populate the map
        Map<String, String> data = new HashMap<>();

        for (int i = 0; i < colKeys.size(); i++) {
            data.put(colKeys.get(i), columnList.get(i));
        }
        
        // Insert data into the AVL tree
        table.insert(data);

        // Testing code
        // table.printTree();

        return "Row inserted into table " + tableName;

        // INSERT INTO student VALUES (1, John, 20, 3.5, True)
        // INSERT INTO student VALUES (2, Mary, 22, 3.8, True)
        // INSERT INTO student VALUES (3, Peter, 19, 2.9, False)
    }

    // DELETE FROM student WHERE id = 1
    // DELETE FROM student WHERE gpa < 2.0
    // DELETE FROM student WHERE age > 25 AND deans_list = False
    public String delete(String[] tokens) {
        // Check if second word is FROM
        if (!tokens[1].equalsIgnoreCase("FROM")) {
            return "ERROR: Invalid DELETE FROM syntax";
        }
    
        // Check if the table exists
        String tableName = tokens[2];
        if (!tableMap.containsKey(tableName)) {
            return "ERROR: Table does not exist";
        }
    
        // Get the table
        AVLTree table = tableMap.get(tableName);
        
        // Check if WHERE clause exists - it must exist for safety
        if (tokens.length <= 3 || !tokens[3].equalsIgnoreCase("WHERE")) {
            return "ERROR: DELETE must include WHERE clause";
        }
    
        // Find nodes to delete
        List<Map<String, String>> nodesToDelete = new ArrayList<>();
        traverseAndFilter(table.root, nodesToDelete, tokens, 4);
    
        // No matching rows
        if (nodesToDelete.isEmpty()) {
            return "No rows matched the delete condition";
        }
    
        // Delete each matching node
        int deletedCount = 0;
        for (Map<String, String> nodeData : nodesToDelete) {
            boolean deleted = deleteNode(table, nodeData);
            if (deleted) deletedCount++;
        }
    
        return "Deleted " + deletedCount + " rows from table " + tableName;
    }
    
    private boolean deleteNode(AVLTree table, Map<String, String> nodeData) {
        AVLTree.Node parent = null;
        AVLTree.Node current = table.root;
        String idColumn = table.getColumns().get(0);
        String targetId = nodeData.get(idColumn);
    
        // Find the node to delete and its parent
        while (current != null) {
            String currentId = current.columnData.get(idColumn);
            int comparison = targetId.compareTo(currentId);
            
            if (comparison == 0) {
                break; // Found the node to delete
            }
            
            parent = current;
            if (comparison < 0) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
    
        if (current == null) {
            return false; // Node not found
        }
    
        // Case 1: Node has no children
        if (current.left == null && current.right == null) {
            if (parent == null) {
                table.root = null;
            } else if (parent.left == current) {
                parent.left = null;
            } else {
                parent.right = null;
            }
        }
        // Case 2: Node has only right child
        else if (current.left == null) {
            if (parent == null) {
                table.root = current.right;
            } else if (parent.left == current) {
                parent.left = current.right;
            } else {
                parent.right = current.right;
            }
        }
        // Case 3: Node has only left child
        else if (current.right == null) {
            if (parent == null) {
                table.root = current.left;
            } else if (parent.left == current) {
                parent.left = current.left;
            } else {
                parent.right = current.left;
            }
        }
        // Case 4: Node has both children
        else {
            // Find the inorder successor (smallest value in right subtree)
            AVLTree.Node successorParent = current;
            AVLTree.Node successor = current.right;
            
            while (successor.left != null) {
                successorParent = successor;
                successor = successor.left;
            }
            
            // Replace current's data with successor's data
            current.columnData = successor.columnData;
            
            // Delete the successor
            if (successorParent == current) {
                successorParent.right = successor.right;
            } else {
                successorParent.left = successor.right;
            }
        }
    
        // After deletion, rebalance the tree starting from the parent node
        if (parent != null) {
            updateTreeBalance(table, parent);
        }
        
        return true;
    }
    
    private void updateTreeBalance(AVLTree table, AVLTree.Node start) {
        AVLTree.Node current = start;
        
        while (current != null) {
            // Update height
            int leftHeight = getHeight(current.left);
            int rightHeight = getHeight(current.right);
            current.height = 1 + Math.max(leftHeight, rightHeight);
            
            // Get balance factor
            int balance = leftHeight - rightHeight;
            
            // Left heavy
            if (balance > 1) {
                int leftChildBalance = getHeight(current.left.left) - getHeight(current.left.right);
                if (leftChildBalance < 0) {
                    current = table.LRRotate(current);
                } else {
                    current = table.LLRotate(current);
                }
            }
            // Right heavy
            else if (balance < -1) {
                int rightChildBalance = getHeight(current.right.left) - getHeight(current.right.right);
                if (rightChildBalance > 0) {
                    current = table.RLRotate(current);
                } else {
                    current = table.RRRotate(current);
                }
            }
            
            // Move up to parent
            current = findParent(table.root, current);
        }
    }
    
    private AVLTree.Node findParent(AVLTree.Node root, AVLTree.Node node) {
        if (root == null || node == null || root == node) {
            return null;
        }
        
        if ((root.left != null && root.left == node) || 
            (root.right != null && root.right == node)) {
            return root;
        }
        
        AVLTree.Node found = findParent(root.left, node);
        if (found == null) {
            found = findParent(root.right, node);
        }
        return found;
    }
    
    private int getHeight(AVLTree.Node node) {
        return node == null ? 0 : node.height;
    }

    public String select(String[] tokens) {
        // Check if second word is FROM
        if (!tokens[2].equalsIgnoreCase("FROM")) {
            return "ERROR: Invalid SELECT syntax";
        }
    
        // Check if the table exists
        String tableName = tokens[3];
        if (!tableMap.containsKey(tableName)) {
            return "ERROR: Table does not exist";
        }
    
        // Get the table
        AVLTree table = tableMap.get(tableName);
        List<String> colKeys = table.getColumns();
    
        // Initialize result StringBuilder
        StringBuilder result = new StringBuilder();
        
        // Add header row
        result.append(String.join(" | ", colKeys)).append("\n");
        result.append("-".repeat(result.length())).append("\n");
    
        // Check if WHERE clause exists
        boolean hasWhere = tokens.length > 4 && tokens[4].equalsIgnoreCase("WHERE");
        
        // Store the results
        List<Map<String, String>> matchingRows = new ArrayList<>();
        
        // Helper function to traverse tree and apply conditions
        traverseAndFilter(table.root, matchingRows, tokens, hasWhere ? 5 : -1);
    
        // Add matching rows to result
        for (Map<String, String> row : matchingRows) {
            List<String> rowValues = new ArrayList<>();
            for (String col : colKeys) {
                rowValues.add(row.get(col));
            }
            result.append(String.join(" | ", rowValues)).append("\n");
        }
    
        return result.toString();
    }
    
    private void traverseAndFilter(AVLTree.Node node, List<Map<String, String>> results, 
                                 String[] tokens, int whereStartIndex) {
        if (node == null) return;
    
        // In-order traversal to maintain sorted order
        traverseAndFilter(node.left, results, tokens, whereStartIndex);
        
        // If no WHERE clause, add all rows
        if (whereStartIndex == -1) {
            results.add(node.columnData);
        } else {
            // Process WHERE conditions
            if (evaluateConditions(node.columnData, tokens, whereStartIndex)) {
                results.add(node.columnData);
            }
        }
        
        traverseAndFilter(node.right, results, tokens, whereStartIndex);
    }
    
    private boolean evaluateConditions(Map<String, String> row, String[] tokens, int startIndex) {
        // Parse the WHERE clause
        List<String> conditions = new ArrayList<>();
        StringBuilder currentCondition = new StringBuilder();
        
        for (int i = startIndex; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR")) {
                conditions.add(currentCondition.toString().trim());
                conditions.add(token);
                currentCondition = new StringBuilder();
            } else {
                currentCondition.append(token).append(" ");
            }
        }
        conditions.add(currentCondition.toString().trim());
    
        // Evaluate conditions
        boolean result = evaluateSingleCondition(row, conditions.get(0));
        
        for (int i = 1; i < conditions.size(); i += 2) {
            String operator = conditions.get(i);
            boolean nextResult = evaluateSingleCondition(row, conditions.get(i + 1));
            
            if (operator.equalsIgnoreCase("AND")) {
                result = result && nextResult;
            } else if (operator.equalsIgnoreCase("OR")) {
                result = result || nextResult;
            }
        }
        
        return result;
    }
    
    private boolean evaluateSingleCondition(Map<String, String> row, String condition) {
        String[] parts = condition.split("\\s+");
        String column = parts[0];
        String operator = parts[1];
        String value = parts[2].replace("VALUES", "").trim(); // Remove "VALUES" if present
        
        String rowValue = row.get(column);
        
        // Special handling for ID column
        if (column.equalsIgnoreCase("id")) {
            // Compare as integers for ID field
            int numValue = Integer.parseInt(value);
            int numRowValue = Integer.parseInt(rowValue);
            
            switch (operator) {
                case ">": return numRowValue > numValue;
                case "<": return numRowValue < numValue;
                case ">=": return numRowValue >= numValue;
                case "<=": return numRowValue <= numValue;
                case "=": return numRowValue == numValue;
                default: return false;
            }
        }
        
        // Handle other data types as before
        try {
            // Try numeric comparison
            double numValue = Double.parseDouble(value);
            double numRowValue = Double.parseDouble(rowValue);
            
            switch (operator) {
                case ">": return numRowValue > numValue;
                case "<": return numRowValue < numValue;
                case ">=": return numRowValue >= numValue;
                case "<=": return numRowValue <= numValue;
                case "=": return numRowValue == numValue;
                default: return false;
            }
        } catch (NumberFormatException e) {
            // String or boolean comparison
            if (value.equalsIgnoreCase("True") || value.equalsIgnoreCase("False")) {
                boolean boolValue = Boolean.parseBoolean(value);
                boolean boolRowValue = Boolean.parseBoolean(rowValue);
                return operator.equals("=") ? boolValue == boolRowValue : boolValue != boolRowValue;
            } else {
                // String comparison
                return operator.equals("=") ? rowValue.equals(value) : !rowValue.equals(value);
            }
        }
    }

    public String update(String[] tokens) {
        // Check if syntax is correct (UPDATE table SET column = value WHERE condition)
        if (tokens.length < 7 || !tokens[2].equalsIgnoreCase("SET")) {
            return "ERROR: Invalid UPDATE syntax";
        }

        // Get table name and check if it exists
        String tableName = tokens[1];
        if (!tableMap.containsKey(tableName)) {
            return "ERROR: Table does not exist";
        }

        // Get the table
        AVLTree table = tableMap.get(tableName);

        // Find SET clause (column = value)
        String updateColumn = tokens[3];
        if (!tokens[4].equals("=")) {
            return "ERROR: Invalid SET clause syntax";
        }
        String updateValue = tokens[5];

        // Check if WHERE clause exists - it must exist for safety
        if (tokens.length <= 6 || !tokens[6].equalsIgnoreCase("WHERE")) {
            return "ERROR: UPDATE must include WHERE clause";
        }

        // Find nodes to update
        List<Map<String, String>> nodesToUpdate = new ArrayList<>();
        traverseAndFilter(table.root, nodesToUpdate, tokens, 7);

        // No matching rows
        if (nodesToUpdate.isEmpty()) {
            return "No rows matched the update condition";
        }

        // Update matching nodes
        int updatedCount = 0;
        for (Map<String, String> nodeData : nodesToUpdate) {
            // Delete old node
            deleteNode(table, nodeData);
            
            // Create updated node data
            Map<String, String> updatedData = new HashMap<>(nodeData);
            updatedData.put(updateColumn, updateValue);
            
            // Insert updated node
            table.insert(updatedData);
            updatedCount++;
        }

        return "Updated " + updatedCount + " rows in table " + tableName;
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
