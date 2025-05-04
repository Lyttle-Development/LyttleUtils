package com.lyttledev.lyttleutils.utils.storage;

import java.util.HashMap;
import java.util.Map;

/**
 * Memory class to store and manage key-value pairs.
 * This is a simple in-memory storage solution.
 *
 * @param <K> Type of the key
 * @param <V> Type of the value
 */
public class Memory<K, V> {
    // A generic map to keep track of players and their associated value (like location or gravestones)
    private final Map<K, V> valueMap = new HashMap<>();

    // Add value associated with a key
    public void addvalue(K key, V value) {
        valueMap.put(key, value);
    }

    // Remove value associated with a key
    public void removevalue(K key) {
        valueMap.remove(key);
    }

    // Get value associated with a key
    public Object getvalue(K key) {
        return valueMap.get(key);
    }

    // Check if value exists for a given key
    public boolean hasvalue(K key) {
        return valueMap.containsKey(key);
    }
}