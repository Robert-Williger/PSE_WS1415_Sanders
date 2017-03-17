package model.map;

public interface IPixelConverter {

    int getCoordDistance(int pixelDistance, int zoomStep);

    int getPixelDistance(int coordDistance, int zoomStep);

    float getPixelDistancef(float coordDistance, int zoomStep);

    double getCoordDistanced(double pixelDistance, int zoomStep);

    double getPixelDistanced(double coordDistance, int zoomStep);
}