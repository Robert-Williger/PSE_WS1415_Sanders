package adminTool.quadtree;

import java.awt.Rectangle;
import java.util.List;

import adminTool.quadtree.IQuadtreePolicy;

public class MultipleBoundingBoxQuadtreePolicy implements IQuadtreePolicy {

    private final List<List<Rectangle>> bounds;

    public MultipleBoundingBoxQuadtreePolicy(final List<List<Rectangle>> bounds) {
        this.bounds = bounds;
    }

    @Override
    public boolean intersects(final int index, final int height, final int x, final int y, final int size) {
        return getBounds(index, height).intersects(x, y, size, size);
    }

    private Rectangle getBounds(final int index, final int height) {
        return bounds.get(height).get(index);
    }

}
