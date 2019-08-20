package adminTool;

import java.io.File;
import java.util.Collection;

import adminTool.elements.Boundary;
import adminTool.elements.Building;
import adminTool.elements.IPointAccess;
import adminTool.elements.MultiElement;
import adminTool.elements.Way;
import adminTool.elements.POI;
import adminTool.elements.PointLabel;

public interface IOSMParser {

    void read(File file) throws Exception;

    Collection<Way> getWays();

    Collection<MultiElement> getAreas();

    Collection<POI> getPOIs();

    Collection<Building> getBuildings();

    Collection<PointLabel> getPointLabels();

    Collection<Boundary> getBoundaries();

    IPointAccess getPoints();
}