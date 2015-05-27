package model.map;

import java.awt.Point;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import model.elements.Area;
import model.elements.Building;
import model.elements.Node;
import model.elements.POI;
import model.elements.Street;
import model.elements.StreetNode;
import model.elements.Way;

public class Tile implements ITile {

    private final long id;
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

        id = ((((long) zoomStep << 29) | row) << 29) | column;
        this.x = x;
        this.y = y;
        this.ways = ways;
        this.areas = areas;
        this.streets = streets;
        this.buildings = buildings;
        this.pois = pois;
    }

    @Override
    public long getID() {
        return id;
    }

    @Override
    public int getRow() {
        return (int) ((id >> 29) & 0x1FFFFFFFL);
    }

    @Override
    public int getColumn() {
        return (int) (id & 0x1FFFFFFFL);
    }

    @Override
    public int getZoomStep() {
        return (int) (id >> 58);
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

    @Override
    public String getAddress(final Point coordinate) {
        final StreetNode node = getStreetNode(coordinate);
        return node != null ? node.getStreet().getName() : null;
    }

    @Override
    public StreetNode getStreetNode(final Point coordinate) {
        StreetNode ret = null;

        int minDist = Integer.MAX_VALUE;

        for (final Street street : streets) {
            final List<Node> nodes = street.getNodes();
            final Iterator<Node> iterator = nodes.iterator();
            float totalLength = 0;
            final int maxLength = street.getLength();

            Point lastPoint = iterator.next().getLocation();
            while (iterator.hasNext()) {
                final Point currentPoint = iterator.next().getLocation();

                if (!currentPoint.equals(lastPoint)) {
                    final long dx = currentPoint.x - lastPoint.x;
                    final long dy = currentPoint.y - lastPoint.y;
                    final long square = (dx * dx + dy * dy);
                    final float length = (float) Math.sqrt(square);
                    double s = ((coordinate.x - lastPoint.x) * dx + (coordinate.y - lastPoint.y) * dy)
                            / (double) square;

                    if (s < 0) {
                        s = 0;
                    } else if (s > 1) {
                        s = 1;
                    }

                    final double distX = lastPoint.x + s * dx - coordinate.x;
                    final double distY = lastPoint.y + s * dy - coordinate.y;
                    final int distance = (int) Math.sqrt(distX * distX + distY * distY);

                    if (distance < minDist) {
                        ret = new StreetNode((float) ((totalLength + s * length) / maxLength), street);
                        minDist = distance;
                    }

                    totalLength += length;
                }
                lastPoint = currentPoint;
            }
        }

        return ret;
    }

    @Override
    public Building getBuilding(final Point coordinate) {
        for (final Building building : buildings) {
            if (building.getPolygon().contains(coordinate)) {
                return building;
            }
        }

        return null;
    }
}