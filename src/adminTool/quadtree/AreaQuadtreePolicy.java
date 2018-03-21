package adminTool.quadtree;

import adminTool.PointAccess;
import adminTool.elements.MultiElement;

import static adminTool.Util.polygonContainsPoint;
import static adminTool.Util.rectangleIntersectsLine;

import java.awt.Rectangle;

public class AreaQuadtreePolicy extends BoundingBoxQuadtreePolicy {
    private final MultiElement[] elements;
    private final PointAccess points;

    public AreaQuadtreePolicy(final MultiElement[] areas, final PointAccess points, final Rectangle[][] bounds,
            final int maxElementsPerTile) {
        super(bounds, maxElementsPerTile);
        this.elements = areas;
        this.points = points;
    }

    @Override
    public boolean intersects(final int index, final int height, final int x, final int y, final int size) {
        if (!super.intersects(index, height, x, y, size)) {
            return false;
        }

        final MultiElement element = elements[index];
        return edgeIntersection(element, x, y, size) || polygonContainsPoint(element, points, x, y);
    }

    private boolean edgeIntersection(final MultiElement element, final int x, final int y, final int size) {
        int last = element.size() - 1;
        for (int i = 0; i < element.size(); ++i) {
            if (rectangleIntersectsLine(x, y, size, size, points.getX(element.getNode(i)),
                    points.getY(element.getNode(i)), points.getX(element.getNode(last)),
                    points.getY(element.getNode(last)))) {
                return true;
            }
            last = i;
        }

        return false;
    }
}
