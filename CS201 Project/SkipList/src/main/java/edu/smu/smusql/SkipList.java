package edu.smu.smusql;

import java.util.*;

public class SkipList {
    private static final int MAX_LEVEL = 16;
    private final Random random;
    private Node head;
    private int level;
    private List<String> columns;

    public class Node {
        Map<String, String> columnData;
        Node[] next;
        
        Node(Map<String, String> columnData, int level) {
            this.columnData = columnData;
            this.next = new Node[level + 1];
        }
    }

    public SkipList(List<String> columns) {
        this.random = new Random();
        this.level = 0;
        this.columns = columns;
        // Create header node with null data
        this.head = new Node(null, MAX_LEVEL);
    }

    private int randomLevel() {
        int level = 0;
        while (random.nextDouble() < 0.5 && level < MAX_LEVEL - 1) {
            level++;
        }
        return level;
    }

    public void insert(Map<String, String> columnData) {
        Node[] update = new Node[MAX_LEVEL];
        Node current = head;
        
        // Find insertion point at each level
        for (int i = level; i >= 0; i--) {
            while (current.next[i] != null && 
                   compare(current.next[i].columnData, columnData) < 0) {
                current = current.next[i];
            }
            update[i] = current;
        }

        // Generate random level for new node
        int newLevel = randomLevel();
        
        // If new level is greater than current level
        if (newLevel > level) {
            for (int i = level + 1; i <= newLevel; i++) {
                update[i] = head;
            }
            level = newLevel;
        }

        // Create and insert new node
        Node newNode = new Node(columnData, newLevel);
        for (int i = 0; i <= newLevel; i++) {
            newNode.next[i] = update[i].next[i];
            update[i].next[i] = newNode;
        }
    }

    private int compare(Map<String, String> data1, Map<String, String> data2) {
        if (data1 == null) return -1;
        if (data2 == null) return 1;
        
        // Compare using the first column (id) from columns list
        String idColumn = columns.get(0);
        return data1.get(idColumn).compareTo(data2.get(idColumn));
    }

    public void printList() {
        System.out.println("\nSkip List Structure:");
        for (int i = level; i >= 0; i--) {
            Node current = head.next[i];
            System.out.print("Level " + i + ": ");
            while (current != null) {
                System.out.print(current.columnData.get(columns.get(0)) + " → ");
                current = current.next[i];
            }
            System.out.println("null");
        }
    }

    public List<Map<String, String>> search(String value) {
        List<Map<String, String>> results = new ArrayList<>();
        Node current = head;
        
        // Start from the highest level
        for (int i = level; i >= 0; i--) {
            while (current.next[i] != null && 
                   current.next[i].columnData.get(columns.get(0)).compareTo(value) < 0) {
                current = current.next[i];
            }
        }
        
        // Move to the next node
        current = current.next[0];
        
        // If we found the value, collect all matching entries
        while (current != null && 
               current.columnData.get(columns.get(0)).equals(value)) {
            results.add(current.columnData);
            current = current.next[0];
        }
        
        return results;
    }

    public boolean delete(String[] key) {
        Node current = head;
        Node[] update = new Node[MAX_LEVEL];
        
        // Search for the position to delete
        for (int i = level; i >= 0; i--) {
            while (current.next[i] != null && 
                   compareKeys(current.next[i].columnData.get(columns.get(0)).split(","), key) < 0) {
                current = current.next[i];
            }
            update[i] = current;
        }
        
        current = current.next[0];
        
        // If node is found and keys match
        if (current != null && 
            compareKeys(current.columnData.get(columns.get(0)).split(","), key) == 0) {
            // Update the next references at each level
            for (int i = 0; i < level; i++) {
                if (update[i].next[i] != current) {
                    break;
                }
                update[i].next[i] = current.next[i];
            }
            
            // Update the level of the list if necessary
            while (level > 0 && head.next[level] == null) {
                level--;
            }
            
            return true;
        }
        
        return false;
    }

    private int compareKeys(String[] key1, String[] key2) {
        if (key1 == null || key2 == null) {
            return -1;
        }
        
        // Compare each component of the key
        for (int i = 0; i < Math.min(key1.length, key2.length); i++) {
            int comparison = key1[i].compareTo(key2[i]);
            if (comparison != 0) {
                return comparison;
            }
        }
        
        // If all components are equal, return 0
        return 0;
    }

    public List<Map<String, String>> getAllRecords() {
        List<Map<String, String>> results = new ArrayList<>();
        Node current = head.next[0];
        while (current != null) {
            results.add(current.columnData);
            current = current.next[0];
        }
        return results;
    }

    public List<Map<String, String>> search(WhereClause whereClause) {
        if (whereClause == null) {
            return getAllRecords();
        }

        List<Map<String, String>> results = new ArrayList<>();
        Node current = head.next[0];
        
        while (current != null) {
            if (matchesWhereClause(current.columnData, whereClause)) {
                results.add(current.columnData);
            }
            current = current.next[0];
        }
        
        return results;
    }

    public int update(String column, String value, WhereClause whereClause) {
        int count = 0;
        Node current = head.next[0];
        
        while (current != null) {
            if (matchesWhereClause(current.columnData, whereClause)) {
                current.columnData.put(column, value);
                count++;
            }
            current = current.next[0];
        }
        
        return count;
    }

    public int delete(WhereClause whereClause) {
        int count = 0;
        Node current = head;
        
        // Only operate at level 0 for safe deletion
        while (current.next[0] != null) {
            if (matchesWhereClause(current.next[0].columnData, whereClause)) {
                current.next[0] = current.next[0].next[0];
                count++;
            } else {
                current = current.next[0];
            }
        }
        
        return count;
    }

    private boolean matchesWhereClause(Map<String, String> data, WhereClause whereClause) {
        if (whereClause.type.equals("AND")) {
            return whereClause.conditions.stream()
                .allMatch(cond -> matchesCondition(data, cond));
        } else if (whereClause.type.equals("OR")) {
            return whereClause.conditions.stream()
                .anyMatch(cond -> matchesCondition(data, cond));
        } else {
            return matchesCondition(data, whereClause.conditions.get(0));
        }
    }

    private boolean matchesCondition(Map<String, String> data, Condition cond) {
        String value = data.get(cond.column);
        
        try {
            // Try numeric comparison
            double dataValue = Double.parseDouble(value);
            double condValue = Double.parseDouble(cond.value);
            
            switch (cond.operator) {
                case ">": return dataValue > condValue;
                case "<": return dataValue < condValue;
                case ">=": return dataValue >= condValue;
                case "<=": return dataValue <= condValue;
                case "=": return dataValue == condValue;
                default: return false;
            }
        } catch (NumberFormatException e) {
            // String comparison
            return switch (cond.operator) {
                case "=" -> value.equals(cond.value);
                default -> false;
            };
        }
    }
} 