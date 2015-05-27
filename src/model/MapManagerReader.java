package model;

import java.awt.Dimension;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import model.elements.Area;
import model.elements.Building;
import model.elements.Node;
import model.elements.POI;
import model.elements.Street;
import model.elements.StreetNode;
import model.elements.Way;
import model.map.IMapManager;
import model.map.IPixelConverter;
import model.map.ITile;
import model.map.MapManager;
import model.map.PixelConverter;
import model.map.Tile;

public class MapManagerReader {

    private Node[] nodes;
    private POI[] pois;
    private Street[] streets;
    private Way[] ways;
    private StreetNode[] streetNodes;
    private Building[] buildings;
    private Area[] areas;
    private ITile[][][] tiles;
    private String[] names;
    private String[] numbers;

    private DataInputStream reader;

    private int zoomSteps;
    private int rows;
    private int columns;

    private IPixelConverter converter;
    private Dimension tileSize;

    public IMapManager readMapManager(final DataInputStream stream) throws IOException {
        reader = stream;
        readHeader();
        readElements();
        readTiles();

        return new MapManager(tiles, tileSize, converter);
    }

    public Street[] getStreets() {
        return streets;
    }

    private void readHeader() throws IOException {
        nodes = new Node[reader.readInt()];
        pois = new POI[reader.readInt()];
        names = new String[reader.readInt()];
        numbers = new String[reader.readInt()];
        streets = new Street[reader.readInt()];
        ways = new Way[reader.readInt()];
        streetNodes = new StreetNode[reader.readInt()];
        buildings = new Building[reader.readInt()];
        areas = new Area[reader.readInt()];

        zoomSteps = reader.readInt();
        rows = reader.readInt();
        columns = reader.readInt();
        tiles = new ITile[zoomSteps][][];
        converter = new PixelConverter(reader.readDouble());
        tileSize = new Dimension(reader.readInt(), reader.readInt());
    }

    private void readElements() throws IOException {
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new Node(reader.readInt(), reader.readInt());
        }

        for (int i = 0; i < pois.length; i++) {
            pois[i] = new POI(reader.readInt(), reader.readInt(), reader.read());
        }

        for (int i = 0; i < names.length; i++) {
            names[i] = reader.readUTF();
        }
        names[0] = "Unbekannte Straße";

        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = reader.readUTF();
        }

        for (int i = 0; i < streets.length; i++) {
            streets[i] = new Street(readNodeList(), reader.readInt(), names[reader.readInt()], reader.readLong());
        }

        for (int i = 0; i < ways.length; i++) {
            ways[i] = new Way(readNodeList(), reader.readInt(), names[reader.readInt()]);
        }

        for (int i = 0; i < streetNodes.length; i++) {
            streetNodes[i] = new StreetNode(reader.readFloat(), streets[reader.readInt()]);
        }

        for (int i = 0; i < buildings.length; i++) {
            final List<Node> nodes = readNodeList();

            final int index = reader.readInt();
            final String address = index == 0 ? names[index] : names[index].concat(numbers[reader.readInt()]);

            final int streetNodeIndex = reader.readInt();
            buildings[i] = new Building(nodes, address, streetNodeIndex == -1 ? null : streetNodes[streetNodeIndex]);
        }

        for (int i = 0; i < areas.length; i++) {
            areas[i] = new Area(readNodeList(), reader.readInt());
        }
    }

    private List<Node> readNodeList() throws IOException {
        final int nodes = reader.readShort();
        final List<Node> ret = new ArrayList<Node>(nodes);

        for (int j = 0; j < nodes; j++) {
            ret.add(this.nodes[reader.readInt()]);
        }

        return ret;
    }

    private void readTiles() throws IOException {
        int currentRows = rows;
        int currentCols = columns;
        for (int zoom = zoomSteps - 1; zoom >= 0; zoom--) {
            tiles[zoom] = new ITile[currentRows][currentCols];

            final int tileCoordWidth = converter.getCoordDistance(tileSize.width, zoom);
            final int tileCoordHeight = converter.getCoordDistance(tileSize.height, zoom);

            for (int row = 0; row < currentRows; row++) {
                for (int column = 0; column < currentCols; column++) {
                    final int poiCount = reader.readInt();
                    final int streetCount = reader.readInt();
                    final int wayCount = reader.readInt();
                    final int buildingCount = reader.readInt();
                    final int areaCount = reader.readInt();

                    final Collection<POI> tilePOIs = new ArrayList<POI>(poiCount);
                    final Collection<Street> tileStreets = new ArrayList<Street>(streetCount);
                    final Collection<Way> tileWays = new ArrayList<Way>(wayCount);
                    final Collection<Building> tileBuildings = new ArrayList<Building>(buildingCount);
                    final Collection<Area> tileAreas = new ArrayList<Area>(areaCount);

                    for (int i = 0; i < poiCount; i++) {
                        tilePOIs.add(pois[reader.readInt()]);
                    }

                    for (int i = 0; i < streetCount; i++) {
                        tileStreets.add(streets[reader.readInt()]);
                    }

                    for (int i = 0; i < wayCount; i++) {
                        tileWays.add(ways[reader.readInt()]);
                    }

                    for (int i = 0; i < buildingCount; i++) {
                        tileBuildings.add(buildings[reader.readInt()]);
                    }

                    for (int i = 0; i < areaCount; i++) {
                        tileAreas.add(areas[reader.readInt()]);
                    }

                    tiles[zoom][row][column] = new Tile(zoom, row, column, tileCoordWidth * column, tileCoordHeight
                            * row, tileWays, tileStreets, tileAreas, tileBuildings, tilePOIs);
                }
            }

            currentRows = (currentRows + 1) / 2;
            currentCols = (currentCols + 1) / 2;
        }
    }
}
