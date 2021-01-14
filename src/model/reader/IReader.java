package model.reader;

import java.io.File;

import model.Progressable;
import model.addressIndex.IAddressMatcher;
import model.map.IMapManager;
import model.routing.IRouteManager;

public interface IReader extends Progressable {

    boolean read(File file);

    IMapManager getMapManager();

    IRouteManager getRouteManager();

    IAddressMatcher getTextProcessor();

}