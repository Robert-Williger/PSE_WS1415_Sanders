package model;

import java.io.File;

import model.map.IMapManager;
import model.routing.IRouteManager;
import model.routing.Progressable;

public interface IReader extends Progressable {

    boolean read(File file);

    IMapManager getMapManager();

    IRouteManager getRouteManager();

    ITextProcessor getTextProcessor();

}