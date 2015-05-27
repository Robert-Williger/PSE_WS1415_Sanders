package model.renderEngine;

import java.awt.Image;

import model.map.IMapManager;

public interface IImageFetcher {

    void flush();

    Image getImage(long id);

    void loadImage(long id, int priority);

    IRenderer getRenderer();

    void setMapManager(IMapManager manager);

}