package model.map;

import java.awt.Dimension;
import java.awt.Point;

import model.elements.dereferencers.ITileDereferencer;

public interface IMapManager {

    long getTileID(Point coordinate, int zoomStep);

    long getTileID(int row, int column, int zoomStep);

    int getRows();

    int getColumns();

    Dimension getTileSize();

    AddressNode getAddressNode(Point coordinate);

    AddressNode getAddressNode(Point coordinate, final int zoomStep);

    Point getCurrentGridLocation();

    Point getCoord(Point pixelPoint);

    Point getPixel(Point coordinate);

    IMapState getMapState();

    IPixelConverter getConverter();

    ITileDereferencer createTileDereferencer();

}