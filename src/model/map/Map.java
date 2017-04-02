package model.map;

import java.util.LinkedList;
import java.util.List;

import model.AbstractModel;
import model.targets.AddressPoint;

public class Map extends AbstractModel implements IMap {

    private final IMapManager manager;
    private final IMapState state;
    private final IPixelConverter converter;
    private final List<IMapListener> listeners;

    public Map(final IMapManager manager) {
        this.manager = manager;
        state = manager.getState();
        converter = state.getConverter();
        listeners = new LinkedList<>();
    }

    @Override
    public void zoom(final int steps, final double xOffset, final double yOffset) {
        final int sourceZoom = state.getZoom();
        final int targetZoom = Math.min(state.getMaxZoom(), Math.max(state.getMinZoom(), sourceZoom + steps));

        if (sourceZoom != targetZoom) {
            final double midX = state.getX();
            final double midY = state.getY();
            final double coordX = state.getX() + state.getCoordSectionWidth(sourceZoom) * xOffset;
            final double coordY = state.getY() + state.getCoordSectionHeight(sourceZoom) * yOffset;

            final int trueSteps = targetZoom - sourceZoom;

            final double scaling = Math.pow(2, -trueSteps);

            state.setZoom(targetZoom);
            center(coordX - scaling * (coordX - midX), coordY - scaling * (coordY - midY));

            // TODO is this correct?
            final double deltaX = converter.getPixelDistance(state.getX() - midX, sourceZoom);
            final double deltaY = converter.getPixelDistance(state.getY() - midY, sourceZoom);

            fireZoomEvent(trueSteps, deltaX, deltaY);
        }
    }

    @Override
    public void move(final double deltaX, final double deltaY) {
        final int zoomStep = state.getZoom();
        state.setLocation(state.getX() + converter.getCoordDistance(deltaX, zoomStep),
                state.getY() + converter.getCoordDistance(deltaY, zoomStep));

        fireMoveEvent(deltaX, deltaY);
    }

    @Override
    public AddressPoint getAddress(final int x, final int y) {
        final int zoom = state.getZoom();
        return manager.getAddress(converter.getCoordDistance(x, zoom), converter.getCoordDistance(y, zoom));
    }

    @Override
    public void setSize(final int width, final int height) {
        state.setPixelSectionSize(width, height);

        fireResizeEvent(width, height);
    }

    @Override
    public void center(final double x, final double y, final double width, final double height) {
        final int zoom = state.getZoom();
        final int steps = (int) Math.ceil(Math.max(log2(width / state.getCoordSectionWidth(zoom)),
                log2(height / state.getCoordSectionHeight(zoom))));

        state.setZoom(state.getMinZoom() - steps);
        center(x + width / 2, y + height / 2);
    }

    @Override
    public void center(final double x, final double y) {
        state.setLocation(x, y);

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

    protected void fireZoomEvent(final int steps, final double deltaX, final double deltaY) {
        for (final IMapListener listener : listeners) {
            listener.mapZoomed(steps, deltaX, deltaY);
        }
        fireChange();
    }

    protected void fireResizeEvent(final int width, final int height) {
        for (final IMapListener listener : listeners) {
            listener.mapResized(width, height);
        }
        fireChange();
    }

    @Override
    public int getWidth() {
        return state.getPixelSectionWidth();
    }

    @Override
    public int getHeight() {
        return state.getPixelSectionHeight();
    }

    @Override
    public int getZoom() {
        return state.getZoom();
    }

    @Override
    public int getX() {
        // TODO Auto-generated method stub
        return (int) converter.getPixelDistance(state.getX(), state.getZoom());
    }

    @Override
    public int getY() {
        // TODO Auto-generated method stub
        return (int) converter.getPixelDistance(state.getY(), state.getZoom());
    }
}