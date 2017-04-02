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

    private final java.util.Map<String, IFactory<IPointAccessor>> pointMap;
    private final java.util.Map<String, IFactory<ICollectiveAccessor>> collectiveMap;
    private final IFactory<ITileAccessor> tileFactory;
    private final String[] strings;

    private final ITileAccessor tileAccessor;
    private final ICollectiveAccessor buildingAccessor;
    private final ICollectiveAccessor streetAccessor;
    private final IStringAccessor stringAccessor;
    private final IMapState state;

    private static IFactory<ITileAccessor> emptyFactory() {
        return () -> {
            return new TileAccessor(new HashMap<String, IQuadtree>(),
                    new MapState(256, 256, 0, 1, 256, new PixelConverter(1)));
        };
    }

    public MapManager() {
        // TODO
        this(new HashMap<String, IFactory<IPointAccessor>>(), new HashMap<String, IFactory<ICollectiveAccessor>>(),
                emptyFactory(), new String[0], new MapState(256, 256, 0, 1, 256, new PixelConverter(1)));
    }

    // TODO
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
    }

    @Override
    public AddressPoint getAddress(final int x, final int y) {
        final AddressPoint node = new AddressPoint(state);

        long building = getBuildingAt(x, y, state.getMaxZoom());
        final LongPredicate pred;
        if (building != -1) {
            buildingAccessor.setID(building);
            final long street = buildingAccessor.getAttribute("street");
            pred = l -> {
                streetAccessor.setID(l);
                return streetAccessor.getAttribute("name") == street;
            };

            node.setAddress(stringAccessor.getString(street) + " "
                    + stringAccessor.getString(buildingAccessor.getAttribute("number")));
        } else {
            pred = l -> true;
        }

        // TODO find better abort criterion .. + define consistent max
        // distance for search

        final long[] ids = new long[4];
        final double[] distanceSquares = new double[4];

        for (int zoom = state.getMaxZoom(); zoom >= state.getZoom(); zoom--) {
            fillArrays(x, y, getRow(y, zoom), getColumn(x, zoom), zoom, distanceSquares, ids);

            if (applyAccessPoint(x, y, ids, distanceSquares, node, pred)) {
                if (building == -1 && !applyBuildingAddress(node, ids, distanceSquares)) {
                    streetAccessor.setID(node.getStreet());
                    node.setAddress(stringAccessor.getString(streetAccessor.getAttribute("name")));
                }
                return node;
            }
            --zoom;
        }
        return null;
    }

    private int getRow(final int yCoord, final int zoom) {
        return yCoord / state.getCoordTileSize(zoom);
    }

    private int getColumn(final int xCoord, final int zoom) {
        return xCoord / state.getCoordTileSize(zoom);
    }

    private void fillArrays(final int x, final int y, final int row, final int column, final int zoom,
            final double[] distanceSq, final long[] ids) {
        final int coordTileSize = state.getCoordTileSize(zoom);
        final int tileX = column * coordTileSize;
        final int tileY = row * coordTileSize;
        final int midX = tileX + coordTileSize / 2;
        final int midY = tileY + coordTileSize / 2;
        final int nColumn = x - midX < 0 ? -1 : 1; // neighbor column
        final int nRow = y - midY < 0 ? -1 : 1; // neighbor row

        calculateIDs(row, column, zoom, nRow, nColumn, ids);
        calculateDistanceSquares(x, y, tileX, tileY, nColumn, nRow, coordTileSize, distanceSq);
    }

    private boolean applyAccessPoint(final int x, final int y, final long[] ids, final double[] distanceSquares,
            final AddressPoint point, final LongPredicate pred) {
        double nodeDistanceSq = Long.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            if (distanceSquares[i] < nodeDistanceSq) {
                nodeDistanceSq = applyAccessPoint(x, y, nodeDistanceSq, point, ids[i], pred);
            }
        }

        return nodeDistanceSq != Long.MAX_VALUE;
    }

    private boolean applyBuildingAddress(final AddressPoint node, final long[] ids, final double[] distanceSquares) {
        double buildingDistanceSq = Long.MAX_VALUE;

        for (int i = 0; i < 4; i++) {
            if (distanceSquares[i] < buildingDistanceSq) {
                buildingDistanceSq = applyBuildingAddress(node, buildingDistanceSq, ids[i]);
            }
        }
        return buildingDistanceSq != Long.MAX_VALUE;
    }

    private void calculateIDs(int row, int column, final int zoom, int nRow, int nColumn, long[] ids) {
        for (int i = 0; i < 4; i++) {
            ids[i] = getID(row + (i % 2) * nRow, column + (i / 2) * nColumn, zoom);
        }
    }

    private void calculateDistanceSquares(final int x, final int y, final int tileX, final int tileY,
            final int neighbourColumn, final int neighbourRow, final int coordTileSize,
            final double[] distanceSquares) {
        distanceSquares[0] = 0;

        distanceSquares[1] = Math.abs((neighbourColumn + 1) / 2 * coordTileSize + tileX - x);
        distanceSquares[1] *= distanceSquares[0];

        distanceSquares[2] = Math.abs((neighbourRow + 1) / 2 * coordTileSize + tileY - y);
        distanceSquares[2] *= distanceSquares[0];

        distanceSquares[3] = distanceSquares[1] + distanceSquares[2];
    }

    private double applyAccessPoint(final int x, final int y, double minDistanceSq, final AddressPoint point,
            final long id, final LongPredicate pred) {
        tileAccessor.setID(id);
        for (OfLong iterator = tileAccessor.getElements("street"); iterator.hasNext();) {
            long street = iterator.nextLong();

            if (pred.test(street)) {
                streetAccessor.setID(street);

                float totalLength = 0;
                final int maxLength = streetAccessor.getAttribute("length");

                int lastX = streetAccessor.getX(0);
                int lastY = streetAccessor.getY(0);

                for (int i = 1; i < streetAccessor.size(); i++) {
                    int currentX = streetAccessor.getX(i);
                    int currentY = streetAccessor.getY(i);

                    if (currentX != lastX || currentY != lastY) {
                        final long dx = currentX - lastX;
                        final long dy = currentY - lastY;
                        final long square = (dx * dx + dy * dy);
                        final float length = (float) Math.sqrt(square);
                        final double s = Math.min(1,
                                Math.max(0, ((x - lastX) * dx + (y - lastY) * dy) / (double) square));

                        final double distX = lastX + s * dx - x;
                        final double distY = lastY + s * dy - y;
                        final double distanceSq = (distX * distX + distY * distY);

                        if (distanceSq < minDistanceSq) {
                            // TODO long or int for street id?
                            point.setOffset((float) ((totalLength + s * length) / maxLength));
                            point.setStreet((int) street);
                            point.setLocation((int) (lastX + s * dx), (int) (lastY + s * dy));
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

    private double applyBuildingAddress(final AddressPoint node, double minDistanceSq, final long id) {
        final int x = node.getX();
        final int y = node.getY();

        tileAccessor.setID(id);

        streetAccessor.setID(node.getStreet());
        final int name = streetAccessor.getAttribute("name");
        final String street = stringAccessor.getString(name);

        // TODO use lambda expression (tileAccessor.forEach).
        final PrimitiveIterator.OfLong buildings = tileAccessor.getElements("building");
        while (buildings.hasNext()) {
            final long buildingID = buildings.nextLong();
            buildingAccessor.setID(buildingID);
            if (name == buildingAccessor.getAttribute("name")) {
                final int size = buildingAccessor.size();
                for (int i = 0; i < size; i++) {
                    final int distance = (int) Point.distance(x, y, buildingAccessor.getX(i), buildingAccessor.getY(i));
                    if (distance < minDistanceSq) {
                        minDistanceSq = distance;
                        node.setAddress(
                                street + " " + stringAccessor.getString(buildingAccessor.getAttribute("number")));
                    }
                }
            }
        }

        return minDistanceSq;
    }

    private long getBuildingAt(final int x, final int y, final int zoom) {
        final int row = getRow(y, zoom);
        final int column = getColumn(x, zoom);

        tileAccessor.setRCZ(row, column, zoom);
        final PrimitiveIterator.OfLong iterator = tileAccessor.getElements("building");
        while (iterator.hasNext()) {
            final long building = iterator.nextLong();
            buildingAccessor.setID(building);
            if (CollectiveUtil.contains(buildingAccessor, x, y)) {
                return building;
            }
        }

        return -1;
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
    public long getID(final int row, final int column, final int zoom) {
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
        return getGridSize(state.getCoordSectionHeight(zoom), (int) state.getY(), zoom);
    }

    @Override
    public int getVisibleColumns(final int zoom) {
        return getGridSize(state.getCoordSectionWidth(zoom), (int) state.getX(), zoom);
    }

    @Override
    public int getRow(final int zoom) {
        return (int) (state.getY() - state.getCoordSectionHeight(zoom) / 2) / state.getCoordTileSize(zoom);
    }

    @Override
    public int getColumn(final int zoom) {
        return (int) (state.getX() - state.getCoordSectionWidth(zoom) / 2) / state.getCoordTileSize(zoom);
    }

    private boolean isValid(final int row, final int column, final int zoom) {
        // return row > 0 && row < MAX_COORD && ...
        return ((MAX_COORD - row) | (MAX_COORD - column) | (MAX_ZOOM - zoom) | row | column | zoom) >= 0;
    }
}