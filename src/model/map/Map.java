package model.map;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import model.AbstractModel;

public class Map extends AbstractModel implements IMap {

    private final IMapManager manager;
    private final IMapState state;
    private final IPixelConverter converter;
    private final List<IMapListener> listeners;

    private int width;
    private int height;

    public Map(final IMapManager manager) {
        this.manager = manager;
        state = manager.getState();
        converter = manager.getConverter();
        listeners = new LinkedList<>();
    }

    @Override
    public void zoom(final int steps, final double xOffset, final double yOffset) {
        final int trueSteps = steps > 0 ? Math.min(steps, state.getMaxZoomStep() - state.getZoomStep())
                : Math.max(steps, state.getMinZoomStep() - state.getZoomStep());

        if (trueSteps != 0) {
            // TODO improve this.
            fireZoomInitiatedEvent(trueSteps, state.getZoomStep() < state.getMinZoomStep() + 3 ? 0.5 : xOffset,
                    state.getZoomStep() < state.getMinZoomStep() + 3 ? 0.5 : yOffset);

            final double midX = state.getX() + state.getCoordSectionWidth() * 0.5;
            final double midY = state.getY() + state.getCoordSectionHeight() * 0.5;
            final double coordX = state.getX() + state.getCoordSectionWidth() * xOffset;
            final double coordY = state.getY() + state.getCoordSectionHeight() * yOffset;

            final double scaling = Math.pow(2, -trueSteps);
            final double xDistance = coordX - midX;
            final double yDistance = coordY - midY;

            state.setZoomStep(state.getZoomStep() + trueSteps);
            center(coordX - scaling * xDistance, coordY - scaling * yDistance);

            final double newMidX = state.getX() + state.getCoordSectionWidth() * 0.5;
            final double newMidY = state.getY() + state.getCoordSectionHeight() * 0.5;

            // TODO is this correct?
            final double realXOffset = (newMidX - midX) / state.getCoordSectionWidth() + 0.5;
            final double realYOffset = (newMidY - midY) / state.getCoordSectionHeight() + 0.5;

            fireZoomEvent(trueSteps, realXOffset, realYOffset);
        }
    }

    @Override
    public void moveView(final double deltaX, final double deltaY) {
        final int zoomStep = state.getZoomStep();
        state.setLocation(state.getX() + converter.getCoordDistanced(deltaX, zoomStep),
                state.getY() + converter.getCoordDistanced(deltaY, zoomStep));

        fireMoveEvent(deltaX, deltaY);
    }

    @Override
    public AddressPoint getAddressNode(final int x, final int y) {
        // TODO
        return manager.getAddress((int) state.getX() + converter.getCoordDistance(x, state.getZoomStep()),
                (int) state.getY() + converter.getCoordDistance(y, state.getZoomStep()));
    }

    @Override
    public void setSize(final int width, final int height) {
        final int minZoomStep = state.getMinZoomStep();

        this.width = width;
        this.height = height;

        state.setSectionSize(converter.getCoordDistance(width, minZoomStep),
                converter.getCoordDistance(height, minZoomStep));

        fireResizeEvent(width, height);
    }

    @Override
    public Point getViewLocation() {
        final int zoomStep = state.getZoomStep();
        final int pixelX = (int) converter.getPixelDistanced(state.getX(), zoomStep) % manager.getTileSize();
        final int pixelY = (int) converter.getPixelDistanced(state.getY(), zoomStep) % manager.getTileSize();

        return new Point(pixelX, pixelY);
    }

    @Override
    public void center(final double x, final double y, final double width, final double height) {
        final int steps = (int) Math.ceil(
                Math.max(log2(width / state.getCoordSectionWidth()), log2(height / state.getCoordSectionHeight())));

        state.setZoomStep(state.getMinZoomStep() - steps);
        center(x + width / 2, y + height / 2);
    }

    @Override
    public void center(final double x, final double y) {
        state.setLocation(x - state.getCoordSectionWidth() / 2.0, y - state.getCoordSectionHeight() / 2.0);

        fireChange();
    }

    private double log2(final double value) {
        return Math.log(value) / Math.log(2);
    }

    @Override
    public void addMapListener(final IMapListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeMapListener(final IMapListener listener) {
        listeners.remove(listener);
    }

    protected void fireMoveEvent(final double deltaX, final double deltaY) {
        for (final IMapListener listener : listeners) {
            listener.mapMoved(deltaX, deltaY);
        }
        fireChange();
    }

    protected void fireZoomEvent(final int steps, final double xOffset, final double yOffset) {
        for (final IMapListener listener : listeners) {
            listener.mapZoomed(steps, xOffset, yOffset);
        }
        fireChange();
    }

    protected void fireZoomInitiatedEvent(final int steps, final double xOffset, final double yOffset) {
        for (final IMapListener listener : listeners) {
            listener.mapZoomInitiated(steps, xOffset, yOffset);
        }
    }

    protected void fireResizeEvent(final int width, final int height) {
        for (final IMapListener listener : listeners) {
            listener.mapResized(width, height);
        }
        fireChange();
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}