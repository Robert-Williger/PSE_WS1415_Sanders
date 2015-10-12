package adminTool;

import java.awt.Rectangle;
import java.io.File;
import java.util.Collection;
import java.util.List;

import model.elements.Area;
import model.elements.Building;
import model.elements.Label;
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

    Collection<Label> getLabels();

    List<List<Boundary>> getBoundaries();

}