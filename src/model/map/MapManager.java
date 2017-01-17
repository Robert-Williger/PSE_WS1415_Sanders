package model.map;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.PrimitiveIterator;

import model.IFactory;
import model.elements.AccessPoint;
import model.map.accessors.CollectiveUtil;
import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.IPointAccessor;
import model.map.accessors.IStringAccessor;
import model.map.accessors.ITileAccessor;
import model.map.accessors.StringAccessor;
import model.map.accessors.TileAccessor;

public class MapManager implements IMapManager {
    private final java.util.Map<String, IFactory<IPointAccessor>> pointMap;
    private final java.util.Map<String, IFactory<ICollectiveAccessor>> collectiveMap;
    private final IFactory<ITileAccessor> tileFactory;
    private final String[] strings;

    private final ITileAccessor tileAccessor;
    private final ICollectiveAccessor buildingAccessor;
    private final ICollectiveAccessor streetAccessor;
    private final IStringAccessor stringAccessor;
    private final IPixelConverter converter;
    private final IMapState state;
    private final int tileSize;

    private static IFactory<ITileAccessor> emptyFactory() {
        return new IFactory<ITileAccessor>() {

            @Override
            public ITileAccessor create() {
                return new TileAccessor(new HashMap<String, IQuadtree>(), new PixelConverter(1), 256);
            }

        };
    }

    public MapManager() {
        // TODO
        this(new HashMap<String, IFactory<IPointAccessor>>(), new HashMap<String, IFactory<ICollectiveAccessor>>(),
                emptyFactory(), new String[0], new PixelConverter(1), new MapState(0, 0, 0, 0), 256);
    }

    // TODO
    public MapManager(final java.util.Map<String, IFactory<IPointAccessor>> pointMap,
            final java.util.Map<String, IFactory<ICollectiveAccessor>> collectiveMap,
            final IFactory<ITileAccessor> tileFactory, final String[] strings, final IPixelConverter converter,
            final IMapState state, final int tileSize) {
        this.converter = converter;
        this.tileSize = tileSize;
        this.state = state;

        this.pointMap = pointMap;
        this.collectiveMap = collectiveMap;
        this.tileFactory = tileFactory;
        this.strings = strings;

        this.tileAccessor = createTileAccessor();
        this.buildingAccessor = createCollectiveAccessor("building");
        this.streetAccessor = createCollectiveAccessor("street");
        this.stringAccessor = createStringAccessor();
    }

    @Override
    public AddressNode getAddress(final Point coordinate) {

        final int maxZoom = state.getMaxZoomStep();

        tileAccessor.setRCZ(getRow(coordinate.y, maxZoom), getColumn(coordinate.x, maxZoom), maxZoom);
        // final boolean buildingHit = getBuilding(coordinate);

        // if (buildingHit) {
        // return new AddressNode(buildingAccessor.getAddress(),
        // building.getStreetNode());
        // } else {
        int zoom = state.getMaxZoomStep();
        // TODO find better abort criterion .. + define consistent max
        // distance for search
        while (zoom >= state.getZoomStep()) {
            final int row = getRow(coordinate.y, zoom);
            final int column = getColumn(coordinate.x, zoom);

            AddressNode node = locateAddressNode(coordinate, row, column, zoom);
            if (node != null) {
                return node;
            }
            --zoom;
        }
        return null;
        // }
    }

    private int getRow(final int yCoord, final int zoom) {
        return converter.getPixelDistance(yCoord, zoom) / tileSize;
    }

    private int getColumn(final int xCoord, final int zoom) {
        return converter.getPixelDistance(xCoord, zoom) / tileSize;
    }

    private AddressNode locateAddressNode(final Point coordinate, final int row, final int column, final int zoom) {
        final AddressNode ret = new AddressNode();

        final int coordTileSize = converter.getCoordDistance(tileSize, zoom);
        final int tileX = column * coordTileSize;
        final int tileY = row * coordTileSize;
        final int midX = tileX + coordTileSize / 2;
        final int midY = tileY + coordTileSize / 2;
        final int nColumn = coordinate.x - midX < 0 ? -1 : 1; // neighbor column
        final int nRow = coordinate.y - midY < 0 ? -1 : 1; // neighbor row

        final long[] ids = calculateIDs(row, column, zoom, nRow, nColumn);
        final int[] distances = calculateDistances(coordinate, midX, midY, tileX, tileY, nColumn, nRow, coordTileSize);

        int nodeDistance = Integer.MAX_VALUE;

        for (int i = 0; i < 4; i++) {
            final int tileDistance = distances[i];
            if (tileDistance < nodeDistance) {
                tileAccessor.setID(ids[i]);
                nodeDistance = locateStreetNode(coordinate, nodeDistance, ret);
            }
        }

        if (ret.getAccessPoint() != null) {
            int buildingDistance = Integer.MAX_VALUE;

            for (int i = 0; i < 4; i++) {
                final int tileDistance = distances[i];
                if (tileDistance < buildingDistance) {
                    tileAccessor.setID(ids[i]);
                    buildingDistance = locateBuildingAddress(ret, buildingDistance);
                }
            }
            return ret;
        }

        // TODO return null or addressnode with null values?
        return null;
    }

    private long[] calculateIDs(int row, int column, final int zoom, int nRow, int nColumn) {
        long[] ids = new long[4];

        for (int i = 0; i < 4; i++) {
            ids[i] = getID(row + (i % 2) * nRow, column + (i / 2) * nColumn, zoom);
        }

        return ids;
    }

    private int[] calculateDistances(final Point coordinate, final int midX, final int midY, final int tileX,
            final int tileY, final int neighbourColumn, final int neighbourRow, final int coordTileSize) {
        final int hDistance = Math.abs((neighbourColumn + 1) / 2 * coordTileSize + tileX - coordinate.x);
        final int vDistance = Math.abs((neighbourRow + 1) / 2 * coordTileSize + tileY - coordinate.y);
        final int dDistance = (int) Math.sqrt(hDistance * hDistance + vDistance * vDistance);
        return new int[]{0, hDistance, vDistance, dDistance};
    }

    private int locateStreetNode(final Point coordinate, final int maxDistance, final AddressNode node) {
        // final int distance = (int) Point.distance(node.getX(), node.getY(),
        // coordinate.getX(),
        // coordinate.getY());

        // node.setStreetNode(streetNode);
        // node.setAddress(streetNode.getStreet().getName());

        return maxDistance;
    }

    private int locateBuildingAddress(final AddressNode node, int maxDistance) {
        final AccessPoint accessPoint = node.getAccessPoint();
        final int street = accessPoint.getStreet();
        stringAccessor.setID(street);
        final String streetName = stringAccessor.getString();
        // TODO
        streetAccessor.setID(street);
        final Point location = CollectiveUtil.getLocation(streetAccessor, accessPoint.getOffset());
        final PrimitiveIterator.OfLong buildings = tileAccessor.getElements("building");
        while (buildings.hasNext()) {
            final long buildingID = buildings.nextLong();
            buildingAccessor.setID(buildingID);
            final int bStreet = buildingAccessor.getAttribute("name");
            stringAccessor.setID(bStreet);
            if (streetName.equals(stringAccessor.getString())) {
                final int size = buildingAccessor.size();
                for (int i = 0; i < size; i++) {
                    final int distance = (int) Point.distance(location.x, location.y, buildingAccessor.getX(i),
                            buildingAccessor.getY(i));
                    if (distance < maxDistance) {
                        maxDistance = distance;
                        stringAccessor.setID(buildingAccessor.getAttribute("number"));
                        node.setAddress(streetName + " " + stringAccessor.getString());
                    }
                }
            }
        }

        return maxDistance;
    }

    // private boolean getBuilding(final Point coordinate) {
    // final PrimitiveIterator.OfLong iterator =
    // tileAccessor.getElements("building");
    // while (iterator.hasNext()) {
    // buildingAccessor.setID(iterator.nextLong());
    // if (CollectiveUtil.contains(buildingAccessor, coordinate.x,
    // coordinate.y)) {
    // return true;
    // }
    // }
    //
    // return false;
    // }

    @Override
    public int getRows() {
        final Rectangle bounds = state.getBounds();
        return getGridSize(bounds.height, bounds.y);
    }

    @Override
    public int getColumns() {
        final Rectangle bounds = state.getBounds();
        return getGridSize(bounds.width, bounds.x);
    }

    private int getGridSize(final int size, final int location) {

        final int zoom = state.getZoomStep();
        final double zoomFactor = 1.0 / (1 << zoom - state.getMinZoomStep());
        final int coordWidth = (int) (size * zoomFactor);
        final int coordTileSize = converter.getCoordDistance(tileSize, zoom);

        return (location + coordWidth) / coordTileSize - location / coordTileSize + 1;
    }

    @Override
    public Point getGridLocation() {
        final Point location = state.getLocation();
        final int zoom = state.getZoomStep();
        final int coordTileSize = converter.getCoordDistance(tileSize, zoom);
        final int column = location.x / coordTileSize;
        final int row = location.y / coordTileSize;
        return new Point(column, row);
    }

    @Override
    public Point getCoord(final Point pixelPoint) {
        final Point location = state.getLocation();
        final int zoom = state.getZoomStep();
        final int coordX = converter.getCoordDistance(pixelPoint.x, zoom) + location.x;
        final int coordY = converter.getCoordDistance(pixelPoint.y, zoom) + location.y;

        return new Point(coordX, coordY);
    }

    @Override
    public Point getPixel(final Point coordinate) {
        final Point location = state.getLocation();
        final int zoom = state.getZoomStep();
        final int pixelX = converter.getPixelDistance(coordinate.x - location.x, zoom);
        final int pixelY = converter.getPixelDistance(coordinate.y - location.y, zoom);

        return new Point(pixelX, pixelY);
    }

    @Override
    public IMapState getState() {
        return state;
    }

    @Override
    public IPixelConverter getConverter() {
        return converter;
    }

    @Override
    public long getID(int row, int column, int zoom) {
        return ((((long) zoom << 29) | row) << 29) | column;
    }

    @Override
    public int getTileSize() {
        return tileSize;
    }

    @Override
    public ITileAccessor createTileAccessor() {
        return tileFactory.create();
    }

    @Override
    public IStringAccessor createStringAccessor() {
        return new StringAccessor(strings);
    }

    @Override
    public ICollectiveAccessor createCollectiveAccessor(final String identifier) {
        final IFactory<ICollectiveAccessor> ret = collectiveMap.get(identifier);
        return ret != null ? ret.create() : null;
    }

    @Override
    public IPointAccessor createPointAccessor(String identifier) {
        final IFactory<IPointAccessor> ret = pointMap.get(identifier);
        return ret != null ? ret.create() : null;
    }

}