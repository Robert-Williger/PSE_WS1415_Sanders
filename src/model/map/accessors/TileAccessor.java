package model.map.accessors;

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.function.LongConsumer;

import model.map.IPixelConverter;
import model.map.IQuadtree;

public class TileAccessor implements ITileAccessor {

    private final java.util.Map<String, IQuadtree> map;
    private final IPixelConverter converter;
    private final int tileSize;
    private int row;
    private int column;
    private int zoom;

    public TileAccessor(final java.util.Map<String, IQuadtree> map, final IPixelConverter converter,
            final int tileSize) {
        this.map = map;
        this.converter = converter;
        this.tileSize = tileSize;
    }

    @Override
    public void setID(final long id) {
        final int zoom = (int) (id >> 58);
        final int row = (int) ((id >> 29) & 0x1FFFFFFFL);
        final int column = (int) (id & 0x1FFFFFFFL);
        setRCZ(row, column, zoom);
    }

    @Override
    public void setRCZ(final int row, final int column, final int zoom) {
        this.row = row;
        this.column = column;
        this.zoom = zoom;
    }

    @Override
    public PrimitiveIterator.OfLong getElements(final String identifier) {
        final IQuadtree tree = map.get(identifier);
        if (tree != null) {
            return tree.iterator(row, column, zoom);
        }

        return Arrays.stream(new long[0]).iterator();
    }

    @Override
    public void forEach(String identifier, LongConsumer consumer) {
        final IQuadtree tree = map.get(identifier);
        if (tree != null) {
            tree.forEach(row, column, zoom, consumer);
        }
    }

    @Override
    public int getZoom() {
        return zoom;
    }

    @Override
    public int getX() {
        return column * converter.getCoordDistance(tileSize, zoom);
    }

    @Override
    public int getY() {
        return row * converter.getCoordDistance(tileSize, zoom);
    }

}
