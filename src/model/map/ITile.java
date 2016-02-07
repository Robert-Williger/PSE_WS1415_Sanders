package model.map;

import java.awt.Point;
import java.util.Iterator;

import model.elements.IArea;
import model.elements.IBuilding;
import model.elements.Label;
import model.elements.POI;
import model.elements.IStreet;
import model.elements.StreetNode;
import model.elements.IWay;

public interface ITile {

    long getID();

    int getRow();

    int getColumn();

    int getZoomStep();

    Iterator<IStreet> getStreets();

    Iterator<IWay> getWays();

    Iterator<IBuilding> getBuildings();

    Iterator<IArea> getTerrain();

    Iterator<POI> getPOIs();

    Iterator<Label> getLabels();

    StreetNode getStreetNode(Point coordinate);

    IBuilding getBuilding(Point coordinate);

}