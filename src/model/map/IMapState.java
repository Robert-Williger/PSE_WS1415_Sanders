package model.map;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

public interface IMapState {

    void setZoomStep(int zoomState);

    void setSize(int width, int height);

    void setSize(Dimension size);

    void setLocation(int x, int y);

    void setLocation(Point location);

    void move(int deltaX, int deltaY);

    int getZoomStep();

    int getMaxZoomStep();

    int getMinZoomStep();

    Rectangle getBounds();

    Point getLocation();

    Dimension getSize();

}