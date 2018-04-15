package adminTool.quadtree;

import adminTool.IPointAccess;
import adminTool.Util;
import adminTool.elements.MultiElement;

import static adminTool.Util.rectangleIntersectsLine;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class WayQuadtreePolicy extends BoundingBoxQuadtreePolicy {

    private final List<? extends MultiElement> ways;
    private final int[] maxWayCoordWidths;
    private final IPointAccess points;

    public WayQuadtreePolicy(final List<? extends MultiElement> ways, final IPointAccess points,
            final int maxElementsPerTile, final int maxWayCoordWidth, final int maxHeight) {
        this(ways, points, maxElementsPerTile, createUniformArray(maxWayCoordWidth, maxHeight));
    }

    public WayQuadtreePolicy(final List<? extends MultiElement> ways, final IPointAccess points,
            final int maxElementsPerTile, final int[] maxWayCoordWidths) {
        super(calculateBounds(ways, points), maxElementsPerTile, maxWayCoordWidths.length);
        this.ways = ways;
        this.maxWayCoordWidths = maxWayCoordWidths;
        this.points = points;
    }

    @Override
    public boolean intersects(final int index, final int height, final int x, final int y, final int size) {
        // respect way width by appending offset to tile borders
        final int maxWayCoordWidth = maxWayCoordWidths[height];
        final int rectX = x - maxWayCoordWidth / 2;
        final int rectY = y - maxWayCoordWidth / 2;
        final int rectSize = size + maxWayCoordWidth;

        if (!super.intersects(index, height, rectX, rectY, rectSize)) {
            return false;
        }

        final MultiElement way = ways.get(index);
        for (int i = 1; i < way.size(); i++) {
            if (rectangleIntersectsLine(rectX, rectY, rectSize, rectSize, points.getX(way.getNode(i - 1)),
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
            bounds.add(Util.getBounds(element, points));
        }
        return bounds;
    }

    private static int[] createUniformArray(final int maxWayCoordWidth, final int height) {
        final int[] ret = new int[height];
        for (int i = 0; i < height; ++i) {
            ret[i] = maxWayCoordWidth;
        }
        return ret;
    }
}
