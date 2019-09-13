package model.map;

public interface ITileState {

    // inclusive
    int getLastRow(int zoom);

    int getLastColumn(int zoom);

    int getFirstRow(int zoom);

    int getFirstColumn(int zoom);

    int getTileSize();
}
