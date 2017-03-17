package model.map;

public interface IMapListener {

    void mapZoomInitiated(int steps, double xOffset, double yOffset);

    void mapZoomed(int steps, double xOffset, double yOffset);

    void mapMoved(double deltaX, double deltaY);

    void mapResized(int width, int height);

}