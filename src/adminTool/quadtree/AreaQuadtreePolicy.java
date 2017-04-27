package adminTool.quadtree;

import adminTool.elements.MultiElement;
import adminTool.elements.Node;

import static adminTool.Util.polygonContainsPoint;
import static adminTool.Util.polygonBBContainsPoint;
import static adminTool.Util.rectangleIntersectsLine;

public class AreaQuadtreePolicy extends AbstractQuadtreePolicy {
    private final MultiElement[] elements;

    public AreaQuadtreePolicy(final MultiElement[] areas, final int maxElementsPerTile, final int maxZoomSteps) {
        super(maxElementsPerTile, maxZoomSteps);
        this.elements = areas;
    }

    @Override
    public boolean intersects(final int index, final int zoom, final int x, final int y, final int size) {
        final Node[] nodes = elements[index].getNodes();

        return edgeIntersection(nodes, x, y, size) || (polygonBBContainsPoint(nodes, x, y) && polygonContainsPoint(nodes, x, y));
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
