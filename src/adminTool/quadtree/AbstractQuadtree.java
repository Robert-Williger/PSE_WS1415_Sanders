package adminTool.quadtree;

import util.IntList;

public abstract class AbstractQuadtree implements IQuadtree {

    protected IntList elements;

    public AbstractQuadtree(final IntList elements) {
        this.elements = elements;
    }

    public AbstractQuadtree() {
        this(new IntList());
    }

    @Override
    public abstract AbstractQuadtree getChild(final int child);

    @Override
    public IntList getElements() {
        return elements;
    }

    @Override
    public void traverse(final double x, final double y, final double size, final ElementConsumer consumer) {
        if (isLeaf())
            consumer.consume(elements, x, y, size);
        else {
            final double childSize = IQuadtree.childSize(size);
            for (int c = 0; c < IQuadtree.NUM_CHILDREN; ++c) {
                getChild(c).traverse(IQuadtree.childX(x, childSize, c), IQuadtree.childY(y, childSize, c), childSize,
                        consumer);
            }
        }
    }
}
