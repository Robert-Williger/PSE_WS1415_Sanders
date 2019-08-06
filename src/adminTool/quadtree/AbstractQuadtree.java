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
    public void traverse(final double size, final ElementConsumer consumer) {
        traverse(consumer, 0, 0, size);
    }

    private void traverse(final ElementConsumer consumer, final double x, final double y, final double size) {
        if (isLeaf())
            consumer.consume(elements, x, y, size);
        else {
            final double hs = size / 2;
            for (int i = 0; i < IQuadtree.NUM_CHILDREN; ++i) {
                getChild(i).traverse(consumer, x + IQuadtree.getXOffset(i) * hs, y + IQuadtree.getYOffset(i) * hs, hs);
            }
        }
    }
}
