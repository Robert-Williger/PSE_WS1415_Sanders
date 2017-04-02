package model.renderEngine;

import java.util.List;

import model.map.IMapManager;

public interface IImageLoader {

    void update();

    // Layers ordered from front to back
    List<IImageAccessor> getImageAccessors();

    void setRenderRoute(IRenderRoute route);

    void setMapManager(IMapManager manager);

    int getTileSize();

}