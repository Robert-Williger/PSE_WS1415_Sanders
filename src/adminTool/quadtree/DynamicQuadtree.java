package adminTool.quadtree;

import java.util.PrimitiveIterator.OfInt;

import util.IntList;

public class DynamicQuadtree extends AbstractQuadtree implements IQuadtree {

    private DynamicQuadtree[] children;

    public DynamicQuadtree() {
        super();
        this.children = null;
    }

    private DynamicQuadtree(final IntList elements, final int maxHeight, final int maxElementsPerTile,
            final IQuadtreePolicy policy, final double x, final double y, final int height, final double size) {
        super(distribute(elements, policy, x, y, height, size));
        this.children = null;
    }

    private static IntList distribute(final IntList elements, final IQuadtreePolicy policy, final double x,
            final double y, final int height, final double size) {
        final IntList ret = new IntList(elements.size());

        for (final OfInt iterator = elements.iterator(); iterator.hasNext();) {
            final int element = iterator.nextInt();
            if (policy.intersects(element, height, x, y, size)) {
                ret.add(element);
            }
        }

        return ret;
    }

    @Override
    public boolean isLeaf() {
        return children == null;
    }

    @Override
    public DynamicQuadtree getChild(final int child) {
        return children[child];
    }

    public void add(final int index, final int maxHeight, final int maxElementsPerTile, final IQuadtreePolicy policy,
            final double x, final double y, final int height, final double size) {
        if (isLeaf()) {
            elements.add(index);
            if (height < maxHeight && elements.size() > maxElementsPerTile) {
                final double halfSize = size / 2;
                children = new DynamicQuadtree[4];
                for (int i = 0; i < children.length; i++) {
                    children[i] = new DynamicQuadtree(elements, maxHeight, maxElementsPerTile, policy,
                            x + IQuadtree.getXOffset(i) * halfSize, y + IQuadtree.getYOffset(i) * halfSize, height + 1,
                            halfSize);
                }
                elements = null;
            }
        } else {
            final double halfSize = size / 2;
            for (int i = 0; i < children.length; i++) {
                final double nx = x + IQuadtree.getXOffset(i) * halfSize;
                final double ny = y + IQuadtree.getYOffset(i) * halfSize;
                if (policy.intersects(index, height + 1, nx, ny, halfSize)) {
                    children[i].add(index, maxHeight, maxElementsPerTile, policy, nx, ny, height + 1, halfSize);
                }
            }
        }
    }

}
