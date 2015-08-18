package model.elements;

import java.awt.Polygon;
import java.util.List;

public class Area extends MultiElement implements Typeable {

    protected Polygon polygon;
    private final int type;

    public Area(final List<Node> nodes, final int type) {
        super(nodes);

        this.type = type;
    }

    public Polygon getPolygon() {

        calculatePolygon();

        return polygon;
    }

    protected Polygon calculatePolygon(final List<Node> nodes) {
        final int[] xpoints = new int[nodes.size()];
        final int[] ypoints = new int[nodes.size()];

        int i = 0;

        for (final Node node : nodes) {
            xpoints[i] = node.getX();
            ypoints[i] = node.getY();
            i++;
        }

        return new Polygon(xpoints, ypoints, nodes.size());
    }

    private void calculatePolygon() {
        if (polygon == null) {
            polygon = calculatePolygon(getNodes());
        }
    }

    public int getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();

        // make sure polygon != null
        calculatePolygon();

        int polyHash = 0;
        for (int i = 0; i < polygon.npoints; i++) {
            polyHash = 7 * polyHash + polygon.xpoints[i];
            polyHash = 7 * polyHash + polygon.ypoints[i];
        }
        result = prime * result + polyHash;

        result = prime * result + type;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Area other = (Area) obj;

        // polygon is not compared because polygon doesn't override equals and
        // polygon is never null

        if (type != other.type) {
            return false;
        }
        return true;
    }
}