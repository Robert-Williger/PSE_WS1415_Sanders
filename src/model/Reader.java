package model;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import model.TextProcessor.Entry;
import model.map.BoundingBoxQuadtreePolicy;
import model.map.CollectiveAccessorFactory;
import model.map.CollisionlessQuadtree;
import model.map.ICollisionPolicy;
import model.map.IMapManager;
import model.map.IMapState;
import model.map.IPixelConverter;
import model.map.IQuadtreePolicy;
import model.map.IElementIterator;
import model.map.MapManager;
import model.map.MapState;
import model.map.PixelConverter;
import model.map.Quadtree;
import model.map.accessors.BuildingAccessor;
import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.IPointAccessor;
import model.map.accessors.IStringAccessor;
import model.map.accessors.ITileAccessor;
import model.map.accessors.LabelAccessor;
import model.map.accessors.StreetAccessor;
import model.map.accessors.StringAccessor;
import model.map.accessors.TileAccessor;
import model.routing.DirectedGraph;
import model.routing.IDirectedGraph;
import model.routing.IRouteManager;
import model.routing.RouteManager;

public class Reader implements IReader {

    private final List<IProgressListener> list;

    private DataInputStream stream;

    private IDirectedGraph graph;
    private IMapManager mapManager;
    private IRouteManager routeManager;
    private ITextProcessor textProcessor;
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

        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
        } catch (final IOException e) {
            fireErrorOccured("Beim Lesen der Datei ist ein Fehler aufgetreten.");
            return false;
        }

        totalBytes = (long) Math.ceil(getTotalBytes(zipFile) / 100.0);
        currentBytes = 0;
        progress = -1;

        try {
            graph = readGraph(zipFile);
            mapManager = new MapManagerReader().readMapManager(zipFile);
            textProcessor = new IndexReader().readIndex(zipFile);
        } catch (final Exception e) {
            e.printStackTrace();
            if (!canceled) {
                fireErrorOccured("Beim Kartenimport ist ein Fehler aufgetreten.");
            }

            return false;
        }

        routeManager = new RouteManager(graph, mapManager);

        return true;
    }

    @Override
    public IMapManager getMapManager() {
        return mapManager;
    }

    @Override
    public IRouteManager getRouteManager() {
        return routeManager;
    }

    @Override
    public ITextProcessor getTextProcessor() {
        return textProcessor;
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

    /*
     * Reads the Graph-section of the map file and generates the Graph.
     */
    private IDirectedGraph readGraph(final ZipFile zipFile) throws IOException {
        fireStepCommenced("Lade Graph...");

        final ZipEntry entry = zipFile.getEntry("graph");
        if (entry != null) {
            applyInputStream(zipFile, entry);
            final int nodeCount = stream.readInt();
            final int edgeCount = stream.readInt();
            final int onewayCount = stream.readInt();

            final int[] firstNodes = new int[edgeCount];
            final int[] secondNodes = new int[edgeCount];
            final int[] weights = new int[edgeCount];

            for (int i = 0; i < edgeCount; i++) {
                firstNodes[i] = stream.readInt();
                secondNodes[i] = stream.readInt();
                weights[i] = stream.readInt();
            }

            stream.close();

            return new DirectedGraph(nodeCount, onewayCount, firstNodes, secondNodes, weights);
        }

        return new DirectedGraph(0, 0, new int[0], new int[0], new int[0]);
    }

    private class IndexReader {
        public ITextProcessor readIndex(final ZipFile zipFile) throws IOException {
            fireStepCommenced("Lade Index...");

            final Collection<Entry> entries = readStreets(zipFile);
            return new TextProcessor(entries, mapManager);
        }

        private Collection<Entry> readStreets(final ZipFile zipFile) throws IOException {
            applyInputStream(zipFile, zipFile.getEntry("index"));

            final String[] cities = readCities();

            int maxCityCount = stream.readInt();

            final List<TextProcessor.Entry> list = new ArrayList<>();
            for (int cityCount = 1; cityCount <= maxCityCount; ++cityCount) {
                final int n = stream.readInt();

                for (int j = 0; j < n; j++) {
                    for (int k = 0; k < cityCount; k++) {
                        final int street = stream.readInt();
                        final int city = stream.readInt();
                        final String cityName = city != -1 ? cities[city] : "";

                        list.add(new TextProcessor.Entry(cityName, street));
                    }
                }
            }

            stream.close();

            return list;
        }

        private String[] readCities() throws IOException {
            final String[] cities = new String[stream.readInt()];

            for (int i = 0; i < cities.length; i++) {
                cities[i] = stream.readUTF();
            }
            return cities;
        }

    }

    private class MapManagerReader {

        public IMapManager readMapManager(final ZipFile zipFile) throws IOException {
            fireStepCommenced("Lade Header...");

            final IMapState state = readMapState(zipFile);

            final String[] strings = readStrings(zipFile);
            final int[][] nodes = readNodes(zipFile);
            final Map<String, IElementIterator> elementIteratorMap = new HashMap<>();
            final Map<String, IFactory<ICollectiveAccessor>> collectiveMap = new HashMap<>();
            final Map<String, IFactory<IPointAccessor>> pointMap = new HashMap<>();
            final int labels = readElements(zipFile, nodes, elementIteratorMap, pointMap, collectiveMap,
                    state.getMinZoom());

            final int zoomOffset = (int) Math.ceil(log2(Math.min(state.getCoordMapWidth(), state.getCoordMapHeight())));
            final int coordMapSize = (1 << zoomOffset);

            final IElementIterator iterator = createLabelQuadtree(pointMap.get("label").create(),
                    new StringAccessor(strings), labels, state.getMaxZoom() - state.getMinZoom() + 1,
                    state.getMinZoom(), state.getConverter(), coordMapSize);
            elementIteratorMap.put("label", iterator);

            final IFactory<ITileAccessor> tileFactory = new IFactory<ITileAccessor>() {
                @Override
                public ITileAccessor create() {
                    return new TileAccessor(elementIteratorMap, state);
                }
            };
            return new MapManager(pointMap, collectiveMap, tileFactory, strings, state);
        }

        private double log2(final double value) {
            return (Math.log(value) / Math.log(2));
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

        private int readElements(final ZipFile zipFile, final int[][] nodes,
                final Map<String, IElementIterator> elementIteratorMap, Map<String, IFactory<IPointAccessor>> pointMap,
                final Map<String, IFactory<ICollectiveAccessor>> collectiveMap, final int minZoomStep)
                throws IOException {

            final String[] names = { "street", "way", "area", "building", "label" };
            final String[] outputNames = { "Straßen", "Wege", "Gelände", "Gebäude", "Labels" };
            final int[][] distributions = new int[names.length][];
            final int[][] addresses = new int[names.length][];

            for (int i = 0; i < names.length; i++) {
                distributions[i] = readIntArray(zipFile, names[i] + "Distribution");
                addresses[i] = readIntArray(zipFile, names[i] + "Addresses");
            }

            final CollectiveAccessorFactory[] accessors = new CollectiveAccessorFactory[names.length - 1];
            accessors[0] = new CollectiveAccessorFactory(distributions[0], addresses[0], nodes[0], nodes[1]) {
                @Override
                public ICollectiveAccessor create() {
                    return new StreetAccessor(distribution, data, addresses, x, y);
                }
            };
            accessors[1] = new CollectiveAccessorFactory(distributions[1], addresses[1], nodes[0], nodes[1]);
            accessors[2] = new CollectiveAccessorFactory(distributions[2], addresses[2], nodes[0], nodes[1]);
            accessors[3] = new CollectiveAccessorFactory(distributions[3], addresses[3], nodes[0], nodes[1]) {
                @Override
                public ICollectiveAccessor create() {
                    return new BuildingAccessor(distribution, data, addresses, x, y);
                }
            };
            for (int i = 0; i < names.length - 1; i++) {
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
                return new LabelAccessor(distributions[4], data, 3);
            };
            pointMap.put("label", labelAccessorFactory);
            return data.length / 3;
        }

        private IElementIterator createLabelQuadtree(final IPointAccessor lAccessor, final IStringAccessor sAccessor,
                final int labels, final int zoomSteps, final int minZoomStep, final IPixelConverter converter,
                final int coordMapSize) {

            final Font font = new Font("TimesRoman", Font.PLAIN, 18);
            final FontRenderContext c = new FontRenderContext(new AffineTransform(), true, true);
            final Rectangle[][] lBounds = new Rectangle[zoomSteps][labels];

            for (int i = 0; i < labels; i++) {
                lAccessor.setID(i);
                final Rectangle2D bounds = font.getStringBounds(sAccessor.getString(lAccessor.getAttribute("name")), c);
                final int pw = (int) Math.ceil(bounds.getWidth());
                final int ph = (int) Math.ceil(bounds.getHeight());
                for (int h = 0; h < zoomSteps; h++) {
                    final int cw = converter.getCoordDistance(pw, h + minZoomStep);
                    final int ch = converter.getCoordDistance(ph, h + minZoomStep);
                    lBounds[h][i] = new Rectangle(lAccessor.getX() - cw / 2, lAccessor.getY() - ch / 2, cw, ch);
                }
            }
            final ICollisionPolicy cp = (e1, e2, height) -> lBounds[height][e1].intersects(lBounds[height][e2]);
            final IQuadtreePolicy qp = new BoundingBoxQuadtreePolicy(lBounds, 8);
            return new CollisionlessQuadtree(labels, qp, cp, coordMapSize, minZoomStep);
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