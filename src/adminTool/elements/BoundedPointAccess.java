package adminTool.elements;

public class BoundedPointAccess implements IPointAccess {
    private final double[] points;

    public BoundedPointAccess(final int nodes) {
        this.points = new double[nodes << 1];
    }

    @Override
    public double getX(final int index) {
        return points[index << 1];
    }

    @Override
    public double getY(final int index) {
        return points[(index << 1) + 1];
    }

    @Override
    public void set(final int index, final double x, final double y) {
        points[index << 1] = x;
        points[(index << 1) + 1] = y;
    }

    @Override
    public int size() {
        return points.length >> 1;
    }

}
