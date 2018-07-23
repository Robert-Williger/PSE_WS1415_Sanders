package adminTool.elements;

import util.DoubleList;

public class PointAccess implements IPointAccess {

    private final DoubleList points;

    public PointAccess(final int initialCapacity) {
        this.points = new DoubleList(initialCapacity);
    }

    public PointAccess() {
        this.points = new DoubleList();
    }

    @Override
    public double getX(final int index) {
        return points.get(index << 1);
    }

    @Override
    public double getY(final int index) {
        return points.get((index << 1) + 1);
    }

    @Override
    public void set(final int index, final double x, final double y) {
        points.set(index << 1, x);
        points.set((index << 1) + 1, y);
    }

    @Override
    public int size() {
        return points.size() >> 1;
    }

    public void addPoint(final double x, final double y) {
        points.add(x);
        points.add(y);
    }

}
