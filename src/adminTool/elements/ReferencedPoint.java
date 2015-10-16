package adminTool.elements;

public class ReferencedPoint {

    private static int idCount = -1;

    private final int id;
    private final int x;
    private final int y;

    public ReferencedPoint(final int x, final int y) {
        this(x, y, ++idCount);
    }

    protected ReferencedPoint(final int x, final int y, final int id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getID() {
        return id;
    }
}
