package model.targets;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import model.AbstractModel;
import model.elements.AccessPoint;

public class RoutePoint extends AbstractModel implements IRoutePoint {
    private final List<IPointListener> listener;
    private String address;
    private AccessPoint point;
    private int listIndex;
    private int targetIndex;
    private PointState state;
    private final Point location;

    public RoutePoint() {
        location = new Point();
        state = PointState.unadded;
        listener = new ArrayList<IPointListener>();
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
    public void setAddress(final String address) {
        this.address = address;
        fireAddressEvent();
    }

    @Override
    public void setAccessPoint(final AccessPoint point) {
        this.point = point;
        fireLocationEvent();

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
        this.state = state;
        fireStateEvent();
    }

    @Override
    public void setLocation(final int x, final int y) {
        location.setLocation(x, y);
        fireLocationEvent();
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public int getX() {
        return location.x;
    }

    @Override
    public int getY() {
        return location.y;
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
        return state;
    }

    @Override
    public AccessPoint getAccessPoint() {
        return point;
    }

}