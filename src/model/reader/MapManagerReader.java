package model.reader;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import model.IFactory;
import model.map.CollectiveAccessorFactory;
import model.map.IElementIterator;
import model.map.IMapManager;
import model.map.IMapState;
import model.map.IPixelConverter;
import model.map.MapManager;
import model.map.MapState;
import model.map.PixelConverter;
import model.map.Quadtree;
import model.map.accessors.BuildingAccessor;
import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.IPointAccessor;
import model.map.accessors.ITileAccessor;
import model.map.accessors.LabelAccessor;
import model.map.accessors.POIAccessor;
import model.map.accessors.StreetAccessor;
import model.map.accessors.TileAccessor;
import model.reader.Reader.ReaderContext;

class MapManagerReader {
    private IMapManager mapManager;

    public MapManagerReader() {
        super();
    }

    public IMapManager getMapManager() {
        return mapManager;
    }

    public void readMapManager(final ReaderContext readerContext) throws IOException {
        final IMapState state = readMapState(readerContext);
        final String[] strings = readStrings(readerContext);
        final Map<String, IElementIterator> elementIteratorMap = new HashMap<>();
        final Map<String, IFactory<ICollectiveAccessor>> collectiveMap = new HashMap<>();
        final Map<String, IFactory<IPointAccessor>> pointMap = new HashMap<>();
        readElements(readerContext, elementIteratorMap, pointMap, collectiveMap, state.getMinZoom());

        final IFactory<ITileAccessor> tileFactory = () -> new TileAccessor(elementIteratorMap, state);

        mapManager = new MapManager(pointMap, collectiveMap, tileFactory, strings, state);
    }

    private IMapState readMapState(final ReaderContext readerContext) throws IOException {
        final DataInputStream stream = readerContext.createInputStream("header");
        if (stream != null) {
            final int conversionBits = stream.readInt();
            final IPixelConverter converter = new PixelConverter(conversionBits);

            final int width = stream.readInt();
            final int height = stream.readInt();
            final int minZoomStep = stream.readInt();
            final int maxZoomStep = stream.readInt();
            final int tileSize = stream.readInt();

            stream.close();

            return new MapState(width, height, minZoomStep, maxZoomStep, tileSize, converter);
        }

        return new MapState(1, 1, 0, 1, 1, new PixelConverter(1));
    }

    private String[] readStrings(final ReaderContext readerContext) throws IOException {
        final DataInputStream stream = readerContext.createInputStream("strings");
        if (stream != null) {
            final String[] strings = new String[stream.readInt()];
            for (int count = 0; count < strings.length; count++) {
                strings[count] = stream.readUTF();
            }
            stream.close();

            return strings;
        }

        return new String[0];
    }

    private void readElements(final ReaderContext readerContext, final Map<String, IElementIterator> elementIteratorMap,
            Map<String, IFactory<IPointAccessor>> pointMap,
            final Map<String, IFactory<ICollectiveAccessor>> collectiveMap, final int minZoomStep) throws IOException {
        // TODO handle missing entries!
        final String[] names = { "street", "area", "building", "label", "poi" };
        final int[][] distributions = new int[names.length][];
        final int[][] addresses = new int[names.length][];

        for (int i = 0; i < names.length; i++) {
            distributions[i] = readIntArray(readerContext, names[i] + "Distribution");
            addresses[i] = readIntArray(readerContext, names[i] + "Addresses");
        }

        // TODO improve this
        final CollectiveAccessorFactory[] accessors = new CollectiveAccessorFactory[4];
        accessors[0] = new CollectiveAccessorFactory(distributions[0], addresses[0]) {
            @Override
            public ICollectiveAccessor create() {
                return new StreetAccessor(distribution, data, addresses);
            }
        };
        accessors[1] = new CollectiveAccessorFactory(distributions[1], addresses[1]);
        accessors[2] = new CollectiveAccessorFactory(distributions[2], addresses[2]) {
            @Override
            public ICollectiveAccessor create() {
                return new BuildingAccessor(distribution, data, addresses);
            }
        };
        for (int i = 0; i < 3; i++) {
            collectiveMap.put(names[i], accessors[i]);
            accessors[i].setData(readIntArray(readerContext, names[i] + "s"));
            int[] treeData = readIntArray(readerContext, names[i] + "Tree");
            elementIteratorMap.put(names[i], new Quadtree(treeData, minZoomStep));
        }
        final int[] labelData = readIntArray(readerContext, names[3] + "s");
        pointMap.put(names[3], () -> new LabelAccessor(distributions[3], labelData, 3));
        elementIteratorMap.put(names[3],
                new Quadtree(readIntArray(readerContext, names[3] + "Tree"), minZoomStep));

        final int[] poiData = readIntArray(readerContext, names[4] + "s");
        pointMap.put(names[4], () -> new POIAccessor(distributions[4], poiData, 2));
        elementIteratorMap.put(names[4],
                new Quadtree(readIntArray(readerContext, names[4] + "Tree"), minZoomStep));
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