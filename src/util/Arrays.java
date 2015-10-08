package util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Arrays {

    private Arrays() {
    }

    public static <T> Iterator<T> iterator() {
        return new EmptyIterator<T>();
    }

    public static <T> Iterator<T> iterator(final T[] array) {
        return new ArrayIterator<T>(array);
    }

    public static <T> Iterator<T> iterator(final T[] array, final int limit) {
        return new LimitIterator<T>(array, limit);
    }

    public static <T> Iterator<T> iterator(final T[] array, final int[] subarray) {
        return new SubarrayIterator<T>(array, subarray);
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
