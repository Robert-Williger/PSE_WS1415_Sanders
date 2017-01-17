package model.map;

import java.awt.Point;

import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.IPointAccessor;
import model.map.accessors.IStringAccessor;
import model.map.accessors.ITileAccessor;

public interface IMapManager {

    long getID(int row, int column, int zoom);

    int getTileSize();

    AddressNode getAddress(Point coordinate);

    int getRows();

    int getColumns();

    Point getGridLocation();

    Point getCoord(Point pixelPoint);

    Point getPixel(Point coordinate);

    IMapState getState();

    IPixelConverter getConverter();

    ITileAccessor createTileAccessor();

    IStringAccessor createStringAccessor();

    ICollectiveAccessor createCollectiveAccessor(String identifier);

    IPointAccessor createPointAccessor(String identifier);

}