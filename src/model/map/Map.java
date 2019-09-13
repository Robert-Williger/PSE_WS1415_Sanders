package model.map;

import java.util.LinkedList;
import java.util.List;

import model.AbstractModel;
import model.targets.AddressPoint;

public class Map extends AbstractModel implements IMap {

    private final IAddressFinder addressFinder;
    private final IMapSection section;
    private final IMapBounds bounds;
    private final IPixelMapping converter;
    private final List<IMapListener> listeners;

    public Map(final IMapManager manager) {
        addressFinder = manager.getAddressFinder();
        section = manager.getMapSection();
        converter = manager.getPixelMapping();
        bounds = manager.getMapBounds();
        listeners = new LinkedList<>();
    }

    @Override
    public void zoom(final int steps, final double xOffset, final double yOffset) {
        final int sourceZoom = section.getZoom();
        final int targetZoom = Math.min(bounds.getMaxZoom(), Math.max(bounds.getMinZoom(), sourceZoom + steps));

        if (sourceZoom != targetZoom) {
            final int midX = section.getMidX();
            final int midY = section.getMidY();
            final int coordX = midX + (int) (converter.getCoordDistance(section.getWidth(), sourceZoom) * xOffset);
            final int coordY = midY + (int) (converter.getCoordDistance(section.getHeight(), sourceZoom) * yOffset);

            final int trueSteps = targetZoom - sourceZoom;

            final double scaling = Math.pow(2, -trueSteps);

            section.setZoom(targetZoom);
            section.setMidpoint(coordX - (int) (scaling * (coordX - midX)), coordY - (int) (scaling * (coordY - midY)));

            // TODO is this correct?
            final double deltaX = converter.getPixelDistance(section.getMidX() - midX, sourceZoom);
            final double deltaY = converter.getPixelDistance(section.getMidY() - midY, sourceZoom);

            fireZoomEvent(trueSteps, deltaX, deltaY);
        }
    }

    @Override
    public void move(final int deltaX, final int deltaY) {
        final int zoomStep = section.getZoom();
        section.setMidpoint(section.getMidX() + converter.getCoordDistance(deltaX, zoomStep),
                section.getMidY() + converter.getCoordDistance(deltaY, zoomStep));

        fireMoveEvent(deltaX, deltaY);
    }

    @Override
    public AddressPoint getAddress(final int x, final int y) {
        final int zoom = section.getZoom();
        return addressFinder.getAddress(converter.getCoordDistance(x, zoom), converter.getCoordDistance(y, zoom), zoom);
    }

    @Override
    public void setSize(final int width, final int height) {
        section.setSize(width, height);

        fireResizeEvent(width, height);
    }

    @Override
    public void center(final int x, final int y, final int width, final int height) {
        final int zoom = section.getZoom();
        // final int steps = (int) Math
        // .ceil(Math.max(log2(width / section.getWidth()), log2(height / section.getHeight())));
        //
        // section.setZoom(zoom - steps);
        section.setMidpoint(converter.getCoordDistance(x + width / 2, zoom),
                converter.getCoordDistance(y + height / 2, zoom));
        fireChange();
    }

    @Override
    public void center(final int x, final int y) {
        section.setMidpoint(x, y);

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
        return section.getWidth();
    }

    @Override
    public int getHeight() {
        return section.getHeight();
    }

    @Override
    public int getZoom() {
        return section.getZoom();
    }

    @Override
    public int getX(final int zoom) {
        return (int) converter.getPixelDistance(section.getMidX(), zoom);
    }

    @Override
    public int getY(final int zoom) {
        return (int) converter.getPixelDistance(section.getMidY(), zoom);
    }
}