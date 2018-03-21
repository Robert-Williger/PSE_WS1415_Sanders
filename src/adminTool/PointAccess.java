package adminTool;

public class PointAccess {
    private final int[] points;

    public PointAccess(final int nodes) {
        this.points = new int[nodes << 1];
    }

    public int getX(final int index) {
        return points[index << 1];
    }

    public int getY(final int index) {
        return points[(index << 1) + 1];
    }
    
    public void set(final int index, final int x, final int y) {
        points[index << 1] = x;
        points[(index << 1) + 1] = y;
    }
    
    public int getSize() {
        return points.length >> 1;
    }
}
