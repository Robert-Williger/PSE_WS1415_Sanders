package util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

public final class Arrays {

    private Arrays() {}

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

    public static PrimitiveIterator.OfInt iterator(final int[] array) {
        return iterator(array, 0, array.length);
    }

    public static PrimitiveIterator.OfInt iterator(final int[] array, final int size) {
        return iterator(array, 0, size);
    }

    public static PrimitiveIterator.OfInt iterator(final int[] array, final int from, final int size) {
        return new IntegerArrayIterator(array, from, size);
    }

    public static PrimitiveIterator.OfLong iterator(final long[] array) {
        return iterator(array, 0, array.length);
    }

    public static PrimitiveIterator.OfLong iterator(final long[] array, final int size) {
        return iterator(array, 0, size);
    }

    public static PrimitiveIterator.OfLong iterator(final long[] array, final int from, final int size) {
        return new LongArrayIterator(array, from, size);
    }

    public static PrimitiveIterator.OfDouble iterator(final double[] array) {
        return iterator(array, 0, array.length);
    }

    public static PrimitiveIterator.OfDouble iterator(final double[] array, final int size) {
        return iterator(array, 0, size);
    }

    public static PrimitiveIterator.OfDouble iterator(final double[] array, final int from, final int size) {
        return new DoubleArrayIterator(array, from, size);
    }

    public static PrimitiveIterator.OfDouble iterator(final float[] array) {
        return iterator(array, 0, array.length);
    }

    public static PrimitiveIterator.OfDouble iterator(final float[] array, final int size) {
        return iterator(array, 0, size);
    }

    public static PrimitiveIterator.OfDouble iterator(final float[] array, final int from, final int size) {
        return new FloatArrayIterator(array, from, size);
    }

    public static PrimitiveIterator.OfInt descendingIterator(final int[] array) {
        return new ReversedPrimitiveIterator(array);
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

    private static class ReversedPrimitiveIterator implements PrimitiveIterator.OfInt {

        private final int[] array;
        private int cursor;

        public ReversedPrimitiveIterator(final int[] array) {
            this.array = array;
            this.cursor = array.length - 1;
        }

        @Override
        public boolean hasNext() {
            return cursor >= 0;
        }

        @Override
        public int nextInt() {
            return array[cursor--];
        }
    }

    private static class IntegerArrayIterator implements PrimitiveIterator.OfInt {
        private int cursor; // index of next element to return
        private final int size;
        private final int[] array;

        public IntegerArrayIterator(final int[] array, final int cursor, final int size) {
            this.cursor = cursor;
            this.size = size;
            this.array = array;
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
            return array[cursor++];
        }
    }

    private static class LongArrayIterator implements PrimitiveIterator.OfLong {
        private int cursor; // index of next element to return
        private final int size;
        private final long[] array;

        public LongArrayIterator(final long[] array, final int cursor, final int size) {
            this.cursor = cursor;
            this.size = size;
            this.array = array;
        }

        @Override
        public boolean hasNext() {
            return cursor != size;
        }

        @Override
        public long nextLong() {
            if (cursor >= size) {
                throw new NoSuchElementException();
            }
            return array[cursor++];
        }
    }

    private static class DoubleArrayIterator implements PrimitiveIterator.OfDouble {
        private int cursor; // index of next element to return
        private final int size;
        private final double[] array;

        public DoubleArrayIterator(final double[] array, final int cursor, final int size) {
            this.cursor = cursor;
            this.size = size;
            this.array = array;
        }

        @Override
        public boolean hasNext() {
            return cursor != size;
        }

        @Override
        public double nextDouble() {
            if (cursor >= size) {
                throw new NoSuchElementException();
            }
            return array[cursor++];
        }
    }

    private static class FloatArrayIterator implements PrimitiveIterator.OfDouble {
        private int cursor; // index of next element to return
        private final int size;
        private final float[] array;

        public FloatArrayIterator(final float[] array, final int cursor, final int size) {
            this.cursor = cursor;
            this.size = size;
            this.array = array;
        }

        @Override
        public boolean hasNext() {
            return cursor != size;
        }

        @Override
        public double nextDouble() {
            if (cursor >= size) {
                throw new NoSuchElementException();
            }
            return array[cursor++];
        }
    }
}
