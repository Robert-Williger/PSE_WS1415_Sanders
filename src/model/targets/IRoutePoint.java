package model.targets;

import java.awt.Point;

import model.IModel;
import model.elements.AccessPoint;

public interface IRoutePoint extends IModel {

    void addPointListener(IPointListener listener);

    void removePointListener(IPointListener listener);

    void setAddress(String address);

    void setAccessPoint(AccessPoint point);

    void setListIndex(int index);

    void setTargetIndex(int index);

    void setState(PointState state);

    void setLocation(Point location);

    String getAddress();

    Point getLocation();

    int getListIndex();

    int getTargetIndex();

    PointState getState();

    AccessPoint getAccessPoint();

}