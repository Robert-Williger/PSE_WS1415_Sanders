package adminTool.quadtree;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import adminTool.quadtree.IQuadtreePolicy;

public class BoundingBoxQuadtreePolicy implements IQuadtreePolicy {

    private final List<List<Rectangle>> bounds;
    private final int maxElementsPerTile;

    public BoundingBoxQuadtreePolicy(final List<Rectangle> bounds, final int maxElementsPerTile, final int maxHeight) {
        this(createUniformBounds(bounds, maxHeight), maxElementsPerTile);
    }

    public BoundingBoxQuadtreePolicy(final List<List<Rectangle>> bounds, final int maxElementsPerTile) {
        this.maxElementsPerTile = maxElementsPerTile;
        this.bounds = bounds;
    }

    @Override
    public int getMaxElementsPerTile() {
        return maxElementsPerTile;
    }

    @Override
    public int getMaxHeight() {
        return bounds.size();
    }

    @Override
    public boolean intersects(final int index, final int height, final int x, final int y, final int size) {
        return bounds.get(height).get(index).intersects(x, y, size, size);
    }

    private static List<List<Rectangle>> createUniformBounds(final List<Rectangle> bounds, final int maxHeight) {
        final List<List<Rectangle>> ret = new ArrayList<List<Rectangle>>(maxHeight);
        for (int i = 0; i < maxHeight; ++i) {
            ret.add(bounds);
        }
        return ret;
    }

}
