package model.renderEngine;

import java.awt.Image;

import model.IModel;
import model.map.IMapManager;

public interface IImageAccessor extends IModel {

    void setVisible(boolean visible);

    boolean isVisible();

    int getRows();

    int getColumns();

    Image getImage(int row, int column);

    void setMapManager(IMapManager manager);

}