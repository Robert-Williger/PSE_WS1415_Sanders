package model.renderEngine;

import java.awt.Image;

import model.IModel;
import model.map.IMapManager;

public interface IImageFetcher extends IModel {

    void flush();

    Image getImage(long id);

    void loadImage(long id, int priority);

    void setMapManager(IMapManager manager);

}