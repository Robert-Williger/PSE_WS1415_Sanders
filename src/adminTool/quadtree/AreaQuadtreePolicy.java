package adminTool.quadtree;

import adminTool.elements.MultiElement;
import adminTool.elements.Node;

import static adminTool.Util.polygonContainsPoint;
import static adminTool.Util.rectangleIntersectsLine;

import java.awt.Rectangle;

public class AreaQuadtreePolicy extends BoundingBoxQuadtreePolicy {
    private final MultiElement[] elements;

    public AreaQuadtreePolicy(final MultiElement[] areas, final Rectangle[][] bounds, final int maxElementsPerTile) {
        super(bounds, maxElementsPerTile);
        this.elements = areas;
    }

    @Override
    public boolean intersects(final int index, final int height, final int x, final int y, final int size) {
        if (!super.intersects(index, height, x, y, size)) {
            return false;
        }

        final Node[] nodes = elements[index].getNodes();
        return edgeIntersection(nodes, x, y, size) || polygonContainsPoint(nodes, x, y);
    }

    private static boolean edgeIntersection(final Node[] nodes, final int x, final int y, final int size) {
        Node last = nodes[nodes.length - 1];
        for (final Node node : nodes) {
            if (rectangleIntersectsLine(x, y, size, size, node.getX(), node.getY(), last.getX(), last.getY())) {
                return true;
            }
            last = node;
        }

        return false;
    }
}
