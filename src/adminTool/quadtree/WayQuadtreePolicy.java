package adminTool.quadtree;

import adminTool.elements.IPointAccess;
import adminTool.elements.MultiElement;
import adminTool.util.IntersectionUtil;

import static adminTool.util.IntersectionUtil.rectangleIntersectsSegment;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class WayQuadtreePolicy extends BoundingBoxQuadtreePolicy {

    private final List<? extends MultiElement> ways;
    private final IWayWidthInfo widthInfo;
    private final IPointAccess points;

    public WayQuadtreePolicy(final List<? extends MultiElement> ways, final IPointAccess points,
            final IWayWidthInfo widthInfo) {
        super(calculateBounds(ways, points));
        this.ways = ways;
        this.widthInfo = widthInfo;
        this.points = points;
    }

    public WayQuadtreePolicy(final List<? extends MultiElement> ways, final IPointAccess points,
            final int[] maxWayCoordWidths) {
        this(ways, points, (index, height) -> maxWayCoordWidths[height]);
    }

    @Override
    public boolean intersects(final int index, final int height, final int x, final int y, final int size) {
        // respect way width by appending offset to tile borders
        final int maxWayCoordWidth = widthInfo.getWidth(index, height);
        final int rectX = x - maxWayCoordWidth / 2;
        final int rectY = y - maxWayCoordWidth / 2;
        final int rectSize = size + maxWayCoordWidth;

        if (!super.intersects(index, height, rectX, rectY, rectSize)) {
            return false;
        }

        final MultiElement way = ways.get(index);
        for (int i = 1; i < way.size(); i++) {
            if (rectangleIntersectsSegment(rectX, rectY, rectSize, rectSize, points.getX(way.getNode(i - 1)),
                    points.getY(way.getNode(i - 1)), points.getX(way.getNode(i)), points.getY(way.getNode(i)))) {
                return true;
            }
        }
        return false;
    }

    private static List<Rectangle> calculateBounds(final List<? extends MultiElement> elements,
            final IPointAccess points) {
        final List<Rectangle> bounds = new ArrayList<Rectangle>(elements.size());
        for (final MultiElement element : elements) {
            final Rectangle bb = IntersectionUtil.getBounds(element, points);
            // avoid cases with zero width or height - BoundingBoxQuadtreePolicy always would return false
            bb.width = Math.max(1, bb.width);
            bb.height = Math.max(1, bb.height);
            bounds.add(bb);
        }
        return bounds;
    }

    @FunctionalInterface
    public interface IWayWidthInfo {
        int getWidth(final int index, final int height);
    }
}
