package model.map;

import java.awt.Point;

import model.IModel;

public interface IMap extends IModel {

    void zoom(int steps, double offsetX, double offsetY);

    void moveView(double deltaX, double deltaY);

    AddressNode getAddressNode(int x, int y);

    void setSize(int width, int height);

    int getWidth();

    int getHeight();

    Point getViewLocation();

    void center(final double x, final double y, final double width, final double height);

    void center(final double x, final double y);

    void addMapListener(IMapListener listener);

    void removeMapListener(IMapListener listener);

    default void center(final Point point) {
        center(point.x, point.y);
    }

    default void zoom(int steps) {
        zoom(steps, 0.5, 0.5);
    }
}