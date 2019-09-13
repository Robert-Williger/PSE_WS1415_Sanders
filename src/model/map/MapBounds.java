package model.map;

public class MapBounds implements IMapBounds {

    private final int width;
    private final int height;
    private final int minZoom;
    private final int maxZoom;
    private final int x;
    private final int y;

    public MapBounds(int x, int y, int width, int height, int minZoom, int maxZoom) {
        super();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getMaxZoom() {
        return maxZoom;
    }

    @Override
    public int getMinZoom() {
        return minZoom;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

}
