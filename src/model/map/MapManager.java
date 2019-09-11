package model.map;

import java.awt.Point;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PrimitiveIterator;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

import model.IFactory;
import model.map.accessors.CollectiveUtil;
import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.IPointAccessor;
import model.map.accessors.IStringAccessor;
import model.map.accessors.ITileConversion;
import model.map.accessors.StringAccessor;
import model.map.accessors.TileConversion;
import model.targets.AddressPoint;

public class MapManager implements IMapManager {
    private static final int MAX_STREET_BUILDING_PIXEL_DISTANCE = 75;
    // TODO improve this
    private static final String UNKNOWN_STREET = "Unbekannte Straﬂe";

    private final java.util.Map<String, IFactory<IPointAccessor>> pointMap;
    private final java.util.Map<String, IFactory<ICollectiveAccessor>> collectiveMap;
    private final String[] strings;

    private final ITileConversion tileConversion;
    private final IElementIterator streetIterator;
    private final IElementIterator buildingIterator;

    private final ICollectiveAccessor buildingAccessor;
    private final ICollectiveAccessor streetAccessor;
    private final IStringAccessor stringAccessor;
    private final IMapState state;

    private final java.util.Map<String, IElementIterator> map;

    // TODO improve this
    private final long streetBuildingCoordDistanceSq;

    private static ITileConversion defaultConversion = new TileConversion(
            new MapState(256, 256, 0, 1, 256, new PixelConverter(1)));

    public MapManager() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>(), defaultConversion, new String[0],
                new MapState(256, 256, 0, 1, 256, new PixelConverter(1)));
    }

    public MapManager(final java.util.Map<String, IFactory<IPointAccessor>> pointMap,
            final java.util.Map<String, IFactory<ICollectiveAccessor>> collectiveMap,
            java.util.Map<String, IElementIterator> map, ITileConversion conversion, final String[] strings,
            final IMapState state) {
        this.state = state;

        this.pointMap = pointMap;
        this.collectiveMap = collectiveMap;
        this.map = map;
        this.tileConversion = conversion;
        this.strings = strings;

        this.streetIterator = getElementIterator("street");
        this.buildingIterator = getElementIterator("building");
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

        final int building = getBuildingAt(x, y);

        final long[] tileIds = new long[4];
        final double[] tileDistanceSquares = new double[4];

        if (building != -1) {
            buildingAccessor.setId(building);
            final int street = buildingAccessor.getAttribute("street");
            final IntPredicate pred = l -> {
                streetAccessor.setId(l);
                final int oStreet = streetAccessor.getAttribute("name");
                return street == -1 || oStreet == -1 || oStreet == street;
            };

            if (applyLocation(x, y, tileIds, tileDistanceSquares, pred, target)
                    || applyLocation(x, y, tileIds, tileDistanceSquares, l -> true, target)) {
                if (street == -1) {
                    streetAccessor.setId(target.street);
                    target.address = getStreetName(streetAccessor.getAttribute("name"));
                } else {
                    target.address = getBuildingAddress(street);
                }

                return target.toAddressPoint();
            }
        } else {
            if (applyLocation(x, y, tileIds, tileDistanceSquares, l -> true, target)) {
                if (!applyAddressByBuilding(target, tileIds, tileDistanceSquares)) {
                    streetAccessor.setId(target.street);
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
            final IntPredicate pred, final Target node) {
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
            final IntPredicate pred) {
        for (OfInt iterator = streetIterator.iterator(tileConversion.getRow(id), tileConversion.getColumn(id),
                tileConversion.getZoom(id)); iterator.hasNext();) {
            int street = iterator.nextInt();

            if (pred.test(street)) {
                streetAccessor.setId(street);

                float totalLength = 0;
                final int size = streetAccessor.size();
                final int maxLength = CollectiveUtil.getLength(streetAccessor);

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

        streetAccessor.setId(target.street);
        final int name = streetAccessor.getAttribute("name");
        final String street = getStreetName(name);

        // TODO use lambda expression (tileAccessor.forEach).
        final PrimitiveIterator.OfInt buildings = buildingIterator.iterator(tileConversion.getRow(id),
                tileConversion.getColumn(id), tileConversion.getZoom(id));
        while (buildings.hasNext()) {
            final int buildingID = buildings.nextInt();
            buildingAccessor.setId(buildingID);
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

    private int getBuildingAt(final int x, final int y) {
        final int zoom = state.getMaxZoom();
        final int row = getRow(y, zoom);
        final int column = getColumn(x, zoom);

        final PrimitiveIterator.OfInt iterator = buildingIterator.iterator(row, column, zoom);
        while (iterator.hasNext()) {
            final int building = iterator.nextInt();
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
            tileIDs[i] = tileConversion.getId(row + (i % 2) * nRow, column + (i / 2) * nColumn, zoom);
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
    public ITileConversion getTileConversion() {
        return tileConversion;
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
    public int getFirstRow(final int zoom) {
        return (int) (state.getCoordY() - state.getCoordSectionHeight(zoom) / 2) / state.getCoordTileSize(zoom);
    }

    @Override
    public int getFirstColumn(final int zoom) {
        return (int) (state.getCoordX() - state.getCoordSectionWidth(zoom) / 2) / state.getCoordTileSize(zoom);
    }

    @Override
    public IElementIterator getElementIterator(String identifier) {
        return map.getOrDefault(identifier, new EmptyElementIterator());
    }

    private static class EmptyElementIterator implements IElementIterator {

        @Override
        public OfInt iterator(int row, int column, int zoom) {
            return Arrays.stream(new int[0]).iterator();
        }

        @Override
        public void forEach(int row, int column, int zoom, IntConsumer consumer) {}

        @Override
        public IElementIterator filter(Predicate f) {
            return this;
        }

        @Override
        public IElementIterator sort(Comparator<Integer> idComparator) {
            return this;
        }

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