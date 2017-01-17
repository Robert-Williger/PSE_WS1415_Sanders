package adminTool.map;

import java.awt.Rectangle;
import java.io.DataOutput;

import adminTool.elements.Area;
import adminTool.elements.Node;

public class AreaQuadtreeWriter extends AbstractQuadtreeWriter {

    private final Area[] areas;
    private final Rectangle rect;

    public AreaQuadtreeWriter(final Area[] areas, final int[] addresses, final DataOutput dataOutput,
            final DataOutput treeOutput, final int maxElementsPerTile, final int maxZoomSteps, final int coordMapSize) {
        super(addresses, dataOutput, treeOutput, maxElementsPerTile, maxZoomSteps, coordMapSize);
        this.areas = areas;
        this.rect = new Rectangle();
    }

    @Override
    protected boolean intersects(final int index, final int zoom, final int x, final int y, final int size) {
        rect.setBounds(x, y, size, size);
        final Area area = areas[index];
        final Node[] nodes = area.getNodes();
        for (int i = 1; i < area.size(); i++) {
            if (rect.intersectsLine(nodes[i - 1].getX(), nodes[i - 1].getY(), nodes[i].getX(), nodes[i].getY())) {
                return true;
            }
        }
        return false;
    }
}
