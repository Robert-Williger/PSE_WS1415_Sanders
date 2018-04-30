package adminTool.quadtree;

import adminTool.IPointAccess;
import adminTool.elements.MultiElement;
import adminTool.util.IntersectionUtil;

import static adminTool.util.IntersectionUtil.polygonContainsPoint;
import static adminTool.util.IntersectionUtil.rectangleIntersectsSegment;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class AreaQuadtreePolicy extends BoundingBoxQuadtreePolicy {
    private final List<MultiElement> elements;
    private final IPointAccess points;

    public AreaQuadtreePolicy(final List<MultiElement> areas, final IPointAccess points) {
        super(calculateBounds(areas, points));
        this.elements = areas;
        this.points = points;
    }

    @Override
    public boolean intersects(final int index, final int height, final int x, final int y, final int size) {
        if (!super.intersects(index, height, x, y, size)) {
            return false;
        }

        final MultiElement element = elements.get(index);
        return edgeIntersection(element, x, y, size) || polygonContainsPoint(element, points, x, y);
    }

    private boolean edgeIntersection(final MultiElement element, final int x, final int y, final int size) {
        int last = element.size() - 1;
        for (int i = 0; i < element.size(); ++i) {
            if (rectangleIntersectsSegment(x, y, size, size, points.getX(element.getNode(i)),
                    points.getY(element.getNode(i)), points.getX(element.getNode(last)),
                    points.getY(element.getNode(last)))) {
                return true;
            }
            last = i;
        }

        return false;
    }

    private static List<Rectangle> calculateBounds(final List<MultiElement> elements, final IPointAccess points) {
        final List<Rectangle> bounds = new ArrayList<Rectangle>(elements.size());
        for (int i = 0; i < elements.size(); i++) {
            bounds.add(IntersectionUtil.getBounds(elements.get(i), points));
        }
        return bounds;
    }
}
