package adminTool.quadtree;

import java.util.PrimitiveIterator.OfInt;
import java.util.function.IntFunction;

import adminTool.quadtree.policies.IQuadtreePolicy;

public class CollisionlessQuadtree extends AbstractQuadtree implements IQuadtree {

    private CollisionlessQuadtree[] children;

    public CollisionlessQuadtree(final int elements, final IQuadtreePolicy qp, final ICollisionPolicy cp,
            final double size, final int maxHeight) {
        this(elements, qp, cp, size, e -> maxHeight);
    }

    public CollisionlessQuadtree(final int elements, final IQuadtreePolicy qp, final ICollisionPolicy cp,
            final double size, final IntFunction<Integer> maxElementHeight) {
        for (int element = 0; element < elements; element++) {
            add(element, maxElementHeight.apply(element), qp, cp, 0, 0, 0, size);
        }
    }

    private CollisionlessQuadtree() {}

    private void add(final int element, final int maxHeight, final IQuadtreePolicy qp, final ICollisionPolicy cp,
            final double x, final double y, final int height, final double size) {
        final int destHeight = getCollisionlessHeight(element, qp, cp, x, y, height, size);
        add(element, x, y, size, height, destHeight, maxHeight, qp);
    }

    private void add(final int element, final double x, final double y, final double size, final int height,
            final int destHeight, final int maxHeight, final IQuadtreePolicy qp) {
        if (qp.intersects(element, height, x, y, size)) {
            if (height >= destHeight) {
                elements.add(element);
            }

            if (height + 1 < maxHeight) {
                if (children == null) {
                    children = new CollisionlessQuadtree[4];
                    for (int i = 0; i < children.length; i++) {
                        children[i] = new CollisionlessQuadtree();
                    }
                }
                final double nSize = size / 2;
                final int nHeight = height + 1;
                for (int i = 0; i < children.length; i++) {
                    final double nx = x + (i % 2) * nSize;
                    final double ny = y + (i / 2) * nSize;
                    children[i].add(element, nx, ny, nSize, nHeight, destHeight, maxHeight, qp);
                }
            }
        }
    }

    private int getCollisionlessHeight(final int element, final IQuadtreePolicy qp, final ICollisionPolicy cp,
            final double x, final double y, final int height, final double size) {
        if (isLeaf() || !qp.intersects(element, height, x, y, size) || !intersects(element, cp, height)) {
            return height;
        }

        int ret = height + 1;
        final double nSize = size / 2;
        final int nHeight = height + 1;
        for (int i = 0; i < children.length; i++) {
            final double nx = x + (i % 2) * nSize;
            final double ny = y + (i / 2) * nSize;

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

    @Override
    public CollisionlessQuadtree getChild(int child) {
        return children[child];
    }
}
