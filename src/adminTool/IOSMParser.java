package adminTool;

import java.awt.Rectangle;
import java.io.File;
import java.util.Collection;
import java.util.List;

import adminTool.elements.Boundary;
import adminTool.elements.UnprocessedStreet;
import adminTool.elements.Area;
import adminTool.elements.Building;
import adminTool.elements.Label;
import adminTool.elements.POI;
import adminTool.elements.Way;

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