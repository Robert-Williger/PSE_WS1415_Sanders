package adminTool.quadtree;

public abstract class AbstractQuadtreePolicy implements IQuadtreePolicy {

    private final int maxElementsPerTile;
    private final int maxZoomSteps;

    public AbstractQuadtreePolicy(final int maxElementsPerTile, final int maxZoomSteps) {
        this.maxElementsPerTile = maxElementsPerTile;
        this.maxZoomSteps = maxZoomSteps;
    }

    @Override
    public int getMaxElementsPerTile() {
        return maxElementsPerTile;
    }

    @Override
    public int getMaxZoomSteps() {
        return maxZoomSteps;
    }

}
