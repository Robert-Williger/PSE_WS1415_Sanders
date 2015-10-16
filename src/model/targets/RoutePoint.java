package model.targets;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import model.AbstractModel;
import model.elements.StreetNode;
import model.map.IMapManager;

public class RoutePoint extends AbstractModel implements IRoutePoint {
    private final IMapManager manager;
    private final List<IPointListener> listener;
    private String address;
    private StreetNode node;
    private int index;
    private PointState state;
    private Point location;

    public RoutePoint(final IMapManager manager) {
        this.manager = manager;
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
            e.indexChanged();
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
    public void setStreetNode(final StreetNode node) {
        this.node = node;
        fireLocationEvent();

    }

    @Override
    public void setIndex(final int index) {
        this.index = index;
        fireIndexEvent();
    }

    @Override
    public void setState(final PointState state) {
        this.state = state;
        fireStateEvent();
    }

    @Override
    public void setLocation(final Point location) {
        this.location = location;
        fireLocationEvent();
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public Point getLocation() {
        return location != null ? location : node != null ? manager.getPixel(node.getLocation()) : null;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public PointState getState() {
        return state;
    }

    @Override
    public StreetNode getStreetNode() {
        return node;
    }

}