package model.reader;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import model.IFactory;
import model.map.IElementIterator;
import model.map.IMapBounds;
import model.map.IMapManager;
import model.map.IPixelMapping;
import model.map.MapBounds;
import model.map.MapManager;
import model.map.PixelConverter;
import model.map.Quadtree;
import model.map.accessors.CollectiveAccessor;
import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.IPointAccessor;
import model.map.accessors.IStringAccessor;
import model.map.accessors.ITileIdConversion;
import model.map.accessors.POIAccessor;
import model.map.accessors.StringAccessor;
import model.map.accessors.TileConversion;
import model.reader.Reader.ReaderContext;

class MapManagerReader {
    private IMapManager mapManager;

    private IMapBounds bounds;
    private IPixelMapping mapping;
    private int tileSize;

    public MapManagerReader() {
        super();
    }

    public IMapManager getMapManager() {
        return mapManager;
    }

    public void readMapManager(final ReaderContext readerContext) throws IOException {
        readHeader(readerContext);
        final IStringAccessor stringAccessor = readStringAccessor(readerContext);
        final Map<String, IElementIterator> elementIteratorMap = new HashMap<>();
        final Map<String, IFactory<ICollectiveAccessor>> collectiveMap = new HashMap<>();
        final Map<String, IFactory<IPointAccessor>> pointMap = new HashMap<>();
        readElements(readerContext, elementIteratorMap, pointMap, collectiveMap, bounds.getMinZoom());

        final ITileIdConversion tileConversion = new TileConversion();

        mapManager = new MapManager(pointMap, collectiveMap, elementIteratorMap, stringAccessor, tileConversion, bounds,
                mapping, tileSize);
    }

    private void readHeader(final ReaderContext readerContext) throws IOException {
        final DataInputStream stream = readerContext.createInputStream("header");
        if (stream != null) {
            mapping = new PixelConverter(stream.readInt());

            final int x = stream.readInt();
            final int y = stream.readInt();
            final int width = stream.readInt();
            final int height = stream.readInt();
            final int minZoomStep = stream.readInt();
            final int maxZoomStep = stream.readInt();
            tileSize = stream.readInt();

            stream.close();

            bounds = new MapBounds(x, y, width, height, minZoomStep, maxZoomStep);
        } else {
            bounds = new MapBounds(0, 0, 1, 1, 0, 1);
            tileSize = 1;
            mapping = new PixelConverter(1);
        }
    }

    private IStringAccessor readStringAccessor(final ReaderContext readerContext) throws IOException {
        final DataInputStream stream = readerContext.createInputStream("strings");
        if (stream != null) {
            final String[] strings = new String[stream.readInt()];
            for (int count = 0; count < strings.length; count++) {
                strings[count] = stream.readUTF();
            }
            stream.close();

            return new StringAccessor(strings);
        }

        return new StringAccessor();
    }

    private void readElements(final ReaderContext readerContext, final Map<String, IElementIterator> elementIteratorMap,
            Map<String, IFactory<IPointAccessor>> pointMap,
            final Map<String, IFactory<ICollectiveAccessor>> collectiveMap, final int minZoomStep) throws IOException {
        // TODO handle missing entries!

        for (final String name : readStringArray(readerContext, "collectiveElements")) {
            String[] attributes = readStringArray(readerContext, name + "Attributes");
            int[] distribution = readIntArray(readerContext, name + "Distribution");
            int[] address = readIntArray(readerContext, name + "Addresses");
            int[] data = readIntArray(readerContext, name + "Data");
            int[] tree = readIntArray(readerContext, name + "Tree");
            elementIteratorMap.put(name, new Quadtree(tree, minZoomStep));
            collectiveMap.put(name, () -> new CollectiveAccessor(attributes, distribution, data, address));
        }

        for (final String name : readStringArray(readerContext, "pointElements")) {
            String[] attributes = readStringArray(readerContext, name + "Attributes");
            int[] distribution = readIntArray(readerContext, name + "Distribution");
            int[] data = readIntArray(readerContext, name + "Data");
            int[] tree = readIntArray(readerContext, name + "Tree");
            pointMap.put(name, () -> new POIAccessor(attributes, distribution, data));
            elementIteratorMap.put(name, new Quadtree(tree, minZoomStep));
        }
    }

    private String[] readStringArray(final ReaderContext readerContext, final String entryName) throws IOException {
        final DataInputStream stream = readerContext.createInputStream(entryName);
        if (stream == null)
            return new String[0];

        final String[] ret = new String[stream.readInt()];
        for (int i = 0; i < ret.length; ++i)
            ret[i] = stream.readUTF();

        stream.close();
        return ret;

    }

    private int[] readIntArray(final ReaderContext readerContext, final String entryName) throws IOException {
        final long size = readerContext.getSize(entryName);
        if (size != -1) {
            final DataInputStream stream = readerContext.createInputStream(entryName);
            final int[] ret = readIntArray(stream, (int) (size / 4));
            stream.close();
            return ret;
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