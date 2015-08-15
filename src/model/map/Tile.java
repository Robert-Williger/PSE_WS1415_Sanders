package model.map;

import java.awt.Point;
import java.util.Collection;
import java.util.LinkedList;

import model.elements.Area;
import model.elements.Building;
import model.elements.POI;
import model.elements.Street;
import model.elements.Way;

public class Tile extends AbstractTile {

    private final int x;
    private final int y;
    private final Collection<Way> ways;
    private final Collection<Area> areas;
    private final Collection<Street> streets;
    private final Collection<Building> buildings;
    private final Collection<POI> pois;

    public Tile() {
        this(0, -1, -1, 0, 0);
    }

    public Tile(final int zoomStep, final int row, final int column, final int x, final int y) {
        this(zoomStep, row, column, x, y, new LinkedList<Way>(), new LinkedList<Street>(), new LinkedList<Area>(),
                new LinkedList<Building>(), new LinkedList<POI>());
    }

    public Tile(final int zoomStep, final int row, final int column, final int x, final int y,
            final Collection<Way> ways, final Collection<Street> streets, final Collection<Area> areas,
            final Collection<Building> buildings, final Collection<POI> pois) {
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
        return areas;
    }

    @Override
    public Collection<POI> getPOIs() {
        return pois;
    }
}