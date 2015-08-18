package model.map;

import java.awt.Point;
import java.util.Iterator;
import java.util.NoSuchElementException;

import model.elements.Area;
import model.elements.Building;
import model.elements.POI;
import model.elements.Street;
import model.elements.Way;

public class EmptyTile extends AbstractTile {

    private static final Point location;
    private static final Iterator<Way> ways;
    private static final Iterator<Street> streets;
    private static final Iterator<Building> buildings;
    private static final Iterator<POI> pois;
    private static final Iterator<Area> terrain;

    public EmptyTile(int zoomStep, int row, int column) {
        super(zoomStep, row, column);
    }

    @Override
    public Point getLocation() {
        return location;
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

    private static class EmptyIterator<T> implements Iterator<T> {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public T next() {
            throw new NoSuchElementException();
        }

    }

    static {
        location = new Point(0, 0);
        ways = new EmptyIterator<Way>();
        streets = new EmptyIterator<Street>();
        buildings = new EmptyIterator<Building>();
        pois = new EmptyIterator<POI>();
        terrain = new EmptyIterator<Area>();
    }
}
