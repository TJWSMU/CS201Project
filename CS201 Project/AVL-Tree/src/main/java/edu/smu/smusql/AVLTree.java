package edu.smu.smusql;

import java.util.*;

public class AVLTree {
    public class Node {
        Map<String, String> columnData;
        Node left;
        Node right;
        int height;

        Node (Map<String, String> columnData) {
            this.columnData = columnData;
            this.left = null;
            this.right = null;
            this.height = 1;
        }
    }

    public Node root;
    public List<String> columns;

    public AVLTree(List<String> columns) {
        this.root = null;
        this.columns = columns;
    }

    public Map<String, String> getNodeMap() {
        return root.columnData;
    }

    public List<String> getColumns() {
        return columns;
    }

    // Get height of a node
    public int getHeight(Node node) {
        if (node == null) {
            return 0;
        }

        return node.height;
    }

    // Calculate the balance factor of a node
    public int getBalanceFactor(Node node) {
        if (node == null) {
            return 0;
        }

        return getHeight(node.left) - getHeight(node.right);
    }

    // Update node height
    public void updateHeight(Node node) {
        node.height = 1 + max(getHeight(node.left), getHeight(node.right));
    }

    // Helper method for finding max value
    private int max(int a, int b) {
        return (a > b) ? a : b;
    }

    // LL Rotation
    public Node LLRotate(Node z) {
        Node y = z.left;
        Node yRight = y.right;

        z.left = yRight;
        y.right = z;

        updateHeight(z);
        updateHeight(y);

        return y; 
    }

    // LR Rotation
    public Node LRRotate(Node z) {
        Node y = z.left;
        Node x = y.right;

        y.right = x.left;
        x.left = y;

        z.left = x.right;
        x.right = z;

        updateHeight(x);
        updateHeight(y);
        updateHeight(z);

        return x; 
    }

    // RR Rotation
    public Node RRRotate(Node z) {
        Node y = z.right;
        Node yLeft = y.left;

        z.right = yLeft;
        y.left = z;

        updateHeight(z);
        updateHeight(y);

        return y;
    }

    // RL Rotation
    public Node RLRotate(Node z) {
        Node y = z.right;
        Node x = y.left;

        y.left = x.right;
        x.right = y;

        z.right = x.left;
        x.left = z;

        updateHeight(x);
        updateHeight(y);
        updateHeight(z);

        return x; 
    }

    public Node balance(Node node) {
        int balanceFactor = getBalanceFactor(node);

        // Left heavy
        if (balanceFactor > 1) {
            // Check if it's LR case
            if (getBalanceFactor(node.left) < 0) {
                return LRRotate(node);
            }
            // LL case
            return LLRotate(node);
        }

        // Right heavy
        if (balanceFactor < -1) {
            // Check if it's RL case
            if (getBalanceFactor(node.right) > 0) {
                return RLRotate(node); 
            }
            // RR case
            return RRRotate(node);
        }

        return node;
    }

    public void insert(Map<String, String> columnData) {
        root = insertRec(root, columnData);
    }

    public Node insertRec(Node node, Map<String, String> columnData) {
        if (node == null) {
            return new Node(columnData);
        }

        // Compare using the first column (id) from columns list
        String idColumn = columns.get(0);
        String newId = columnData.get(idColumn);
        String currentId = node.columnData.get(idColumn);
        int comparison = newId.compareTo(currentId);

        if (comparison < 0) {
            node.left = insertRec(node.left, columnData);
        } else if (comparison > 0) {
            node.right = insertRec(node.right, columnData);
        } else {
            // Duplicates are not allowed in AVL Tree
            return node;
        }

        // Update height and balance the node
        updateHeight(node);
        return balance(node);
    }
    
    public void printTree() {
        System.out.println("\nAVL Tree Structure:");
        printTreeRec(root, "", true);
    }

    public void printTreeRec(Node node, String prefix, boolean isLeft) {
        if (node == null) return;

        System.out.println(prefix + (isLeft ? "├── " : "└── ") + 
            columns.get(0) + ": " + node.columnData.get(columns.get(0)) + 
            " (BF: " + getBalanceFactor(node) + 
            ", H: " + node.height + ")");

        printTreeRec(node.left, prefix + (isLeft ? "│   " : "    "), true);
        printTreeRec(node.right, prefix + (isLeft ? "│   " : "    "), false);
    }

    // Main method for testing
    // public static void main(String[] args) {
        
    //     AVLTree tree = new AVLTree();

    //     // Create sample data
    //     String[][] data = {
    //         {"1", "John", "25"},
    //         {"3", "Alice", "22"},
    //         {"2", "Bob", "28"},
    //         {"5", "Charlie", "23"},
    //         {"4", "David", "26"}
    //     };

    //     // Insert data into tree
    //     for (String[] row : data) {
    //         Map<String, String> columnData = new HashMap<>();
    //         columnData.put("id", row[0]);
    //         columnData.put("name", row[1]);
    //         columnData.put("age", row[2]);
            
    //         System.out.println(columnData);
    //         System.out.println("\nInserting ID: " + row[0]);
    //         tree.insert(columnData);
    //         tree.printTree();
    //     }
    // }
}