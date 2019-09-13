package model.map;

public class MapSection implements IMapSection {

    private int midX;
    private int midY;
    private int width;
    private int height;
    private int zoom;

    @Override
    public int getMidX() {
        return midX;
    }

    @Override
    public int getMidY() {
        return midY;
    }

    @Override
    public void setMidpoint(int midX, int midY) {
        this.midX = midX;
        this.midY = midY;
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
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public int getZoom() {
        return zoom;
    }

    @Override
    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

}
