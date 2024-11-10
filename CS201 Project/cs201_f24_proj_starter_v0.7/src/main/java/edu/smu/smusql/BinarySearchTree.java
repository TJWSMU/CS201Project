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
    private Map<String, Integer> columns = new HashMap<>();
    private String[] columnNames;
    private String leftMost;
    private String rightMost;

    public BinarySearchTree(String[] columnNames) {
        root = null;
        for (int i = 0; i < columnNames.length; i++) {
            columnNames[i] = columnNames[i].trim();
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

    public void delete(String key) {
        root = deleteRec(root, key);
    }

    private Node deleteRec(Node root, String key) {
        if (root == null) {
            return root;
        }

        if (key.compareTo(root.data[0]) < 0) {
            root.left = deleteRec(root.left, key);
        } else if (key.compareTo(root.data[0]) > 0) {
            root.right = deleteRec(root.right, key);
        } else {
            // Node with only one child or no child
            if (root.left == null) {
                return root.right;
            } else if (root.right == null) {
                return root.left;
            }

            // Node with two children: Get the inorder successor (smallest in the right subtree)
            root.data = minValue(root.right);

            // Delete the inorder successor
            root.right = deleteRec(root.right, root.data[0]);
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
        searchAndPrintAndRec(root, index1, operator1, value1, index2, operator2, value2, result);
        return result.toString();
    }

    private void searchAndPrintAndRec(Node node, int index1, String operator1, String value1, int index2, String operator2, String value2, StringBuilder result) {
        if (node != null) {
            searchAndPrintAndRec(node.left, index1, operator1, value1, index2, operator2, value2, result);
            if (matchesCondition(node.data[index1], operator1, value1) && matchesCondition(node.data[index2], operator2, value2)) {
                result.append(String.join("\t", node.data)).append("\n");
            }
            searchAndPrintAndRec(node.right, index1, operator1, value1, index2, operator2, value2, result);
        }
    }

    public String searchAndPrintOr(int index1, String operator1, String value1, int index2, String operator2, String value2) {
        StringBuilder result = new StringBuilder();
        searchAndPrintOrRec(root, index1, operator1, value1, index2, operator2, value2, result);
        return result.toString();
    }

    private void searchAndPrintOrRec(Node node, int index1, String operator1, String value1, int index2, String operator2, String value2, StringBuilder result) {
        if (node != null) {
            searchAndPrintOrRec(node.left, index1, operator1, value1, index2, operator2, value2, result);
            if (matchesCondition(node.data[index1], operator1, value1) || matchesCondition(node.data[index2], operator2, value2)) {
                result.append(String.join("\t", node.data)).append("\n");
            }
            searchAndPrintOrRec(node.right, index1, operator1, value1, index2, operator2, value2, result);
        }
    }

    private boolean matchesCondition(String data, String operator, String value) {
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
        String result = "";

        // Append column names separated by a tab
        if (columnNames != null && columnNames.length > 0) {
            for (int i = 0; i < columnNames.length; i++) {
                result += columnNames[i];
                if (i < columnNames.length - 1) {
                    result += "\t";
                }
            }
            result += "\n";
        }

        // Append the data from the tree
        result = toStringRec(root, result);
        return result;
    }

    private String toStringRec(Node node, String result) {
        if (node != null) {
            result = toStringRec(node.left, result);
            result += String.join("\t", node.data) + "\n";
            result = toStringRec(node.right, result);
        }
        return result;
    }

    public static void main(String[] args) {
        
    }
}