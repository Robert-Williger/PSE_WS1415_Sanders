package model.map;

import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.IPointAccessor;
import model.map.accessors.IStringAccessor;
import model.map.accessors.ITileConversion;
import model.targets.AddressPoint;

public interface IMapManager {

    AddressPoint getAddress(int x, int y);

    int getVisibleRows(int zoom);

    int getVisibleColumns(int zoom);

    int getFirstRow(int zoom);

    int getFirstColumn(int zoom);

    IMapState getState();

    ITileConversion getTileConversion();

    IElementIterator getElementIterator(String identifier);

    IStringAccessor createStringAccessor();

    IPointAccessor createPointAccessor(String identifier);

    ICollectiveAccessor createCollectiveAccessor(String identifier);

}