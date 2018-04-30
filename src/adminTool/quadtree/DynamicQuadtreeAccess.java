package adminTool.quadtree;

public class DynamicQuadtreeAccess {
    private final DynamicQuadtree root;
    private final int maxHeight;
    private final int maxElementsPerTile;
    private final int size;
    private final IQuadtreePolicy policy;

    public DynamicQuadtreeAccess(final IQuadtreePolicy policy, final int maxHeight, final int maxElementsPerTile,
            final int size) {
        this.maxHeight = maxHeight;
        this.maxElementsPerTile = maxElementsPerTile;
        this.policy = policy;
        this.size = size;
        this.root = new DynamicQuadtree();
    }

    public DynamicQuadtree getRoot() {
        return root;
    }

    public void add(final int index) {
        root.add(index, maxHeight, maxElementsPerTile, policy, 0, 0, 0, size);
    }

    public int getSize() {
        return size;
    }

}
