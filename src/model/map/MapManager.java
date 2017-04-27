package model.map;

import java.awt.Point;
import java.util.HashMap;
import java.util.PrimitiveIterator;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.LongPredicate;

import model.IFactory;
import model.map.accessors.CollectiveUtil;
import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.IPointAccessor;
import model.map.accessors.IStringAccessor;
import model.map.accessors.ITileAccessor;
import model.map.accessors.StringAccessor;
import model.map.accessors.TileAccessor;
import model.targets.AddressPoint;

public class MapManager implements IMapManager {
    private static final int COORD_BITS = 29;
    private static final int ZOOM_BITS = Long.SIZE - 2 * COORD_BITS - 1;
    private static final int MAX_COORD = (1 << COORD_BITS) - 1;
    private static final int MAX_ZOOM = (1 << ZOOM_BITS) - 1;

    private static final int MAX_STREET_BUILDING_PIXEL_DISTANCE = 75;
    // TODO improve this
    private static final String UNKNOWN_STREET = "Unbekannte Straße";

    private final java.util.Map<String, IFactory<IPointAccessor>> pointMap;
    private final java.util.Map<String, IFactory<ICollectiveAccessor>> collectiveMap;
    private final IFactory<ITileAccessor> tileFactory;
    private final String[] strings;

    private final ITileAccessor tileAccessor;
    private final ICollectiveAccessor buildingAccessor;
    private final ICollectiveAccessor streetAccessor;
    private final IStringAccessor stringAccessor;
    private final IMapState state;

    // TODO improve this
    private final long streetBuildingCoordDistanceSq;

    private static IFactory<ITileAccessor> emptyFactory() {
        return () -> {
            return new TileAccessor(new HashMap<String, IElementIterator>(),
                    new MapState(256, 256, 0, 1, 256, new PixelConverter(1)));
        };
    }

    public MapManager() {
        this(new HashMap<>(), new HashMap<>(), emptyFactory(), new String[0],
                new MapState(256, 256, 0, 1, 256, new PixelConverter(1)));
    }

    public MapManager(final java.util.Map<String, IFactory<IPointAccessor>> pointMap,
            final java.util.Map<String, IFactory<ICollectiveAccessor>> collectiveMap,
            final IFactory<ITileAccessor> tileFactory, final String[] strings, final IMapState state) {
        this.state = state;

        this.pointMap = pointMap;
        this.collectiveMap = collectiveMap;
        this.tileFactory = tileFactory;
        this.strings = strings;

        this.tileAccessor = createTileAccessor();
        this.buildingAccessor = createCollectiveAccessor("building");
        this.streetAccessor = createCollectiveAccessor("street");
        this.stringAccessor = createStringAccessor();

        final int coordDistance = state.getConverter().getCoordDistance(MAX_STREET_BUILDING_PIXEL_DISTANCE,
                state.getMaxZoom());
        streetBuildingCoordDistanceSq = coordDistance * coordDistance;
    }

    @Override
    public AddressPoint getAddress(final int x, final int y) {
        final Target target = new Target();

        final long building = getBuildingAt(x, y);

        final long[] tileIds = new long[4];
        final double[] tileDistanceSquares = new double[4];

        if (building != -1) {
            buildingAccessor.setID(building);
            final int street = buildingAccessor.getAttribute("street");
            final LongPredicate pred = l -> {
                streetAccessor.setID(l);
                final int oStreet = streetAccessor.getAttribute("name");
                return street == -1 || oStreet == -1 || oStreet == street;
            };

            if (applyLocation(x, y, tileIds, tileDistanceSquares, pred, target)
                    || applyLocation(x, y, tileIds, tileDistanceSquares, l -> true, target)) {
                if (street == -1) {
                    streetAccessor.setID(target.street);
                    target.address = getStreetName(streetAccessor.getAttribute("name"));
                } else {
                    target.address = getBuildingAddress(street);
                }

                return target.toAddressPoint();
            }
        } else {
            if (applyLocation(x, y, tileIds, tileDistanceSquares, l -> true, target)) {
                if (!applyAddressByBuilding(target, tileIds, tileDistanceSquares)) {
                    streetAccessor.setID(target.street);
                    target.address = getStreetName(streetAccessor.getAttribute("name"));
                }
                return target.toAddressPoint();
            }
        }

        // TODO find better abort criterion .. + define consistent max
        // distance for search

        return null;
    }

    private boolean applyLocation(final int x, final int y, final long[] tileIds, final double[] tileDistanceSquares,
            final LongPredicate pred, final Target node) {
        double minDistSq = Long.MAX_VALUE;

        for (int zoom = state.getMaxZoom(); zoom >= state.getZoom(); zoom--) {
            fillArrays(x, y, zoom, tileDistanceSquares, tileIds);

            for (int i = 0; i < 4; i++) {
                if (tileDistanceSquares[i] < minDistSq) {
                    minDistSq = applyLocation(x, y, minDistSq, node, tileIds[i], pred);
                }
            }

            if (minDistSq != Long.MAX_VALUE) {
                return true;
            }
            --zoom;
        }

        return false;
    }

    private double applyLocation(final int x, final int y, double minDistanceSq, final Target target, final long id,
            final LongPredicate pred) {
        tileAccessor.setID(id);
        for (OfLong iterator = tileAccessor.getElements("street"); iterator.hasNext();) {
            long street = iterator.nextLong();

            if (pred.test(street)) {
                streetAccessor.setID(street);

                float totalLength = 0;
                final int size = streetAccessor.size();
                final int maxLength = streetAccessor.getAttribute("length");

                int lastX = streetAccessor.getX(0);
                int lastY = streetAccessor.getY(0);

                for (int i = 1; i < size; i++) {
                    int currentX = streetAccessor.getX(i);
                    int currentY = streetAccessor.getY(i);

                    if (currentX != lastX || currentY != lastY) {
                        final long dx = currentX - lastX;
                        final long dy = currentY - lastY;
                        final long square = (dx * dx + dy * dy);
                        final float length = (float) Math.sqrt(square);
                        final double s = Math.min(1,
                                Math.max(0, ((x - lastX) * dx + (y - lastY) * dy) / (double) square));
                        final double distanceSq = Point.distanceSq(lastX + s * dx, lastY + s * dy, x, y);

                        if (distanceSq < minDistanceSq) {
                            // TODO long or int for street id?
                            target.offset = (float) ((totalLength + s * length) / maxLength);
                            target.street = (int) street;
                            target.x = (int) (lastX + s * dx);
                            target.y = (int) (lastY + s * dy);
                            minDistanceSq = distanceSq;
                        }

                        totalLength += length;
                    }
                    lastX = currentX;
                    lastY = currentY;
                }
            }
        }

        return minDistanceSq;
    }

    private boolean applyAddressByBuilding(final Target target, final long[] ids, final double[] distanceSquares) {
        double buildingDistanceSq = streetBuildingCoordDistanceSq;

        for (int i = 0; i < 4; i++) {
            if (distanceSquares[i] < buildingDistanceSq) {
                buildingDistanceSq = applyAddressByBuilding(target, buildingDistanceSq, ids[i]);
            }
        }
        return buildingDistanceSq != streetBuildingCoordDistanceSq;
    }

    private double applyAddressByBuilding(final Target target, double minDistSq, final long id) {
        final int x = target.x;
        final int y = target.y;

        tileAccessor.setID(id);

        streetAccessor.setID(target.street);
        final int name = streetAccessor.getAttribute("name");
        final String street = getStreetName(name);

        // TODO use lambda expression (tileAccessor.forEach).
        final PrimitiveIterator.OfLong buildings = tileAccessor.getElements("building");
        while (buildings.hasNext()) {
            final long buildingID = buildings.nextLong();
            buildingAccessor.setID(buildingID);
            if (name == buildingAccessor.getAttribute("street")) {
                final int size = buildingAccessor.size();

                int lastX = buildingAccessor.getX(0);
                int lastY = buildingAccessor.getY(0);

                for (int i = 1; i < size; i++) {
                    int currentX = buildingAccessor.getX(i);
                    int currentY = buildingAccessor.getY(i);

                    if (currentX != lastX || currentY != lastY) {
                        final long dx = currentX - lastX;
                        final long dy = currentY - lastY;
                        final long square = (dx * dx + dy * dy);
                        final double s = Math.min(1,
                                Math.max(0, ((x - lastX) * dx + (y - lastY) * dy) / (double) square));
                        final double distanceSq = Point.distanceSq(lastX + s * dx, lastY + s * dy, x, y);

                        if (distanceSq < minDistSq) {
                            minDistSq = distanceSq;
                            target.address = getBuildingAddress(street);
                        }
                    }
                    lastX = currentX;
                    lastY = currentY;
                }
            }
        }

        return minDistSq;
    }

    private String getStreetName(final int streetNameStringID) {
        return streetNameStringID != -1 ? stringAccessor.getString(streetNameStringID) : UNKNOWN_STREET;
    }

    private String getBuildingAddress(final int streetNameStringID) {
        final String name = getStreetName(streetNameStringID);
        return getBuildingAddress(name);
    }

    private String getBuildingAddress(final String streetName) {
        final int number = buildingAccessor.getAttribute("number");
        return streetName + (number != -1 ? (" " + stringAccessor.getString(number)) : "");
    }

    private long getBuildingAt(final int x, final int y) {
        final int zoom = state.getMaxZoom();
        final int row = getRow(y, zoom);
        final int column = getColumn(x, zoom);

        tileAccessor.setRCZ(row, column, zoom);
        final PrimitiveIterator.OfLong iterator = tileAccessor.getElements("building");
        while (iterator.hasNext()) {
            final long building = iterator.nextLong();
            if (CollectiveUtil.contains(buildingAccessor, building, x, y)) {
                return building;
            }
        }

        return -1;
    }

    private void fillArrays(final int x, final int y, final int zoom, final double[] distanceSq, final long[] tileIDs) {
        final int row = getRow(y, zoom);
        final int column = getColumn(x, zoom);
        final int coordTileSize = state.getCoordTileSize(zoom);
        final int tileX = column * coordTileSize;
        final int tileY = row * coordTileSize;
        final int midX = tileX + coordTileSize / 2;
        final int midY = tileY + coordTileSize / 2;
        final int nColumn = x - midX < 0 ? -1 : 1; // neighbor column
        final int nRow = y - midY < 0 ? -1 : 1; // neighbor row

        calculateTileIDs(row, column, zoom, nRow, nColumn, tileIDs);
        calculateTileDistanceSquares(x, y, tileX, tileY, nRow, nColumn, coordTileSize, distanceSq);
    }

    private void calculateTileIDs(int row, int column, final int zoom, int nRow, int nColumn, long[] tileIDs) {
        for (int i = 0; i < 4; i++) {
            tileIDs[i] = getTileID(row + (i % 2) * nRow, column + (i / 2) * nColumn, zoom);
        }
    }

    private void calculateTileDistanceSquares(final int x, final int y, final int tileX, final int tileY,
            final int nRow, final int nColumn, final int coordTileSize, final double[] distanceSquares) {
        distanceSquares[0] = 0;

        distanceSquares[1] = Math.abs((nColumn + 1) / 2 * coordTileSize + tileX - x);
        distanceSquares[1] *= distanceSquares[0];

        distanceSquares[2] = Math.abs((nRow + 1) / 2 * coordTileSize + tileY - y);
        distanceSquares[2] *= distanceSquares[0];

        distanceSquares[3] = distanceSquares[1] + distanceSquares[2];
    }

    private int getRow(final int yCoord, final int zoom) {
        return yCoord / state.getCoordTileSize(zoom);
    }

    private int getColumn(final int xCoord, final int zoom) {
        return xCoord / state.getCoordTileSize(zoom);
    }

    private int getGridSize(final int sectionSize, final int location, final int zoom) {
        final int cTileSize = state.getCoordTileSize(zoom);

        return (location + sectionSize / 2) / cTileSize - (location - sectionSize / 2) / cTileSize + 1;
    }

    @Override
    public IMapState getState() {
        return state;
    }

    @Override
    public long getTileID(final int row, final int column, final int zoom) {
        return isValid(row, column, zoom) ? ((((long) zoom << COORD_BITS) | row) << COORD_BITS) | column : -1;
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

    @Override
    public int getVisibleRows(final int zoom) {
        return getGridSize(state.getCoordSectionHeight(zoom), (int) state.getCoordY(), zoom);
    }

    @Override
    public int getVisibleColumns(final int zoom) {
        return getGridSize(state.getCoordSectionWidth(zoom), (int) state.getCoordX(), zoom);
    }

    @Override
    public int getRow(final int zoom) {
        return (int) (state.getCoordY() - state.getCoordSectionHeight(zoom) / 2) / state.getCoordTileSize(zoom);
    }

    @Override
    public int getColumn(final int zoom) {
        return (int) (state.getCoordX() - state.getCoordSectionWidth(zoom) / 2) / state.getCoordTileSize(zoom);
    }

    private boolean isValid(final int row, final int column, final int zoom) {
        // return row > 0 && row < MAX_COORD && ...
        return ((MAX_COORD - row) | (MAX_COORD - column) | (MAX_ZOOM - zoom) | row | column | zoom) >= 0;
    }

    private class Target {

        public float offset;
        public String address;
        public int x;
        public int y;
        public int street;

        public AddressPoint toAddressPoint() {
            return new AddressPoint(address, x, y, street, offset, state.getConverter());
        }
    }
}