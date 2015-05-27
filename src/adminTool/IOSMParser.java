package adminTool;

import java.awt.Rectangle;
import java.io.File;
import java.util.Collection;

import model.elements.Area;
import model.elements.Building;
import model.elements.POI;
import model.elements.Way;

public interface IOSMParser {

    void read(File file) throws Exception;

    Rectangle getBoundingBox();

    Collection<Way> getWays();

    Collection<Area> getTerrain();

    Collection<POI> getPOIs();

    Collection<Building> getBuildings();

    Collection<UnprocessedStreet> getStreets();

}