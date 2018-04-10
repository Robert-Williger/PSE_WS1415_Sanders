package adminTool;

import util.IntList;

public class UnboundedPointAccess implements IPointAccess {
    private final IntList points;

    public UnboundedPointAccess() {
        this.points = new IntList();
    }

    @Override
    public int getX(final int index) {
        return points.get(index << 1);
    }

    @Override
    public int getY(final int index) {
        return points.get((index << 1) + 1);
    }

    public void addPoint(final int x, final int y) {
        points.add(x);
        points.add(y);
    }

    @Override
    public int getPoints() {
        return points.size() >> 1;
    }
}
