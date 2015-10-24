package model;

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import model.elements.Area;
import model.elements.Building;
import model.elements.Label;
import model.elements.MultiElement;
import model.elements.POI;
import model.elements.Street;
import model.elements.StreetNode;
import model.elements.Way;
import model.map.AddressNode;
import model.map.IMapManager;
import model.map.IMapState;
import model.map.IPixelConverter;
import model.map.ITile;
import model.map.ITileSource;
import model.map.MapManager;
import model.map.MapState;
import model.map.OffsetTileSource;
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
            readIndex(managerReader.streets, managerReader.labels);
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

        final long[] edges = new long[edgeCount];
        final int[] weights = new int[edgeCount];

        int weight = 0;
        for (int i = 0; i < edgeCount; i++) {
            long node1 = reader.readCompressedInt();
            edges[i] = (node1 << 32) | reader.readCompressedInt() + node1;
            weight += reader.readCompressedInt();
            weights[i] = weight;
        }

        return new Graph(nodeCount, edges, weights);
    }

    private void readIndex(final Street[] streets, final Label[] labels) throws IOException {
        fireStepCommenced("Lade Index...");

        String[] cities = new String[reader.readCompressedInt()];

        for (int i = 0; i < cities.length; i++) {
            cities[i] = reader.readUTF();
        }

        final HashMap<String, StreetNode> nodeMap = new HashMap<String, StreetNode>();
        final HashMap<String, String[]> cityMap = new HashMap<String, String[]>();
        final TextProcessor.Entry[][] entries = new TextProcessor.Entry[reader.readCompressedInt()][];

        int maxCollisions = reader.readCompressedInt();
        int entryCount = -1;

        for (int i = 0; i < maxCollisions; i++) {
            int occurances = reader.readCompressedInt();
            int firstLevelLast = 0;
            for (int j = 0; j < occurances; j++) {
                final String[] cityNames = new String[i + 1];
                final TextProcessor.Entry[] entry = new TextProcessor.Entry[i + 1];

                int secondLevelLast = reader.readCompressedInt() + firstLevelLast;
                firstLevelLast = secondLevelLast;

                nodeMap.put(streets[secondLevelLast].getName(), new StreetNode(0.5f, streets[secondLevelLast]));

                cityNames[0] = cities[reader.readCompressedInt()];
                entry[0] = new TextProcessor.Entry(streets[secondLevelLast], cityNames[0]);

                for (int k = 1; k < cityNames.length; k++) {
                    secondLevelLast += reader.readCompressedInt();

                    nodeMap.put(streets[secondLevelLast].getName(), new StreetNode(0.5f, streets[secondLevelLast]));
                    cityNames[k] = cities[reader.readCompressedInt()];
                    entry[k] = new TextProcessor.Entry(streets[secondLevelLast], cityNames[k]);
                }
                cityMap.put(streets[secondLevelLast].getName(), cityNames);
                entries[++entryCount] = entry;
            }
        }

        for (final Label label : labels) {
            final AddressNode addressNode = manager.getAddressNode(label.getLocation());
            if (addressNode != null) {
                nodeMap.put(label.getName(), addressNode.getStreetNode());
            }
        }

        tp = new AdvancedTextProcessor(entries, labels, manager, 5);
    }

    private class MapManagerReader {
        private int[] xPoints;
        private int[] yPoints;
        private POI[] pois;
        private Street[] streets;
        private Way[] ways;
        private Building[] buildings;
        private Area[] areas;
        private Label[] labels;
        private ITile[][][] tiles;
        private int[][] offsets;
        private String[] names;
        private String[] numbers;
        private ITileSource source;

        private int rows;

        private IPixelConverter converter;
        private IMapState state;
        private Dimension tileSize;

        public IMapManager readMapManager() throws IOException {
            readHeader();
            readElements();
            readTiles();

            return new MapManager(source, converter, state, tileSize);
        }

        private void readHeader() throws IOException {
            final int width = reader.readCompressedInt();
            final int height = reader.readCompressedInt();
            final int minZoomStep = reader.readCompressedInt();
            final int maxZoomStep = reader.readCompressedInt();

            final int zoomSteps = maxZoomStep - minZoomStep + 1;
            state = new MapState(width, height, minZoomStep, maxZoomStep);

            rows = reader.readCompressedInt();

            tiles = new ITile[zoomSteps][][];
            offsets = new int[zoomSteps][];

            converter = new PixelConverter(reader.readDouble());
            tileSize = new Dimension(reader.readCompressedInt(), reader.readCompressedInt());
        }

        private void readElements() throws IOException {
            fireStepCommenced("Lade Nodes...");

            int count = 0;
            // nodes = new Node[reader.readCompressedInt()];
            xPoints = new int[reader.readCompressedInt()];
            yPoints = new int[xPoints.length];
            for (count = 0; count < xPoints.length; count++) {
                xPoints[count] = reader.readCompressedInt();
                yPoints[count] = reader.readCompressedInt();
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
                    final long node1 = reader.readCompressedInt();
                    final long id = (node1 << 32) | (node1 + reader.readCompressedInt());

                    final int[][] points = readPoints();
                    streets[count] = new Street(points[0], points[1], type, names[reader.readCompressedInt()], id);
                }
                ++type;
            }

            fireStepCommenced("Lade Wege...");

            count = 0;
            type = 0;
            distribution = readIntArray(reader.readCompressedInt());
            ways = new Way[distribution[distribution.length - 1]];

            for (int i = 0; i < distribution.length; i++) {
                final int number = distribution[i];
                for (; count < number; count++) {
                    final int[][] points = readPoints();

                    ways[count] = new Way(points[0], points[1], type, names[reader.readCompressedInt()]);
                }
                ++type;
            }

            fireStepCommenced("Lade Gelände...");

            count = 0;
            type = 0;
            distribution = readIntArray(reader.readCompressedInt());
            areas = new Area[distribution[distribution.length - 1]];

            for (int i = 0; i < distribution.length; i++) {
                final int number = distribution[i];
                for (; count < number; count++) {
                    final int[][] points = readPoints();

                    areas[count] = new Area(points[0], points[1], type);
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

            fireStepCommenced("Lade Gebäude...");

            buildings = new Building[reader.readCompressedInt()];
            final int streetNodes = reader.readCompressedInt();

            for (count = 0; count < streetNodes; count++) {
                final int[][] points = readPoints();

                buildings[count] = Building.create(points[0], points[1], new StreetNode(reader.readFloat(),
                        streets[reader.readCompressedInt()]), numbers[reader.readCompressedInt()]);
            }

            for (; count < buildings.length; count++) {
                final int[][] points = readPoints();

                buildings[count] = Building.create(points[0], points[1]);
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
                ++type;
            }
        }

        private int[][] readPoints() throws IOException {
            final int length = reader.readCompressedInt();
            final int[][] ret = new int[2][length];

            int id = 0;
            for (int j = 0; j < length; j++) {
                id += reader.readCompressedInt();
                ret[0][j] = xPoints[id];
                ret[1][j] = yPoints[id];
            }
            return ret;
        }

        private void readTiles() throws IOException {
            fireStepCommenced("Lade Kacheln...");

            int currentRows = rows;

            final ITileFactory factory = new StorageTileFactory(reader, pois, streets, ways, buildings, areas, labels);

            for (int zoom = state.getMaxZoomStep(); zoom >= state.getMinZoomStep(); zoom--) {
                final int relativeZoom = zoom - state.getMinZoomStep();

                tiles[relativeZoom] = new ITile[currentRows][];
                final int[] currentOffsets = new int[currentRows];
                offsets[relativeZoom] = currentOffsets;

                for (int row = 0; row < currentRows; row++) {
                    final int offset = reader.readCompressedInt();
                    currentOffsets[row] = offset;

                    final int columns = reader.readCompressedInt();

                    tiles[relativeZoom][row] = new ITile[columns];

                    for (int column = 0; column < columns; column++) {
                        tiles[relativeZoom][row][column] = factory.createTile(row, column + offset, zoom);
                    }
                }

                currentRows = (currentRows + 1) / 2;

                if (zoom != state.getMinZoomStep()) {
                    readSimplifications();
                }
            }

            source = new OffsetTileSource(tiles, offsets, state.getMinZoomStep());
        }

        private void readSimplifications() throws IOException {
            int size = reader.readCompressedInt();

            // TODO do not create new arrays --> use subarray by inner class or
            // new class
            for (int i = 0; i < size; i++) {
                final int area = reader.readCompressedInt();
                final int[][] simplifications = readSimplifications(areas[area]);
                areas[area] = new Area(simplifications[0], simplifications[1], areas[area].getType());
            }

            size = reader.readCompressedInt();
            for (int i = 0; i < size; i++) {
                final int way = reader.readCompressedInt();
                final int[][] simplifications = readSimplifications(ways[way]);
                ways[way] = new Way(simplifications[0], simplifications[1], ways[way].getType(), ways[way].getName());
            }
        }

        private int[][] readSimplifications(final MultiElement element) throws IOException {
            final int length = reader.readCompressedInt();
            final int[][] newNodes = new int[2][length];
            final int[] oldXNodes = element.getXPoints();
            final int[] oldYNodes = element.getYPoints();

            int index = 0;
            for (int j = 0; j < length; j++) {
                index += reader.readCompressedInt();
                newNodes[0][j] = oldXNodes[index];
                newNodes[1][j] = oldYNodes[index];
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
            xPoints = null;
            yPoints = null;
            pois = null;
            streets = null;
            ways = null;
            buildings = null;
            areas = null;
            names = null;
            numbers = null;
            labels = null;
            tiles = null;
            offsets = null;
            source = null;

            converter = null;
            state = null;
            tileSize = null;
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