package model.map.accessors;

import java.util.PrimitiveIterator;

public interface ITileAccessor extends IAccessor {

    void setRCZ(int row, int column, int zoom);

    int getZoom();

    int getX();

    int getY();

    PrimitiveIterator.OfLong getElements(String identifier);

}
