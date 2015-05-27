package model;

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
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
import model.routing.Graph;
import model.routing.IGraph;
import model.routing.IProgressListener;
import model.routing.IRouteManager;
import model.routing.RouteManager;

public class Reader implements IReader {

    private final List<IProgressListener> list;

    private IGraph graph;
    private DataInputStream reader;
    private IMapManager manager;
    private IRouteManager rm;
    private ITextProcessor tp;
    private boolean canceled;

    private static final Collection<POI> EMPTY_POIS;
    private static final Collection<Street> EMPTY_STREETS;
    private static final Collection<Way> EMPTY_WAYS;
    private static final Collection<Building> EMPTY_BUILDINGS;
    private static final Collection<Area> EMPTY_AREAS;

    public Reader() {
        list = new LinkedList<IProgressListener>();
    }

    @Override
    public boolean read(final File file) {
        canceled = false;
        MapManagerReader managerReader = new MapManagerReader();

        try {
            reader = new DataInputStream(new BufferedInputStream(new ProgressableInputStream(file)));
            graph = readGraph();
            manager = managerReader.readMapManager();
        } catch (final Exception e) {
            managerReader = null;

            if (!canceled) {
                fireErrorOccured("Beim Kartenimport ist ein Fehler aufgetreten.");
            }

            try {
                reader.close();
            } catch (final IOException e1) {
            }

            return false;
        }

        final HashMap<String, StreetNode> hm = new HashMap<String, StreetNode>();

        for (final Street street : managerReader.getStreets()) {
            if (!street.getName().equals("Unbekannte Straße")) {
                hm.put(street.getName(), new StreetNode(0.5f, street));
            }
        }

        managerReader = null;

        tp = new TextProcessor(hm, 5);

        rm = new RouteManager(graph, manager);

        try {
            reader.close();
        } catch (final IOException e) {
        }

        return true;
    }

    @Override
    public IMapManager getMapManager() {
        return manager;
    }

    @Override
    public IRouteManager getRouteManager() {
        return rm;
    }

    @Override
    public ITextProcessor getTextProcessor() {
        return tp;

    }

    /*
     * Reads the Graph-section of the tsk file and generates the Graph.
     */
    private IGraph readGraph() throws IOException {

        int nodeCount = 0;
        final List<Long> edges = new ArrayList<Long>();
        final List<Integer> weights = new ArrayList<Integer>();

        nodeCount = reader.readInt();
        final int edgeCount = reader.readInt();

        for (int i = 0; i < edgeCount; i++) {
            edges.add(reader.readLong());
        }

        for (int i = 0; i < edgeCount; i++) {
            weights.add(reader.readInt());
        }

        return new Graph(nodeCount, edges, weights);
    }

    private class ProgressableInputStream extends FileInputStream {
        private final int size;
        private int current;
        private int progress;

        public ProgressableInputStream(final File in) throws IOException {
            super(in);

            progress = -1;
            size = (int) Math.ceil(super.available() / 100.0);
        }

        @Override
        public int read() throws IOException {
            final int tmp = ++current / size;
            if (tmp != progress) {
                progress = tmp;
                fireProgressDone(progress);
            }

            return super.read();
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            final int nr = super.read(b, off, len);

            final int tmp = (current += nr) / size;
            if (tmp != progress) {
                progress = tmp;
                fireProgressDone(progress);
            }

            return nr;
        }

        @Override
        public int read(final byte[] b) throws IOException {
            final int nr = super.read(b);

            final int tmp = (current += nr) / size;
            if (tmp != progress) {
                progress = tmp;
                fireProgressDone(progress);
            }

            return nr;
        }
    }

    private class MapManagerReader {
        private Node[] nodes;
        private POI[] pois;
        private Street[] streets;
        private Way[] ways;
        private Building[] buildings;
        private Area[] areas;
        private ITile[][][] tiles;
        private String[] names;
        private String[] numbers;

        private int zoomSteps;
        private int rows;
        private int columns;

        private IPixelConverter converter;
        private Dimension tileSize;

        public IMapManager readMapManager() throws IOException {
            readHeader();
            readElements();
            readTiles();
            cleanUp();

            return new MapManager(tiles, tileSize, converter);
        }

        public Street[] getStreets() {
            return streets;
        }

        private void readHeader() throws IOException {
            zoomSteps = reader.readInt();
            rows = reader.readInt();
            columns = reader.readInt();
            tiles = new ITile[zoomSteps][][];
            converter = new PixelConverter(reader.readDouble());
            tileSize = new Dimension(reader.readInt(), reader.readInt());
        }

        private void readElements() throws IOException {
            nodes = new Node[reader.readInt()];
            for (int i = 0; i < nodes.length; i++) {
                nodes[i] = new Node(reader.readInt(), reader.readInt());
            }

            names = new String[reader.readInt()];
            for (int i = 0; i < names.length; i++) {
                names[i] = reader.readUTF();
            }
            names[0] = "Unbekannte Straße";

            numbers = new String[reader.readInt()];
            for (int i = 0; i < numbers.length; i++) {
                numbers[i] = reader.readUTF();
            }

            streets = new Street[reader.readInt()];
            for (int i = 0; i < streets.length; i++) {
                streets[i] = new Street(readNodeList(), reader.read(), names[reader.readShort()], reader.readLong());
            }

            ways = new Way[reader.readInt()];
            for (int i = 0; i < ways.length; i++) {
                ways[i] = new Way(readNodeList(), reader.read(), names[reader.readShort()]);
            }

            areas = new Area[reader.readInt()];
            for (int i = 0; i < areas.length; i++) {
                areas[i] = new Area(readNodeList(), reader.read());
            }

            pois = new POI[reader.readInt()];
            for (int i = 0; i < pois.length; i++) {
                pois[i] = new POI(reader.readInt(), reader.readInt(), reader.read());
            }

            buildings = new Building[reader.readInt()];
            final int streetNodes = reader.readInt();

            for (int i = 0; i < streetNodes; i++) {
                final List<Node> nodes = readNodeList();
                final Street street = streets[reader.readInt()];

                buildings[i] = new Building(nodes, street.getName() + " " + numbers[reader.readShort()],
                        new StreetNode(reader.readFloat(), street));
            }

            for (int i = streetNodes; i < buildings.length; i++) {
                buildings[i] = new Building(readNodeList(), "", null);
            }
        }

        private List<Node> readNodeList() throws IOException {
            final int nodes = reader.readUnsignedShort();

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

                int y = 0;
                for (int row = 0; row < currentRows; row++) {
                    int x = 0;
                    for (int column = 0; column < currentCols; column++) {
                        final int poiCount = reader.readInt();
                        final int streetCount = reader.readInt();
                        final int wayCount = reader.readInt();
                        final int buildingCount = reader.readInt();
                        final int areaCount = reader.readInt();

                        final Collection<POI> tilePOIs = poiCount == 0 ? EMPTY_POIS : new ArrayList<POI>(poiCount);
                        final Collection<Street> tileStreets = streetCount == 0 ? EMPTY_STREETS
                                : new ArrayList<Street>(streetCount);
                        final Collection<Way> tileWays = wayCount == 0 ? EMPTY_WAYS : new ArrayList<Way>(wayCount);
                        final Collection<Building> tileBuildings = buildingCount == 0 ? EMPTY_BUILDINGS
                                : new ArrayList<Building>(buildingCount);
                        final Collection<Area> tileAreas = areaCount == 0 ? EMPTY_AREAS
                                : new ArrayList<Area>(areaCount);

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

                        tiles[zoom][row][column] = new Tile(zoom, row, column, x, y, tileWays, tileStreets, tileAreas,
                                tileBuildings, tilePOIs);

                        x += tileCoordWidth;
                    }
                    y += tileCoordHeight;
                }

                currentRows = (currentRows + 1) / 2;
                currentCols = (currentCols + 1) / 2;
            }
        }

        private void cleanUp() {
            nodes = null;
            pois = null;
            ways = null;
            buildings = null;
            areas = null;
            names = null;
            numbers = null;
        }
    }

    @Override
    public void cancelCalculation() {
        canceled = true;
        try {
            reader.close();
        } catch (final IOException e) {
        }
    }

    @Override
    public void addProgressListener(final IProgressListener listener) {
        list.add(listener);
    }

    @Override
    public void removeProgressListener(final IProgressListener listener) {
        list.remove(listener);
    }

    protected void fireProgressDone(final int progress) {
        for (final IProgressListener listener : list) {
            listener.progressDone(progress);
        }
    }

    protected void fireErrorOccured(final String message) {
        for (final IProgressListener listener : list) {
            listener.errorOccured(message);
        }
    }

    static {
        EMPTY_POIS = new ArrayList<POI>(0);
        EMPTY_STREETS = new ArrayList<Street>(0);
        EMPTY_WAYS = new ArrayList<Way>(0);
        EMPTY_BUILDINGS = new ArrayList<Building>(0);
        EMPTY_AREAS = new ArrayList<Area>(0);
    }
}