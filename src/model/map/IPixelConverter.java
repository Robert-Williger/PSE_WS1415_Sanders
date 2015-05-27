package model.map;

public interface IPixelConverter {

    int getCoordDistance(int pixelDistance, int zoomStep);

    int getPixelDistance(int coordDistance, int zoomStep);

    float getPixelDistancef(float coordDistance, int zoomStep);

}