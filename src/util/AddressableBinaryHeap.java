package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddressableBinaryHeap<T> implements IAddressablePriorityQueue<T> {
    private final List<Entry<T>> heap;
    private final HashMap<T, Entry<T>> mapping;

    public AddressableBinaryHeap() {
        this.heap = new ArrayList<>();
        this.mapping = new HashMap<>();
    }

    @Override
    public void addAll(final List<T> elements, final List<Double> priority) {
        for (int i = 0; i < elements.size(); i++) {
            double key;
            if (priority == null || i >= priority.size()) {
                key = Integer.MAX_VALUE;
            } else {
                key = priority.get(i);
            }
            final Entry<T> entry = new Entry<>(elements.get(i), Integer.MAX_VALUE, key);
            add(entry);
        }

        buildHeap();
    }

    @Override
    public void addAll(final List<T> elements) {
        addAll(elements, null);
    }

    @Override
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    @Override
    public int size() {
        return heap.size();
    }

    @Override
    public void insert(final T element, final double key) {
        final Entry<T> entry = new Entry<>(element, Integer.MAX_VALUE, key);
        add(entry);
        siftUp(getLastIndex());
    }

    @Override
    public void insert(final T element) {
        insert(element, Integer.MAX_VALUE);
    }

    @Override
    public T min() {
        T ret = null;

        if (size() > 0) {
            ret = heap.get(0).content;
        }
        return ret;
    }

    @Override
    public T deleteMin() {
        final T min = min();
        moveLastElementTo(0);
        siftDown(0);
        mapping.remove(min);

        return min;
    }

    @Override
    public boolean remove(final T element) {
        final Entry<T> entry = mapping.get(element);

        if (entry != null) {
            final int index = entry.index;
            moveLastElementTo(index);
            siftDown(index);
            mapping.remove(element);

            return true;
        }

        return false;
    }

    @Override
    public void changeKey(final T element, final double key) {
        final Entry<T> entry = mapping.get(element);
        if (entry != null) {
            if (entry.priority > key) {
                entry.setPriority(key);
                siftUp(entry.index);
            } else if (entry.priority < key) {
                entry.setPriority(key);
                siftDown(entry.index);
            }
        }
    }

    @Override
    public void merge(final IAddressablePriorityQueue<T> queue) {
        for (int i = 0; i <= queue.size(); i++) {
            insert(queue.deleteMin());
        }
    }

    @Override
    public boolean contains(final T element) {
        return mapping.containsKey(element);
    }

    private void swap(final int a, final int b) {
        final Entry<T> entryA = heap.get(a);
        final Entry<T> entryB = heap.get(b);
        entryA.setIndex(b);
        entryB.setIndex(a);
        this.heap.set(a, this.heap.set(b, entryA));
    }

    private int getParent(final int i) {
        return (i - 1) / 2;
    }

    private int getChildLeft(final int i) {
        return (i * 2) + 1;
    }

    private int getChildRight(final int i) {
        return (i * 2) + 2;
    }

    private void siftUp(final int index) {
        if (index != 0 && heap.get(getParent(index)).priority > heap.get(index).priority) {
            swap(getParent(index), index);
            siftUp(getParent(index));
        }
    }

    private void siftDown(final int index) {
        if (getChildLeft(index) < heap.size()) {
            int currentItem;
            if (getChildRight(index) > heap.size() || getChildRight(index) >= heap.size()
                    || heap.get(getChildLeft(index)).priority < heap.get(getChildRight(index)).priority) {
                currentItem = getChildLeft(index);
            } else {
                currentItem = getChildRight(index);
            }

            if (heap.get(index).priority > heap.get(currentItem).priority) {
                swap(index, currentItem);
                siftDown(currentItem);
            }
        }
    }

    private void buildHeap() {
        for (int i = heap.size() / 2; i >= 0; i--) {
            siftDown(i);
        }
    }

    private void add(final Entry<T> e) {
        e.setIndex(heap.size());
        mapping.put(e.content, e);
        heap.add(e);
    }

    private int getLastIndex() {
        return heap.size() - 1;
    }

    private void moveLastElementTo(final int index) {
        if (heap.size() > 1) {
            heap.get(getLastIndex()).setIndex(index);
            heap.set(index, heap.remove(getLastIndex()));
        } else if (heap.size() == 1 && index == 0) {
            heap.remove(0);
        }
    }

    private static class Entry<T> {
        private final T content;
        private int index;
        private double priority;

        private Entry(final T content, final int index, final double priority) {
            this.content = content;
            this.index = index;
            this.priority = priority;
        }

        public void setIndex(final int index) {
            this.index = index;
        }

        public void setPriority(final double priority) {
            this.priority = priority;
        }
    }

    @Override
    public void clear() {
        mapping.clear();
        heap.clear();
    }
}