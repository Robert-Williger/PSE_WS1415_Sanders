package util;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

public class IntList {
    private static final int DEFAULT_CAPACITY = 10;
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private int[] data;
    private int size;

    public IntList(final int[] data) {
        this.data = data;
        this.size = data.length;
    }

    public IntList(int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        this.data = new int[initialCapacity];
    }

    public IntList() {
        this(DEFAULT_CAPACITY);
    }

    public void trimToSize() {
        if (size < data.length) {
            data = java.util.Arrays.copyOf(data, size);
        }
    }

    public boolean addAll(final IntList list) {
        int[] a = list.data;
        int numNew = list.size;
        ensureCapacityInternal(size + numNew); // Increments modCount
        System.arraycopy(a, 0, data, size, numNew);
        size += numNew;

        return numNew != 0;
    }

    public void ensureCapacity(int minCapacity) {
        int minExpand = 0;

        if (minCapacity > minExpand) {
            ensureExplicitCapacity(minCapacity);
        }
    }

    private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(minCapacity);
    }

    private void ensureExplicitCapacity(int minCapacity) {
        if (minCapacity - data.length > 0)
            grow(minCapacity);
    }

    private void grow(int minCapacity) {
        int newCapacity = data.length + (data.length >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);

        data = java.util.Arrays.copyOf(data, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(int o) {
        return indexOf(o) >= 0;
    }

    public int indexOf(int o) {

        for (int i = 0; i < size; i++) {
            if (data[i] == o) {
                return i;
            }
        }

        return -1;
    }

    public int lastIndexOf(int o) {
        for (int i = size - 1; i >= 0; i--) {
            if (data[i] == o) {
                return i;
            }
        }

        return -1;
    }

    public int get(int index) {
        rangeCheck(index);

        return data[index];
    }

    public int set(int index, int element) {
        rangeCheck(index);

        int oldValue = data[index];
        data[index] = element;
        return oldValue;
    }

    public void add(int e) {
        ensureCapacityInternal(size + 1);
        data[size++] = e;
    }

    public void add(int index, int element) {
        rangeCheckForAdd(index);

        ensureCapacityInternal(size + 1);
        System.arraycopy(data, index, data, index + 1, size - index);
        data[index] = element;
        size++;
    }

    public int removeIndex(int index) {
        rangeCheck(index);

        int oldValue = data[index];

        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(data, index + 1, data, index, numMoved);

        return oldValue;
    }

    public boolean removeElement(int o) {
        for (int index = 0; index < size; index++) {
            if (o == data[index]) {
                fastRemove(index);
                return true;
            }
        }

        return false;
    }

    public void clear() {
        size = 0;
    }

    public void reverse() {
        for (int i = 0, mid = size >> 1, j = size - 1; i < mid; i++, j--) {
            int copy = data[j];
            data[j] = data[i];
            data[i] = copy;
        }
    }

    private void fastRemove(int index) {
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(data, index + 1, data, index, numMoved);
    }

    protected void removeRange(int fromIndex, int toIndex) {
        int numMoved = size - toIndex;
        System.arraycopy(data, toIndex, data, fromIndex, numMoved);
        int newSize = size - (toIndex - fromIndex);

        size = newSize;
    }

    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size;
    }

    public PrimitiveIterator.OfInt iterator() {
        return new Itr();
    }

    public PrimitiveIterator.OfInt iterator(final int index) {
        return new Itr();
    }

    public int[] toArray() {
        int[] ret = new int[size];
        System.arraycopy(data, 0, ret, 0, size);
        return ret;
    }

    @Override
    public String toString() {
        int iMax = size - 1;
        if (iMax == -1)
            return "IntList: []";

        StringBuilder b = new StringBuilder();
        b.append("IntList: [");
        for (int i = 0;; i++) {
            b.append(data[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }

    }

    private class Itr implements PrimitiveIterator.OfInt {
        int cursor; // index of next element to return

        public Itr() {
            this(0);
        }

        public Itr(final int cursor) {
            this.cursor = cursor;
        }

        @Override
        public boolean hasNext() {
            return cursor != size;
        }

        @Override
        public int nextInt() {
            if (cursor >= size) {
                throw new NoSuchElementException();
            }
            return data[cursor++];
        }
    }
}
