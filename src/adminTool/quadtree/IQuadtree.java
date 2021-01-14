package adminTool.quadtree;

import util.IntList;

public interface IQuadtree {

    static final int NUM_CHILDREN = 4;

    boolean isLeaf();

    IQuadtree getChild(final int child);

    IntList getElements();

    void traverse(double x, double y, double size, ElementConsumer consumer);

    default IQuadtree locate(double x, double y, double size, final double px, final double py) {
        IQuadtree ret = this;

        while (!ret.isLeaf()) {
            size = childSize(size);
            final int child = child(x, y, size, px, py);
            ret = ret.getChild(child);
            x = childX(x, size, child);
            y = childY(y, size, child);
        }

        return ret;
    }

    static int xOffset(final int child) {
        return child & 1;
    }

    static int yOffset(final int child) {
        return child >> 1;
    }

    static int child(final int xOffset, final int yOffset) {
        return (yOffset << 1) | xOffset;
    }

    static int child(final double x, final double y, final double childSize, final double px, final double py) {
        int xOffset = px < x + childSize ? 0 : 1;
        int yOffset = py < y + childSize ? 0 : 1;

        return child(xOffset, yOffset);
    }

    static double childX(final double x, final double childSize, final int child) {
        return x + xOffset(child) * childSize;
    }

    static double childY(final double y, final double childSize, final int child) {
        return y + yOffset(child) * childSize;
    }

    static double childSize(final double size) {
        return size / 2;
    }

    @FunctionalInterface
    static interface ElementConsumer {
        void consume(final IntList elements, final double x, final double y, final double size);
    }
}
