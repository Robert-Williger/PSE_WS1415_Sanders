package model.map;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import model.elements.Building;
import model.elements.Node;
import model.elements.StreetNode;

public class MapManager implements IMapManager {

    private final IPixelConverter converter;
    private final IMapState state;
    private final ITile[][][] tiles;
    private final Dimension tileSize;
    private final ITile emptyTile;

    public MapManager() {
        this(new ITile[1][1][0], new Dimension(256, 256), new PixelConverter(1));
    }

    public MapManager(final ITile[][][] tiles, final Dimension tileSize, final IPixelConverter converter) {
        this.converter = converter;
        this.tileSize = tileSize;
        this.tiles = tiles;
        emptyTile = new Tile();

        final int maxZoomStep = tiles.length - 1;
        final ITile[][] lowestZoomedLevel = tiles[maxZoomStep];
        final int width = converter.getCoordDistance(tileSize.width, maxZoomStep) * lowestZoomedLevel[0].length;
        final int height = converter.getCoordDistance(tileSize.height, maxZoomStep) * lowestZoomedLevel.length;

        state = new MapState(new Dimension(width, height), maxZoomStep);
    }

    @Override
    public ITile getTile(final long id) {
        final int zoomStep = (int) (id >> 58);
        final int row = (int) ((id >> 29) & 0x1FFFFFFFL);
        final int column = (int) (id & 0x1FFFFFFFL);

        return getTile(row, column, zoomStep);
    }

    @Override
    public ITile getTile(final Point coordinate, final int zoomStep) {
        final int row = converter.getPixelDistance(coordinate.y, zoomStep) / tileSize.height;
        final int column = converter.getPixelDistance(coordinate.x, zoomStep) / tileSize.width;

        return getTile(row, column, zoomStep);
    }

    @Override
    public ITile getTile(final int row, final int column, final int zoomStep) {
        if (zoomStep >= 0 && zoomStep < tiles.length) {
            if (row >= 0 && row < tiles[zoomStep].length) {
                if (column >= 0 && column < tiles[zoomStep][row].length) {
                    return tiles[zoomStep][row][column];
                }
            }
        }

        return emptyTile;
    }

    @Override
    public Dimension getTileSize() {
        return tileSize;
    }

    @Override
    public AddressNode getAddressNode(final Point coordinate) {
        final AddressNode ret;

        final ITile zoomedTile = getTile(coordinate, state.getMaxZoomStep());

        final Building building = zoomedTile.getBuilding(coordinate);

        if (building != null && building.getStreetNode() != null) {
            ret = new AddressNode(building.getAddress(), building.getStreetNode());
        } else {
            final ITile tile = getTile(coordinate, state.getZoomStep());
            ret = locateAddressNode(coordinate, tile);
        }

        return ret;
    }

    private AddressNode locateAddressNode(final Point coordinate, final ITile midTile) {
        AddressNode ret;

        final int row = midTile.getRow();
        final int column = midTile.getColumn();
        final int zoomStep = midTile.getZoomStep();
        final int tileWidth = converter.getCoordDistance(tileSize.width, zoomStep);
        final int tileHeight = converter.getCoordDistance(tileSize.height, zoomStep);
        final int tileX = midTile.getColumn() * tileWidth;
        final int tileY = midTile.getRow() * tileHeight;
        final int midX = tileX + tileWidth / 2;
        final int midY = tileY + tileHeight / 2;

        StreetNode streetNode = null;
        int nodeDistance = Integer.MAX_VALUE;

        final int neighbourColumn = coordinate.x - midX < 0 ? -1 : 1;
        final int neighbourRow = coordinate.y - midY < 0 ? -1 : 1;
        final int hDistance = Math.abs((neighbourColumn + 1) / 2 * tileWidth + tileX - coordinate.x);
        final int vDistance = Math.abs((neighbourRow + 1) / 2 * tileHeight + tileY - coordinate.y);
        final int dDistance = (int) Math.sqrt(hDistance * hDistance + vDistance * vDistance);
        final int[] distances = {0, hDistance, vDistance, dDistance};
        final ITile[] tiles = new ITile[4];
        tiles[0] = midTile;
        tiles[1] = getTile(row + neighbourRow, column, zoomStep);
        tiles[2] = getTile(row, column + neighbourColumn, zoomStep);
        tiles[3] = getTile(row + neighbourRow, column + neighbourColumn, zoomStep);

        for (int i = 0; i < 4; i++) {
            final int tileDistance = distances[i];
            if (tileDistance < nodeDistance) {
                final StreetNode node = tiles[i].getStreetNode(coordinate);
                if (node != null) {
                    final int distance = (int) node.getLocation().distance(coordinate);
                    if (distance < nodeDistance) {
                        nodeDistance = distance;
                        streetNode = node;
                    }
                }
            }
        }

        if (streetNode != null) {
            Building building = null;
            int buildingDistance = Integer.MAX_VALUE;

            for (int i = 0; i < 4; i++) {
                final int tileDistance = distances[i];
                if (tileDistance < buildingDistance) {
                    final ITile tile = tiles[i];
                    final DistancedBuilding dBuilding = locateBuilding(streetNode, tile, buildingDistance);
                    if (dBuilding != null) {
                        buildingDistance = dBuilding.distance;
                        building = dBuilding.building;
                    }
                }
            }

            if (building != null) {
                ret = new AddressNode(building.getAddress(), streetNode);
            } else {
                ret = new AddressNode(streetNode.getStreet().getName(), streetNode);
            }
        } else {
            ret = null;
        }

        return ret;
    }

    private DistancedBuilding locateBuilding(final StreetNode streetNode, final ITile tile, int minDistance) {
        DistancedBuilding ret = null;

        final String streetName = streetNode.getStreet().getName();
        final Point nodeLocation = streetNode.getLocation();

        for (final Building building : tile.getBuildings()) {
            final StreetNode buildingNode = building.getStreetNode();
            if (buildingNode != null && buildingNode.getStreet().getName().equals(streetName)) {
                for (final Node node : building.getNodes()) {
                    final int distance = (int) nodeLocation.distance(node.getLocation());
                    if (distance < minDistance) {
                        ret = new DistancedBuilding(building, distance);
                        minDistance = distance;
                    }
                }
            }
        }

        return ret;
    }

    @Override
    public int getRows() {
        final Rectangle bounds = state.getBounds();
        final int zoom = state.getZoomStep();
        final double zoomFactor = 1.0 / (1 << zoom);
        final int coordHeight = (int) (bounds.height * zoomFactor);
        return (bounds.y + coordHeight) / converter.getCoordDistance(tileSize.height, zoom) - bounds.y
                / converter.getCoordDistance(tileSize.height, zoom) + 1;
    }

    @Override
    public int getColumns() {
        final Rectangle bounds = state.getBounds();
        final int zoom = state.getZoomStep();
        final double zoomFactor = 1.0 / (1 << zoom);
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

    private class DistancedBuilding {
        private final Building building;
        private final int distance;

        public DistancedBuilding(final Building building, final int distance) {
            this.building = building;
            this.distance = distance;
        }
    }

}