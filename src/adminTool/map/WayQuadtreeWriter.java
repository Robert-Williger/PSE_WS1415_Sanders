package adminTool.map;

import java.awt.Rectangle;
import java.io.DataOutput;

import adminTool.elements.Node;
import adminTool.elements.Way;

public class WayQuadtreeWriter extends AbstractQuadtreeWriter {

    private final Way[] ways;
    private final Rectangle rect;
    private final int[] maxWayCoordWidths;

    public WayQuadtreeWriter(final Way[] ways, final int[] addresses, final DataOutput dataOutput,
            final DataOutput treeOutput, final int maxElementsPerTile, final int maxZoomSteps, final int coordMapSize,
            final int[] maxWayCoordWidths) {
        super(addresses, dataOutput, treeOutput, maxElementsPerTile, maxZoomSteps, coordMapSize);
        this.ways = ways;
        this.maxWayCoordWidths = maxWayCoordWidths;
        rect = new Rectangle();
    }

    @Override
    protected boolean intersects(final int index, final int zoom, final int x, final int y, final int size) {
        // TODO ways have thickness

        final Way way = ways[index];
        final Node[] nodes = way.getNodes();
        final int maxWayCoordWidth = maxWayCoordWidths[zoom];

        // respect way width by appending offset to tile borders
        rect.setBounds(x - maxWayCoordWidth / 2, y - maxWayCoordWidth / 2, size + maxWayCoordWidth,
                size + maxWayCoordWidth);

        for (int i = 1; i < way.size(); i++) {
            if (rect.intersectsLine(nodes[i - 1].getX(), nodes[i - 1].getY(), nodes[i].getX(), nodes[i].getY())) {
                return true;
            }
        }
        return false;
    }
}