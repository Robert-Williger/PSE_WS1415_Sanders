package adminTool;

public class ReferencedRectangle implements Comparable<ReferencedRectangle> {
    private static int idCount = -1;
    private final int id;

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    // Rectangle with (x, y) in coordinates and (width, height) in pixels
    public ReferencedRectangle(final int coordX, final int coordY, final int pixelWidth, final int pixelHeight) {
        this.x = coordX;
        this.y = coordY;
        this.width = pixelWidth;
        this.height = pixelHeight;
        this.id = ++idCount;
    }

    public int getID() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public int compareTo(final ReferencedRectangle o) {
        return id - o.id;
    }
}
