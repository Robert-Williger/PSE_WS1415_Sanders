package adminTool.quadtree;

import static adminTool.quadtree.IQuadtree.childSize;
import static adminTool.quadtree.IQuadtree.childX;
import static adminTool.quadtree.IQuadtree.childY;
import java.util.PrimitiveIterator.OfInt;

import adminTool.quadtree.policies.IQuadtreePolicy;
import util.IntList;

public class DynamicQuadtree extends AbstractQuadtree implements IQuadtree {

    private DynamicQuadtree[] children;

    public DynamicQuadtree() {
        super();
        this.children = null;
    }

    private DynamicQuadtree(final IntList elements, final int maxHeight, final int maxElementsPerTile,
            final IQuadtreePolicy policy, final int height, final double x, final double y, final double size) {
        super(distribute(elements, policy, height, x, y, size));
        this.children = null;
    }

    private static IntList distribute(final IntList elements, final IQuadtreePolicy policy, final int height,
            final double x, final double y, final double size) {
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

    public void add(final int index, final int maxHeight, final int maxElements, final IQuadtreePolicy policy,
            final int height, final double x, final double y, final double size, final ElementConsumer consumer) {
        final double childSize = childSize(size);
        if (isLeaf()) {
            if (elements.size() < maxElements || height == maxHeight) {
                elements.add(index);
                consumer.consume(elements, x, y, size);
                return;
            }
            children = new DynamicQuadtree[IQuadtree.NUM_CHILDREN];
            for (int c = 0; c < IQuadtree.NUM_CHILDREN; c++) {
                children[c] = new DynamicQuadtree(elements, maxHeight, maxElements, policy, height + 1,
                        childX(x, childSize, c), childY(y, childSize, c), childSize);
            }
            elements = null;
        }
        for (int c = 0; c < IQuadtree.NUM_CHILDREN; c++) {
            final double nx = childX(x, childSize, c);
            final double ny = childY(y, childSize, c);
            if (policy.intersects(index, height + 1, nx, ny, childSize))
                children[c].add(index, maxHeight, maxElements, policy, height + 1, nx, ny, childSize, consumer);
        }
    }
}
