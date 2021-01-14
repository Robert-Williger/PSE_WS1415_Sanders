package adminTool.quadtree;

import java.util.PrimitiveIterator.OfInt;

import adminTool.quadtree.policies.IQuadtreePolicy;
import util.IntList;

public class Quadtree extends AbstractQuadtree implements IQuadtree {
    private final Quadtree[] children;

    public Quadtree(final int elements, final IQuadtreePolicy policy, final double size, final int maxHeight,
            final int maxElementsPerTile) {
        this.elements = new IntList(elements);
        for (int i = 0; i < elements; i++) {
            this.elements.add(i);
        }

        children = createChildren(policy, 0, 0, 0, size, maxHeight, maxElementsPerTile);
    }

    private Quadtree(final IntList elements, final IQuadtreePolicy policy, final double x, final double y,
            final int height, final double size, final int maxHeight, final int maxElementsPerTile) {
        this.elements = new IntList();
        for (final OfInt iterator = elements.iterator(); iterator.hasNext();) {
            final int element = iterator.nextInt();
            if (policy.intersects(element, height, x, y, size)) {
                this.elements.add(element);
            }
        }

        children = createChildren(policy, x, y, height, size, maxHeight, maxElementsPerTile);
    }

    private Quadtree[] createChildren(final IQuadtreePolicy policy, final double x, final double y, final int height,
            final double size, final int maxHeight, final int maxElementsPerTile) {
        final Quadtree[] children;
        if (elements.size() > maxElementsPerTile && height + 1 < maxHeight) {
            final double childSize = IQuadtree.childSize(size);
            children = new Quadtree[NUM_CHILDREN];
            for (int c = 0; c < children.length; c++) {
                children[c] = new Quadtree(elements, policy, IQuadtree.childX(x, childSize, c),
                        IQuadtree.childY(y, childSize, c), height + 1, childSize, maxHeight, maxElementsPerTile);
            }
        } else {
            children = null;
        }

        return children;
    }

    @Override
    public boolean isLeaf() {
        return children == null;
    }

    @Override
    public Quadtree getChild(final int child) {
        return children[child];
    }
}
