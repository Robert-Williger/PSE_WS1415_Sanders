package model.map;

public interface IMapListener {

    void mapZoomed(int steps, double deltaX, double deltaY);

    void mapMoved(double deltaX, double deltaY);

    void mapResized(int width, int height);

}