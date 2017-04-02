package model.renderEngine;

import java.awt.Image;

import model.IModel;
import model.map.IMapManager;

public interface IImageAccessor extends IModel {

    void setVisible(boolean visible);

    boolean isVisible();

    int getX(int zoom);

    int getY(int zoom);

    int getWidth();

    int getHeight();
    // int getRows(int zoom);
    //
    // int getColumns(int zoom);
    //
    // int getRow(int zoom);
    //
    // int getColumn(int zoom);
    //
    // int getXOffset(int zoom);
    //
    // int getYOffset(int zoom);

    Image getImage(int row, int column, int zoom);

    void setMapManager(IMapManager manager);

}