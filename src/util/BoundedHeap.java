package util;

import java.util.ArrayList;

public class BoundedHeap<T extends Comparable<T>> {
    private final ArrayList<T> heap;
    private final int capacity;

    public BoundedHeap(final int capacity) {
        this.heap = new ArrayList<>();
        this.capacity = capacity;
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public int size() {
        return heap.size();
    }

    public boolean insert(final T element) {
        final int size = heap.size();
        if (size < capacity) {
            heap.add(element);
            siftUp(size);
            return true;
        } else if (element.compareTo(max()) < 0) {
            heap.set(0, element);
            siftDown(0);
            return true;
        }

        return false;
    }

    public T max() {
        return !isEmpty() ? heap.get(0) : null;
    }

    public T deleteMax() {
        final T max = max();

        final int size = size();
        if (size > 1) {
            heap.set(0, heap.remove(size - 1));
            siftDown(0);
        } else {
            heap.clear();
        }

        return max;
    }

    private void swap(final int a, final int b) {
        this.heap.set(a, this.heap.set(b, heap.get(a)));
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
        if (index != 0 && heap.get(getParent(index)).compareTo(heap.get(index)) < 0) {
            swap(getParent(index), index);
            siftUp(getParent(index));
        }
    }

    private void siftDown(final int index) {
        int leftChild = getChildLeft(index);
        int rightChild = getChildRight(index);
        if (leftChild < heap.size()) {
            final int swapIndex;
            if (rightChild >= heap.size() || heap.get(leftChild).compareTo(heap.get(rightChild)) > 0) {
                swapIndex = getChildLeft(index);
            } else {
                swapIndex = getChildRight(index);
            }

            if (heap.get(index).compareTo(heap.get(swapIndex)) < 0) {
                swap(index, swapIndex);
                siftDown(swapIndex);
            }
        }
    }
}