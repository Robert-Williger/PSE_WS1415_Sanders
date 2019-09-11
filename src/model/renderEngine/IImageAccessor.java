package model.renderEngine;

import java.awt.Image;

import model.IModel;
import model.map.accessors.ITileConversion;

public interface IImageAccessor extends IModel {

    void setVisible(boolean visible);

    boolean isVisible();

    Image getImage(int row, int column, int zoom);

    void setTileConversion(ITileConversion conversion);

    void addTileListener(ITileListener listener);

    void removeTileListener(ITileListener listener);
}