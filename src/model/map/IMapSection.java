package model.map;

// Position and size are represented in zoom level independent measure
public interface IMapSection {

    // In map coordinates, not pixels
    int getMidX();

    int getMidY();

    void setMidpoint(int midX, int midY);

    // In map pixels, not in map coordinates
    int getWidth();

    int getHeight();

    void setSize(int width, int height);

    int getZoom();

    void setZoom(int zoom);

}
