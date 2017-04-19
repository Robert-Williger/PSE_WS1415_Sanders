package adminTool.map;

import java.util.zip.ZipOutputStream;

import adminTool.Util;
import adminTool.elements.Area;
import adminTool.elements.Node;

public class AreaQuadtreeWriter extends AbstractQuadtreeWriter {

    private final Area[] areas;

    public AreaQuadtreeWriter(final Area[] areas, final ZipOutputStream zipOutput, final String name,
            final int maxElementsPerTile, final int maxZoomSteps, final int coordMapSize) {
        super(zipOutput, name, areas.length, maxElementsPerTile, maxZoomSteps, coordMapSize);
        this.areas = areas;
    }

    @Override
    protected boolean intersects(final int index, final int zoom, final int x, final int y, final int size) {
        final Node[] nodes = areas[index].getNodes();

        return edgeIntersection(nodes, x, y, size) || rectangleInPolygon(nodes, x, y);
    }

    private static boolean rectangleInPolygon(final Node[] nodes, final int x, final int y) {
        boolean ret = false;
        for (int i = 0, j = nodes.length - 1; i < nodes.length; j = i++) {
            if (((nodes[i].getY() > y) != (nodes[j].getY() > y)) && (x < (nodes[j].getX() - nodes[i].getX())
                    * (y - nodes[i].getY()) / (nodes[j].getY() - nodes[i].getY()) + nodes[i].getX()))
                ret = !ret;
        }
        return ret;
    }

    private static boolean edgeIntersection(final Node[] nodes, final int x, final int y, final int size) {
        Node last = nodes[nodes.length - 1];
        for (final Node node : nodes) {
            if (Util.rectangleIntersectsLine(x, y, size, size, node.getX(), node.getY(), last.getX(), last.getY())) {
                return true;
            }
            last = node;
        }

        return false;
    }
}
