package edu.smu.smusql;

import java.util.HashMap;
import java.util.Map;

public class BinarySearchTree {

    private class Node {
        String[] data;
        Node left, right;

        Node(String[] data) {
            this.data = data;
            left = null;
            right = null;
        }
    }

    private Node root;
    private Map<String, Integer> columns = new HashMap<>(); //column name as key, index in array as value
    private String[] columnNames;
    private String leftMost; //Stores the leftmost primary key
    private String rightMost; //Stores the rightmost primary key 

    public BinarySearchTree(String[] columnNames) {
        root = null;
        for (int i = 0; i < columnNames.length; i++) {
            columns.put(columnNames[i], i);
        }
        this.columnNames = columnNames;
    }

    public Map<String, Integer> getColumns() {
        return columns;
    }

    public void insert(String[] data) {
        root = insertRec(root, data);
    }

    /**
     * Inserts a new node with the given data into the binary search tree.
     * If the tree is empty, a new node is created as the root.
     * If a node with the same key already exists, the new node is inserted to the left.
     *
     * @param root The root of the binary search tree.
     * @param data The data to be inserted, where the first element acts as the primary key.
     * @return The root of the binary search tree after insertion.
     */
    private Node insertRec(Node root, String[] data) {
        if (root == null) {
            root = new Node(data);
            // Update leftmost and rightmost when inserting the first node
            if (this.root == null) {
                leftMost = data[0];
                rightMost = data[0];
            } else {
                if (data[0].compareTo(leftMost) < 0) {
                    leftMost = data[0];
                }
                if (data[0].compareTo(rightMost) > 0) {
                    rightMost = data[0];
                }
            }
            return root;
        }

        // Insert the row depending on the value of the first column 
        // (first column acts as primary key)
        // allow for duplicate keys, insert to left if duplicate
        if (data[0].compareTo(root.data[0]) < 0) {
            root.left = insertRec(root.left, data);
            if (data[0].compareTo(leftMost) < 0) {
                leftMost = data[0];
            }
        } else if (data[0].compareTo(root.data[0]) > 0) {
            root.right = insertRec(root.right, data);
            if (data[0].compareTo(rightMost) > 0) {
                rightMost = data[0];
            }
        } else {
            root.left = insertRec(root.left, data);
            if (data[0].compareTo(leftMost) < 0) {
                leftMost = data[0];
            }
        }

        return root;
    }

    public int update(int index, String newValue, 
    int index1, String value1, String operator1, String logic, 
    int index2, String value2, String operator2) {
        int[] count = new int[1]; // Use an array to keep track of the count
        root = updateRec(root, index, newValue, index1, value1, operator1, logic, index2, value2, operator2, count);
        return count[0];
    }

    /**
     * Recursively updates nodes in the binary search tree based on specified conditions.
     *
     * @param root The root node of the binary search tree.
     * @param index The index of the data array in the node to be updated.
     * @param newValue The new value to be set at the specified index.
     * @param index1 The index of the data array in the node for the first condition.
     * @param value1 The value to compare against for the first condition.
     * @param operator1 The operator to use for the first condition (e.g., "=", "<", ">", "<=", ">=").
     * @param logic The logical operator to combine the two conditions ("AND" or "OR"). If null, only the first condition is used.
     * @param index2 The index of the data array in the node for the second condition.
     * @param value2 The value to compare against for the second condition.
     * @param operator2 The operator to use for the second condition (e.g., "=", "<", ">", "<=", ">=").
     * @param count An array of size 1 to keep track of the number of nodes updated.
     * @return The root node of the binary search tree after updates.
     */
    private Node updateRec(Node root, int index, String newValue, int index1, String value1, String operator1, String logic, int index2, String value2, String operator2, int[] count) {
        if (root == null) {
            return root;
        }
    
        root.left = updateRec(root.left, index, newValue, index1, value1, operator1, logic, index2, value2, operator2, count);
        root.right = updateRec(root.right, index, newValue, index1, value1, operator1, logic, index2, value2, operator2, count);
    
        boolean condition1 = matchesCondition(root.data[index1], value1, operator1);
        boolean condition2 = logic != null ? matchesCondition(root.data[index2], value2, operator2) : false;
    
        boolean shouldUpdate = logic == null ? condition1 : (logic.equals("AND") ? (condition1 && condition2) : (condition1 || condition2));
    
        if (shouldUpdate) {
            count[0]++; // Increment the count of updated nodes
            root.data[index] = newValue;
        }
    
        return root;
    }

    public int delete(int index1, String value1, String operator1, String logic, int index2, String value2, String operator2) {
        int[] count = new int[1]; // Use an array to keep track of the count
        root = deleteRec(root, index1, value1, operator1, logic, index2, value2, operator2, count);
        return count[0];
    }
    
    /**
     * Deletes nodes from the binary search tree based on the specified conditions.
     *
     * @param root The root node of the binary search tree.
     * @param index1 The index of the first condition to check in the node's data.
     * @param value1 The value to compare against for the first condition.
     * @param operator1 The operator to use for the first condition (e.g., "=", "<", ">", "<=", ">=").
     * @param logic The logical operator to combine the first and second conditions ("AND" or "OR").
     * @param index2 The index of the second condition to check in the node's data.
     * @param value2 The value to compare against for the second condition.
     * @param operator2 The operator to use for the second condition (e.g., "=", "<", ">", "<=", ">=").
     * @param count An array to keep track of the number of deleted nodes.
     * @return The new root of the binary search tree after deletion.
     */
    private Node deleteRec(Node root, int index1, String value1, String operator1, String logic, int index2, String value2, String operator2, int[] count) {
        if (root == null) {
            return root;
        }
    
        root.left = deleteRec(root.left, index1, value1, operator1, logic, index2, value2, operator2, count);
        root.right = deleteRec(root.right, index1, value1, operator1, logic, index2, value2, operator2, count);
    
        boolean condition1 = matchesCondition(root.data[index1], value1, operator1);
        boolean condition2 = logic != null ? matchesCondition(root.data[index2], value2, operator2) : false;
    
        boolean shouldDelete = logic == null ? condition1 : (logic.equals("AND") ? (condition1 && condition2) : (condition1 || condition2));
    
        if (shouldDelete) {
            count[0]++; // Increment the count of deleted nodes
            if (root.left == null) {
                return root.right;
            } else if (root.right == null) {
                return root.left;
            }
    
            // Node with two children: Get the inorder successor (smallest in the right subtree)
            root.data = minValue(root.right);
    
            // Delete the inorder successor
            root.right = deleteRec(root.right, index1, root.data[index1], "=", null, -1, null, null, count);
        }
    
        return root;
    }
    
    private String[] minValue(Node root) {
        String[] minv = root.data;
        while (root.left != null) {
            minv = root.left.data;
            root = root.left;
        }
        return minv;
    }

    /*
     * Used for the select statement where column name = value
     */
    public String searchAndPrintEquals(int index, String value) {
        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columnNames)).append("\n");
        // If the value is outside the range of the primary key, row cannot be found.
        if (index == 0 && (value.compareTo(leftMost) < 0 || value.compareTo(rightMost) > 0)) {
            return result.toString();
        }
        searchAndPrintEqualsRec(root, index, value, result);
        return result.toString();
    }

    /**
     * Recursively searches the binary search tree for nodes where the data at the specified index
     * equals the given value, and appends the matching nodes' data to the result.
     *
     * @param node   The current node in the binary search tree.
     * @param index  The index in the node's data array to compare with the value.
     * @param value  The value to compare against the node's data at the specified index.
     * @param result The StringBuilder to append the matching nodes' data to.
     */
    private void searchAndPrintEqualsRec(Node node, int index, String value, StringBuilder result) {
        if (node != null) {
            searchAndPrintEqualsRec(node.left, index, value, result);
            if (node.data[index].equals(value)) {
                result.append(String.join("\t", node.data)).append("\n");
            }
            searchAndPrintEqualsRec(node.right, index, value, result);
        }
    }

    /*
     * Used for the select statement where column name < value
     */
    public String searchAndPrintLessThan(int index, String value) {
        StringBuilder result = new StringBuilder();
        //Check if result is less than the leftmost, if so row cannot be found
        if (index == 0 && value.compareTo(leftMost) < 0) {
            return result.toString();
        }
        result.append(String.join("\t", columnNames)).append("\n");
        searchAndPrintLessThanRec(root, index, value, result);
        return result.toString();
    }

    /**
     * Recursively searches the binary search tree and appends to the result all nodes
     * whose data at the specified index is less than the given value.
     *
     * @param node   the current node in the binary search tree
     * @param index  the index of the data array to compare
     * @param value  the value to compare against
     * @param result the StringBuilder to append the results to
     */
    private void searchAndPrintLessThanRec(Node node, int index, String value, StringBuilder result) {
        if (node != null) {
            searchAndPrintLessThanRec(node.left, index, value, result);
            if (node.data[index].compareTo(value) < 0) {
                result.append(String.join("\t", node.data)).append("\n");
            }
            searchAndPrintLessThanRec(node.right, index, value, result);
        }
    }

    /*
     * Used for the select statement where column name <= value
     */
    public String searchAndPrintLessThanOrEqual(int index, String value) {
        StringBuilder result = new StringBuilder();
        //Check if result is less than the leftmost, if so row cannot be found
        if (index == 0 && value.compareTo(leftMost) < 0) {
            return result.toString();
        }
        result.append(String.join("\t", columnNames)).append("\n");
        searchAndPrintLessThanOrEqualRec(root, index, value, result);
        return result.toString();
    }

    /**
     * Recursively searches the binary search tree for nodes with data less than or equal to the specified value
     * at the given index and appends the matching nodes' data to the result.
     *
     * @param node   The current node in the binary search tree.
     * @param index  The index of the data array to compare the value against.
     * @param value  The value to compare the node's data against.
     * @param result The StringBuilder object to append the matching nodes' data.
     */
    private void searchAndPrintLessThanOrEqualRec(Node node, int index, String value, StringBuilder result) {
        if (node != null) {
            searchAndPrintLessThanOrEqualRec(node.left, index, value, result);
            if (node.data[index].compareTo(value) <= 0) {
                result.append(String.join("\t", node.data)).append("\n");
            }
            searchAndPrintLessThanOrEqualRec(node.right, index, value, result);
        }
    }

    /*
     * Used for the select statement where column name > value
     */
    public String searchAndPrintMoreThan(int index, String value) {
        StringBuilder result = new StringBuilder();
        //Check if result is greater than the rightmost, if so row cannot be found
        if (index == 0 && value.compareTo(rightMost) > 0) {
            return result.toString();
        }
        result.append(String.join("\t", columnNames)).append("\n");
        searchAndPrintMoreThanRec(root, index, value, result);
        return result.toString();
    }

    /**
     * Recursively searches the binary search tree for nodes where the value at the specified index
     * is greater than the given value, and appends the data of those nodes to the result.
     *
     * @param node   The current node in the binary search tree.
     * @param index  The index of the value in the node's data array to compare.
     * @param value  The value to compare against.
     * @param result The StringBuilder to append the results to.
     */
    private void searchAndPrintMoreThanRec(Node node, int index, String value, StringBuilder result) {
        if (node != null) {
            searchAndPrintMoreThanRec(node.left, index, value, result);
            if (node.data[index].compareTo(value) > 0) {
                result.append(String.join("\t", node.data)).append("\n");
            }
            searchAndPrintMoreThanRec(node.right, index, value, result);
        }
    }

    /*
     * Used for the select statement where column name >= value
     */
    public String searchAndPrintMoreThanOrEqual(int index, String value) {
        StringBuilder result = new StringBuilder();
        //Check if result is greater than the rightmost, if so row cannot be found
        if (index == 0 && value.compareTo(rightMost) > 0) {
            return result.toString();
        }
        result.append(String.join("\t", columnNames)).append("\n");
        searchAndPrintMoreThanOrEqualRec(root, index, value, result);
        return result.toString();
    }

    /**
     * Recursively searches the binary search tree for nodes with data at the specified index
     * that is greater than or equal to the given value, and appends the matching nodes' data
     * to the result StringBuilder.
     *
     * @param node  the current node in the binary search tree
     * @param index the index of the data array to compare
     * @param value the value to compare against
     * @param result the StringBuilder to append the matching nodes' data
     */
    private void searchAndPrintMoreThanOrEqualRec(Node node, int index, String value, StringBuilder result) {
        if (node != null) {
            searchAndPrintMoreThanOrEqualRec(node.left, index, value, result);
            if (node.data[index].compareTo(value) >= 0) {
                result.append(String.join("\t", node.data)).append("\n");
            }
            searchAndPrintMoreThanOrEqualRec(node.right, index, value, result);
        }
    }

    /*
     * Used for the select statement where column name , value1 AND column name , value2
     */
    public String searchAndPrintAnd(int index1, String operator1, String value1, int index2, String operator2, String value2) {
        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columnNames)).append("\n");
        searchAndPrintAndRec(root, index1, operator1, value1, index2, operator2, value2, result);
        return result.toString();
    }

    /**
     * Recursively searches the binary search tree and appends nodes that match the given conditions to the result.
     *
     * @param node the current node in the binary search tree
     * @param index1 the index of the first condition to check in the node's data
     * @param operator1 the operator for the first condition (e.g., "=", "<", ">", "<=", ">=")
     * @param value1 the value to compare against for the first condition
     * @param index2 the index of the second condition to check in the node's data
     * @param operator2 the operator for the second condition (e.g., "=", "<", ">", "<=", ">=")
     * @param value2 the value to compare against for the second condition
     * @param result the StringBuilder to append the matching node's data to
     */
    private void searchAndPrintAndRec(Node node, int index1, String operator1, String value1, int index2, String operator2, String value2, StringBuilder result) {
        if (node != null) {
            searchAndPrintAndRec(node.left, index1, operator1, value1, index2, operator2, value2, result);
            if (matchesCondition(node.data[index1], value1, operator1) && matchesCondition(node.data[index2], value2, operator2)) {
                result.append(String.join("\t", node.data)).append("\n");
            }
            searchAndPrintAndRec(node.right, index1, operator1, value1, index2, operator2, value2, result);
        }
    }

    /*
     * Used for the select statement where column name , value1 OR column name , value2
     */
    public String searchAndPrintOr(int index1, String operator1, String value1, int index2, String operator2, String value2) {
        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columnNames)).append("\n");
        searchAndPrintOrRec(root, index1, operator1, value1, index2, operator2, value2, result);
        return result.toString();
    }

    /**
     * Recursively searches the binary search tree and appends nodes that match either of the given conditions to the result.
     *
     * @param node the current node in the binary search tree
     * @param index1 the index of the first condition to check in the node's data
     * @param operator1 the operator for the first condition (e.g., "=", "<", ">", "<=", ">=")
     * @param value1 the value to compare against for the first condition
     * @param index2 the index of the second condition to check in the node's data
     * @param operator2 the operator for the second condition (e.g., "=", "<", ">", "<=", ">=")
     * @param value2 the value to compare against for the second condition
     * @param result the StringBuilder to append the matching node's data to
     */
    private void searchAndPrintOrRec(Node node, int index1, String operator1, String value1, int index2, String operator2, String value2, StringBuilder result) {
        if (node != null) {
            searchAndPrintOrRec(node.left, index1, operator1, value1, index2, operator2, value2, result);
            if (matchesCondition(node.data[index1], value1, operator1) || matchesCondition(node.data[index2], value2, operator2)) {
                result.append(String.join("\t", node.data)).append("\n");
            }
            searchAndPrintOrRec(node.right, index1, operator1, value1, index2, operator2, value2, result);
        }
    }

    /*
     * Helper method to check if the data matches the condition.
     */
    private boolean matchesCondition(String data, String value, String operator) {
        switch (operator) {
            case "=":
                return data.equals(value);
            case "<":
                return data.compareTo(value) < 0;
            case ">":
                return data.compareTo(value) > 0;
            case "<=":
                return data.compareTo(value) <= 0;
            case ">=":
                return data.compareTo(value) >= 0;
            default:
                return false;
        }
    }
    

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Append column names separated by a tab
        if (columnNames != null && columnNames.length > 0) {
            for (int i = 0; i < columnNames.length; i++) {
                sb.append(columnNames[i]);
                if (i < columnNames.length - 1) {
                    sb.append("\t");
                }
            }
            sb.append("\n");
        }

        // Append the data from the tree
        toStringRec(root, sb);
        return sb.toString();
    }

    private void toStringRec(Node node, StringBuilder sb) {
        if (node != null) {
            toStringRec(node.left, sb);
            sb.append(String.join("\t", node.data)).append("\n");
            toStringRec(node.right, sb);
        }
    }

    
}