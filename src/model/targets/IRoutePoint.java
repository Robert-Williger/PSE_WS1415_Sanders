package model.targets;

import model.IModel;

public interface IRoutePoint extends IModel {

    void addPointListener(IPointListener listener);

    void setAddressPoint(AddressPoint point);

    void setListIndex(int index);

    void setTargetIndex(int index);

    void setState(PointState state);

    void setLocation(int x, int y);

    AddressPoint getAddressPoint();

    int getX(int zoom);

    int getY(int zoom);

    int getListIndex();

    int getTargetIndex();

    PointState getState();

    default String getAddress() {
        final AddressPoint point = getAddressPoint();
        return point != null ? point.getAddress() : null;
    }
}