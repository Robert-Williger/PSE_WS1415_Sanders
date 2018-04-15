package adminTool.quadtree;

import java.util.PrimitiveIterator.OfInt;

import util.IntList;

public class Quadtree {

    private final Quadtree[] children;
    private final IntList elements;

    private static final int xOffsets[] = new int[] { 0, 1, 0, 1 };
    private static final int yOffsets[] = new int[] { 0, 0, 1, 1 };

    public Quadtree(final int elements, final IQuadtreePolicy policy, final int size) {
        this.elements = new IntList(elements);
        for (int i = 0; i < elements; i++) {
            this.elements.add(i);
        }

        children = createChildren(this.elements, policy, 0, 0, 0, size);
    }

    private Quadtree(final IntList elements, final IQuadtreePolicy policy, final int x, final int y, final int height,
            final int size) {
        this.elements = new IntList();

        for (final OfInt iterator = elements.iterator(); iterator.hasNext();) {
            final int element = iterator.nextInt();
            if (policy.intersects(element, height, x, y, size)) {
                this.elements.add(element);
            }
        }

        children = createChildren(this.elements, policy, x, y, height, size);
    }

    private Quadtree[] createChildren(final IntList list, final IQuadtreePolicy policy, final int x, final int y,
            final int height, final int size) {
        final Quadtree[] children;
        if (elements.size() > policy.getMaxElementsPerTile() && height + 1 < policy.getMaxHeight()) {
            final int halfSize = size / 2;
            children = new Quadtree[4];
            for (int i = 0; i < children.length; i++) {
                children[i] = new Quadtree(elements, policy, x + getXOffset(i) * halfSize, y + getYOffset(i) * halfSize,
                        height + 1, halfSize);
            }
        } else {
            children = null;
        }

        return children;
    }

    public boolean isLeaf() {
        return children == null;
    }

    public Quadtree[] getChildren() {
        return children;
    }

    public IntList getElements() {
        return elements;
    }

    public static int getXOffset(final int child) {
        return xOffsets[child];
    }

    public static int getYOffset(final int child) {
        return yOffsets[child];
    }
}
