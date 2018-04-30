package adminTool.quadtree;

import adminTool.IPointAccess;
import adminTool.util.IntersectionUtil;

public class SquareQuadtreePolicy implements IQuadtreePolicy {
    private final IPointAccess points;
    private final int length;

    public SquareQuadtreePolicy(final IPointAccess points, final int radius) {
        this.points = points;
        this.length = radius;
    }

    @Override
    public boolean intersects(final int index, final int height, final int x, final int y, final int size) {
        // respect radius by appending offset to tile borders
        final int rectX = x - length;
        final int rectY = y - length;
        final int rectSize = size + 2 * length;

        return IntersectionUtil.rectangleContainsPoint(rectX, rectY, rectX + rectSize, rectY + rectSize,
                points.getX(index), points.getY(index));
    }

}
