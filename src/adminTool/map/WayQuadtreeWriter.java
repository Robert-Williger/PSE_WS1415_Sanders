package adminTool.map;

import java.awt.Rectangle;
import java.io.DataOutput;

import adminTool.elements.Node;
import adminTool.elements.Way;

public class WayQuadtreeWriter extends AbstractQuadtreeWriter {

    private final Way[] ways;
    private final Rectangle rect;

    public WayQuadtreeWriter(final Way[] ways, final int[] addresses, final DataOutput dataOutput,
            final DataOutput treeOutput, final int maxElementsPerTile, final int maxZoomSteps, final int coordMapSize) {
        super(addresses, dataOutput, treeOutput, maxElementsPerTile, maxZoomSteps, coordMapSize);
        this.ways = ways;
        rect = new Rectangle();
    }

    @Override
    protected boolean intersects(final int index, final int zoom, final int x, final int y, final int size) {
        // TODO ways have thickness
        rect.setBounds(x, y, size, size);
        final Way way = ways[index];
        final Node[] nodes = way.getNodes();
        for (int i = 1; i < way.size(); i++) {
            if (rect.intersectsLine(nodes[i - 1].getX(), nodes[i - 1].getY(), nodes[i].getX(), nodes[i].getY())) {
                return true;
            }
        }
        return false;
    }
}