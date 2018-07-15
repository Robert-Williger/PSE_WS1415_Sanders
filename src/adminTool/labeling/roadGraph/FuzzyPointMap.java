package adminTool.labeling.roadGraph;

import java.awt.Point;
import java.util.PrimitiveIterator;

import adminTool.elements.IPointAccess;
import adminTool.elements.UnboundedPointAccess;
import adminTool.quadtree.DynamicQuadtree;
import adminTool.quadtree.DynamicQuadtreeAccess;
import adminTool.quadtree.IQuadtreePolicy;
import adminTool.quadtree.SquareQuadtreePolicy;

public class FuzzyPointMap {
    private static final int DEFAULT_MAX_HEIGHT = 10;
    private static final int DEFAULT_MAX_ELEMENTS_PER_TILE = 32;
    private final long maxDistanceSq;
    private final DynamicQuadtreeAccess access;
    private final UnboundedPointAccess points;

    public FuzzyPointMap(final int maxX, final int maxY, final int maxDistance) {
        this(maxX, maxY, maxDistance, DEFAULT_MAX_HEIGHT, DEFAULT_MAX_ELEMENTS_PER_TILE);
    }

    public FuzzyPointMap(final int maxX, final int maxY, final int maxDistance, final int maxHeight,
            final int maxElementsPerTile) {
        this.maxDistanceSq = maxDistance * maxDistance;
        this.points = new UnboundedPointAccess();

        final IQuadtreePolicy policy = new SquareQuadtreePolicy(points, maxDistance);
        final int size = 1 << (int) Math.ceil(log2(Math.max(maxX, maxY)));
        this.access = new DynamicQuadtreeAccess(policy, maxHeight, maxElementsPerTile, size);
    }

    public int getPoint(final int x, final int y) {
        int ret = -1;

        DynamicQuadtree tree = access.getRoot();
        int qX = 0;
        int qY = 0;
        int qSize = access.getSize();
        while (!tree.isLeaf()) {
            qSize /= 2;
            final int xOffset = x < qX + qSize ? 0 : 1;
            final int yOffset = y < qY + qSize ? 0 : 1;
            tree = tree.getChild(xOffset, yOffset);
            qX += xOffset * qSize;
            qY += yOffset * qSize;
        }

        double minDistanceSq = maxDistanceSq;

        for (final PrimitiveIterator.OfInt iterator = tree.getElements().iterator(); iterator.hasNext();) {
            final int index = iterator.nextInt();
            double distanceSq = Point.distanceSq(points.getX(index), points.getY(index), x, y);
            if (distanceSq < minDistanceSq) {
                ret = index;
                minDistanceSq = distanceSq;
            }
        }
        return ret;
    }

    public void addPoint(final int x, final int y) {
        points.addPoint(x, y);
        access.add(points.getPoints() - 1);
    }

    public IPointAccess getPoints() {
        return points;
    }

    private final double log2(final double value) {
        return (Math.log(value) / Math.log(2));
    }
}
