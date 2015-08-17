package model.map;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

public class MapState implements IMapState {

    private int zoomStep;
    private final Rectangle bounds;
    private final int minZoomStep;
    private final int maxZoomStep;
    private final int height;
    private final int width;

    public MapState(final Dimension coordSize, final int maxZoomStep, final int minZoomStep) {
        this.minZoomStep = minZoomStep;
        this.maxZoomStep = maxZoomStep;
        height = coordSize.height;
        width = coordSize.width;

        bounds = new Rectangle();
        zoomStep = minZoomStep;
    }

    @Override
    public void setZoomStep(final int zoomStep) {
        this.zoomStep = Math.min(maxZoomStep, Math.max(zoomStep, minZoomStep));
    }

    @Override
    public void setSize(final int width, final int height) {
        bounds.setSize(width, height);
        setLocation(getLocation());
    }

    @Override
    public void setSize(final Dimension size) {
        setSize(size.width, size.height);
    }

    @Override
    public void setLocation(final int x, final int y) {
        int xCoord = x;
        int yCoord = y;

        final double zoomFactor = 1.0 / (1 << (zoomStep - minZoomStep));
        final int coordWidth = (int) (bounds.width * zoomFactor);
        final int coordHeight = (int) (bounds.height * zoomFactor);
        if (coordWidth <= width) {
            if (x < 0) {
                xCoord = 0;
            } else if (x + coordWidth > width) {
                xCoord = width - coordWidth;
            }
        } else {
            xCoord = (width - coordWidth) / 2;
        }

        if (coordHeight <= height) {
            if (y < 0) {
                yCoord = 0;
            } else if (y + coordHeight > height) {
                yCoord = height - coordHeight;
            }
        } else {
            yCoord = (height - coordHeight) / 2;
        }

        bounds.setLocation(xCoord, yCoord);
    }

    @Override
    public void setLocation(final Point location) {
        setLocation(location.x, location.y);
    }

    @Override
    public void move(final int deltaX, final int deltaY) {
        setLocation(bounds.x + deltaX, bounds.y + deltaY);
    }

    @Override
    public int getZoomStep() {
        return zoomStep;
    }

    @Override
    public int getMaxZoomStep() {
        return maxZoomStep;
    }

    @Override
    public int getMinZoomStep() {
        return minZoomStep;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public Point getLocation() {
        return bounds.getLocation();
    }

    @Override
    public Dimension getSize() {
        return bounds.getSize();
    }
}
