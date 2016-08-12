package model.targets;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import model.AbstractModel;
import model.elements.AccessPoint;
import model.elements.dereferencers.IStreetDereferencer;
import model.map.IMapManager;

public class RoutePoint extends AbstractModel implements IRoutePoint {
    private final IMapManager manager;
    private final List<IPointListener> listener;
    private final IStreetDereferencer street;
    private final Point accessPointLocation;
    private String address;
    private AccessPoint accessPoint;
    private int listIndex;
    private int targetIndex;
    private PointState state;
    private Point location;

    public RoutePoint(final IMapManager manager) {
        this.manager = manager;
        this.street = manager.createTileDereferencer().getStreetDereferencer();
        state = PointState.unadded;
        accessPointLocation = new Point();
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
    public void removePointListener(final IPointListener listener) {
        this.listener.remove(listener);
    }

    @Override
    public void setAddress(final String address) {
        this.address = address;
        fireAddressEvent();
    }

    @Override
    public void setAccessPoint(final AccessPoint point) {
        this.accessPoint = point;
        this.street.setID(point.getStreet());
        calculateLocation();
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
        return location != null ? location : manager.getPixel(accessPointLocation);
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
        return accessPoint;
    }

    private void calculateLocation() {

        final int size = street.size();

        if (size > 0) {
            final float totalLength = street.getLength();
            final float maxLength = totalLength * accessPoint.getOffset();

            int lastX = street.getX(0);
            int lastY = street.getY(0);
            float currentOffsetLength = 0f;

            for (int i = 1; i < size; i++) {
                final int currentX = street.getX(i);
                final int currentY = street.getY(i);
                final double distance = Point.distance(lastX, lastY, currentX, currentY);

                if (currentOffsetLength + distance > maxLength || i == size - 1) {
                    final int xDistance = currentX - lastX;
                    final int yDistance = currentY - lastY;

                    final float partOffsetLength = maxLength - currentOffsetLength;
                    final float partOffset = (float) (partOffsetLength / distance);
                    accessPointLocation.setLocation((int) (lastX + xDistance * partOffset + 0.49f), (int) (lastY
                            + yDistance * partOffset + 0.49f));

                    return;
                }

                currentOffsetLength += distance;
                lastX = currentX;
                lastY = currentY;
            }
        }
    }
}