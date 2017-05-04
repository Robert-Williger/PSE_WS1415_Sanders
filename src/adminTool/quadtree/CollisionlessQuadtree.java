package adminTool.quadtree;

import java.util.PrimitiveIterator.OfInt;

import util.IntList;

public class CollisionlessQuadtree {

    private CollisionlessQuadtree[] children;
    private final IntList elements;

    public CollisionlessQuadtree(final int elements, final IQuadtreePolicy quadtreePolicy,
            final ICollisionPolicy collisionPolicy, final int size) {
        this.elements = new IntList();
        for (int element = 0; element < elements; element++) {
            add(element, quadtreePolicy, collisionPolicy, 0, 0, 0, size);
        }
    }

    private CollisionlessQuadtree() {
        elements = new IntList();
    }

    private void add(final int element, final IQuadtreePolicy qp, final ICollisionPolicy cp, final int x, final int y,
            final int height, final int size) {
        if (qp.intersects(element, height, x, y, size)) {
            final int destHeight = getCollisionlessHeight(element, qp, cp, x, y, height, size);
            if (destHeight < qp.getMaxHeight()) {
                add(element, x, y, size, height, destHeight, qp);
            }
        }
    }

    private void add(final int element, final int x, final int y, final int size, final int height,
            final int destHeight, final IQuadtreePolicy qp) {
        if (height >= destHeight && qp.intersects(element, height, x, y, size)) {
            elements.add(element);

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
        if (children == null || !intersects(element, cp, height)) {
            return height;
        }

        int ret = height + 1;
        final int nSize = size / 2;
        final int nHeight = height + 1;
        for (int i = 0; i < children.length; i++) {
            final int nx = x + (i % 2) * nSize;
            final int ny = y + (i / 2) * nSize;
            if (qp.intersects(element, nHeight, nx, ny, nSize)) {
                ret = Math.max(children[i].getCollisionlessHeight(element, qp, cp, nx, ny, nHeight, nSize), ret);
            }
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
}
