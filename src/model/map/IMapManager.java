package model.map;

import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.IPointAccessor;
import model.map.accessors.IStringAccessor;
import model.map.accessors.ITileIdConversion;

public interface IMapManager {

    IAddressFinder getAddressFinder();

    IMapSection getMapSection();

    IMapBounds getMapBounds();

    IPixelMapping getPixelMapping();

    ITileIdConversion getTileIdConversion();

    ITileState getTileState();

    IElementIterator getElementIterator(String identifier);

    IStringAccessor getStringAccessor();

    IPointAccessor createPointAccessor(String identifier);

    ICollectiveAccessor createCollectiveAccessor(String identifier);

}