package adminTool.quadtree;

import adminTool.quadtree.IQuadtree.ElementConsumer;
import adminTool.quadtree.policies.IQuadtreePolicy;

public class DynamicQuadtreeAccess {
    private final DynamicQuadtree root;
    private final int maxHeight;
    private final int maxElements;
    private final double size;
    private final IQuadtreePolicy policy;

    public DynamicQuadtreeAccess(final IQuadtreePolicy policy, final int maxHeight, final int maxElements,
            final double size) {
        this.maxHeight = maxHeight;
        this.maxElements = maxElements;
        this.policy = policy;
        this.size = size;
        this.root = new DynamicQuadtree();
    }

    public DynamicQuadtree getRoot() {
        return root;
    }

    public void add(final int index) {
        add(index, (elements, x, y, size) -> {});
    }

    public void add(final int index, final ElementConsumer consumer) {
        root.add(index, maxHeight, maxElements, policy, 0, 0, 0, size, consumer);
    }

    public double getSize() {
        return size;
    }

}
