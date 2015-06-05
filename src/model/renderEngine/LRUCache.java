package model.renderEngine;

import java.awt.Image;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache implements ICache {

    // for set capacity only
    private final LRULinkedHashMap unsynchronizedLRUMap;
    private Map<Long, Image> LRUMap;
    private final Collection<Image> freeList;

    public LRUCache(final int capacity, final boolean isSynchronized, final Collection<Image> freeList) {
        unsynchronizedLRUMap = new LRULinkedHashMap(capacity);
        if (isSynchronized) {
            LRUMap = Collections.synchronizedMap(unsynchronizedLRUMap);
        } else {
            LRUMap = unsynchronizedLRUMap;
        }

        this.freeList = freeList;
    }

    @Override
    public void reset() {
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
    public void put(final long id, final Image image) {
        LRUMap.put(id, image);
    }

    @Override
    public Image get(final long id) {
        return LRUMap.get(id);
    }

    @Override
    public boolean contains(final long id) {
        return LRUMap.containsKey(id);
    }

    private class LRULinkedHashMap extends LinkedHashMap<Long, Image> {
        private static final long serialVersionUID = 1L;
        private int capacity;

        public LRULinkedHashMap(final int capacity) {
            super(capacity + 1, .75F, true);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(final Map.Entry<Long, Image> eldest) {
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
