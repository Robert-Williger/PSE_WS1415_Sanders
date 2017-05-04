package model.map;

import java.awt.Rectangle;

public class BoundingBoxQuadtreePolicy implements IQuadtreePolicy {

    private final Rectangle[][] bounds;
    private final int maxElementsPerTile;

    public BoundingBoxQuadtreePolicy(final Rectangle[][] bounds, final int maxElementsPerTile) {
        this.maxElementsPerTile = maxElementsPerTile;
        this.bounds = bounds;
    }

    @Override
    public int getMaxElementsPerTile() {
        return maxElementsPerTile;
    }

    @Override
    public int getMaxHeight() {
        return bounds.length;
    }

    @Override
    public boolean intersects(final int index, final int height, final int x, final int y, final int size) {
        return bounds[height][index].intersects(x, y, size, size);
    }

}
