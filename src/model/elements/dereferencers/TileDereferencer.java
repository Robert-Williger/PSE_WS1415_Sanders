package model.elements.dereferencers;

import java.util.Iterator;

import model.IByteSource;

public class TileDereferencer implements ITileDereferencer {
    private long id;
    private long startAdress;
    private final IByteSource source;

    public TileDereferencer(final IByteSource source) {
        this.source = source;
    }

    @Override
    public long getID() {
        return id;
    }

    @Override
    public int getRow() {
        return 0;
    }

    @Override
    public int getColumn() {
        return 0;
    }

    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getY() {
        return 0;
    }

    @Override
    public int getZoomStep() {
        return 0;
    }

    @Override
    public boolean istEmpty() {
        return false;
    }

    @Override
    public Iterator<Integer> getStreets() {
        return null;
    }

    @Override
    public Iterator<Integer> getWays() {
        return null;
    }

    @Override
    public Iterator<Integer> getBuildings() {
        return null;
    }

    @Override
    public Iterator<Integer> getTerrain() {
        return null;
    }

    @Override
    public Iterator<Integer> getPOIs() {
        return null;
    }

    @Override
    public Iterator<Integer> getLabels() {
        return null;
    }

    @Override
    public void setID(long id) {
        this.id = id;
    }

    @Override
    public IBuildingDereferencer getBuildingDereferencer() {
        return null;
    }

    @Override
    public IAreaDereferencer getAreaDereferencer() {
        return null;
    }

    @Override
    public IStreetDereferencer getStreetDereferencer() {
        return null;
    }

    @Override
    public IWayDereferencer getWayDereferencer() {
        return null;
    }

    @Override
    public IPOIDereferencer getPOIDereferencer() {
        return null;
    }

    @Override
    public ILabelDereferencer getLabelDereferencer() {
        return null;
    }
}
