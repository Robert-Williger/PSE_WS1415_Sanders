package util;

import java.util.PrimitiveIterator;
import java.util.function.IntConsumer;

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

    public IntList(final IntList list) {
        this.data = new int[list.data.length];
        System.arraycopy(list.data, 0, data, 0, data.length);
        this.size = list.size;
    }

    // list[from,to)
    public IntList(final IntList list, final int from, final int to) {
        if (to < from)
            throw new IllegalArgumentException("Illegal Interval: [" + from + ", " + to + ")");
        this.size = to - from;
        this.data = new int[size];
        System.arraycopy(list.data, from, data, 0, data.length);

    }

    public IntList() {
        this(DEFAULT_CAPACITY);
    }

    public int get(final int index) {
        rangeCheck(index);

        return data[index];
    }

    public void add(final int e) {
        ensureCapacityInternal(size + 1);
        data[size++] = e;
    }

    public void add(final int index, final int element) {
        rangeCheckForAdd(index);

        ensureCapacityInternal(size + 1);
        System.arraycopy(data, index, data, index + 1, size - index);
        data[index] = element;
        size++;
    }

    public int set(final int index, final int element) {
        rangeCheck(index);

        int oldValue = data[index];
        data[index] = element;
        return oldValue;
    }

    public boolean addAll(final IntList list) {
        int[] a = list.data;
        int numNew = list.size;
        ensureCapacityInternal(size + numNew); // Increments modCount
        System.arraycopy(a, 0, data, size, numNew);
        size += numNew;

        return numNew != 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public PrimitiveIterator.OfInt iterator() {
        return Arrays.iterator(data, size);
    }

    public PrimitiveIterator.OfInt iterator(final int index) {
        return Arrays.iterator(data, index, size - index);
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

    public void trimToSize() {
        if (size < data.length) {
            data = java.util.Arrays.copyOf(data, size);
        }
    }

    public void ensureCapacity(final int minCapacity) {
        int minExpand = 0;

        if (minCapacity > minExpand) {
            ensureExplicitCapacity(minCapacity);
        }
    }

    public boolean contains(final int o) {
        return indexOf(o) >= 0;
    }

    public int indexOf(final int o) {
        for (int i = 0; i < size; i++) {
            if (data[i] == o) {
                return i;
            }
        }

        return -1;
    }

    public int lastIndexOf(final int o) {
        for (int i = size - 1; i >= 0; i--) {
            if (data[i] == o) {
                return i;
            }
        }

        return -1;
    }

    public int removeIndex(final int index) {
        rangeCheck(index);

        int oldValue = data[index];

        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(data, index + 1, data, index, numMoved);
        --size;

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

    private void rangeCheck(final int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private void rangeCheckForAdd(final int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(final int index) {
        return "Index: " + index + ", Size: " + size;
    }

    public void forEach(final IntConsumer consumer) {
        for (int i = 0; i < size; ++i) {
            consumer.accept(data[i]);
        }
    }
}
