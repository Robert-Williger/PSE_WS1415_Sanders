package adminTool;

public class ReferencedPOI {

    private static int idCount = -1;

    private final int id;
    private final int x;
    private final int y;

    public ReferencedPOI(final int x, final int y) {
        this.x = x;
        this.y = y;
        id = ++idCount;
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
