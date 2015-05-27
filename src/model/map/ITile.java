package model.map;

import java.awt.Point;
import java.util.Collection;

import model.elements.Area;
import model.elements.Building;
import model.elements.POI;
import model.elements.Street;
import model.elements.StreetNode;
import model.elements.Way;

public interface ITile {

    long getID();

    int getRow();

    int getColumn();

    int getZoomStep();

    Point getLocation();

    Collection<Street> getStreets();

    Collection<Way> getWays();

    Collection<Building> getBuildings();

    Collection<Area> getTerrain();

    Collection<POI> getPOIs();

    String getAddress(Point coordinate);

    StreetNode getStreetNode(Point coordinate);

    Building getBuilding(Point coordinate);

}