package adminTool;

import java.io.File;
import java.util.Collection;
import java.util.List;

import adminTool.elements.Boundary;
import adminTool.elements.Building;
import adminTool.elements.Label;
import adminTool.elements.MultiElement;
import adminTool.elements.Way;
import adminTool.elements.POI;

public interface IOSMParser {

    void read(File file) throws Exception;

    Collection<Way> getWays();
    
    Collection<Way> getOneways();
    
    Collection<MultiElement> getTerrain();

    Collection<POI> getPOIs();

    Collection<Building> getBuildings();

    Collection<Label> getLabels();

    List<List<Boundary>> getBoundaries();

    NodeAccess getNodes();
}