package model.map;

import java.awt.Dimension;
import java.awt.Point;

public interface IMapManager {

    ITile getTile(long tileID);

    ITile getTile(Point coordinate, int zoomStep);

    ITile getTile(int row, int column, int zoomStep);

    Dimension getTileSize();

    AddressNode getAddressNode(Point coordinate);

    int getRows();

    int getColumns();

    Point getCurrentGridLocation();

    Point getCoord(Point pixelPoint);

    Point getPixel(Point coordinate);

    IMapState getMapState();

    IPixelConverter getConverter();

}