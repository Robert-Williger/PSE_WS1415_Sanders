package adminTool;

public class BoundedPointAccess implements IPointAccess {
    private final int[] points;

    public BoundedPointAccess(final int nodes) {
        this.points = new int[nodes << 1];
    }

    @Override
    public int getX(final int index) {
        return points[index << 1];
    }

    @Override
    public int getY(final int index) {
        return points[(index << 1) + 1];
    }

    public void setPoint(final int index, final int x, final int y) {
        points[index << 1] = x;
        points[(index << 1) + 1] = y;
    }

    @Override
    public int getPoints() {
        return points.length >> 1;
    }
}
