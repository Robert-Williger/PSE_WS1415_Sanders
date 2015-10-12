package model;

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import model.elements.Area;
import model.elements.Building;
import model.elements.Label;
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
import model.map.factories.ITileFactory;
import model.map.factories.StorageTileFactory;
import model.routing.Graph;
import model.routing.IGraph;
import model.routing.IRouteManager;
import model.routing.RouteManager;

public class Reader implements IReader {

    private final List<IProgressListener> list;

    private IGraph graph;
    private CompressedInputStream reader;
    private IMapManager manager;
    private IRouteManager rm;
    private ITextProcessor tp;
    private boolean canceled;

    public Reader() {
        list = new LinkedList<IProgressListener>();
    }

    @Override
    public boolean read(final File file) {
        canceled = false;
        MapManagerReader managerReader = new MapManagerReader();

        try {
            reader = new CompressedInputStream(new BufferedInputStream(new ProgressableInputStream(file)));
            graph = readGraph();
            manager = managerReader.readMapManager();
            readIndex(managerReader.getStreets());
            managerReader.cleanUp();
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

        managerReader = null;

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

    /*
     * Reads the Graph-section of the tsk file and generates the Graph.
     */
    private IGraph readGraph() throws IOException {
        fireStepCommenced("Lade Graph...");
        final int nodeCount = reader.readCompressedInt();
        final int edgeCount = reader.readCompressedInt();

        final List<Long> edges = new ArrayList<Long>(edgeCount);
        final List<Integer> weights = new ArrayList<Integer>(edgeCount);

        int weight = 0;
        for (int i = 0; i < edgeCount; i++) {
            long node1 = reader.readCompressedInt();
            edges.add((node1 << 32) | reader.readCompressedInt() + node1);
            weight += reader.readCompressedInt();
            weights.add(weight);
        }

        return new Graph(nodeCount, edges, weights);
    }

    private void readIndex(final Street[] streets) throws IOException {
        String[] cities = new String[reader.readCompressedInt()];

        for (int i = 0; i < cities.length; i++) {
            cities[i] = reader.readUTF();
        }

        final HashMap<String, StreetNode> nodeMap = new HashMap<String, StreetNode>();
        final HashMap<String, String[]> cityMap = new HashMap<String, String[]>();

        int maxCollisions = reader.readCompressedInt();

        for (int i = 0; i < maxCollisions; i++) {
            int occurances = reader.readCompressedInt();
            int firstLevelLast = 0;
            for (int j = 0; j < occurances; j++) {
                final String[] cityNames = new String[i + 1];

                int secondLevelLast = reader.readCompressedInt() + firstLevelLast;
                firstLevelLast = secondLevelLast;

                nodeMap.put(streets[secondLevelLast].getName(), new StreetNode(0.5f, streets[secondLevelLast]));

                cityNames[0] = cities[reader.readCompressedInt()];

                for (int k = 1; k < cityNames.length; k++) {
                    secondLevelLast += reader.readCompressedInt();

                    nodeMap.put(streets[secondLevelLast].getName(), new StreetNode(0.5f, streets[secondLevelLast]));
                    cityNames[k] = cities[reader.readCompressedInt()];
                }
                cityMap.put(streets[secondLevelLast].getName(), cityNames);
            }
        }

        tp = new TextProcessor(nodeMap, cityMap, 5);
    }

    private class MapManagerReader {
        private Node[] nodes;
        private POI[] pois;
        private Street[] streets;
        private Way[] ways;
        private Building[] buildings;
        private Area[] areas;
        private Label[] labels;
        private ITile[][][] tiles;
        private String[] names;
        private String[] numbers;

        private Node[][] origWays;
        private Node[][] origAreas;

        private int minZoomStep;
        private int maxZoomStep;
        private int rows;
        private int columns;

        private IPixelConverter converter;
        private Dimension tileSize;

        public IMapManager readMapManager() throws IOException {
            readHeader();
            readElements();
            readTiles();

            return new MapManager(tiles, tileSize, converter, minZoomStep);
        }

        public Street[] getStreets() {
            return streets;
        }

        private void readHeader() throws IOException {
            minZoomStep = reader.readCompressedInt();
            maxZoomStep = reader.readCompressedInt();
            rows = reader.readCompressedInt();
            columns = reader.readCompressedInt();
            tiles = new ITile[maxZoomStep - minZoomStep + 1][][];
            converter = new PixelConverter(reader.readDouble());
            tileSize = new Dimension(reader.readCompressedInt(), reader.readCompressedInt());
        }

        private void readElements() throws IOException {
            fireStepCommenced("Lade Nodes...");

            int count = 0;
            nodes = new Node[reader.readCompressedInt()];
            for (count = 0; count < nodes.length; count++) {
                nodes[count] = new Node(reader.readCompressedInt(), reader.readCompressedInt());
            }

            fireStepCommenced("Lade Adressen...");

            names = new String[reader.readCompressedInt()];
            for (count = 0; count < names.length; count++) {
                names[count] = reader.readUTF();
            }
            names[0] = "Unbekannte Straße";

            numbers = new String[reader.readCompressedInt()];
            for (count = 0; count < numbers.length; count++) {
                numbers[count] = reader.readUTF();
            }

            fireStepCommenced("Lade Straßen...");

            count = 0;
            int type = 0;
            int[] distribution = readIntArray(reader.readCompressedInt());

            streets = new Street[distribution[distribution.length - 1]];

            for (int i = 0; i < distribution.length; i++) {
                final int number = distribution[i];
                for (; count < number; count++) {
                    long node1 = reader.readCompressedInt();
                    long id = (node1 << 32) | (node1 + reader.readCompressedInt());
                    streets[count] = new Street(readNodeArray(), type, names[reader.readCompressedInt()], id);
                }
                ++type;
            }

            fireStepCommenced("Lade Wege...");

            count = 0;
            type = 0;
            distribution = readIntArray(reader.readCompressedInt());
            ways = new Way[distribution[distribution.length - 1]];
            origWays = new Node[ways.length][];

            for (int i = 0; i < distribution.length; i++) {
                final int number = distribution[i];
                for (; count < number; count++) {
                    ways[count] = new Way(readNodeArray(), type, names[reader.readCompressedInt()]);
                    origWays[count] = ways[count].getNodes();
                }
                ++type;
            }

            fireStepCommenced("Lade Gelände...");

            count = 0;
            type = 0;
            distribution = readIntArray(reader.readCompressedInt());
            areas = new Area[distribution[distribution.length - 1]];
            origAreas = new Node[areas.length][];

            for (int i = 0; i < distribution.length; i++) {
                final int number = distribution[i];
                for (; count < number; count++) {
                    areas[count] = new Area(readNodeArray(), type);
                    origAreas[count] = areas[count].getNodes();
                }

                ++type;
            }

            fireStepCommenced("Lade Points of Interest...");

            count = 0;
            type = 0;
            distribution = readIntArray(reader.readCompressedInt());
            pois = new POI[distribution[distribution.length - 1]];

            for (int i = 0; i < distribution.length; i++) {
                final int number = distribution[i];
                for (; count < number; count++) {
                    pois[count] = new POI(reader.readCompressedInt(), reader.readCompressedInt(), type);
                }
                ++type;
            }

            // TODO implement me

            fireStepCommenced("Lade Gebäude...");
            buildings = new Building[reader.readCompressedInt()];
            final int streetNodes = reader.readCompressedInt();

            for (count = 0; count < streetNodes; count++) {
                buildings[count] = Building.create(readNodeArray(),
                        new StreetNode(reader.readFloat(), streets[reader.readCompressedInt()]),
                        numbers[reader.readCompressedInt()]);
            }

            for (; count < buildings.length; count++) {
                buildings[count] = Building.create(readNodeArray());
            }

            fireStepCommenced("Lade Labels...");

            count = 0;
            type = 0;
            distribution = readIntArray(reader.readCompressedInt());
            labels = new Label[distribution[distribution.length - 1]];

            for (int i = 0; i < distribution.length; i++) {
                final int number = distribution[i];
                for (; count < number; count++) {
                    int x = reader.readCompressedInt();
                    int y = reader.readCompressedInt();

                    labels[count] = Label.create(reader.readUTF(), type, x, y);
                }
            }
        }

        private Node[] readNodeArray() throws IOException {
            final Node[] n = new Node[reader.readCompressedInt()];
            int id = 0;
            for (int j = 0; j < n.length; j++) {
                id += reader.readCompressedInt();
                n[j] = this.nodes[id];
            }
            return n;
        }

        private void readTiles() throws IOException {
            fireStepCommenced("Lade Kacheln...");

            // TODO own class with interface ITile createTile()
            // final TileFactory factory = new TileFactory();

            int currentRows = rows;
            int currentCols = columns;

            final ITileFactory factory = new StorageTileFactory(reader, pois, streets, ways, buildings, areas, labels);
            for (int zoom = maxZoomStep; zoom >= minZoomStep; zoom--) {

                tiles[zoom - minZoomStep] = new ITile[currentRows][currentCols];

                for (int row = 0; row < currentRows; row++) {
                    for (int column = 0; column < currentCols; column++) {

                        // tile = new Tile(zoom, row, column, tileWays,
                        // tileStreets, tileAreas, tileBuildings,
                        // tilePOIs);

                        tiles[zoom - minZoomStep][row][column] = factory.createTile(row, column, zoom);
                    }
                }

                currentRows = (currentRows + 1) / 2;
                currentCols = (currentCols + 1) / 2;

                if (zoom != minZoomStep) {
                    readSimplifications();
                }
            }
        }

        private void readSimplifications() throws IOException {
            int size = reader.readCompressedInt();

            for (int i = 0; i < size; i++) {
                final int area = reader.readCompressedInt();
                areas[area] = new Area(readSimplifications(origAreas, area), areas[area].getType());
            }

            size = reader.readCompressedInt();
            for (int i = 0; i < size; i++) {
                final int way = reader.readCompressedInt();
                ways[way] = new Way(readSimplifications(origWays, way), ways[way].getType(), ways[way].getName());
            }
        }

        private Node[] readSimplifications(final Node[][] origElements, final int element) throws IOException {
            final Node[] newNodes = new Node[reader.readCompressedInt()];
            final Node[] oldNodes = origElements[element];

            int index = 0;
            for (int j = 0; j < newNodes.length; j++) {
                index += reader.readCompressedInt();
                newNodes[j] = oldNodes[index];
            }

            return newNodes;
        }

        private int[] readIntArray(final int length) throws IOException {
            final int[] ret = new int[length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = reader.readCompressedInt();
            }

            return ret;
        }

        public void cleanUp() {
            nodes = null;
            pois = null;
            ways = null;
            buildings = null;
            areas = null;
            names = null;
            numbers = null;
        }
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
}