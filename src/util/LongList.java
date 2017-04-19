package util;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

public class LongList {
    private static final int DEFAULT_CAPACITY = 10;
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private long[] data;
    private int size;

    public LongList(int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        this.data = new long[initialCapacity];
    }

    public LongList() {
        this(DEFAULT_CAPACITY);
    }

    public boolean addAll(final LongList list) {
        long[] a = list.data;
        int numNew = list.size;
        ensureCapacityInternal(size + numNew); // Increments modCount
        System.arraycopy(a, 0, data, size, numNew);
        size += numNew;

        return numNew != 0;
    }

    public void trimToSize() {
        if (size < data.length) {
            data = java.util.Arrays.copyOf(data, size);
        }
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

    public boolean contains(long o) {
        return indexOf(o) >= 0;
    }

    public int indexOf(long o) {

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

    public long get(int index) {
        rangeCheck(index);

        return data[index];
    }

    public long set(int index, long element) {
        rangeCheck(index);

        long oldValue = data[index];
        data[index] = element;
        return oldValue;
    }

    public void add(long e) {
        ensureCapacityInternal(size + 1);
        data[size++] = e;
    }

    public void add(int index, long element) {
        rangeCheckForAdd(index);

        ensureCapacityInternal(size + 1);
        System.arraycopy(data, index, data, index + 1, size - index);
        data[index] = element;
        size++;
    }

    public long removeIndex(int index) {
        rangeCheck(index);

        long oldValue = data[index];

        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(data, index + 1, data, index, numMoved);

        return oldValue;
    }

    public boolean removeElement(long o) {
        for (int index = 0; index < size; index++) {
            if (o == data[index]) {
                fastRemove(index);
                return true;
            }
        }

        return false;
    }

    private void fastRemove(int index) {
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(data, index + 1, data, index, numMoved);
    }

    public void clear() {
        size = 0;
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

    public PrimitiveIterator.OfLong iterator() {
        return new Itr();
    }

    private class Itr implements PrimitiveIterator.OfLong {
        int cursor; // index of next element to return

        @Override
        public boolean hasNext() {
            return cursor != size;
        }

        @Override
        public long nextLong() {
            if (cursor >= size) {
                throw new NoSuchElementException();
            }
            return data[cursor++];
        }
    }
}
