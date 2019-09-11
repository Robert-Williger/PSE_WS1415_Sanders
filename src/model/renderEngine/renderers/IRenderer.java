package model.renderEngine.renderers;

import java.awt.Image;

import model.map.IMapManager;

public interface IRenderer {

    boolean render(long tileID, Image image);

    void setMapManager(IMapManager manager);

}