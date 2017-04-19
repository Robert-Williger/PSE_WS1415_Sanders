package model;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import model.ITextProcessor.Entry;
import model.map.CollectiveAccessorFactory;
import model.map.IMapManager;
import model.map.IMapState;
import model.map.IPixelConverter;
import model.map.IElementIterator;
import model.map.MapManager;
import model.map.MapState;
import model.map.PixelConverter;
import model.map.Quadtree;
import model.map.accessors.BuildingAccessor;
import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.IPointAccessor;
import model.map.accessors.ITileAccessor;
import model.map.accessors.LabelAccessor;
import model.map.accessors.StreetAccessor;
import model.map.accessors.TileAccessor;
import model.routing.DirectedGraph;
import model.routing.IDirectedGraph;
import model.routing.IRouteManager;
import model.routing.RouteManager;

public class Reader implements IReader {

    private final List<IProgressListener> list;

    private DataInputStream stream;

    private IDirectedGraph graph;
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

        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (file == null) {
            return false;
        }

        totalBytes = (long) Math.ceil(getTotalBytes(zipFile) / 100.0);

        currentBytes = 0;
        progress = -1;
        try {
            graph = readGraph(zipFile);
            manager = managerReader.readMapManager(zipFile);
            tp = new AdvancedTextProcessor(new Entry[0][], manager);
        } catch (final Exception e) {
            managerReader = null;

            if (!canceled) {
                fireErrorOccured("Beim Kartenimport ist ein Fehler aufgetreten.");
            }

            return false;
        }

        managerReader = null;

        rm = new RouteManager(graph, manager);

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

        if (stream != null) {
            try {
                stream.close();
            } catch (final IOException e) {
            }
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

    private long getTotalBytes(final ZipFile zipFile) {
        return zipFile.stream().mapToInt((e) -> (int) e.getSize()).sum();
    }

    private void applyInputStream(final ZipFile zipFile, final ZipEntry entry) throws IOException {
        stream = createInputStream(zipFile, entry);
    }

    private DataInputStream createInputStream(final ZipFile zipFile, final ZipEntry entry) throws IOException {
        return new DataInputStream(new BufferedInputStream(new ProgressableInputStream(zipFile.getInputStream(entry))));
    }

    /*
     * Reads the Graph-section of the map file and generates the Graph.
     */
    private IDirectedGraph readGraph(final ZipFile zipFile) throws IOException {
        fireStepCommenced("Lade Graph...");

        final ZipEntry entry = zipFile.getEntry("graph");
        if (entry != null) {
            applyInputStream(zipFile, entry);
            final int nodeCount = stream.readInt();

            final int[] firstNodes = new int[stream.readInt()];
            final int[] secondNodes = new int[firstNodes.length];
            final int[] weights = new int[firstNodes.length];
            final int[] oneways = new int[stream.readInt()];

            for (int i = 0; i < firstNodes.length; i++) {
                firstNodes[i] = stream.readInt();
                secondNodes[i] = stream.readInt();
                weights[i] = stream.readInt();
            }

            int id = 0;
            for (int i = 0; i < oneways.length; i++) {
                id += stream.readInt();
                oneways[i] = id;
            }

            stream.close();

            return new DirectedGraph(nodeCount, firstNodes, secondNodes, weights, oneways);
        }

        return new DirectedGraph(0, new int[0], new int[0], new int[0], new int[0]);
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

        public IMapManager readMapManager(final ZipFile zipFile) throws IOException {
            fireStepCommenced("Lade Header...");

            final IMapState state = readMapState(zipFile);
            final int[][] distributions = readDistributions(zipFile);

            final String[] strings = readStrings(zipFile);
            final int[][] nodes = readNodes(zipFile);
            final Map<String, IElementIterator> elementIteratorMap = new HashMap<>();
            final Map<String, IFactory<ICollectiveAccessor>> collectiveMap = new HashMap<>();
            final Map<String, IFactory<IPointAccessor>> pointMap = new HashMap<>();
            readElements(zipFile, nodes, distributions, elementIteratorMap, pointMap, collectiveMap,
                    state.getMinZoom());
            final IFactory<ITileAccessor> tileFactory = new IFactory<ITileAccessor>() {
                @Override
                public ITileAccessor create() {
                    return new TileAccessor(elementIteratorMap, state);
                }
            };
            return new MapManager(pointMap, collectiveMap, tileFactory, strings, state);
        }

        private IMapState readMapState(final ZipFile zipFile) throws IOException {
            final ZipEntry entry = zipFile.getEntry("header");
            if (entry == null) {
                return null;
            }
            applyInputStream(zipFile, entry);
            final IPixelConverter converter = readConverter(stream);

            final int width = stream.readInt();
            final int height = stream.readInt();
            final int minZoomStep = stream.readInt();
            final int maxZoomStep = stream.readInt();
            final int tileSize = stream.readInt();

            stream.close();

            return new MapState(width, height, minZoomStep, maxZoomStep, tileSize, converter);
        }

        private IPixelConverter readConverter(final DataInputStream stream) throws IOException {
            final int conversionBits = stream.readInt();
            return new PixelConverter(conversionBits);
        }

        private int[][] readDistributions(final ZipFile zipFile) throws IOException {
            // TODO improve this
            final ZipEntry entry = zipFile.getEntry("distributions");
            if (entry == null) {
                return null;
            }
            applyInputStream(zipFile, entry);

            final int[][] distributions = new int[5][];
            for (int i = 0; i < 5; i++) {
                distributions[i] = readIntArray(stream, stream.readInt());
            }
            stream.close();

            return distributions;
        }

        private int[][] readNodes(final ZipFile zipFile) throws IOException {
            fireStepCommenced("Lade Nodes...");

            final ZipEntry entry = zipFile.getEntry("nodes");
            if (entry == null) {
                // TODO
                return new int[0][0];
            }
            applyInputStream(zipFile, entry);

            int count = 0;

            final int[] xPoints = new int[stream.readInt()];
            final int[] yPoints = new int[xPoints.length];
            for (count = 0; count < xPoints.length; count++) {
                xPoints[count] = stream.readInt();
                yPoints[count] = stream.readInt();
            }
            stream.close();

            return new int[][] { xPoints, yPoints };
        }

        private String[] readStrings(final ZipFile zipFile) throws IOException {
            fireStepCommenced("Lade Adressen...");

            final ZipEntry entry = zipFile.getEntry("strings");
            if (entry == null) {
                return new String[0];
            }
            applyInputStream(zipFile, entry);

            final String[] strings = new String[stream.readInt()];
            for (int count = 0; count < strings.length; count++) {
                strings[count] = stream.readUTF();
            }
            stream.close();

            return strings;
        }

        private void readElements(final ZipFile zipFile, final int[][] nodes, final int[][] distributions,
                final Map<String, IElementIterator> elementIteratorMap, Map<String, IFactory<IPointAccessor>> pointMap,
                final Map<String, IFactory<ICollectiveAccessor>> collectiveMap, final int minZoomStep)
                throws IOException {

            final String[] names = { "street", "way", "area", "building" };
            final String[] outputNames = { "Straßen", "Wege", "Gelände", "Gebäude" };

            final CollectiveAccessorFactory[] accessors = new CollectiveAccessorFactory[names.length];
            accessors[0] = new CollectiveAccessorFactory(nodes[0], nodes[1], distributions[0]) {
                @Override
                public ICollectiveAccessor create() {
                    return new StreetAccessor(data, x, y, distribution);
                }
            };
            accessors[1] = new CollectiveAccessorFactory(nodes[0], nodes[1], distributions[1]);
            accessors[2] = new CollectiveAccessorFactory(nodes[0], nodes[1], distributions[2]);
            accessors[3] = new CollectiveAccessorFactory(nodes[0], nodes[1], distributions[3]) {
                @Override
                public ICollectiveAccessor create() {
                    return new BuildingAccessor(data, x, y, distribution);
                }
            };
            for (int i = 0; i < names.length; i++) {
                fireStepCommenced("Lade " + outputNames[i] + "...");

                final ZipEntry entry = zipFile.getEntry(names[i] + "s");
                if (entry == null) {
                    // TODO
                    continue;
                }
                accessors[i].setData(readIntArray(zipFile, entry));
                collectiveMap.put(names[i], accessors[i]);

                // TODO handle missing entries!
                int[] elementData = readIntArray(zipFile, names[i] + "Data");
                int[] treeData = readIntArray(zipFile, names[i] + "Tree");
                elementIteratorMap.put(names[i], new Quadtree(treeData, elementData, minZoomStep));
            }

            fireStepCommenced("Lade Points of Interest...");

            fireStepCommenced("Lade Labels...");
            // TODO handle missing entry!
            final int[] data = readIntArray(zipFile, "labels");
            final IFactory<IPointAccessor> labelAccessorFactory = () -> {
                return new LabelAccessor(distributions[4], data);
            };
            pointMap.put("label", labelAccessorFactory);
        }

        private int[] readIntArray(final ZipFile zipFile, final String name) throws IOException {
            return readIntArray(zipFile, zipFile.getEntry(name));
        }

        private int[] readIntArray(final ZipFile zipFile, final ZipEntry entry) throws IOException {
            assert zipFile != null;

            if (entry != null) {
                final long size = entry.getSize();
                if (size != -1) {
                    applyInputStream(zipFile, entry);
                    final int[] ret = readIntArray(stream, (int) (size / 4));
                    stream.close();
                    return ret;
                }
            }

            // TODO what to do here?
            return new int[0];
        }

        private int[] readIntArray(final DataInputStream stream, final int size) throws IOException {
            final int[] ret = new int[size];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = stream.readInt();
            }

            return ret;
        }
    }

    private class ProgressableInputStream extends FilterInputStream {

        public ProgressableInputStream(final InputStream inputStream) throws IOException {
            super(inputStream);
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