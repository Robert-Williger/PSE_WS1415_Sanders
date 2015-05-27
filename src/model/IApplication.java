package model;

import java.io.File;

import model.map.IMap;
import model.renderEngine.IImageLoader;
import model.routing.IRouteManager;
import model.routing.Progressable;

public interface IApplication extends IModel, Progressable {

    IMap getMap();

    IRouteManager getRouteManager();

    ITextProcessor getTextProcessing();

    IImageLoader getImageLoader();

    boolean setMapData(File file);

}