package model.map;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import model.IModel;

public interface IMap extends IModel {

    void zoom(int steps, Point location);

    void moveView(int deltaX, int deltaY);

    AddressNode getAddressNode(Point location);

    void setViewSize(Dimension size);

    Point getViewLocation();

    void center(Rectangle bounds);

    void center(Point point);

}