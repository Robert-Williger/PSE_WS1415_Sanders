package model.map;

import java.awt.Point;
import java.util.PrimitiveIterator;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.IntPredicate;

import model.map.accessors.CollectiveUtil;
import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.IStringAccessor;
import model.map.accessors.ITileIdConversion;
import model.map.accessors.TileConversion;
import model.targets.AddressPoint;

public class AddressFinder implements IAddressFinder {
    private static final int MAX_STREET_BUILDING_PIXEL_DISTANCE = 75;
    // TODO improve this
    private static final String UNKNOWN_STREET = "Unbekannte Straße";

    private final ITileIdConversion tileConversion;
    private final IElementIterator streetIterator;
    private final IElementIterator buildingIterator;

    private final ICollectiveAccessor buildingAccessor;
    private final ICollectiveAccessor streetAccessor;
    private final IStringAccessor stringAccessor;

    private final IMapBounds mapBounds;
    private final IPixelMapping pixelMapping;
    private final ITileState tileState;

    private final long streetBuildingCoordDistanceSq;

    public AddressFinder(IElementIterator streetIterator, IElementIterator buildingIterator,
            ICollectiveAccessor buildingAccessor, ICollectiveAccessor streetAccessor, IStringAccessor stringAccessor,
            IMapBounds mapBounds, IMapSection mapSection, IPixelMapping pixelMapping, ITileState tileState) {
        super();
        this.tileConversion = new TileConversion();
        this.streetIterator = streetIterator;
        this.buildingIterator = buildingIterator;
        this.buildingAccessor = buildingAccessor;
        this.streetAccessor = streetAccessor;
        this.stringAccessor = stringAccessor;
        this.mapBounds = mapBounds;
        this.pixelMapping = pixelMapping;
        this.tileState = tileState;

        final int coordDistance = pixelMapping.getCoordDistance(MAX_STREET_BUILDING_PIXEL_DISTANCE,
                mapBounds.getMaxZoom());
        streetBuildingCoordDistanceSq = coordDistance * coordDistance;
    }

    @Override
    public AddressPoint getAddress(final int x, final int y, final int zoom) {
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

            if (applyLocation(x, y, zoom, tileIds, tileDistanceSquares, pred, target)
                    || applyLocation(x, y, zoom, tileIds, tileDistanceSquares, l -> true, target)) {
                if (street == -1) {
                    streetAccessor.setId(target.street);
                    target.address = getStreetName(streetAccessor.getAttribute("name"));
                } else {
                    target.address = getBuildingAddress(street);
                }

                return target.toAddressPoint();
            }
        } else {
            if (applyLocation(x, y, zoom, tileIds, tileDistanceSquares, l -> true, target)) {
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

    private boolean applyLocation(final int x, final int y, final int zoom, final long[] tileIds,
            final double[] tileDistanceSquares, final IntPredicate pred, final Target node) {
        double minDistSq = Long.MAX_VALUE;

        for (int z = mapBounds.getMaxZoom(); z >= zoom; z--) {
            fillArrays(x, y, z, tileDistanceSquares, tileIds);

            for (int i = 0; i < 4; i++) {
                if (tileDistanceSquares[i] < minDistSq) {
                    minDistSq = applyLocation(x, y, minDistSq, node, tileIds[i], pred);
                }
            }

            if (minDistSq != Long.MAX_VALUE) {
                return true;
            }
            --z;
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
        final int zoom = mapBounds.getMaxZoom();
        final int coordTileSize = pixelMapping.getCoordDistance(tileState.getTileSize(), zoom);
        final int row = y / coordTileSize;
        final int column = x / coordTileSize;

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
        final int coordTileSize = pixelMapping.getCoordDistance(tileState.getTileSize(), zoom);
        final int row = y / coordTileSize;
        final int column = x / coordTileSize;
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

    private class Target {

        public float offset;
        public String address;
        public int x;
        public int y;
        public int street;

        public AddressPoint toAddressPoint() {
            return new AddressPoint(address, x, y, street, offset, pixelMapping);
        }
    }
}
