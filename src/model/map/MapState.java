package model.map;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;

public class MapState implements IMapState {

    private int zoom;
    private final int minZoomStep;
    private final int maxZoomStep;

    // current section's midpoint in coordinates
    private final Point2D location;

    // current section's size in pixels
    private final Dimension pixelSectionSize;

    // current section's sizes in coordinates per zoom
    private final Dimension[] coordSectionSize;

    // total map size in coordinates
    private final Dimension totalSize;

    // size of a tile in pixels
    private final int pixelTileSize;

    // sizes of a tile in coords per zoom step
    private final int[] coordTileSize;

    private final IPixelConverter converter;

    public MapState(final int width, final int height, final int minZoomStep, final int maxZoomStep,
            final int pixelTileSize, final IPixelConverter converter) {
        this.minZoomStep = minZoomStep;
        this.maxZoomStep = maxZoomStep;
        this.converter = converter;

        this.pixelTileSize = pixelTileSize;

        pixelSectionSize = new Dimension();
        coordSectionSize = new Dimension[maxZoomStep - minZoomStep + 1];
        for (int i = 0; i < coordSectionSize.length; i++) {
            coordSectionSize[i] = new Dimension();
        }

        coordTileSize = new int[maxZoomStep - minZoomStep + 1];
        for (int i = 0; i < coordTileSize.length; i++) {
            coordTileSize[i] = converter.getCoordDistance(pixelTileSize, i + minZoomStep);
        }
        location = new Point();
        totalSize = new Dimension(width, height);
        zoom = minZoomStep;
    }

    @Override
    public void setZoom(final int zoomStep) {
        this.zoom = Math.min(maxZoomStep, Math.max(zoomStep, minZoomStep));
        updateCoordSectionSize();
    }

    @Override
    public void setPixelSectionSize(final int width, final int height) {
        pixelSectionSize.setSize(width, height);
        updateCoordSectionSize();
        setCoordLocation(location.getX(), location.getY());
    }

    @Override
    public void setCoordLocation(final double x, final double y) {
        final int coordSectionWidth = getCoordSectionWidth(zoom);
        final int coordSectionHeight = getCoordSectionHeight(zoom);

        double xCoord = coordSectionWidth <= totalSize.width
                ? Math.max(coordSectionWidth / 2, Math.min(totalSize.width - coordSectionWidth / 2, x))
                : totalSize.width / 2;

        double yCoord = coordSectionHeight <= totalSize.height
                ? Math.max(coordSectionHeight / 2, Math.min(totalSize.height - coordSectionHeight / 2, y))
                : totalSize.height / 2;

        location.setLocation(xCoord, yCoord);
    }

    @Override
    public void setPixelLocation(final double x, final double y) {
        setCoordLocation(converter.getCoordDistance(x, zoom), converter.getCoordDistance(y, zoom));
    }

    @Override
    public int getZoom() {
        return zoom;
    }

    @Override
    public int getMaxZoom() {
        return maxZoomStep;
    }

    @Override
    public int getMinZoom() {
        return minZoomStep;
    }

    private void updateCoordSectionSize() {
        for (int zoom = 0; zoom < coordSectionSize.length; zoom++) {
            final int coordWidth = converter.getCoordDistance(pixelSectionSize.width, zoom + minZoomStep);
            final int coordHeight = converter.getCoordDistance(pixelSectionSize.height, zoom + minZoomStep);
            coordSectionSize[zoom].setSize(coordWidth, coordHeight);
        }
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
    public int getCoordMapWidth() {
        return totalSize.width;
    }

    @Override
    public int getCoordMapHeight() {
        return totalSize.height;
    }

    @Override
    public double getCoordX() {
        return location.getX();
    }

    @Override
    public double getCoordY() {
        return location.getY();
    }

    @Override
    public int getCoordSectionWidth(final int zoom) {
        return coordSectionSize[zoom - minZoomStep].width;
    }

    @Override
    public int getCoordSectionHeight(final int zoom) {
        return coordSectionSize[zoom - minZoomStep].height;
    }

    @Override
    public int getPixelTileSize() {
        return pixelTileSize;
    }

    @Override
    public int getCoordTileSize(final int zoom) {
        return coordTileSize[zoom - minZoomStep];
    }

    @Override
    public IPixelConverter getConverter() {
        return converter;
    }
}
