package model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import model.ITextProcessor.Entry;
import model.map.CollectiveAccessorFactory;
import model.map.IMapManager;
import model.map.IMapState;
import model.map.IPixelConverter;
import model.map.IQuadtree;
import model.map.MapManager;
import model.map.MapState;
import model.map.PixelConverter;
import model.map.Quadtree;
import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.IPointAccessor;
import model.map.accessors.ITileAccessor;
import model.map.accessors.TileAccessor;
import model.routing.DirectedGraph;
import model.routing.IDirectedGraph;
import model.routing.IRouteManager;
import model.routing.RouteManager;

public class Reader implements IReader {

    private final List<IProgressListener> list;

    private IDirectedGraph graph;
    private CompressedInputStream reader;
    private IMapManager manager;
    private IRouteManager rm;
    private ITextProcessor tp;
    private boolean canceled;

    private long totalBytes;
    private long currentBytes;
    private long progress;

    public Reader() {
        list = new LinkedList<>();
    }

    @Override
    public boolean read(final File file) {
        canceled = false;
        MapManagerReader managerReader = new MapManagerReader();

        final String path = "quadtree";
        totalBytes = (long) Math.ceil(getTotalBytes(path) / 100.0);
        currentBytes = 0;
        progress = -1;
        try {
            reader = createInputStream(file);
            graph = readGraph();
            reader.close();
            manager = managerReader.readMapManager(new File(path).getAbsolutePath());
            tp = new AdvancedTextProcessor(new Entry[0][], manager);
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

    private CompressedInputStream createInputStream(final String path) throws IOException {
        return createInputStream(new File(path));
    }

    private CompressedInputStream createInputStream(final File file) throws IOException {
        return new CompressedInputStream(new BufferedInputStream(new ProgressableInputStream(file)));
    }

    private long getTotalBytes(final String path) {
        long ret = 0;
        for (final File file : new File(path).listFiles()) {
            ret += file.length();
        }
        return ret;
    }

    /*
     * Reads the Graph-section of the tsk file and generates the Graph.
     */
    private IDirectedGraph readGraph() throws IOException {
        fireStepCommenced("Lade Graph...");

        final int nodeCount = reader.readCompressedInt();

        final int[] firstNodes = new int[reader.readCompressedInt()];
        final int[] secondNodes = new int[firstNodes.length];
        final int[] weights = new int[firstNodes.length];
        final int[] oneways = new int[reader.readCompressedInt()];

        for (int i = 0; i < firstNodes.length; i++) {
            firstNodes[i] = reader.readCompressedInt();
            secondNodes[i] = reader.readCompressedInt();
            weights[i] = reader.readCompressedInt();
        }

        int id = 0;
        for (int i = 0; i < oneways.length; i++) {
            id += reader.readCompressedInt();
            oneways[i] = id;
        }

        return new DirectedGraph(nodeCount, firstNodes, secondNodes, weights, oneways);
    }

    // private void readIndex(final IStreet[] streets, final Label[] labels)
    // throws IOException {
    // fireStepCommenced("Lade Index...");
    //
    // String[] cities = new String[reader.readCompressedInt()];
    //
    // for (int i = 0; i < cities.length; i++) {
    // cities[i] = reader.readUTF();
    // }
    //
    // final HashMap<String, StreetNode> nodeMap = new HashMap<String,
    // StreetNode>();
    // final HashMap<String, String[]> cityMap = new HashMap<String,
    // String[]>();
    // final TextProcessor.Entry[][] entries = new
    // TextProcessor.Entry[reader.readCompressedInt()][];
    //
    // int maxCollisions = reader.readCompressedInt();
    // int entryCount = -1;
    //
    // for (int i = 0; i < maxCollisions; i++) {
    // int occurances = reader.readCompressedInt();
    // int firstLevelLast = 0;
    // for (int j = 0; j < occurances; j++) {
    // final String[] cityNames = new String[i + 1];
    // final TextProcessor.Entry[] entry = new TextProcessor.Entry[i + 1];
    //
    // int secondLevelLast = reader.readCompressedInt() + firstLevelLast;
    // firstLevelLast = secondLevelLast;
    //
    // nodeMap.put(streets[secondLevelLast].getName(), new StreetNode(0.5f,
    // streets[secondLevelLast]));
    //
    // cityNames[0] = cities[reader.readCompressedInt()];
    // // TODO
    // // entry[0] = new TextProcessor.Entry(streets[secondLevelLast],
    // // cityNames[0]);
    //
    // for (int k = 1; k < cityNames.length; k++) {
    // secondLevelLast += reader.readCompressedInt();
    //
    // nodeMap.put(streets[secondLevelLast].getName(), new StreetNode(0.5f,
    // streets[secondLevelLast]));
    // cityNames[k] = cities[reader.readCompressedInt()];
    // // TODO
    // // entry[k] = new
    // // TextProcessor.Entry(streets[secondLevelLast],
    // // cityNames[k]);
    // }
    // cityMap.put(streets[secondLevelLast].getName(), cityNames);
    // entries[++entryCount] = entry;
    // }
    // }

    // TODO reactivate!
    // for (final Label label : labels) {
    // final AddressNode addressNode =
    // manager.getAddressNode(label.getLocation());
    // if (addressNode != null) {
    // nodeMap.put(label.getName(), addressNode.getStreetNode());
    // }
    // }

    // tp = new AdvancedTextProcessor(entries, labels, manager);
    // }

    private class MapManagerReader {

        public IMapManager readMapManager(final String path) throws IOException {
            fireStepCommenced("Lade Header...");
            final CompressedInputStream reader = createInputStream(path + "/header");
            final IPixelConverter converter = readConverter(reader);
            final IMapState state = readMapState(reader, converter);
            final int[][] distributions = readDistributions(reader);
            reader.close();

            final String[] strings = readStrings(path);
            final int[][] nodes = readNodes(path);
            final Map<String, IQuadtree> quadtreeMap = new HashMap<>();
            final Map<String, IFactory<ICollectiveAccessor>> collectiveMap = new HashMap<>();
            readElements(path, nodes, distributions, quadtreeMap, collectiveMap, state.getMinZoom());
            final IFactory<ITileAccessor> tileFactory = new IFactory<ITileAccessor>() {
                @Override
                public ITileAccessor create() {
                    return new TileAccessor(quadtreeMap, state);
                }
            };
            return new MapManager(new HashMap<String, IFactory<IPointAccessor>>(), collectiveMap, tileFactory, strings,
                    state);
        }

        private IMapState readMapState(final CompressedInputStream reader, final IPixelConverter converter)
                throws IOException {
            final int width = reader.readInt();
            final int height = reader.readInt();
            final int minZoomStep = reader.readInt();
            final int maxZoomStep = reader.readInt();
            final int tileSize = reader.readInt();
            return new MapState(width, height, minZoomStep, maxZoomStep, tileSize, converter);
        }

        private IPixelConverter readConverter(final CompressedInputStream reader) throws IOException {
            final int conversionBits = reader.readInt();
            return new PixelConverter(conversionBits);
        }

        private int[][] readDistributions(final CompressedInputStream reader) throws IOException {
            final int[][] distributions = new int[4][];
            for (int i = 0; i < 4; i++) {
                distributions[i] = readIntArray(reader.readInt(), reader);
            }

            return distributions;
        }

        private int[][] readNodes(final String path) throws IOException {
            fireStepCommenced("Lade Nodes...");

            reader = createInputStream(path + "/nodes");
            int count = 0;

            final int[] xPoints = new int[reader.readInt()];
            final int[] yPoints = new int[xPoints.length];
            for (count = 0; count < xPoints.length; count++) {
                xPoints[count] = reader.readInt();
                yPoints[count] = reader.readInt();
            }
            reader.close();

            return new int[][] { xPoints, yPoints };
        }

        private String[] readStrings(final String path) throws IOException {
            fireStepCommenced("Lade Adressen...");

            reader = createInputStream(path + "/strings");
            final String[] strings = new String[reader.readInt()];
            for (int count = 0; count < strings.length; count++) {
                strings[count] = reader.readUTF();
            }
            strings[0] = "Unbekannte Straße";
            reader.close();

            return strings;
        }

        private void readElements(final String path, final int[][] nodes, final int[][] distributions,
                final Map<String, IQuadtree> quadtreeMap,
                final Map<String, IFactory<ICollectiveAccessor>> collectiveMap, final int minZoomStep)
                throws IOException {

            final String[] names = { "street", "way", "area", "building" };
            final String[] outputNames = { "Straßen", "Wege", "Gelände", "Gebäude" };

            final CollectiveAccessorFactory[] accessors = new CollectiveAccessorFactory[names.length];
            accessors[0] = new CollectiveAccessorFactory(nodes[0], nodes[1], distributions[0])
            // {
            // @Override
            // public ICollectiveAccessor create() {
            // return new StreetAccessor(x, y, data, distribution);
            // }
            // }
            ;
            accessors[1] = new CollectiveAccessorFactory(nodes[0], nodes[1], distributions[1]);
            accessors[2] = new CollectiveAccessorFactory(nodes[0], nodes[1], distributions[2]);
            accessors[3] = new CollectiveAccessorFactory(nodes[0], nodes[1], distributions[3])
            // {
            // @Override
            // public ICollectiveAccessor create() {
            // return new BuildingAccessor(x, y, data, distribution);
            // }
            // }
            ;
            for (int i = 0; i < names.length; i++) {
                fireStepCommenced("Lade " + outputNames[i] + "...");
                accessors[i].setData(readIntArray(path + "/" + names[i] + "s"));
                collectiveMap.put(names[i], accessors[i]);

                int[] elementData = readIntArray(path + "/" + names[i] + "Data");
                int[] treeData = readIntArray(path + "/" + names[i] + "Tree");
                quadtreeMap.put(names[i], new Quadtree(treeData, elementData, minZoomStep));
            }

            fireStepCommenced("Lade Points of Interest...");

            fireStepCommenced("Lade Labels...");
        }

        private int[] readIntArray(final int length, final CompressedInputStream reader) throws IOException {
            final int[] ret = new int[length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = reader.readInt();
            }

            return ret;
        }

        private int[] readIntArray(final String path) throws IOException {
            final CompressedInputStream reader = createInputStream(path);

            final int[] ret = new int[reader.available() / 4];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = reader.readInt();
            }

            reader.close();

            return ret;
        }
    }

    private class ProgressableInputStream extends FileInputStream {

        public ProgressableInputStream(final File in) throws IOException {
            super(in);
        }

        @Override
        public int read() throws IOException {
            final long tmp = ++currentBytes / totalBytes;
            if (tmp != progress) {
                progress = tmp;
                fireProgressDone((int) progress);
            }

            return super.read();
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            final int nr = super.read(b, off, len);

            final long tmp = (currentBytes += nr) / totalBytes;
            if (tmp != progress) {
                progress = tmp;
                fireProgressDone((int) progress);
            }

            return nr;
        }

        @Override
        public int read(final byte[] b) throws IOException {
            final int nr = super.read(b);

            final long tmp = (currentBytes += nr) / totalBytes;
            if (tmp != progress) {
                progress = tmp;
                fireProgressDone((int) progress);
            }

            return nr;
        }
    }
}