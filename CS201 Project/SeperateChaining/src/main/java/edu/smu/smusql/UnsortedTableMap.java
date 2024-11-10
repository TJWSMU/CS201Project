package edu.smu.smusql;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * A map implementation using an unsorted table (array list).
 */
public class UnsortedTableMap<K, V> implements Map<K, V> {
    private ArrayList<MapEntry<K, V>> table = new ArrayList<>();

    /** Default constructor */
    public UnsortedTableMap() { }

    /** Private utility class for entries */
    private static class MapEntry<K, V> implements Entry<K, V> {
        private K key;
        private V value;
        public MapEntry(K key, V value) {
            this.key = key; this.value = value;
        }
        public K getKey() { return key; }
        public V getValue() { return value; }
        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }
    }

    /** Helper method to find index of key */
    private int findIndex(Object key) {
        int n = table.size();
        for (int j = 0; j < n; j++)
            if (table.get(j).getKey().equals(key))
                return j;
        return -1;
    }

    /** Public methods */

    @Override
    public int size() {
        return table.size();
    }

    @Override
    public boolean isEmpty() {
        return table.isEmpty();
    }

    @Override
    public V get(Object key) {
        int index = findIndex(key);
        if (index == -1) return null;
        return table.get(index).getValue();
    }

    @Override
    public V put(K key, V value) {
        int index = findIndex(key);
        if (index == -1) {
            table.add(new MapEntry<>(key, value));
            return null;
        } else {
            return table.get(index).setValue(value);
        }
    }

    @Override
    public V remove(Object key) {
        int index = findIndex(key);
        int n = size();
        if (index == -1) return null;
        V answer = table.get(index).getValue();
        if (index != n - 1)
            table.set(index, table.get(n - 1)); // Swap with last
        table.remove(n - 1);
        return answer;
    }

    @Override
    public void clear() {
        table.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return findIndex(key) != -1;
    }

    @Override
    public boolean containsValue(Object value) {
        for (MapEntry<K, V> entry : table) {
            if (entry.getValue().equals(value))
                return true;
        }
        return false;
    }

    @Override
    public Set<K> keySet() {
        Set<K> keySet = new HashSet<>();
        for (MapEntry<K, V> entry : table) {
            keySet.add(entry.getKey());
        }
        return keySet;
    }

    @Override
    public Collection<V> values() {
        ArrayList<V> values = new ArrayList<>();
        for (MapEntry<K, V> entry : table) {
            values.add(entry.getValue());
        }
        return values;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entrySet = new HashSet<>();
        for (MapEntry<K, V> entry : table) {
            entrySet.add(entry);
        }
        return entrySet;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> e : m.entrySet()) {
            this.put(e.getKey(), e.getValue());
        }
    }
}
