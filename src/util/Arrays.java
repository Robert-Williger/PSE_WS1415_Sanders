package util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class Arrays {

    private Arrays() {
    }

    public static <T> Iterator<T> iterator() {
        return new EmptyIterator<>();
    }

    public static <T> Iterator<T> iterator(final T[] array) {
        return new ArrayIterator<>(array);
    }

    public static <T> Iterator<T> descendingIterator(final T[] array) {
        return new ReversedIterator<>(array);
    }

    public static <T> Iterator<T> iterator(final T[] array, final int limit) {
        return new LimitIterator<>(array, limit);
    }

    public static <T> Iterator<T> iterator(final T[] array, final int[] subarray) {
        return new SubarrayIterator<>(array, subarray);
    }

    public static <T> void reverse(final T[] array) {
        for (int i = 0, mid = array.length >> 1, j = array.length - 1; i < mid; i++, j--) {
            T copy = array[j];
            array[j] = array[i];
            array[i] = copy;
        }
    }

    public static void reverse(final int[] array) {
        for (int i = 0, mid = array.length >> 1, j = array.length - 1; i < mid; i++, j--) {
            int copy = array[j];
            array[j] = array[i];
            array[i] = copy;
        }
    }

    private static class ArrayIterator<T> implements Iterator<T> {

        private final T[] array;
        private int count;

        public ArrayIterator(final T[] array) {
            this.array = array;
        }

        @Override
        public boolean hasNext() {
            return count < array.length;
        }

        @Override
        public T next() {
            return array[count++];
        }

    }

    private static class ReversedIterator<T> implements Iterator<T> {

        private final T[] array;
        private int count;

        public ReversedIterator(final T[] array) {
            this.array = array;
            this.count = array.length - 1;
        }

        @Override
        public boolean hasNext() {
            return count >= 0;
        }

        @Override
        public T next() {
            return array[count--];
        }

    }

    private static class SubarrayIterator<T> implements Iterator<T> {

        private final T[] array;
        private final int[] subArray;
        private int count;

        public SubarrayIterator(final T[] array, final int[] subArray) {
            this.array = array;
            this.subArray = subArray;
        }

        @Override
        public boolean hasNext() {
            return count < subArray.length;
        }

        @Override
        public T next() {
            return array[subArray[count++]];
        }
    }

    private static class EmptyIterator<T> implements Iterator<T> {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public T next() {
            throw new NoSuchElementException();
        }

    }

    private static class LimitIterator<T> implements Iterator<T> {

        private final T[] array;
        private int count;
        private final int limit;

        public LimitIterator(final T[] array, final int limit) {
            this.array = array;
            this.limit = limit;
        }

        @Override
        public boolean hasNext() {
            return count < limit;
        }

        @Override
        public T next() {
            return array[count++];
        }

    }
}
