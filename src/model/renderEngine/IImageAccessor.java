package model.renderEngine;

import java.awt.Image;

import model.IModel;

public interface IImageAccessor extends IModel {

    void setVisible(boolean visible);

    boolean isVisible();

    String getName();

    Image getImage(int row, int column, int zoom);

}