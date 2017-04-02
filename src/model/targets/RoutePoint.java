package model.targets;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import model.AbstractModel;

public class RoutePoint extends AbstractModel implements IRoutePoint {
    private final List<IPointListener> listener;
    private int listIndex;
    private int targetIndex;
    private PointState pointState;
    private final Point location;
    private AddressPoint addressPoint;

    public RoutePoint() {
        location = new Point();
        pointState = PointState.unadded;
        listener = new ArrayList<>();
    }

    private void fireAddressEvent() {
        fireChange();
        for (final IPointListener e : listener) {
            e.addressChanged();
        }
    }

    private void fireIndexEvent() {
        fireChange();
        for (final IPointListener e : listener) {
            e.listIndexChanged();
        }
    }

    private void fireLocationEvent() {
        fireChange();
        for (final IPointListener e : listener) {
            e.locationChanged();
        }
    }

    private void fireStateEvent() {
        fireChange();
        for (final IPointListener e : listener) {
            e.stateChanged();
        }
    }

    @Override
    public void addPointListener(final IPointListener listener) {
        this.listener.add(listener);
    }

    @Override
    public void setAddressPoint(final AddressPoint point) {
        if (point == addressPoint) {
            return;
        }

        final String address = getAddress();
        final int x = getX();
        final int y = getY();

        this.addressPoint = point;

        // TODO improve this
        if (point == null) {
            fireLocationEvent();
            fireAddressEvent();
            return;
        }

        if (point.getX() != x || point.getY() != y) {
            fireLocationEvent();
        }

        // TODO null values in address?
        if (point.getAddress() == address)
            return;

        if (point.getAddress() != null && !point.getAddress().equals(address)) {
            fireAddressEvent();
        }
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
    public void setState(final PointState state) {
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
    public int getX() {
        return addressPoint != null ? addressPoint.getX() : location.x;
    }

    @Override
    public int getY() {
        return addressPoint != null ? addressPoint.getY() : location.y;
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
    public PointState getState() {
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