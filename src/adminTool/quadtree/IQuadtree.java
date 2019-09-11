package adminTool.quadtree;

import util.IntList;

public interface IQuadtree {

    static final int NUM_CHILDREN = 4;

    boolean isLeaf();

    IQuadtree getChild(final int child);

    IntList getElements();

    void traverse(double size, ElementConsumer consumer);

    default IQuadtree getChild(final int xOffset, final int yOffset) {
        return getChild(getChildIndex(xOffset, yOffset));
    }

    static int getXOffset(final int child) {
        return child & 0b01;
    }

    static int getYOffset(final int child) {
        return (child & 0b10) >> 1;
    }

    static int getChildIndex(final int xOffset, final int yOffset) {
        return (yOffset << 1) | xOffset;
    }

    @FunctionalInterface
    static interface ElementConsumer {
        void consume(final IntList elements, final double x, final double y, final double size);
    }
}
