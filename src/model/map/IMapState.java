package model.map;

public interface IMapState {

    int getCoordMapWidth();

    int getCoordMapHeight();

    int getPixelTileSize();

    int getCoordTileSize(int zoom);

    void setZoom(int zoom);

    int getZoom();

    int getMaxZoom();

    int getMinZoom();

    void setLocation(double x, double y);

    double getX();

    double getY();

    void setPixelSectionSize(int width, int height);

    int getPixelSectionWidth();

    int getPixelSectionHeight();

    int getCoordSectionWidth(int zoom);

    int getCoordSectionHeight(int zoom);

    IPixelConverter getConverter();

}