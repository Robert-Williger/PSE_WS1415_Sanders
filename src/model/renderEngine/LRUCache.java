package model.renderEngine;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> implements ICache<K, V> {

    // for set capacity only
    private final LRULinkedHashMap unsynchronizedLRUMap;
    private final Map<K, V> LRUMap;
    private final Collection<V> freeList;

    public LRUCache(final int capacity, final boolean isSynchronized, final Collection<V> freeList) {
        unsynchronizedLRUMap = new LRULinkedHashMap(capacity);
        LRUMap = isSynchronized ? Collections.synchronizedMap(unsynchronizedLRUMap) : unsynchronizedLRUMap;

        this.freeList = freeList;
    }

    @Override
    public void clear() {
        LRUMap.clear();
    }

    // not synchronized, but should not cause any errors
    // existing entries which exceed the new capacity are not removed
    @Override
    public void setSize(final int capacity) {
        unsynchronizedLRUMap.setCapacity(capacity);
    }

    @Override
    public int getSize() {
        return unsynchronizedLRUMap.capacity;
    }

    @Override
    public void put(final K key, final V value) {
        LRUMap.put(key, value);
    }

    @Override
    public V get(final K key) {
        return LRUMap.get(key);
    }

    @Override
    public boolean contains(final long id) {
        return LRUMap.containsKey(id);
    }

    private class LRULinkedHashMap extends LinkedHashMap<K, V> {
        private static final long serialVersionUID = 1L;
        private int capacity;

        public LRULinkedHashMap(final int capacity) {
            super(capacity + 1, .75F, true);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
            if (size() > capacity) {
                freeList.add(eldest.getValue());
                return true;
            }

            return false;
        }

        public void setCapacity(final int capacity) {
            this.capacity = capacity;
        }
    }
}
