package adminTool.quadtree.policies;

import adminTool.elements.IPointAccess;
import adminTool.util.IntersectionUtil;

public class SquareQuadtreePolicy implements IQuadtreePolicy {
    private final IPointAccess points;
    private final double length;

    public SquareQuadtreePolicy(final IPointAccess points, final double radius) {
        this.points = points;
        this.length = radius;
    }

    @Override
    public boolean intersects(final int index, final int height, final double x, final double y, final double size) {
        // respect radius by appending offset to tile borders
        final double rectX = x - length;
        final double rectY = y - length;
        final double rectSize = size + 2 * length;

        return IntersectionUtil.rectangleContainsPoint(rectX, rectY, rectX + rectSize, rectY + rectSize,
                points.getX(index), points.getY(index));
    }

}
