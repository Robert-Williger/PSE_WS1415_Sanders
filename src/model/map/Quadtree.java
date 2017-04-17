package model.map;

import java.util.PrimitiveIterator;
import java.util.function.LongConsumer;

import util.LongList;

public class Quadtree implements IElementIterator {
    private final int[] treeData;
    private final int[] elementData;
    private final int zoomOffset;

    private static final int SIZE_BITS = 16;
    private static final int LOWER_BITS = 0xFFFF;

    public Quadtree(final int[] treeData, final int[] elementData, final int zoomOffset) {
        this.treeData = treeData;
        this.elementData = elementData;
        this.zoomOffset = zoomOffset;
    }

    @Override
    public void forEach(int row, int column, int zoom, LongConsumer consumer) {
        zoom -= zoomOffset;
        int maxValue = (1 << zoom) - 1;
        if (row > maxValue || column > maxValue || row < 0 || column < 0) {
            return;
        }

        final long treeAddress = getAddress(row, column, zoom);

        int treeData = getTreeData(treeAddress);
        int elementIndex = getIndex(treeData);
        int sizeInformation = getElement(elementIndex);
        int size = (sizeInformation & LOWER_BITS) + (sizeInformation >>> SIZE_BITS);
        ++elementIndex;
        consume(consumer, elementIndex, elementIndex + size);

        if (isInnerNode(getTreeData(treeAddress))) {
            consumeRecursive(treeAddress, consumer);
        }
    }

    @Override
    public PrimitiveIterator.OfLong iterator(int row, int column, int zoom) {
        final LongList elements = new LongList();
        final LongConsumer consumer = (l) -> elements.add(l);
        forEach(row, column, zoom, consumer);

        return elements.iterator();
    }

    private long getAddress(final int row, final int column, final int zoom) {
        long address = 0;

        for (int i = zoom - 1; i >= 0; i--) {
            address = getTreeData(address + 1);

            final int xChoice = (column >> i) & 1;
            final int yChoice = (row >> i) & 1;
            final int choice = xChoice + 2 * yChoice;

            for (int j = 0; j < choice; j++) {
                address += getTreeData(address) & 1;
                ++address;
            }

            if (isChild(getTreeData(address))) {
                return address;
            }
        }

        return address;
    }

    private void consumeRecursive(long treeAddress, final LongConsumer consumer) {
        treeAddress = getTreeData(treeAddress + 1);
        for (int i = 0; i < 4; i++) {
            int treeData = getTreeData(treeAddress);
            int elementIndex = getIndex(treeData);
            int size = getElement(elementIndex) & LOWER_BITS;
            ++elementIndex;
            consume(consumer, elementIndex, elementIndex + size);

            if (isInnerNode(treeData)) {
                consumeRecursive(treeAddress, consumer);
                ++treeAddress;
            }

            ++treeAddress;
        }
    }

    private void consume(final LongConsumer consumer, int index, final int lastIndex) {
        while (index < lastIndex) {
            consumer.accept(getElement(index));
            ++index;
        }
    }

    private int getIndex(final int data) {
        return data >>> 1;
    }

    private int getElement(final int elementIndex) {
        return elementData[elementIndex];
    }

    private int getTreeData(final long treeAddress) {
        return treeData[(int) treeAddress];
    }

    private boolean isChild(final int data) {
        return (data & 1) == 0;
    }

    private boolean isInnerNode(final int data) {
        return (data & 1) == 1;
    }

}