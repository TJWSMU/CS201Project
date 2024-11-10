package edu.smu.smusql;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Map.Entry;

/**
 * An abstract base class for hash map implementations.
 */
public abstract class AbstractHashMap<K, V> implements Map<K, V> {
    protected int n = 0;           // Number of entries in the map
    protected int capacity;        // Capacity of the table
    private int prime;             // Prime number for hash function
    private long scale, shift;     // Shift and scale factors

    /** Default values */
    private static final int DEFAULT_CAPACITY = 17;
    private static final int DEFAULT_PRIME = 109345121;

    /** Constructors */

    /** Default constructor */
    public AbstractHashMap() {
        this(DEFAULT_CAPACITY);
    }

    /** Constructor with specified capacity */
    public AbstractHashMap(int capacity) {
        this(capacity, DEFAULT_PRIME);
    }

    /** Constructor with specified capacity and prime */
    public AbstractHashMap(int capacity, int prime) {
        this.capacity = capacity;
        this.prime = prime;
        Random rand = new Random();
        scale = rand.nextInt(prime - 1) + 1;
        shift = rand.nextInt(prime);
        createTable();
    }

    /** Public methods */

    @Override
    public int size() {
        return n;
    }

    @Override
    public boolean isEmpty() {
        return n == 0;
    }

    /** Hash function */
    protected int hashValue(K key) {
        return (int) ((Math.abs(key.hashCode() * scale + shift) % prime) % capacity);
    }

    @Override
    public V get(Object key) {
        return bucketGet(hashValue((K) key), (K) key);
    }

    @Override
    public V put(K key, V value) {
        V answer = bucketPut(hashValue(key), key, value);
        if (n > capacity * 0.75) // Load factor threshold
            resize(2 * capacity - 1);
        return answer;
    }

    @Override
    public V remove(Object key) {
        return bucketRemove(hashValue((K) key), (K) key);
    }

    /** Abstract methods to be implemented by subclasses */
    protected abstract void createTable();
    protected abstract V bucketGet(int h, K k);
    protected abstract V bucketPut(int h, K k, V v);
    protected abstract V bucketRemove(int h, K k);

    /** Resize the table to have given capacity */
    private void resize(int newCapacity) {
        Set<Entry<K, V>> entries = entrySet();
        capacity = newCapacity;
        createTable(); // Create new table with updated capacity
        n = 0; // Reset size
        for (Entry<K, V> e : entries)
            put(e.getKey(), e.getValue());
    }

    /** The following methods are part of the Map interface but are left to be implemented by subclasses */

    @Override
    public abstract Set<Entry<K, V>> entrySet();

    @Override
    public void clear() {
        n = 0;
        createTable();
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        for (Entry<K, V> entry : entrySet()) {
            if (entry.getValue().equals(value))
                return true;
        }
        return false;
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        for (Entry<K, V> entry : entrySet()) {
            keys.add(entry.getKey());
        }
        return keys;
    }

    @Override
    public Collection<V> values() {
        ArrayList<V> values = new ArrayList<>();
        for (Entry<K, V> entry : entrySet()) {
            values.add(entry.getValue());
        }
        return values;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }
}
