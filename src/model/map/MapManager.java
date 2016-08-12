package model.map;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;

import model.IFactory;
import model.elements.dereferencers.IAccessPointDereferencer;
import model.elements.dereferencers.IBuildingDereferencer;
import model.elements.dereferencers.IStreetDereferencer;
import model.elements.dereferencers.ITileDereferencer;

public class MapManager implements IMapManager {

    private final IPixelConverter converter;
    private final IMapState state;
    private final Dimension tileSize;
    private final ITileDereferencer tile;
    private final IFactory<ITileDereferencer> factory;

    public MapManager(final IFactory<ITileDereferencer> factory, final IPixelConverter converter,
            final IMapState state, final Dimension tileSize) {
        this.factory = factory;
        this.converter = converter;
        this.tileSize = tileSize;
        this.state = state;

        this.tile = createTileDereferencer();
    }

    @Override
    public long getTileID(final Point coordinate, final int zoomStep) {
        final int row = converter.getPixelDistance(coordinate.y, zoomStep) / tileSize.height;
        final int column = converter.getPixelDistance(coordinate.x, zoomStep) / tileSize.width;

        return getTileID(row, column, zoomStep);
    }

    @Override
    public long getTileID(final int row, final int column, final int zoomStep) {
        return ((((long) zoomStep << 29) | row) << 29) | column;
    }

    @Override
    public Dimension getTileSize() {
        return tileSize;
    }

    @Override
    public AddressNode getAddressNode(final Point coordinate, int zoomStep) {
        tile.setID(getTileID(coordinate, state.getMaxZoomStep()));

        final IBuildingDereferencer building = getBuilding(coordinate);

        if (building != null && building.hasAccessPoint()) {
            final IAccessPointDereferencer accessPoint = building.getAccessPointDereferencer();
            accessPoint.setID(building.getAccessPoint());
            return new AddressNode(building.getAddress(), accessPoint.getOffset(), accessPoint.getStreet());
        } else {
            // TODO find better abort criterion .. + define consistent max
            // distance for search
            while (zoomStep >= state.getZoomStep()) {
                tile.setID(getTileID(coordinate, zoomStep));
                AddressNode node = locateAddressNode(coordinate);
                if (node != null) {
                    return node;
                }
                --zoomStep;
            }
            return null;
        }
    }

    @Override
    public AddressNode getAddressNode(final Point coordinate) {
        return getAddressNode(coordinate, state.getZoomStep());
    }

    private AddressNode locateAddressNode(final Point coordinate) {
        AddressNode ret;

        final int row = tile.getRow();
        final int column = tile.getColumn();
        final int zoomStep = tile.getZoomStep();
        final int tileWidth = converter.getCoordDistance(tileSize.width, zoomStep);
        final int tileHeight = converter.getCoordDistance(tileSize.height, zoomStep);
        final int tileX = tile.getX();
        final int tileY = tile.getY();
        final int midX = tileX + tileWidth / 2;
        final int midY = tileY + tileHeight / 2;

        LocalizedAccessPoint localizedAccessPoint = null;
        int nodeDistance = Integer.MAX_VALUE;

        final int neighbourColumn = coordinate.x - midX < 0 ? -1 : 1;
        final int neighbourRow = coordinate.y - midY < 0 ? -1 : 1;
        final int hDistance = Math.abs((neighbourColumn + 1) / 2 * tileWidth + tileX - coordinate.x);
        final int vDistance = Math.abs((neighbourRow + 1) / 2 * tileHeight + tileY - coordinate.y);
        final int dDistance = (int) Math.sqrt(hDistance * hDistance + vDistance * vDistance);

        final int[] distances = {0, hDistance, vDistance, dDistance};
        final int[] xOffsets = {0, neighbourRow, 0, neighbourRow};
        final int[] yOffsets = {0, 0, neighbourColumn, neighbourColumn};

        for (int i = 0; i < 4; i++) {
            tile.setID(getTileID(row + xOffsets[i], column + yOffsets[i], zoomStep));
            final int tileDistance = distances[i];
            if (tileDistance < nodeDistance) {
                final LocalizedAccessPoint node = getAccessPoint(coordinate);
                if (node != null) {
                    final int distance = (int) Point.distance(node.x, node.y, coordinate.getX(), coordinate.getY());
                    if (distance < nodeDistance) {
                        nodeDistance = distance;
                        localizedAccessPoint = node;
                    }
                }
            }
        }

        if (localizedAccessPoint != null) {
            DistancedBuilding distanceBuilding = null;

            for (int i = 0; i < 4; i++) {
                final int tileDistance = distances[i];
                if (tileDistance < distanceBuilding.distance) {
                    tile.setID(getTileID(row + xOffsets[i], column + yOffsets[i], zoomStep));
                    final DistancedBuilding distance = locateBuilding(localizedAccessPoint, distanceBuilding.distance);
                    if (distance != null) {
                        distanceBuilding = distance;
                    }
                }
            }

            final IBuildingDereferencer building = tile.getBuildingDereferencer();
            final String address;
            if (distanceBuilding != null) {
                building.setID(distanceBuilding.building);
                address = building.getAddress();
            } else {
                building.setID(localizedAccessPoint.street);
                address = building.getStreet();
            }
            return new AddressNode(address, localizedAccessPoint.offset, localizedAccessPoint.street);
        } else {
            ret = null;
        }

        return ret;
    }

    private DistancedBuilding locateBuilding(final LocalizedAccessPoint point, int minDistance) {
        int id = -1;

        final IBuildingDereferencer building = tile.getBuildingDereferencer();
        final IAccessPointDereferencer accessPoint = building.getAccessPointDereferencer();
        for (final Iterator<Integer> buildingIt = tile.getBuildings(); buildingIt.hasNext();) {
            final int buildingID = buildingIt.next();
            building.setID(buildingID);
            if (building.hasAccessPoint()) {
                accessPoint.setID(building.getAccessPoint());
                // TODO check behaviour... maybe change to street name
                // comparision
                if (accessPoint.getStreet() == point.street) {
                    for (int i = 0; i < building.size(); i++) {
                        final int distance = (int) Point.distance(point.x, point.y, building.getX(i), building.getY(i));
                        if (distance < minDistance) {
                            id = buildingID;
                            minDistance = distance;
                        }
                    }
                }
            }
        }

        if (id != -1) {
            return new DistancedBuilding(minDistance, id);
        }
        return null;
    }

    @Override
    public int getRows() {
        final Rectangle bounds = state.getBounds();
        final int zoom = state.getZoomStep();
        final double zoomFactor = 1.0 / (1 << zoom - state.getMinZoomStep());
        final int coordHeight = (int) (bounds.height * zoomFactor);

        return (bounds.y + coordHeight) / converter.getCoordDistance(tileSize.height, zoom) - bounds.y
                / converter.getCoordDistance(tileSize.height, zoom) + 1;
    }

    @Override
    public int getColumns() {
        final Rectangle bounds = state.getBounds();
        final int zoom = state.getZoomStep();
        final double zoomFactor = 1.0 / (1 << zoom - state.getMinZoomStep());
        final int coordWidth = (int) (bounds.width * zoomFactor);

        return (bounds.x + coordWidth) / converter.getCoordDistance(tileSize.width, zoom) - bounds.x
                / converter.getCoordDistance(tileSize.width, zoom) + 1;
    }

    @Override
    public Point getCurrentGridLocation() {
        final Point location = state.getLocation();
        final int zoomStep = state.getZoomStep();
        final int column = location.x / converter.getCoordDistance(tileSize.width, zoomStep);
        final int row = location.y / converter.getCoordDistance(tileSize.height, zoomStep);
        return new Point(column, row);
    }

    @Override
    public Point getCoord(final Point pixelPoint) {
        final Point location = state.getLocation();
        final int zoomStep = state.getZoomStep();
        final int coordX = converter.getCoordDistance(pixelPoint.x, zoomStep) + location.x;
        final int coordY = converter.getCoordDistance(pixelPoint.y, zoomStep) + location.y;

        return new Point(coordX, coordY);
    }

    @Override
    public Point getPixel(final Point coordinate) {
        final Point location = state.getLocation();
        final int zoomStep = state.getZoomStep();
        final int pixelX = converter.getPixelDistance(coordinate.x - location.x, zoomStep);
        final int pixelY = converter.getPixelDistance(coordinate.y - location.y, zoomStep);

        return new Point(pixelX, pixelY);
    }

    @Override
    public IMapState getMapState() {
        return state;
    }

    @Override
    public IPixelConverter getConverter() {
        return converter;
    }

    /*- gets the closest node on a street to the given coordinate in the (global) tile*/
    private LocalizedAccessPoint getAccessPoint(final Point coordinate) {
        LocalizedAccessPoint ret = null;

        long minDistSq = Long.MAX_VALUE;

        final IStreetDereferencer street = tile.getStreetDereferencer();
        for (final Iterator<Integer> streetIt = tile.getStreets(); streetIt.hasNext();) {
            final int streetID = streetIt.next();
            street.setID(streetID);

            float totalLength = 0;
            final int maxLength = street.getLength();

            int lastX = street.getX(0);
            int lastY = street.getY(0);

            for (int i = 1; i < street.size(); i++) {
                int currentX = street.getX(i);
                int currentY = street.getY(i);

                if (currentX != lastX || currentY != lastY) {
                    final long dx = currentX - lastX;
                    final long dy = currentY - lastY;
                    final long square = (dx * dx + dy * dy);
                    final float length = (float) Math.sqrt(square);
                    double s = ((coordinate.x - lastX) * dx + (coordinate.y - lastY) * dy) / (double) square;

                    if (s < 0) {
                        s = 0;
                    } else if (s > 1) {
                        s = 1;
                    }

                    final double distX = lastX + s * dx - coordinate.x;
                    final double distY = lastY + s * dy - coordinate.y;
                    final long distanceSq = (long) (distX * distX + distY * distY);

                    if (distanceSq < minDistSq) {
                        ret = new LocalizedAccessPoint((float) ((totalLength + s * length) / maxLength), streetID,
                                (int) (lastX + s * dx), (int) (lastY + s * dy));
                        minDistSq = distanceSq;
                    }

                    totalLength += length;
                }
                lastX = currentX;
                lastY = currentY;
            }
        }

        return ret;
    }

    // searches for a building at the given coordinate in the (global) tile
    private IBuildingDereferencer getBuilding(final Point coordinate) {
        final IBuildingDereferencer dereferencer = tile.getBuildingDereferencer();
        for (final Iterator<Integer> iterator = tile.getBuildings(); iterator.hasNext();) {
            dereferencer.setID(iterator.next());
            if (dereferencer.contains(coordinate.x, coordinate.y)) {
                return dereferencer;
            }
        }

        return null;
    }

    @Override
    public ITileDereferencer createTileDereferencer() {
        // TODO implement this
        return factory.create();
    }

    private static class DistancedBuilding {
        private final int distance;
        private final int building;

        public DistancedBuilding(final int distance, final int building) {
            this.building = building;
            this.distance = distance;
        }
    }

    private static class LocalizedAccessPoint {
        private final float offset;
        private final int street;
        private final int x;
        private final int y;

        public LocalizedAccessPoint(final float offset, final int street, final int x, final int y) {
            this.offset = offset;
            this.street = street;
            this.x = x;
            this.y = y;
        }
    }
}