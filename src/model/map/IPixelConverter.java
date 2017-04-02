package model.map;

public interface IPixelConverter {

    int getCoordDistance(int pixelDistance, int zoomStep);

    int getPixelDistance(int coordDistance, int zoomStep);

    float getCoordDistance(float pixelDistance, int zoomStep);

    float getPixelDistance(float coordDistance, int zoomStep);

    double getCoordDistance(double pixelDistance, int zoomStep);

    double getPixelDistance(double coordDistance, int zoomStep);
}