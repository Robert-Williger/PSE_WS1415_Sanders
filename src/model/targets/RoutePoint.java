package model.targets;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import model.AbstractModel;

public class RoutePoint extends AbstractModel implements IRoutePoint {
    private final List<IPointListener> listeners;
    private int listIndex;
    private int targetIndex;
    private State pointState;
    private final Point location;
    private AddressPoint addressPoint;

    public RoutePoint() {
        location = new Point();
        pointState = State.unadded;
        listeners = new ArrayList<>();
    }

    private void fireAddressEvent() {
        fireChange();
        for (final IPointListener e : listeners) {
            e.addressChanged();
        }
    }

    private void fireIndexEvent() {
        fireChange();
        for (final IPointListener e : listeners) {
            e.listIndexChanged();
        }
    }

    private void fireLocationEvent() {
        fireChange();
        for (final IPointListener e : listeners) {
            e.locationChanged();
        }
    }

    private void fireStateEvent() {
        fireChange();
        for (final IPointListener e : listeners) {
            e.stateChanged();
        }
    }

    @Override
    public void addPointListener(final IPointListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void setAddressPoint(final AddressPoint point) {
        if (point == addressPoint) {
            return;
        }

        this.addressPoint = point;

        // TODO only fire if changed?
        fireLocationEvent();
        fireAddressEvent();
    }

    @Override
    public void setListIndex(final int index) {
        this.listIndex = index;
        fireIndexEvent();
    }

    @Override
    public void setTargetIndex(final int index) {
        this.targetIndex = index;
    }

    @Override
    public void setState(final State state) {
        this.pointState = state;
        fireStateEvent();
    }

    @Override
    public void setLocation(final int x, final int y) {
        final int oldX = location.x;
        final int oldY = location.y;
        location.setLocation(x, y);

        if (addressPoint == null && (x != oldX || y != oldY)) {
            fireLocationEvent();
        }
    }

    @Override
    public int getX(final int zoom) {
        return addressPoint != null ? addressPoint.getX(zoom) : location.x;
    }

    @Override
    public int getY(final int zoom) {
        return addressPoint != null ? addressPoint.getY(zoom) : location.y;
    }

    @Override
    public int getListIndex() {
        return listIndex;
    }

    @Override
    public int getTargetIndex() {
        return targetIndex;
    }

    @Override
    public State getState() {
        return pointState;
    }

    @Override
    public String getAddress() {
        return addressPoint != null ? addressPoint.getAddress() : null;
    }

    @Override
    public AddressPoint getAddressPoint() {
        return addressPoint;
    }

}