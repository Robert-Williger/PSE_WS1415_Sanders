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
            final double midX = state.getCoordX();
            final double midY = state.getCoordY();
            final double coordX = state.getCoordX() + state.getCoordSectionWidth(sourceZoom) * xOffset;
            final double coordY = state.getCoordY() + state.getCoordSectionHeight(sourceZoom) * yOffset;

            final int trueSteps = targetZoom - sourceZoom;

            final double scaling = Math.pow(2, -trueSteps);

            state.setZoom(targetZoom);
            state.setCoordLocation(coordX - scaling * (coordX - midX), coordY - scaling * (coordY - midY));

            // TODO is this correct?
            final double deltaX = converter.getPixelDistance(state.getCoordX() - midX, sourceZoom);
            final double deltaY = converter.getPixelDistance(state.getCoordY() - midY, sourceZoom);

            fireZoomEvent(trueSteps, deltaX, deltaY);
        }
    }

    @Override
    public void move(final double deltaX, final double deltaY) {
        final int zoomStep = state.getZoom();
        state.setCoordLocation(state.getCoordX() + converter.getCoordDistance(deltaX, zoomStep),
                state.getCoordY() + converter.getCoordDistance(deltaY, zoomStep));

        fireMoveEvent(deltaX, deltaY);
    }

    @Override
    public AddressPoint getAddress(final int x, final int y) {
        final int zoom = state.getZoom();
        return manager.getAddress(converter.getCoordDistance(x, zoom), converter.getCoordDistance(y, zoom));
    }

    @Override
    public void setSize(final int width, final int height) {
        int x = getX();
        int y = getY();
        state.setPixelSectionSize(width, height);

        fireResizeEvent(width, height);
        fireMoveEvent(getX() - x, getY() - y);
    }

    @Override
    public void center(final double x, final double y, final double width, final double height) {
        final int zoom = state.getZoom();
        final int steps = (int) Math.ceil(
                Math.max(log2(width / state.getPixelSectionWidth()), log2(height / state.getPixelSectionHeight())));

        state.setZoom(zoom - steps);
        state.setCoordLocation(converter.getCoordDistance(x + width / 2, zoom),
                converter.getCoordDistance(y + height / 2, zoom));

        fireChange();
    }

    @Override
    public void center(final double x, final double y) {
        state.setPixelLocation(x, y);

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
    public int getX(final int zoom) {
        return (int) converter.getPixelDistance(state.getCoordX(), zoom);
    }

    @Override
    public int getY(final int zoom) {
        return (int) converter.getPixelDistance(state.getCoordY(), zoom);
    }
}