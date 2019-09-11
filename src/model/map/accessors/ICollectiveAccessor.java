package model.map.accessors;

import java.awt.Point;

public interface ICollectiveAccessor extends IElementAccessor, Iterable<Point> {

    int getX(int index);

    int getY(int index);

    int size();

    void setZoom(int zoom);

    int getZoom();

}
