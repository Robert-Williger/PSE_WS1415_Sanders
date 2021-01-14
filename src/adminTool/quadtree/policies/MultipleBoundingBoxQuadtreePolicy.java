package adminTool.quadtree.policies;

import java.awt.geom.Rectangle2D;
import java.util.List;

public class MultipleBoundingBoxQuadtreePolicy implements IQuadtreePolicy {

    private final List<List<Rectangle2D>> bounds;

    public MultipleBoundingBoxQuadtreePolicy(final List<List<Rectangle2D>> bounds) {
        this.bounds = bounds;
    }

    @Override
    public boolean intersects(final int index, final int height, final double x, final double y, final double size) {
        return getBounds(index, height).intersects(x, y, size, size);
    }

    private Rectangle2D getBounds(final int index, final int height) {
        return bounds.get(height).get(index);
    }

}
