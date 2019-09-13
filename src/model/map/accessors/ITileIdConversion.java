package model.map.accessors;

public interface ITileIdConversion {

    long getId(int row, int column, int zoom);

    int getRow(long id);

    int getColumn(long id);

    int getZoom(long id);

}
