package model.map;

import java.awt.Point;
import java.util.Iterator;

import model.elements.Area;
import model.elements.Building;
import model.elements.POI;
import model.elements.Street;
import model.elements.Way;

public class Tile extends AbstractTile {

    private final int x;
    private final int y;
    private final Way[] ways;
    private final Area[] areas;
    private final Street[] streets;
    private final Building[] buildings;
    private final POI[] pois;

    public Tile() {
        this(0, -1, -1, 0, 0);
    }

    public Tile(final int zoomStep, final int row, final int column, final int x, final int y) {
        this(zoomStep, row, column, x, y, new Way[0], new Street[0], new Area[0], new Building[0], new POI[0]);
    }

    public Tile(final int zoomStep, final int row, final int column, final int x, final int y, final Way[] ways,
            final Street[] streets, final Area[] areas, final Building[] buildings, final POI[] pois) {
        super(zoomStep, row, column);
        this.x = x;
        this.y = y;
        this.ways = ways;
        this.areas = areas;
        this.streets = streets;
        this.buildings = buildings;
        this.pois = pois;
    }

    @Override
    public Point getLocation() {
        return new Point(x, y);
    }

    @Override
    public Iterator<Street> getStreets() {
        return new ArrayIterator<Street>(streets);// streets.iterator();
    }

    @Override
    public Iterator<Way> getWays() {
        return new ArrayIterator<Way>(ways);
    }

    @Override
    public Iterator<Building> getBuildings() {
        return new ArrayIterator<Building>(buildings);
    }

    @Override
    public Iterator<Area> getTerrain() {
        return new ArrayIterator<Area>(areas);
    }

    @Override
    public Iterator<POI> getPOIs() {
        return new ArrayIterator<POI>(pois);
    }

    private class ArrayIterator<T> implements Iterator<T> {

        private final T[] array;
        private int count;

        public ArrayIterator(final T[] array) {
            this.array = array;
        }

        @Override
        public boolean hasNext() {
            return count < array.length;
        }

        @Override
        public T next() {
            return array[count++];
        }

    }
}