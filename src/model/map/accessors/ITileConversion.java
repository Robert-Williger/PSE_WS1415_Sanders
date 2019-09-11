package model.map.accessors;

public interface ITileConversion {

    long getId(int row, int column, int zoom);

    int getRow(long id);

    int getRow(int y, int zoom);

    int getColumn(long id);

    int getColumn(int x, int zoom);

    int getZoom(long id);

    int getX(int column, int zoom);

    int getX(long id);

    int getY(int row, int zoom);

    int getY(long id);

}
