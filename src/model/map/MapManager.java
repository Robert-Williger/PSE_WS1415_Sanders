package model.map;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.IntConsumer;

import model.IFactory;
import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.IPointAccessor;
import model.map.accessors.IStringAccessor;
import model.map.accessors.StringAccessor;

public class MapManager implements IMapManager {
    private final java.util.Map<String, IFactory<IPointAccessor>> pointMap;
    private final java.util.Map<String, IFactory<ICollectiveAccessor>> collectiveMap;
    private final IStringAccessor stringAccessor;

    private final ITileState tileState;

    private final IMapBounds mapBounds;
    private final IMapSection mapSection;
    private final IPixelMapping pixelMapping;

    private final IAddressFinder addressFinder;

    private final java.util.Map<String, IElementIterator> iteratorMap;

    public MapManager() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>(), new StringAccessor(),
                new MapBounds(0, 0, 1 << 21, 1 << 21, 0, 19), new PixelConverter(21), 256);
    }

    public MapManager(final java.util.Map<String, IFactory<IPointAccessor>> pointMap,
            final java.util.Map<String, IFactory<ICollectiveAccessor>> collectiveMap,
            java.util.Map<String, IElementIterator> iteratorMap, final IStringAccessor stringAccessor,
            final IMapBounds mapBounds, final IPixelMapping mapping, final int tileSize) {
        this.pointMap = pointMap;
        this.collectiveMap = collectiveMap;
        this.stringAccessor = stringAccessor;
        this.iteratorMap = iteratorMap;

        this.mapBounds = mapBounds;
        this.pixelMapping = mapping;
        this.mapSection = new MapSection();

        this.tileState = new TileState(mapSection, mapping, tileSize);

        IElementIterator streetIterator = getElementIterator("street");
        IElementIterator buildingIterator = getElementIterator("building");
        ICollectiveAccessor buildingAccessor = createCollectiveAccessor("building");
        ICollectiveAccessor streetAccessor = createCollectiveAccessor("street");
        this.addressFinder = new AddressFinder(streetIterator, buildingIterator, buildingAccessor, streetAccessor,
                stringAccessor, mapBounds, mapSection, pixelMapping, tileState);
    }

    @Override
    public IStringAccessor getStringAccessor() {
        return stringAccessor;
    }

    @Override
    public ICollectiveAccessor createCollectiveAccessor(final String identifier) {
        return collectiveMap.getOrDefault(identifier, new NullFactory<>()).create();
    }

    @Override
    public IPointAccessor createPointAccessor(final String identifier) {
        return pointMap.getOrDefault(identifier, new NullFactory<>()).create();
    }

    @Override
    public IElementIterator getElementIterator(String identifier) {
        return iteratorMap.getOrDefault(identifier, new EmptyElementIterator());
    }

    @Override
    public IMapSection getMapSection() {
        return mapSection;
    }

    @Override
    public IMapBounds getMapBounds() {
        return mapBounds;
    }

    @Override
    public IPixelMapping getPixelMapping() {
        return pixelMapping;
    }

    @Override
    public ITileState getTileState() {
        return tileState;
    }

    @Override
    public IAddressFinder getAddressFinder() {
        return addressFinder;
    }

    private static class NullFactory<T> implements IFactory<T> {

        @Override
        public T create() {
            return null;
        }

    }

    private static class EmptyElementIterator implements IElementIterator {

        @Override
        public OfInt iterator(int row, int column, int zoom) {
            return Arrays.stream(new int[0]).iterator();
        }

        @Override
        public void forEach(int row, int column, int zoom, IntConsumer consumer) {}

        @Override
        public IElementIterator filter(Predicate f) {
            return this;
        }

        @Override
        public IElementIterator sort(Comparator<Integer> idComparator) {
            return this;
        }

    }

}