package adminTool.quadtree;

import java.awt.geom.Rectangle2D;
import java.util.List;

import adminTool.quadtree.policies.IQuadtreePolicy;

public class BoundingBoxQuadtreePolicy implements IQuadtreePolicy {

    private final List<Rectangle2D> bounds;

    public BoundingBoxQuadtreePolicy(final List<Rectangle2D> bounds) {
        this.bounds = bounds;
    }

    @Override
    public boolean intersects(final int index, final int height, final double x, final double y, final double size) {
        return bounds.get(index).intersects(x, y, size, size);
    }

}