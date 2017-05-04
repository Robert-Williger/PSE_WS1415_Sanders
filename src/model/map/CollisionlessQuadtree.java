package model.map;

import java.util.PrimitiveIterator.OfInt;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.LongConsumer;

import util.IntList;

public class CollisionlessQuadtree implements IElementIterator {

    private CollisionlessQuadtree[] children;
    private static int minZoomStep;
    private final IntList elements;

    public CollisionlessQuadtree(final int elements, final IQuadtreePolicy qp, final ICollisionPolicy cp,
            final int size, final int minZoom) {
        this.elements = new IntList();

        for (int element = 0; element < elements; element++) {
            add(element, qp, cp, 0, 0, 0, size);
        }

        // TODO improve this.
        minZoomStep = minZoom;
    }

    private CollisionlessQuadtree() {
        elements = new IntList();
    }

    private void add(final int element, final IQuadtreePolicy qp, final ICollisionPolicy cp, final int x, final int y,
            final int height, final int size) {
        final int destHeight = getCollisionlessHeight(element, qp, cp, x, y, height, size);
        add(element, x, y, size, height, destHeight, qp);
    }

    private void add(final int element, final int x, final int y, final int size, final int height,
            final int destHeight, final IQuadtreePolicy qp) {
        if (qp.intersects(element, height, x, y, size)) {
            if (height >= destHeight) {
                elements.add(element);
            }

            if (height + 1 < qp.getMaxHeight()) {
                if (children == null) {
                    children = new CollisionlessQuadtree[4];
                    for (int i = 0; i < children.length; i++) {
                        children[i] = new CollisionlessQuadtree();
                    }
                }
                final int nSize = size / 2;
                final int nHeight = height + 1;
                for (int i = 0; i < children.length; i++) {
                    final int nx = x + (i % 2) * nSize;
                    final int ny = y + (i / 2) * nSize;
                    children[i].add(element, nx, ny, nSize, nHeight, destHeight, qp);
                }
            }
        }
    }

    private int getCollisionlessHeight(final int element, final IQuadtreePolicy qp, final ICollisionPolicy cp,
            final int x, final int y, final int height, final int size) {
        if (isLeaf() || !qp.intersects(element, height, x, y, size) || !intersects(element, cp, height)) {
            return height;
        }

        int ret = height + 1;
        final int nSize = size / 2;
        final int nHeight = height + 1;
        for (int i = 0; i < children.length; i++) {
            final int nx = x + (i % 2) * nSize;
            final int ny = y + (i / 2) * nSize;

            ret = Math.max(children[i].getCollisionlessHeight(element, qp, cp, nx, ny, nHeight, nSize), ret);
        }

        return ret;
    }

    private boolean intersects(final int element, final ICollisionPolicy collisionPolicy, final int height) {
        for (final OfInt iterator = elements.iterator(); iterator.hasNext();) {
            if (collisionPolicy.intersect(element, iterator.nextInt(), height)) {
                return true;
            }
        }

        return false;
    }

    public boolean isLeaf() {
        return children == null;
    }

    public CollisionlessQuadtree[] getChildren() {
        return children;
    }

    public IntList getElements() {
        return elements;
    }

    @Override
    public OfLong iterator(int row, int column, int zoom) {
        return new It(getTree(row, column, zoom).getElements().iterator());
    }

    @Override
    public void forEach(int row, int column, int zoom, LongConsumer consumer) {
        for (final OfInt iterator = getTree(row, column, zoom).getElements().iterator(); iterator.hasNext();) {
            consumer.accept(iterator.nextInt());
        }
    }

    private CollisionlessQuadtree getTree(final int row, final int column, int zoom) {
        zoom -= minZoomStep;
        CollisionlessQuadtree tree = this;
        for (int i = zoom - 1; i >= 0; i--) {
            if (tree.isLeaf()) {
                return tree;
            }
            final int xChoice = (column >> i) & 1;
            final int yChoice = (row >> i) & 1;
            final int choice = xChoice + 2 * yChoice;

            tree = tree.children[choice];
        }

        return tree;
    }

    private static class It implements OfLong {
        private final OfInt iterator;

        public It(final OfInt iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public long nextLong() {
            return iterator.nextInt();
        }

    }
}
