package model.map;

public interface IMapBounds {

    // In map coordinates, not pixels
    int getWidth();

    int getHeight();

    int getX();

    int getY();

    int getMaxZoom();

    int getMinZoom();

}
