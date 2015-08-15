package model.map;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;

import model.elements.Area;
import model.elements.Building;
import model.elements.POI;
import model.elements.Street;
import model.elements.Way;

public class EmptyTile extends AbstractTile {

    private static final Point location;
    private static final Collection<Way> ways;
    private static final Collection<Street> streets;
    private static final Collection<Building> buildings;
    private static final Collection<POI> pois;
    private static final Collection<Area> terrain;

    public EmptyTile(int zoomStep, int row, int column) {
        super(zoomStep, row, column);
    }

    @Override
    public Point getLocation() {
        return location;
    }

    @Override
    public Collection<Street> getStreets() {
        return streets;
    }

    @Override
    public Collection<Way> getWays() {
        return ways;
    }

    @Override
    public Collection<Building> getBuildings() {
        return buildings;
    }

    @Override
    public Collection<Area> getTerrain() {
        return terrain;
    }

    @Override
    public Collection<POI> getPOIs() {
        return pois;
    }

    static {
        location = new Point(0, 0);
        ways = new ArrayList<Way>(0);
        streets = new ArrayList<Street>(0);
        buildings = new ArrayList<Building>(0);
        pois = new ArrayList<POI>(0);
        terrain = new ArrayList<Area>(0);
    }
}
