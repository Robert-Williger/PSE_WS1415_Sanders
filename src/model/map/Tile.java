package model.map;

import java.util.Iterator;

import util.Arrays;
import model.elements.Area;
import model.elements.Building;
import model.elements.Label;
import model.elements.POI;
import model.elements.Street;
import model.elements.Way;

public class Tile extends AbstractTile {

    private final Way[] ways;
    private final Area[] areas;
    private final Street[] streets;
    private final Building[] buildings;
    private final POI[] pois;
    private final Label[] labels;

    public Tile() {
        this(0, -1, -1);
    }

    public Tile(final int zoomStep, final int row, final int column) {
        this(zoomStep, row, column, new Way[0], new Street[0], new Area[0], new Building[0], new POI[0], new Label[0]);
    }

    public Tile(final int zoomStep, final int row, final int column, final Way[] ways, final Street[] streets,
            final Area[] areas, final Building[] buildings, final POI[] pois, final Label[] labels) {
        super(zoomStep, row, column);
        this.ways = ways;
        this.areas = areas;
        this.streets = streets;
        this.buildings = buildings;
        this.pois = pois;
        this.labels = labels;
    }

    @Override
    public Iterator<Street> getStreets() {
        return Arrays.iterator(streets);
    }

    @Override
    public Iterator<Way> getWays() {
        return Arrays.iterator(ways);
    }

    @Override
    public Iterator<Building> getBuildings() {
        return Arrays.iterator(buildings);
    }

    @Override
    public Iterator<Area> getTerrain() {
        return Arrays.iterator(areas);
    }

    @Override
    public Iterator<POI> getPOIs() {
        return Arrays.iterator(pois);
    }

    @Override
    public Iterator<Label> getLabels() {
        return Arrays.iterator(labels);
    }
}