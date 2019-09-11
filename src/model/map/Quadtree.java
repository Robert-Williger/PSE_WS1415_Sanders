package model.map;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.IntConsumer;

public class Quadtree implements IElementIterator {

    private final int[] data;
    private final int zoomOffset;

    public Quadtree(final int[] data, final int zoomOffset) {
        this.data = data;
        this.zoomOffset = zoomOffset;
    }

    @Override
    public Quadtree filter(final Predicate p) {
        int[] newData = new int[data.length];
        int newSize = fillRec(p, newData, 0, 0, zoomOffset);

        return new Quadtree(Arrays.copyOf(newData, newSize), zoomOffset);
    }

    private int fillRec(final Predicate p, final int[] newData, final int origAddress, final int newAddress,
            final int zoom) {
        final boolean origLeaf = isLeaf(data[origAddress]);
        final int origFirstData = origAddress + (origLeaf ? 1 : 4);
        final boolean newLeaf = origLeaf || p.cutOffTrees() && !hasElements(p, origFirstData, zoom, false);
        final int newFirstData = newAddress + (newLeaf ? 1 : 4);

        int retAddress = newFirstData;

        newData[newAddress] = -1;
        for (int i = origFirstData; data[i] != -1; ++i)
            if (p.test(data[i], zoom, origLeaf))
                newData[retAddress++] = data[i];
        newData[retAddress++] = -1;

        if (!newLeaf)
            for (int i = 0; i < 4; ++i) {
                newData[newAddress + i] = retAddress;
                retAddress = fillRec(p, newData, data[origAddress + i], retAddress, zoom + 1);
            }
        return retAddress;
    }

    @Override
    public IElementIterator sort(Comparator<Integer> idComparator) {
        int[] newData = Arrays.copyOf(data, data.length);
        sortRec(idComparator, newData, 0);
        return new Quadtree(newData, zoomOffset);
    }

    private void sortRec(final Comparator<Integer> c, final int[] newData, final int origAddress) {
        final boolean leaf = isLeaf(data[origAddress]);
        final int firstData = origAddress + (leaf ? 1 : 4);
        int lastData = firstData;
        for (; data[lastData] != -1; ++lastData) {}

        System.arraycopy(Arrays.stream(newData, firstData, lastData + 1).boxed().sorted(c).mapToInt(i -> i).toArray(),
                0, data, firstData, lastData - firstData);

        if (!isLeaf(data[origAddress]))
            for (int i = 0; i < 4; ++i) {
                sortRec(c, newData, data[origAddress + i]);
            }
    }

    private boolean hasElements(final Predicate p, int dataStart, int zoom, boolean leaf) {
        for (int i = dataStart; data[i] != -1; ++i)
            if (p.test(data[i], zoom, leaf))
                return true;
        return false;
    }

    private int firstData(final int row, final int column, final int zoom) {
        int address = 0;

        for (int i = zoom - 1; i >= 0; i--) {
            if (isLeaf(data[address]))
                return address + 1;

            final int xChoice = (column >> i) & 1;
            final int yChoice = (row >> i) & 1;
            final int choice = xChoice + (yChoice << 1);

            address = data[address + choice];
        }

        return address + (isLeaf(data[address]) ? 1 : 4);
    }

    private boolean isLeaf(final int node) {
        return node == -1;
    }

    @Override
    public OfInt iterator(final int row, final int column, int zoom) {
        zoom -= zoomOffset;

        return isInvalid(row, column, zoom) ? new EmptyIterator() : new DataIterator(firstData(row, column, zoom));
    }

    @Override
    public void forEach(int row, int column, int zoom, IntConsumer consumer) {
        zoom -= zoomOffset;
        if (isInvalid(row, column, zoom)) {
            return;
        }

        for (int i = firstData(row, column, zoom); data[i] != -1; i++) {
            consumer.accept(data[i]);
        }
    }

    private boolean isInvalid(final int row, final int column, int zoom) {
        int maxValue = (1 << zoom) - 1;
        return row > maxValue || column > maxValue || row < 0 || column < 0;
    }

    private class DataIterator implements OfInt {
        private int address;

        public DataIterator(final int startAddress) {
            this.address = startAddress;
        }

        @Override
        public boolean hasNext() {
            return data[address] != -1;
        }

        @Override
        public int nextInt() {
            return data[address++];
        }

    }

    private class EmptyIterator implements OfInt {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public int nextInt() {
            return -1;
        }
    }

}
