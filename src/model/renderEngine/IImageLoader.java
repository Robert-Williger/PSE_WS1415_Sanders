package model.renderEngine;

import model.map.IMapManager;

public interface IImageLoader {

    void update();

    IImageAccessor getBackgroundAccessor();

    IImageAccessor getPOIAccessor();

    IImageAccessor getRouteAccessor();

    void setRenderRoute(IRenderRoute route);

    void setMapManager(IMapManager manager);

}