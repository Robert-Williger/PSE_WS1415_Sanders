package model.map;

import java.util.Iterator;

import util.Arrays;
import model.elements.IArea;
import model.elements.IBuilding;
import model.elements.Label;
import model.elements.POI;
import model.elements.IStreet;
import model.elements.IWay;

public class Tile extends AbstractTile {

    private final IWay[] ways;
    private final IArea[] iAreas;
    private final IStreet[] iStreets;
    private final IBuilding[] iBuildings;
    private final POI[] pois;
    private final Label[] labels;

    public Tile() {
        this(0, -1, -1);
    }

    public Tile(final int zoomStep, final int row, final int column) {
        this(zoomStep, row, column, new IWay[0], new IStreet[0], new IArea[0], new IBuilding[0], new POI[0], new Label[0]);
    }

    public Tile(final int zoomStep, final int row, final int column, final IWay[] ways, final IStreet[] streets,
            final IArea[] areas, final IBuilding[] buildings, final POI[] pois, final Label[] labels) {
        super(zoomStep, row, column);
        this.ways = ways;
        this.iAreas = areas;
        this.iStreets = streets;
        this.iBuildings = buildings;
        this.pois = pois;
        this.labels = labels;
    }

    @Override
    public Iterator<IStreet> getStreets() {
        return Arrays.iterator(iStreets);
    }

    @Override
    public Iterator<IWay> getWays() {
        return Arrays.iterator(ways);
    }

    @Override
    public Iterator<IBuilding> getBuildings() {
        return Arrays.iterator(iBuildings);
    }

    @Override
    public Iterator<IArea> getTerrain() {
        return Arrays.iterator(iAreas);
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