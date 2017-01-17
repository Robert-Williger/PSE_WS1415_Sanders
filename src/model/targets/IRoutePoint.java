package model.targets;

import model.IModel;
import model.elements.AccessPoint;

public interface IRoutePoint extends IModel {

    void addPointListener(IPointListener listener);

    void setAddress(String address);

    void setAccessPoint(AccessPoint point);

    void setListIndex(int index);

    void setTargetIndex(int index);

    void setState(PointState state);

    void setLocation(int x, int y);

    String getAddress();

    int getX();

    int getY();

    int getListIndex();

    int getTargetIndex();

    PointState getState();

    AccessPoint getAccessPoint();

}