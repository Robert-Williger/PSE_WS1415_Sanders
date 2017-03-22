package model.map;

import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.IPointAccessor;
import model.map.accessors.IStringAccessor;
import model.map.accessors.ITileAccessor;

public interface IMapManager {

    long getID(int row, int column, int zoom);

    int getTileSize();

    AddressPoint getAddress(int x, int y);

    int getVisibleRows();

    int getVisibleColumns();

    int getRow();

    int getColumn();

    IMapState getState();

    IPixelConverter getConverter();

    ITileAccessor createTileAccessor();

    IStringAccessor createStringAccessor();

    ICollectiveAccessor createCollectiveAccessor(String identifier);

    IPointAccessor createPointAccessor(String identifier);

}