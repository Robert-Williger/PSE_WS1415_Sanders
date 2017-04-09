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

    void setCoordLocation(double x, double y);

    void setPixelLocation(double x, double y);

    double getCoordX();

    double getCoordY();

    void setPixelSectionSize(int width, int height);

    int getPixelSectionWidth();

    int getPixelSectionHeight();

    int getCoordSectionWidth(int zoom);

    int getCoordSectionHeight(int zoom);

    IPixelConverter getConverter();

}