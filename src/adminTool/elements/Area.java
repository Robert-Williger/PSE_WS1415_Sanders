package adminTool.elements;

import java.awt.Polygon;

public class Area extends MultiElement implements Typeable {

    private final int type;

    public Area(final Node[] nodes, final int type) {
        super(nodes);

        this.type = type;
    }

    protected Polygon calculatePolygon(final Node[] nodes) {
        final int[] xpoints = new int[nodes.length];
        final int[] ypoints = new int[nodes.length];

        int i = 0;

        for (final Node node : nodes) {
            xpoints[i] = node.getX();
            ypoints[i] = node.getY();
            i++;
        }

        return new Polygon(xpoints, ypoints, nodes.length);
    }

    public int getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + type;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof Area)) {
            return false;
        }
        Area other = (Area) obj;
        if (type != other.type) {
            return false;
        }
        return true;
    }

}