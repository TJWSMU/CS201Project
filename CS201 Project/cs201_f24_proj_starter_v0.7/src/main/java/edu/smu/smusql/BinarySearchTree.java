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
    private String leftMost;
    private String rightMost;

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

    private Node insertRec(Node root, String[] data) {
        if (root == null) {
            root = new Node(data);
            return root;
        }

        //Insert the row depending on the value of the first column 
        //(first column acts as primary key)
        //allow for duplicate keys, insert to left if duplicate
        if (data[0].compareTo(root.data[0]) < 0)
            root.left = insertRec(root.left, data);
        else if (data[0].compareTo(root.data[0]) > 0)
            root.right = insertRec(root.right, data);
        else
            root.left = insertRec(root.left, data);

        return root;
    }

    public int update(int index, String newValue, 
    int index1, String value1, String operator1, String logic, 
    int index2, String value2, String operator2) {
        int[] count = new int[1]; // Use an array to keep track of the count
        root = updateRec(root, index, newValue, index1, value1, operator1, logic, index2, value2, operator2, count);
        return count[0];
    }

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

    public boolean search(String key) {
        return searchRec(root, key);
    }

    private boolean searchRec(Node root, String key) {
        if (root == null)
            return false;

        if (root.data[0].equals(key))
            return true;

        if (root.data[0].compareTo(key) > 0)
            return searchRec(root.left, key);

        return searchRec(root.right, key);
    }

    public void inorder() {
        inorderRec(root);
    }

    private void inorderRec(Node root) {
        if (root != null) {
            inorderRec(root.left);
            System.out.println(root.data[0]);
            inorderRec(root.right);
        }
    }

    public String searchAndPrintEquals(int index, String value) {
        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columnNames)).append("\n");
        searchAndPrintEqualsRec(root, index, value, result);
        return result.toString();
    }

    private void searchAndPrintEqualsRec(Node node, int index, String value, StringBuilder result) {
        if (node != null) {
            searchAndPrintEqualsRec(node.left, index, value, result);
            if (node.data[index].equals(value)) {
                result.append(String.join("\t", node.data)).append("\n");
            }
            searchAndPrintEqualsRec(node.right, index, value, result);
        }
    }

    public String searchAndPrintLessThan(int index, String value) {
        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columnNames)).append("\n");
        searchAndPrintLessThanRec(root, index, value, result);
        return result.toString();
    }

    private void searchAndPrintLessThanRec(Node node, int index, String value, StringBuilder result) {
        if (node != null) {
            searchAndPrintLessThanRec(node.left, index, value, result);
            if (node.data[index].compareTo(value) < 0) {
                result.append(String.join("\t", node.data)).append("\n");
            }
            searchAndPrintLessThanRec(node.right, index, value, result);
        }
    }

    public String searchAndPrintLessThanOrEqual(int index, String value) {
        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columnNames)).append("\n");
        searchAndPrintLessThanOrEqualRec(root, index, value, result);
        return result.toString();
    }

    private void searchAndPrintLessThanOrEqualRec(Node node, int index, String value, StringBuilder result) {
        if (node != null) {
            searchAndPrintLessThanOrEqualRec(node.left, index, value, result);
            if (node.data[index].compareTo(value) <= 0) {
                result.append(String.join("\t", node.data)).append("\n");
            }
            searchAndPrintLessThanOrEqualRec(node.right, index, value, result);
        }
    }

    public String searchAndPrintMoreThan(int index, String value) {
        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columnNames)).append("\n");
        searchAndPrintMoreThanRec(root, index, value, result);
        return result.toString();
    }

    private void searchAndPrintMoreThanRec(Node node, int index, String value, StringBuilder result) {
        if (node != null) {
            searchAndPrintMoreThanRec(node.left, index, value, result);
            if (node.data[index].compareTo(value) > 0) {
                result.append(String.join("\t", node.data)).append("\n");
            }
            searchAndPrintMoreThanRec(node.right, index, value, result);
        }
    }

    public String searchAndPrintMoreThanOrEqual(int index, String value) {
        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columnNames)).append("\n");
        searchAndPrintMoreThanOrEqualRec(root, index, value, result);
        return result.toString();
    }

    private void searchAndPrintMoreThanOrEqualRec(Node node, int index, String value, StringBuilder result) {
        if (node != null) {
            searchAndPrintMoreThanOrEqualRec(node.left, index, value, result);
            if (node.data[index].compareTo(value) >= 0) {
                result.append(String.join("\t", node.data)).append("\n");
            }
            searchAndPrintMoreThanOrEqualRec(node.right, index, value, result);
        }
    }

    public String searchAndPrintAnd(int index1, String operator1, String value1, int index2, String operator2, String value2) {
        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columnNames)).append("\n");
        searchAndPrintAndRec(root, index1, operator1, value1, index2, operator2, value2, result);
        return result.toString();
    }

    private void searchAndPrintAndRec(Node node, int index1, String operator1, String value1, int index2, String operator2, String value2, StringBuilder result) {
        if (node != null) {
            searchAndPrintAndRec(node.left, index1, operator1, value1, index2, operator2, value2, result);
            if (matchesCondition(node.data[index1], value1, operator1) && matchesCondition(node.data[index2], value2, operator2)) {
                result.append(String.join("\t", node.data)).append("\n");
            }
            searchAndPrintAndRec(node.right, index1, operator1, value1, index2, operator2, value2, result);
        }
    }

    public String searchAndPrintOr(int index1, String operator1, String value1, int index2, String operator2, String value2) {
        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columnNames)).append("\n");
        searchAndPrintOrRec(root, index1, operator1, value1, index2, operator2, value2, result);
        return result.toString();
    }

    private void searchAndPrintOrRec(Node node, int index1, String operator1, String value1, int index2, String operator2, String value2, StringBuilder result) {
        if (node != null) {
            searchAndPrintOrRec(node.left, index1, operator1, value1, index2, operator2, value2, result);
            if (matchesCondition(node.data[index1], value1, operator1) || matchesCondition(node.data[index2], value2, operator2)) {
                result.append(String.join("\t", node.data)).append("\n");
            }
            searchAndPrintOrRec(node.right, index1, operator1, value1, index2, operator2, value2, result);
        }
    }

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