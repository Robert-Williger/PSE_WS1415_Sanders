package adminTool.quadtree;

import adminTool.elements.IPointAccess;
import adminTool.util.IntersectionUtil;

public class PointQuadtreePolicy implements IQuadtreePolicy {
    private final IPointAccess points;

    public PointQuadtreePolicy(final IPointAccess points, final int radius) {
        this.points = points;
    }

    @Override
    public boolean intersects(final int index, final int height, final int x, final int y, final int size) {
        return IntersectionUtil.rectangleContainsPoint(x, y, x + size, y + size, points.getX(index),
                points.getY(index));
    }

}
