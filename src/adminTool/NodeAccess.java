package adminTool;

public class NodeAccess {
    private final double[] points;

    public NodeAccess(final int nodes) {
        this.points = new double[nodes << 1];
    }

    public double getLat(final int index) {
        return points[index << 1];
    }

    public double getLon(final int index) {
        return points[(index << 1) + 1];
    }
    
    public void set(final int index, final double lat, final double lon) {
        points[index << 1] = lat;
        points[(index << 1) + 1] = lon;
    }
    
    public int size() {
        return points.length >> 1;
    }
}
