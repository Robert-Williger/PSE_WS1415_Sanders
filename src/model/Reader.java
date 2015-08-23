package model;

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
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

    private static final POI[] EMPTY_POIS;
    private static final Street[] EMPTY_STREETS;
    private static final Way[] EMPTY_WAYS;
    private static final Building[] EMPTY_BUILDINGS;
    private static final Area[] EMPTY_AREAS;

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
        final int nodeCount = readInt();
        final int edgeCount = readInt();

        final List<Long> edges = new ArrayList<Long>(edgeCount);
        final List<Integer> weights = new ArrayList<Integer>(edgeCount);

        int lastWeight = 0;
        for (int i = 0; i < edgeCount; i++) {
            long node1 = readInt();
            edges.add((node1 << 32) | readInt() + node1);
            int currentWeight = readInt() + lastWeight;
            weights.add(currentWeight);
            lastWeight = currentWeight;
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
            cleanUp();

            return new MapManager(tiles, tileSize, converter, minZoomStep);
        }

        public Street[] getStreets() {
            return streets;
        }

        private void readHeader() throws IOException {
            minZoomStep = readInt();
            maxZoomStep = readInt();
            rows = readInt();
            columns = readInt();
            tiles = new ITile[maxZoomStep - minZoomStep + 1][][];
            converter = new PixelConverter(reader.readDouble());
            tileSize = new Dimension(readInt(), readInt());
        }

        private void readElements() throws IOException {
            fireStepCommenced("Lade Nodes...");

            int count = 0;
            nodes = new Node[readInt()];
            for (count = 0; count < nodes.length; count++) {
                nodes[count] = new Node(readInt(), readInt());
            }

            fireStepCommenced("Lade Adressen...");

            names = new String[readInt()];
            for (count = 0; count < names.length; count++) {
                names[count] = reader.readUTF();
            }
            names[0] = "Unbekannte Straße";

            numbers = new String[readInt()];
            for (count = 0; count < numbers.length; count++) {
                numbers[count] = reader.readUTF();
            }

            fireStepCommenced("Lade Straßen...");

            count = 0;
            int type = 0;
            int[] distribution = readIntArray(readInt());

            streets = new Street[distribution[distribution.length - 1]];

            for (int i = 0; i < distribution.length; i++) {
                final int number = distribution[i];
                for (; count < number; count++) {
                    long node1 = readInt();
                    long id = (node1 << 32) | (node1 + readInt());
                    streets[count] = new Street(readNodeArray(), type, names[readInt()], id);
                }
                ++type;
            }

            fireStepCommenced("Lade Wege...");

            count = 0;
            type = 0;
            distribution = readIntArray(readInt());
            ways = new Way[distribution[distribution.length - 1]];
            origWays = new Node[ways.length][];

            for (int i = 0; i < distribution.length; i++) {
                final int number = distribution[i];
                for (; count < number; count++) {
                    ways[count] = new Way(readNodeArray(), type, names[readInt()]);
                    origWays[count] = ways[count].getNodes();
                }
                ++type;
            }

            fireStepCommenced("Lade Gelände...");

            count = 0;
            type = 0;
            distribution = readIntArray(readInt());
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
            distribution = readIntArray(readInt());
            pois = new POI[distribution[distribution.length - 1]];

            for (int i = 0; i < distribution.length; i++) {
                final int number = distribution[i];
                for (; count < number; count++) {
                    pois[count] = new POI(readInt(), readInt(), type);
                }
                ++type;
            }

            fireStepCommenced("Lade Gebäude...");
            buildings = new Building[readInt()];
            final int streetNodes = readInt();

            for (count = 0; count < streetNodes; count++) {
                buildings[count] = Building.create(readNodeArray(), new StreetNode(reader.readFloat(),
                        streets[readInt()]), numbers[readInt()]);
            }

            for (; count < buildings.length; count++) {
                buildings[count] = Building.create(readNodeArray());
            }
        }

        private Node[] readNodeArray() throws IOException {
            final Node[] n = new Node[readInt()];
            int last = 0;
            for (int j = 0; j < n.length; j++) {
                int current = readInt() + last;
                n[j] = this.nodes[current];
                last = current;
            }
            return n;
        }

        private void readTiles() throws IOException {
            fireStepCommenced("Lade Kacheln...");

            int currentRows = rows;
            int currentCols = columns;
            final ITile emptyTile = new EmptyTile(-1, -1, -1);
            for (int zoom = maxZoomStep; zoom >= minZoomStep; zoom--) {

                tiles[zoom - minZoomStep] = new ITile[currentRows][currentCols];

                final int tileCoordWidth = converter.getCoordDistance(tileSize.width, zoom);
                final int tileCoordHeight = converter.getCoordDistance(tileSize.height, zoom);

                int y = 0;
                for (int row = 0; row < currentRows; row++) {
                    int x = 0;
                    for (int column = 0; column < currentCols; column++) {
                        byte flags = reader.readByte();

                        final ITile tile;
                        if (flags == 0) {
                            tile = emptyTile;
                        } else {
                            final POI[] tilePOIs;
                            final Street[] tileStreets;
                            final Way[] tileWays;
                            final Building[] tileBuildings;
                            final Area[] tileAreas;

                            if ((flags & 1) == 0) {
                                tilePOIs = EMPTY_POIS;
                            } else {
                                tilePOIs = new POI[readInt()];
                                fillElements(pois, tilePOIs);
                            }

                            if ((flags >> 1 & 1) == 0) {
                                tileStreets = EMPTY_STREETS;
                            } else {
                                tileStreets = new Street[readInt()];
                                fillElements(streets, tileStreets);
                            }

                            if ((flags >> 2 & 1) == 0) {
                                tileWays = EMPTY_WAYS;
                            } else {
                                tileWays = new Way[readInt()];
                                fillElements(ways, tileWays);
                            }

                            if ((flags >> 3 & 1) == 0) {
                                tileBuildings = EMPTY_BUILDINGS;
                            } else {
                                tileBuildings = new Building[readInt()];
                                fillElements(buildings, tileBuildings);
                            }

                            if ((flags >> 4 & 1) == 0) {
                                tileAreas = EMPTY_AREAS;
                            } else {
                                tileAreas = new Area[readInt()];
                                fillElements(areas, tileAreas);
                            }

                            tile = new Tile(zoom, row, column, x, y, tileWays, tileStreets, tileAreas, tileBuildings,
                                    tilePOIs);
                        }

                        tiles[zoom - minZoomStep][row][column] = tile;

                        x += tileCoordWidth;
                    }
                    y += tileCoordHeight;
                }

                currentRows = (currentRows + 1) / 2;
                currentCols = (currentCols + 1) / 2;

                if (zoom != minZoomStep) {
                    readSimplifications();
                }
            }
        }

        private <T> void fillElements(final T[] source, final T[] destination) throws IOException {
            int last = 0;
            for (int i = 0; i < destination.length; i++) {
                int current = readInt() + last;
                destination[i] = source[current];
                last = current;
            }
        }

        private void readSimplifications() throws IOException {
            int size = readInt();

            for (int i = 0; i < size; i++) {
                final int area = readInt();
                areas[area] = new Area(readSimplifications(origAreas, area), areas[area].getType());
            }

            size = readInt();
            for (int i = 0; i < size; i++) {
                final int way = readInt();
                ways[way] = new Way(readSimplifications(origWays, way), ways[way].getType(), ways[way].getName());
            }
        }

        private Node[] readSimplifications(final Node[][] origElements, final int element) throws IOException {
            final Node[] newNodes = new Node[readInt()];
            final Node[] oldNodes = origElements[element];

            int last = 0;
            for (int j = 0; j < newNodes.length; j++) {
                final int index = readInt() + last;
                newNodes[j] = oldNodes[index];
                last = index;
            }

            return newNodes;
        }

        private int[] readIntArray(final int length) throws IOException {
            final int[] ret = new int[length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = readInt();
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

    private int readInt() throws IOException {
        int ret = 0;

        byte in;
        while (((in = reader.readByte()) & 0x80) == 0) {
            ret = (ret << 7) | in;
        }
        in = (byte) (in & 0x7F);
        ret = (ret << 7) | in;

        return ret;
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
        EMPTY_POIS = new POI[0];
        EMPTY_STREETS = new Street[0];
        EMPTY_WAYS = new Way[0];
        EMPTY_BUILDINGS = new Building[0];
        EMPTY_AREAS = new Area[0];
    }
}