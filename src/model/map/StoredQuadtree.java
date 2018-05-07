package model.map;

import java.util.PrimitiveIterator.OfLong;
import java.util.function.LongConsumer;

public class StoredQuadtree implements IElementIterator {

    private final int[] data;
    private final int zoomOffset;

    public StoredQuadtree(final int[] data, final int zoomOffset) {
        this.data = data;
        this.zoomOffset = zoomOffset;
    }

    private int getAddress(final int row, final int column, final int zoom) {
        int address = 0;

        for (int i = zoom - 1; i >= 0; i--) {
            final int xChoice = (column >> i) & 1;
            final int yChoice = (row >> i) & 1;
            final int choice = xChoice + 2 * yChoice;

            final int nodeAddress = data[address + choice];
            if (!exists(nodeAddress)) {
                return address;
            }

            address = nodeAddress;
        }

        return address;
    }

    private boolean exists(final int node) {
        return node != -1;
    }

    @Override
    public OfLong iterator(final int row, final int column, int zoom) {
        zoom -= zoomOffset;
        if (isInvalid(row, column, zoom)) {
            return new EmptyIterator();
        }

        return new DataIterator(getAddress(row, column, zoom) + 4);
    }

    @Override
    public void forEach(int row, int column, int zoom, LongConsumer consumer) {
        zoom -= zoomOffset;
        if (isInvalid(row, column, zoom)) {
            return;
        }

        for (int i = getAddress(row, column, zoom) + 4; data[i] != -1; i++) {
            consumer.accept(data[i]);
        }
    }

    private boolean isInvalid(final int row, final int column, int zoom) {
        int maxValue = (1 << zoom) - 1;
        return row > maxValue || column > maxValue || row < 0 || column < 0;
    }

    private class DataIterator implements OfLong {
        private int address;

        public DataIterator(final int startAddress) {
            this.address = startAddress;
        }

        @Override
        public boolean hasNext() {
            return data[address] != -1;
        }

        @Override
        public long nextLong() {
            return data[address++];
        }

    }

    private class EmptyIterator implements OfLong {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public long nextLong() {
            return -1;
        }
    }
}
