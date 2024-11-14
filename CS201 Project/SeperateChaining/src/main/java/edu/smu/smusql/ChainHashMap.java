package edu.smu.smusql;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

/**
 * A hash map implemented with separate chaining using an array of unsorted table maps.
 */
public class ChainHashMap<K, V> extends AbstractHashMap<K, V> {
    // Fixed-capacity array of UnsortedTableMap that serve as buckets
    private UnsortedTableMap<K, V>[] table;
    private static final int MAX_BUCKET_SIZE = 10;
    //private static final double LOAD_FACTOR_THRESHOLD = 0.75;
    /** Constructors */

    /** Default constructor with default capacity and prime factor */
    public ChainHashMap() {
        super();
    }

    /** Constructor with specified capacity */
    public ChainHashMap(int capacity) {
        super(capacity);
    }

    /** Constructor with specified capacity and prime factor */
    public ChainHashMap(int capacity, int prime) {
        super(capacity, prime);
    }

    /** Creates an empty table with the current capacity */
    @SuppressWarnings({"unchecked"})
    @Override
    protected void createTable() {
        table = (UnsortedTableMap<K, V>[]) new UnsortedTableMap[capacity]; // Safe cast
    }

    /**
     * Returns the value associated with key k in bucket h, or null if not found.
     */
    @Override
    protected V bucketGet(int h, K k) {
        UnsortedTableMap<K, V> bucket = table[h];
        if (bucket == null) return null;
        return bucket.get(k);
    }

    /**
     * Associates key k with value v in bucket h; returns old value or null if new.
     */
    @Override
    protected V bucketPut(int h, K k, V v) {
        UnsortedTableMap<K, V> bucket = table[h];
        if (bucket == null)
            bucket = table[h] = new UnsortedTableMap<>();
        int oldSize = bucket.size();
        V answer = bucket.put(k, v);
        n += (bucket.size() - oldSize); // n is total number of entries in the map
        return answer;
    }

    /**
     * Removes the entry for key k from bucket h; returns the value or null if not found.
     */
    @Override
    protected V bucketRemove(int h, K k) {
        UnsortedTableMap<K, V> bucket = table[h];
        if (bucket == null) return null;
        int oldSize = bucket.size();
        V answer = bucket.remove(k);
        n -= (oldSize - bucket.size());
        return answer;
    }

    /**
     * Returns a set of all entries in the map.
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> buffer = new HashSet<>();
        for (int h = 0; h < capacity; h++) {
            if (table[h] != null) {
                buffer.addAll(table[h].entrySet());
            }
        }
        return buffer;
    }

    @Override
    public V put(K key, V value) {
        int bucketIndex = hashValue(key);
        V answer = bucketPut(bucketIndex, key, value);
        if (table[bucketIndex].size() > MAX_BUCKET_SIZE) { // Resize up if any bucket size is more tha max bucket size
            resize(2 * capacity - 1);
        }
        return answer;
    }

    // @Override
    // public V put(K key, V value) {
    //     int bucketIndex = hashValue(key);
    //     V answer = bucketPut(bucketIndex, key, value);
    
    //     // Check both conditions
    //     if (table[bucketIndex].size() > MAX_BUCKET_SIZE) {
    //         resize(2 * capacity - 1);
    //     }
    //     return answer;
    // }
    
}
