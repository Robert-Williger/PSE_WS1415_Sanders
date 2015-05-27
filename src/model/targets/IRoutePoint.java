package model.targets;

import java.awt.Point;

import model.IModel;
import model.elements.StreetNode;

public interface IRoutePoint extends IModel {

    void addPointListener(IPointListener listener);

    void setAddress(String address);

    void setStreetNode(StreetNode node);

    void setIndex(int index);

    void setState(PointState state);

    void setLocation(Point location);

    String getAddress();

    Point getLocation();

    int getIndex();

    PointState getState();

    StreetNode getStreetNode();

}