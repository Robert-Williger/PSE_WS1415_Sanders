package adminTool.util;

import java.awt.Point;
import java.util.PrimitiveIterator;

import adminTool.elements.IPointAccess;
import adminTool.elements.PointAccess;
import adminTool.quadtree.DynamicQuadtreeAccess;
import adminTool.quadtree.IQuadtree;
import adminTool.quadtree.policies.IQuadtreePolicy;
import adminTool.quadtree.policies.SquareQuadtreePolicy;
import util.IntList;

public class FuzzyPointMap {
    private static final int DEFAULT_MAX_HEIGHT = 20;
    private static final int DEFAULT_MAX_ELEMENTS = 4;
    private static final int DEFAULT_NULL_ELEMENT = -1;
    private final double maxDistanceSq;
    private final DynamicQuadtreeAccess access;
    private final PointAccess points;
    private final IntList map;
    private final int nullElement;

    public FuzzyPointMap(final double maxX, final double maxY, final double maxDistance) {
        this(maxX, maxY, maxDistance, DEFAULT_MAX_HEIGHT, DEFAULT_MAX_ELEMENTS, DEFAULT_NULL_ELEMENT);
    }

    public FuzzyPointMap(final double maxX, final double maxY, final double maxDistance, final int maxHeight,
            final int maxElementsPerTile, final int nullElement) {
        this.nullElement = nullElement;
        this.maxDistanceSq = maxDistance * maxDistance;
        this.points = new PointAccess();
        this.map = new IntList();

        final IQuadtreePolicy policy = new SquareQuadtreePolicy(points, maxDistance);
        final double size = Math.max(maxX, maxY);
        this.access = new DynamicQuadtreeAccess(policy, maxHeight, maxElementsPerTile, size);
    }

    public int get(final double x, final double y) {
        int ret = nullElement;

        IQuadtree tree = access.getRoot().locate(0, 0, access.getSize(), x, y);

        double minDistanceSq = maxDistanceSq;

        for (final PrimitiveIterator.OfInt iterator = tree.getElements().iterator(); iterator.hasNext();) {
            final int index = iterator.nextInt();
            double distanceSq = Point.distanceSq(points.getX(index), points.getY(index), x, y);
            if (distanceSq < minDistanceSq) {
                ret = map.get(index);
                minDistanceSq = distanceSq;
            }
        }
        return ret;
    }

    public void put(final double x, final double y, final int value) {
        points.addPoint(x, y);
        map.add(value);
        access.add(points.size() - 1);
    }

    public IPointAccess getPoints() {
        return points;
    }
}
