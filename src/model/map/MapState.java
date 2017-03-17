package model.map;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;

public class MapState implements IMapState {

    private int zoomStep;
    private final int minZoomStep;
    private final int maxZoomStep;

    // current section's location in coordinates
    private final Point2D location;

    // current section's size in pixels
    private final Dimension pixelSectionSize;

    // current section's size in coordinates
    private final Dimension coordSectionSize;

    // total map size in coordinates
    private final Dimension totalSize;

    public MapState(final int width, final int height, final int minZoomStep, final int maxZoomStep) {
        this.minZoomStep = minZoomStep;
        this.maxZoomStep = maxZoomStep;

        pixelSectionSize = new Dimension();
        coordSectionSize = new Dimension();
        location = new Point();
        totalSize = new Dimension(width, height);
        zoomStep = minZoomStep;
    }

    @Override
    public void setZoomStep(final int zoomStep) {
        this.zoomStep = Math.min(maxZoomStep, Math.max(zoomStep, minZoomStep));
        updateCoordSectionSize();
    }

    @Override
    public void setSectionSize(final int width, final int height) {
        pixelSectionSize.setSize(width, height);
        updateCoordSectionSize();
        setLocation(location.getX(), location.getY());
    }

    @Override
    public void setLocation(final double x, final double y) {
        double xCoord = x;
        double yCoord = y;

        if (coordSectionSize.width <= totalSize.width) {
            if (x < 0) {
                xCoord = 0;
            } else if (x + coordSectionSize.width > totalSize.width) {
                xCoord = totalSize.width - coordSectionSize.width;
            }
        } else {
            xCoord = (totalSize.width - coordSectionSize.width) / 2;
        }

        if (coordSectionSize.height <= totalSize.height) {
            if (y < 0) {
                yCoord = 0;
            } else if (y + coordSectionSize.height > totalSize.height) {
                yCoord = totalSize.height - coordSectionSize.height;
            }
        } else {
            yCoord = (totalSize.height - coordSectionSize.height) / 2;
        }

        location.setLocation(xCoord, yCoord);
    }

    // @Override
    // public void move(final double deltaX, final double deltaY) {
    // setLocation(location.getX() + deltaX, location.getY() + deltaY);
    // }

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

    private void updateCoordSectionSize() {
        final double zoomFactor = 1.0 / (1 << (zoomStep - minZoomStep));
        final int coordWidth = (int) (pixelSectionSize.width * zoomFactor);
        final int coordHeight = (int) (pixelSectionSize.height * zoomFactor);
        coordSectionSize.setSize(coordWidth, coordHeight);
    }

    @Override
    public int getPixelSectionWidth() {
        return pixelSectionSize.width;
    }

    @Override
    public int getPixelSectionHeight() {
        return pixelSectionSize.height;
    }

    @Override
    public int getTotalWidth() {
        return totalSize.width;
    }

    @Override
    public int getTotalHeight() {
        return totalSize.height;
    }

    @Override
    public double getX() {
        return location.getX();
    }

    @Override
    public double getY() {
        return location.getY();
    }

    @Override
    public int getCoordSectionWidth() {
        return coordSectionSize.width;
    }

    @Override
    public int getCoordSectionHeight() {
        return coordSectionSize.height;
    }
}
