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
    public void zoom(final int steps, final Point location) {
        final Dimension size = state.getSize();
        int zoom = state.getZoomStep() - state.getMinZoomStep();
        final int width = (int) (size.width * (1.0 / (1 << zoom)));
        final int height = (int) (size.height * (1.0 / (1 << zoom)));

        final Point midPoint = state.getLocation();
        midPoint.translate(width / 2, height / 2);

        final Point coordLocation = manager.getCoord(location);

        final int weightedX;
        final int weightedY;
        final int trueSteps;

        // TODO check this
        if (steps > 0) {
            trueSteps = Math.min(steps, state.getMaxZoomStep() - state.getZoomStep());
            weightedX = (coordLocation.x - midPoint.x) >> trueSteps;
            weightedY = (coordLocation.y - midPoint.y) >> trueSteps;
        } else {
            trueSteps = Math.max(steps, state.getMinZoomStep() - state.getZoomStep());
            weightedX = (coordLocation.x - midPoint.x) << -steps;
            weightedY = (coordLocation.y - midPoint.y) << -steps;
        }

        if (trueSteps != 0) {
            state.setZoomStep(state.getZoomStep() + steps);
            Point pixelLocation = manager.getPixel(new Point(coordLocation.x - weightedX, coordLocation.y - weightedY));
            center(pixelLocation.x, pixelLocation.y);
        }
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
        final int minZoomStep = state.getMinZoomStep();
        state.setSize(converter.getCoordDistance(size.width, minZoomStep),
                converter.getCoordDistance(size.height, minZoomStep));

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

        state.setZoomStep(state.getMinZoomStep() - steps);
        center(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
    }

    @Override
    public void center(final Point point) {
        center(point.x, point.y);

        final Point location = manager.getCoord(point);
        final Dimension size = state.getSize();

        final int zoomFactor = (1 << state.getZoomStep() - state.getMinZoomStep());
        final int coordWidth = (int) ((double) size.width / zoomFactor);
        final int coordHeight = (int) ((double) size.height / zoomFactor);

        state.setLocation(location.x - coordWidth / 2, location.y - coordHeight / 2);

        fireChange();
    }

    private void center(final int x, final int y) {
        center(new Point(x, y));
    }

    private double log2(final double value) {
        return Math.log(value) / Math.log(2);
    }

}