package model.map;

public interface ITileSource {

    ITile getTile(int row, int column, int zoomStep);

}
