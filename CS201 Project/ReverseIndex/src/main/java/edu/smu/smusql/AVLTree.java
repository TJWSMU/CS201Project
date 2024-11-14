package edu.smu.smusql;

import java.util.ArrayList;
import java.util.List;

public class AVLTree<K extends Comparable<K>, V> {

    // Node class representing each node in the AVL Tree
    private class Node {
        K key;
        List<V> values; // For handling multiple values with the same key
        Node left;
        Node right;
        int height;

        Node(K key, V value) {
            this.key = key;
            this.values = new ArrayList<>();
            this.values.add(value);
            this.height = 1; // New node is initially added at leaf
        }
    }

    private Node root;

    // Public method to insert a key-value pair into the AVL Tree
    public void insert(K key, V value) {
        root = insert(root, key, value);
    }

    // Recursive method to insert a key-value pair and rebalance the tree
    private Node insert(Node node, K key, V value) {
        if (node == null)
            return new Node(key, value);

        // Perform normal BST insertion
        int cmp = key.compareTo(node.key);
        if (cmp < 0)
            node.left = insert(node.left, key, value);
        else if (cmp > 0)
            node.right = insert(node.right, key, value);
        else {
            node.values.add(value); // Add to the list of values
            return node;
        }

        // Update height of this ancestor node
        node.height = 1 + Math.max(height(node.left), height(node.right));

        // Get balance factor to check whether this node became unbalanced
        int balance = getBalance(node);

        // Balance the node if unbalanced
        return balanceNode(node, balance, key);
    }

    // Method to balance the node after insertion
    private Node balanceNode(Node node, int balance, K key) {
        // Left Left Case
        if (balance > 1 && key.compareTo(node.left.key) < 0)
            return rightRotate(node);

        // Right Right Case
        if (balance < -1 && key.compareTo(node.right.key) > 0)
            return leftRotate(node);

        // Left Right Case
        if (balance > 1 && key.compareTo(node.left.key) > 0) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }

        // Right Left Case
        if (balance < -1 && key.compareTo(node.right.key) < 0) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }

        // Return the (unchanged) node pointer
        return node;
    }

    // Method to delete a specific value associated with a key
    public void delete(K key, V value) {
        root = delete(root, key, value);
    }
    
    // Recursive method to delete a value associated with a key and rebalance the tree
    private Node delete(Node node, K key, V value) {
        if (node == null)
            return null;
    
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            // Move to the left subtree
            node.left = delete(node.left, key, value);
        } else if (cmp > 0) {
            // Move to the right subtree
            node.right = delete(node.right, key, value);
        } else {
            // Key matches, attempt to remove the specific value from the list
            node.values.remove(value);
    
            if (node.values.isEmpty()) {
                // If no more values are associated with this key, delete the node
                if (node.left == null || node.right == null) {
                    // Node with only one child or no child
                    node = (node.left != null) ? node.left : node.right;
                } else {
                    // Node with two children: find the inorder successor
                    Node temp = minValueNode(node.right);
                    node.key = temp.key;
                    node.values = new ArrayList<>(temp.values);  // Clone to avoid reference issues
                    node.right = delete(node.right, temp.key, temp.values.get(0)); // Delete one instance of the successor key
                }
            }
        }
    
        // If the tree had only one node then return
        if (node == null)
            return null;
    
        // Update height
        node.height = 1 + Math.max(height(node.left), height(node.right));
    
        // Get balance factor and balance the node if unbalanced
        return balanceDeletion(node, getBalance(node));
    }
    
    // Method to balance the node after deletion
    private Node balanceDeletion(Node node, int balance) {
        // Left Left Case
        if (balance > 1 && getBalance(node.left) >= 0)
            return rightRotate(node);

        // Left Right Case
        if (balance > 1 && getBalance(node.left) < 0) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }

        // Right Right Case
        if (balance < -1 && getBalance(node.right) <= 0)
            return leftRotate(node);

        // Right Left Case
        if (balance < -1 && getBalance(node.right) > 0) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }

        return node;
    }

    // Method to search for values by key
    public List<V> search(K key) {
        Node node = searchNode(root, key);
        return (node != null) ? node.values : null;
    }

    // Recursive method to search for a node
    private Node searchNode(Node node, K key) {
        if (node == null)
            return null;

        int cmp = key.compareTo(node.key);
        if (cmp == 0)
            return node;
        else if (cmp < 0)
            return searchNode(node.left, key);
        else
            return searchNode(node.right, key);
    }

    // Implement range query methods
    public List<V> getValuesGreaterThan(K key) {
        List<V> result = new ArrayList<>();
        getValuesGreaterThan(root, key, result);
        return result;
    }

    private void getValuesGreaterThan(Node node, K key, List<V> result) {
        if (node == null) return;

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            // Node's key is greater than key
            getValuesGreaterThan(node.left, key, result);
            result.addAll(node.values);
            getValuesGreaterThan(node.right, key, result);
        } else {
            // Node's key is less than or equal to key
            getValuesGreaterThan(node.right, key, result);
        }
    }

    public List<V> getValuesLessThan(K key) {
        List<V> result = new ArrayList<>();
        getValuesLessThan(root, key, result);
        return result;
    }

    private void getValuesLessThan(Node node, K key, List<V> result) {
        if (node == null) return;

        int cmp = key.compareTo(node.key);
        if (cmp > 0) {
            // Node's key is less than key
            getValuesLessThan(node.left, key, result);
            result.addAll(node.values);
            getValuesLessThan(node.right, key, result);
        } else {
            // Node's key is greater than or equal to key
            getValuesLessThan(node.left, key, result);
        }
    }

    public List<V> getValuesGreaterThanOrEqual(K key) {
        List<V> result = new ArrayList<>();
        getValuesGreaterThanOrEqual(root, key, result);
        return result;
    }

    private void getValuesGreaterThanOrEqual(Node node, K key, List<V> result) {
        if (node == null) return;

        int cmp = key.compareTo(node.key);
        if (cmp <= 0) {
            // Node's key is greater than or equal to key
            getValuesGreaterThanOrEqual(node.left, key, result);
            result.addAll(node.values);
            getValuesGreaterThanOrEqual(node.right, key, result);
        } else {
            // Node's key is less than key
            getValuesGreaterThanOrEqual(node.right, key, result);
        }
    }

    public List<V> getValuesLessThanOrEqual(K key) {
        List<V> result = new ArrayList<>();
        getValuesLessThanOrEqual(root, key, result);
        return result;
    }

    private void getValuesLessThanOrEqual(Node node, K key, List<V> result) {
        if (node == null) return;

        int cmp = key.compareTo(node.key);
        if (cmp >= 0) {
            // Node's key is less than or equal to key
            getValuesLessThanOrEqual(node.left, key, result);
            result.addAll(node.values);
            getValuesLessThanOrEqual(node.right, key, result);
        } else {
            // Node's key is greater than key
            getValuesLessThanOrEqual(node.left, key, result);
        }
    }

    // Utility methods
    private int height(Node node) {
        return (node == null) ? 0 : node.height;
    }

    private int getBalance(Node node) {
        return (node == null) ? 0 : height(node.left) - height(node.right);
    }

    private Node rightRotate(Node y) {
        Node x = y.left;
        Node T2 = x.right;

        // Perform rotation
        x.right = y;
        y.left = T2;

        // Update heights
        y.height = 1 + Math.max(height(y.left), height(y.right));
        x.height = 1 + Math.max(height(x.left), height(x.right));

        // Return new root
        return x;
    }

    private Node leftRotate(Node x) {
        Node y = x.right;
        Node T2 = y.left;

        // Perform rotation
        y.left = x;
        x.right = T2;

        // Update heights
        x.height = 1 + Math.max(height(x.left), height(x.right));
        y.height = 1 + Math.max(height(y.left), height(y.right));

        // Return new root
        return y;
    }

    // Helper method to find the node with the minimum key value
    private Node minValueNode(Node node) {
        Node current = node;

        // Loop down to find the leftmost leaf
        while (current.left != null)
            current = current.left;

        return current;
    }
    public List<V> getValuesInRange(K minKey, K maxKey) {
        List<V> result = new ArrayList<>();
        getValuesInRange(root, minKey, maxKey, result);
        return result;
    }
    
    private void getValuesInRange(Node node, K minKey, K maxKey, List<V> result) {
        if (node == null) return;
    
        int cmpMin = minKey.compareTo(node.key);
        int cmpMax = maxKey.compareTo(node.key);
    
        if (cmpMin < 0) getValuesInRange(node.left, minKey, maxKey, result);
    
        if (cmpMin <= 0 && cmpMax >= 0) result.addAll(node.values);
    
        if (cmpMax > 0) getValuesInRange(node.right, minKey, maxKey, result);
    }
}
