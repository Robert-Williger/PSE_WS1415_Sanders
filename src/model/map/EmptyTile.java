package model.map;

import java.util.Iterator;

import util.Arrays;
import model.elements.IArea;
import model.elements.IBuilding;
import model.elements.Label;
import model.elements.POI;
import model.elements.IStreet;
import model.elements.IWay;

public class EmptyTile extends AbstractTile {

    private static final Iterator<IWay> ways;
    private static final Iterator<IStreet> iStreets;
    private static final Iterator<IBuilding> iBuildings;
    private static final Iterator<POI> pois;
    private static final Iterator<IArea> terrain;
    private static final Iterator<Label> labels;

    public EmptyTile(int zoomStep, int row, int column) {
        super(zoomStep, row, column);
    }

    @Override
    public Iterator<IStreet> getStreets() {
        return iStreets;
    }

    @Override
    public Iterator<IWay> getWays() {
        return ways;
    }

    @Override
    public Iterator<IBuilding> getBuildings() {
        return iBuildings;
    }

    @Override
    public Iterator<IArea> getTerrain() {
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
        iStreets = Arrays.iterator();
        iBuildings = Arrays.iterator();
        pois = Arrays.iterator();
        terrain = Arrays.iterator();
        labels = Arrays.iterator();
    }
}
