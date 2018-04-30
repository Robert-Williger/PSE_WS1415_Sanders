package adminTool.quadtree;

import java.util.PrimitiveIterator.OfInt;

import util.IntList;

public class DynamicQuadtree implements IQuadtree {
    private DynamicQuadtree[] children;
    private IntList elements;

    DynamicQuadtree() {
        this.children = null;
        this.elements = new IntList();
    }

    private DynamicQuadtree(final IntList elements, final int maxHeight, final int maxElementsPerTile,
            final IQuadtreePolicy policy, final int x, final int y, final int height, final int size) {
        this.children = null;
        this.elements = new IntList(elements.size());

        for (final OfInt iterator = elements.iterator(); iterator.hasNext();) {
            final int element = iterator.nextInt();
            if (policy.intersects(element, height, x, y, size)) {
                this.elements.add(element);
            }
        }
    }

    @Override
    public boolean isLeaf() {
        return children == null;
    }

    @Override
    public DynamicQuadtree getChild(final int child) {
        return children[child];
    }

    @Override
    public IntList getElements() {
        return elements;
    }

    public DynamicQuadtree getChild(final int xOffset, final int yOffset) {
        return getChild(IQuadtree.getChildIndex(xOffset, yOffset));
    }

    public void add(final int index, final int maxHeight, final int maxElementsPerTile, final IQuadtreePolicy policy,
            final int x, final int y, final int height, final int size) {
        if (isLeaf()) {
            elements.add(index);
            if (height < maxHeight && elements.size() > maxElementsPerTile) {
                final int halfSize = size / 2;
                children = new DynamicQuadtree[4];
                for (int i = 0; i < children.length; i++) {
                    children[i] = new DynamicQuadtree(elements, maxHeight, maxElementsPerTile, policy,
                            x + IQuadtree.getXOffset(i) * halfSize, y + IQuadtree.getYOffset(i) * halfSize, height + 1,
                            halfSize);
                }
                elements = null;
            }
        } else {
            final int halfSize = size / 2;
            for (int i = 0; i < children.length; i++) {
                final int nx = x + IQuadtree.getXOffset(i) * halfSize;
                final int ny = y + IQuadtree.getYOffset(i) * halfSize;
                if (policy.intersects(index, height + 1, nx, ny, halfSize)) {
                    children[i].add(index, maxHeight, maxElementsPerTile, policy, nx, ny, height + 1, halfSize);
                }
            }
        }
    }

}
