package model.map;

import java.awt.Point;

import model.IModel;
import model.targets.AddressPoint;

public interface IMap extends IModel {

    int getZoom();

    void zoom(int steps, double offsetX, double offsetY);

    void move(int deltaX, int deltaY);

    AddressPoint getAddress(int x, int y);

    void setSize(int width, int height);

    int getWidth();

    int getHeight();

    // midpoint of current section in pixels
    int getX(int zoom);

    int getY(int zoom);

    void center(final int x, final int y, final int width, final int height);

    void center(final int x, final int y);

    void addMapListener(IMapListener listener);

    void removeMapListener(IMapListener listener);

    default void center(final Point point) {
        center(point.x, point.y);
    }

    default void zoom(int steps) {
        zoom(steps, 0.5, 0.5);
    }

    default int getX() {
        return getX(getZoom());
    }

    default int getY() {
        return getY(getZoom());
    }
}