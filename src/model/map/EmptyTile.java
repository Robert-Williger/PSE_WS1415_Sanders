package model.map;

import java.util.Iterator;

import util.Arrays;
import model.elements.Area;
import model.elements.Building;
import model.elements.Label;
import model.elements.POI;
import model.elements.Street;
import model.elements.Way;

public class EmptyTile extends AbstractTile {

    private static final Iterator<Way> ways;
    private static final Iterator<Street> streets;
    private static final Iterator<Building> buildings;
    private static final Iterator<POI> pois;
    private static final Iterator<Area> terrain;
    private static final Iterator<Label> labels;

    public EmptyTile(int zoomStep, int row, int column) {
        super(zoomStep, row, column);
    }

    @Override
    public Iterator<Street> getStreets() {
        return streets;
    }

    @Override
    public Iterator<Way> getWays() {
        return ways;
    }

    @Override
    public Iterator<Building> getBuildings() {
        return buildings;
    }

    @Override
    public Iterator<Area> getTerrain() {
        return terrain;
    }

    @Override
    public Iterator<POI> getPOIs() {
        return pois;
    }

    @Override
    public Iterator<Label> getLabels() {
        return labels;
    }

    static {
        ways = Arrays.iterator();
        streets = Arrays.iterator();
        buildings = Arrays.iterator();
        pois = Arrays.iterator();
        terrain = Arrays.iterator();
        labels = Arrays.iterator();
    }
}
