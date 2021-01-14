package adminTool.quadtree.policies;

import adminTool.elements.IPointAccess;
import adminTool.util.IntersectionUtil;

public class PointQuadtreePolicy implements IQuadtreePolicy {
    private final IPointAccess points;

    public PointQuadtreePolicy(final IPointAccess points, final int radius) {
        this.points = points;
    }

    @Override
    public boolean intersects(final int index, final int height, final double x, final double y, final double size) {
        return IntersectionUtil.rectangleContainsPoint(x, y, x + size, y + size, points.getX(index),
                points.getY(index));
    }

}
