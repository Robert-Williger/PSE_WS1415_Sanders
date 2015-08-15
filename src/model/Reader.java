package model;

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import model.map.EmptyTile;
import model.map.IMapManager;
import model.map.IPixelConverter;
import model.map.ITile;
import model.map.MapManager;
import model.map.PixelConverter;
import model.map.Tile;
import model.routing.Graph;
import model.routing.IGraph;
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
        fireStepCommenced("Lade Graph...");
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
            fireStepCommenced("Lade Knoten...");

            int count = 0;
            nodes = new Node[reader.readInt()];
            for (count = 0; count < nodes.length; count++) {
                nodes[count] = new Node(reader.readInt(), reader.readInt());
            }

            fireStepCommenced("Lade Adressen...");

            names = new String[reader.readInt()];
            for (count = 0; count < names.length; count++) {
                names[count] = reader.readUTF();
            }
            names[0] = "Unbekannte Straße";

            numbers = new String[reader.readInt()];
            for (count = 0; count < numbers.length; count++) {
                numbers[count] = reader.readUTF();
            }

            fireStepCommenced("Lade Straßen...");

            count = 0;
            int size = reader.readInt();
            int[] distribution = readIntArray(size);
            int[] types = readIntArray(size);
            streets = new Street[distribution[size - 1]];

            for (int i = 0; i < size; i++) {
                final int type = types[i];
                final int number = distribution[i];
                for (; count < number; count++) {
                    streets[count] = new Street(readNodeList(), type, names[reader.readShort()], reader.readLong());
                }
            }

            fireStepCommenced("Lade Wege...");

            count = 0;
            size = reader.readInt();
            distribution = readIntArray(size);
            types = readIntArray(size);
            ways = new Way[distribution[size - 1]];

            for (int i = 0; i < size; i++) {
                final int type = types[i];
                final int number = distribution[i];
                for (; count < number; count++) {
                    ways[count] = new Way(readNodeList(), type, names[reader.readShort()]);
                }
            }

            fireStepCommenced("Lade Gelände...");

            count = 0;
            size = reader.readInt();
            distribution = readIntArray(size);
            types = readIntArray(size);
            areas = new Area[distribution[size - 1]];

            for (int i = 0; i < size; i++) {
                final int type = types[i];
                final int number = distribution[i];
                for (; count < number; count++) {
                    areas[count] = new Area(readNodeList(), type);
                }
            }

            fireStepCommenced("Lade Points of Interest...");

            count = 0;
            size = reader.readInt();
            distribution = readIntArray(size);
            types = readIntArray(size);
            pois = new POI[distribution[size - 1]];

            for (int i = 0; i < size; i++) {
                final int type = types[i];
                final int number = distribution[i];
                for (; count < number; count++) {
                    pois[count] = new POI(reader.readInt(), reader.readInt(), type);
                }
            }

            fireStepCommenced("Lade Gebäude...");
            buildings = new Building[reader.readInt()];
            final int streetNodes = reader.readInt();

            for (count = 0; count < streetNodes; count++) {
                final List<Node> nodes = readNodeList();
                final Street street = streets[reader.readInt()];

                buildings[count] = new Building(nodes, street.getName() + " " + numbers[reader.readShort()],
                        new StreetNode(reader.readFloat(), street));
            }

            for (; count < buildings.length; count++) {
                buildings[count] = new Building(readNodeList(), "", null);
            }
        }

        private List<Node> readNodeList() throws IOException {
            final int nodes = reader.readUnsignedShort();

            final Node[] n = new Node[nodes];
            for (int j = 0; j < nodes; j++) {
                n[j] = this.nodes[reader.readInt()];
            }

            return Arrays.asList(n);
        }

        private void readTiles() throws IOException {
            fireStepCommenced("Lade Kacheln...");

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
                        byte flags = reader.readByte();
                        final ITile tile;
                        if (flags == 0) {
                            tile = new EmptyTile(zoom, row, column);
                        } else {
                            final Collection<POI> tilePOIs;
                            final Collection<Street> tileStreets;
                            final Collection<Way> tileWays;
                            final Collection<Building> tileBuildings;
                            final Collection<Area> tileAreas;

                            if ((flags & 1) == 0) {
                                tilePOIs = EMPTY_POIS;
                            } else {
                                final POI[] p = new POI[reader.readInt()];
                                for (int i = 0; i < p.length; i++) {
                                    p[i] = pois[reader.readInt()];
                                }
                                tilePOIs = Arrays.asList(p);
                            }

                            if ((flags >> 1 & 1) == 0) {
                                tileStreets = EMPTY_STREETS;
                            } else {
                                final Street[] s = new Street[reader.readInt()];
                                for (int i = 0; i < s.length; i++) {
                                    s[i] = streets[reader.readInt()];
                                }
                                tileStreets = Arrays.asList(s);
                            }

                            if ((flags >> 2 & 1) == 0) {
                                tileWays = EMPTY_WAYS;
                            } else {
                                final Way[] w = new Way[reader.readInt()];
                                for (int i = 0; i < w.length; i++) {
                                    w[i] = ways[reader.readInt()];
                                }
                                tileWays = Arrays.asList(w);
                            }

                            if ((flags >> 3 & 1) == 0) {
                                tileBuildings = EMPTY_BUILDINGS;
                            } else {
                                final Building[] b = new Building[reader.readInt()];
                                for (int i = 0; i < b.length; i++) {
                                    b[i] = buildings[reader.readInt()];
                                }
                                tileBuildings = Arrays.asList(b);
                            }

                            if ((flags >> 4 & 1) == 0) {
                                tileAreas = EMPTY_AREAS;
                            } else {
                                final Area[] a = new Area[reader.readInt()];
                                for (int i = 0; i < a.length; i++) {
                                    a[i] = areas[reader.readInt()];
                                }
                                tileAreas = Arrays.asList(a);
                            }

                            tile = new Tile(zoom, row, column, x, y, tileWays, tileStreets, tileAreas, tileBuildings,
                                    tilePOIs);
                        }

                        tiles[zoom][row][column] = tile;

                        x += tileCoordWidth;
                    }
                    y += tileCoordHeight;
                }

                currentRows = (currentRows + 1) / 2;
                currentCols = (currentCols + 1) / 2;
            }
        }

        private int[] readIntArray(final int length) throws IOException {
            final int[] ret = new int[length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = reader.readInt();
            }

            return ret;
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

    protected void fireStepCommenced(final String step) {
        for (final IProgressListener listener : list) {
            listener.stepCommenced(step);
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