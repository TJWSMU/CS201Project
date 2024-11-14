package edu.smu.smusql;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

public class LinearProbingHashMap<K, V> extends AbstractHashMap<K, V> {
    private MapEntry<K, V>[] table;
    private MapEntry<K, V> DEFUNCT = new MapEntry<>(null, null); // Sentinel for deleted entries
    private int collisions = 0;
    private int totalProbeLength = 0;
    private boolean useLinearProbing = true;
    private int c1 = 1; // first constant for quadratic probing
    private int c2 = 1; // second constant for quadratic probing

    // Constructors
    public LinearProbingHashMap() {
        super();
    }

    public LinearProbingHashMap(int capacity) {
        super(capacity);
    }

    public LinearProbingHashMap(int capacity, int prime) {
        super(capacity, prime);
    }

    // MapEntry inner class
    private static class MapEntry<K, V> implements Entry<K, V> {
        private K key;
        private V value;

        public MapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    protected void createTable() {
        table = (MapEntry<K, V>[]) new MapEntry[capacity];
    }

    private boolean isAvailable(int index) {
        return (table[index] == null || table[index] == DEFUNCT);
    }

    public int getCollisions() { return collisions; }
    public double getAverageProbeLength() { 
        return size() == 0 ? 0 : totalProbeLength / (double) size(); 
    }

    // Quadratic probing
    private int findSlot(int hash, K key) {
        int avail = -1;
        int index = hash;
        int probe = 0;

        do {
            if (isAvailable(index)) {
                if (avail == -1) {
                    avail = index;
                }
                if (table[index] == null) {
                    break;
                }
            } else {
                collisions++;
                if (key.equals(table[index].getKey())) {
                    return index;
                }
            }

            probe++;
            totalProbeLength++;

            if (useLinearProbing) {
                index = (hash + probe) % capacity;
            } else {
                index = (int) (hash + c1 * probe + c2 * probe * probe) % capacity;
            }

            if (index < 0) {
                index += capacity;
            }
        } while (probe < capacity);

        return -(avail + 1);
    }

    @Override
    protected V bucketGet(int hash, K key) {
        int index = findSlot(hash, key);
        if (index < 0)
            return null;
        return table[index].getValue();
    }

    @Override
    protected V bucketPut(int hash, K key, V value) {
        int index = findSlot(hash, key);
        if (index >= 0) {
            // Key exists, update value
            return table[index].setValue(value);
        }
        // Convert negative index and insert new entry
        table[-(index + 1)] = new MapEntry<>(key, value);
        n++;
        return null;
    }

    @Override
    protected V bucketRemove(int hash, K key) {
        int index = findSlot(hash, key);
        if (index < 0)
            return null;
        V answer = table[index].getValue();
        table[index] = DEFUNCT;
        n--;
        return answer;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> buffer = new HashSet<>();
        for (int i = 0; i < capacity; i++) {
            if (table[i] != null && table[i] != DEFUNCT) {
                buffer.add(table[i]);
            }
        }
        return buffer;
    }

    public void setLinearProbing() {
        useLinearProbing = true;
    }

    public void setQuadraticProbing(int c1, int c2) {
        useLinearProbing = false;
        this.c1 = c1;
        this.c2 = c2;
    }

    public void resetStatistics() {
        collisions = 0;
        totalProbeLength = 0;
    }
}