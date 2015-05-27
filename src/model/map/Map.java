package model.map;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import model.AbstractModel;

public class Map extends AbstractModel implements IMap {

    private final IMapManager manager;
    private final IMapState state;
    private final IPixelConverter converter;

    public Map(final IMapManager manager) {
        this.manager = manager;
        state = manager.getMapState();
        converter = manager.getConverter();
    }

    @Override
    public void zoom(final int steps) {
        final Dimension size = state.getSize();
        final int width = (int) (size.width * (1.0 / (1 << state.getZoomStep())));
        final int height = (int) (size.height * (1.0 / (1 << state.getZoomStep())));
        final Point midPoint = state.getLocation();
        midPoint.translate(width / 2, height / 2);

        state.setZoomStep(state.getZoomStep() + steps);
        center(midPoint);
    }

    @Override
    public void moveView(final int deltaX, final int deltaY) {
        final int zoomStep = state.getZoomStep();
        state.move(converter.getCoordDistance(deltaX, zoomStep), converter.getCoordDistance(deltaY, zoomStep));

        fireChange();
    }

    @Override
    public AddressNode getAddressNode(final Point location) {
        return manager.getAddressNode(manager.getCoord(location));
    }

    @Override
    public void setViewSize(final Dimension size) {
        state.setSize(converter.getCoordDistance(size.width, 0), converter.getCoordDistance(size.height, 0));

        fireChange();
    }

    @Override
    public Point getViewLocation() {
        final Point location = state.getLocation();
        final int zoomStep = state.getZoomStep();
        final Dimension tileSize = manager.getTileSize();
        final int pixelX = converter.getPixelDistance(location.x, zoomStep) % tileSize.width;
        final int pixelY = converter.getPixelDistance(location.y, zoomStep) % tileSize.height;

        return new Point(pixelX, pixelY);
    }

    @Override
    public void center(final Rectangle bounds) {
        final Dimension size = state.getSize();
        final int steps = (int) Math.ceil(Math.max(log2(bounds.width / size.getWidth()),
                log2(bounds.height / size.getHeight())));

        state.setZoomStep(-steps);
        center(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
    }

    @Override
    public void center(final Point point) {
        center(point.x, point.y);
    }

    private double log2(final double value) {
        return Math.log(value) / Math.log(2);
    }

    private void center(final int x, final int y) {
        final Dimension size = state.getSize();
        final int coordWidth = (int) ((double) size.width / (1 << state.getZoomStep()));
        final int coordHeight = (int) ((double) size.height / (1 << state.getZoomStep()));

        state.setLocation(x - coordWidth / 2, y - coordHeight / 2);

        fireChange();
    }

}