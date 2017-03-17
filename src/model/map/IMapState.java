package model.map;

public interface IMapState {

    void setZoomStep(int zoomStep);

    void setSectionSize(int width, int height);

    void setLocation(double x, double y);

    // void move(double deltaX, double deltaY);

    int getZoomStep();

    int getMaxZoomStep();

    int getMinZoomStep();

    int getPixelSectionWidth();

    int getPixelSectionHeight();

    int getCoordSectionWidth();

    int getCoordSectionHeight();

    int getTotalWidth();

    int getTotalHeight();

    double getX();

    double getY();
}