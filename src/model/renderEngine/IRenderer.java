package model.renderEngine;

import java.awt.Image;

import model.IModel;
import model.map.IMapManager;

public interface IRenderer extends IModel {

    boolean render(long tileID, Image image);

    void setMapManager(IMapManager manager);

}