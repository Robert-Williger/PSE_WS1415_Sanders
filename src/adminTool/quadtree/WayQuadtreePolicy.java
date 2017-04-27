package adminTool.quadtree;

import adminTool.elements.MultiElement;
import adminTool.elements.Node;
import static adminTool.Util.rectangleIntersectsLine;

public class WayQuadtreePolicy extends AbstractQuadtreePolicy {

    private final MultiElement[] ways;
    private final int[] maxWayCoordWidths;

    public WayQuadtreePolicy(final MultiElement[] ways, final int maxElementsPerTile, final int maxZoomSteps,
            final int[] maxWayCoordWidths) {
        super(maxElementsPerTile, maxZoomSteps);
        this.ways = ways;
        this.maxWayCoordWidths = maxWayCoordWidths;
    }

    @Override
    public boolean intersects(final int index, final int zoom, final int x, final int y, final int size) {
        final MultiElement way = ways[index];
        final Node[] nodes = way.getNodes();
        final int maxWayCoordWidth = maxWayCoordWidths[zoom];

        // respect way width by appending offset to tile borders
        final int rectX = x - maxWayCoordWidth / 2;
        final int rectY = y - maxWayCoordWidth / 2;
        final int rectSize = size + maxWayCoordWidth;

        for (int i = 1; i < way.size(); i++) {
            if (rectangleIntersectsLine(rectX, rectY, rectSize, rectSize, nodes[i - 1].getX(), nodes[i - 1].getY(),
                    nodes[i].getX(), nodes[i].getY())) {
                return true;
            }
        }
        return false;
    }
}
