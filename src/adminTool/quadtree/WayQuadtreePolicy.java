package adminTool.quadtree;

import adminTool.PointAccess;
import adminTool.elements.MultiElement;

import static adminTool.Util.rectangleIntersectsLine;

import java.awt.Rectangle;

public class WayQuadtreePolicy extends BoundingBoxQuadtreePolicy {

    private final MultiElement[] ways;
    private final int[] maxWayCoordWidths;
    private final PointAccess points;

    public WayQuadtreePolicy(final MultiElement[] ways, final PointAccess points, final Rectangle[][] bounds,
            final int maxElementsPerTile, final int[] maxWayCoordWidths) {
        super(bounds, maxElementsPerTile);
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

        final MultiElement way = ways[index];
        for (int i = 1; i < way.size(); i++) {
            if (rectangleIntersectsLine(rectX, rectY, rectSize, rectSize, points.getX(way.getNode(i - 1)),
                    points.getY(way.getNode(i - 1)), points.getX(way.getNode(i)), points.getY(way.getNode(i)))) {
                return true;
            }
        }
        return false;
    }
}
