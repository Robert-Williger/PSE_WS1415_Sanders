package adminTool.quadtree;

import java.awt.Rectangle;
import java.util.List;

import adminTool.quadtree.IQuadtreePolicy;

public class BoundingBoxQuadtreePolicy implements IQuadtreePolicy {

    private final List<Rectangle> bounds;

    public BoundingBoxQuadtreePolicy(final List<Rectangle> bounds) {
        this.bounds = bounds;
    }

    @Override
    public boolean intersects(final int index, final int height, final int x, final int y, final int size) {
        return bounds.get(index).intersects(x, y, size, size);
    }

}