package model.map;

import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.IPointAccessor;
import model.map.accessors.IStringAccessor;
import model.map.accessors.ITileAccessor;
import model.targets.AddressPoint;

public interface IMapManager {

    long getID(int row, int column, int zoom);

    AddressPoint getAddress(int x, int y);

    int getVisibleRows(int zoom);

    int getVisibleColumns(int zoom);

    int getRow(int zoom);

    int getColumn(int zoom);

    IMapState getState();

    ITileAccessor createTileAccessor();

    IStringAccessor createStringAccessor();

    IPointAccessor createPointAccessor(String identifier);

    ICollectiveAccessor createCollectiveAccessor(String identifier);

}