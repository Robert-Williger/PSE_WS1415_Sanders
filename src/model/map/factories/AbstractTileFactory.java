package model.map.factories;

import java.io.IOException;

import model.CompressedInputStream;
import model.elements.Area;
import model.elements.Building;
import model.elements.Label;
import model.elements.POI;
import model.elements.Street;
import model.elements.Way;
import model.map.EmptyTile;
import model.map.ITile;

public abstract class AbstractTileFactory implements ITileFactory {

    protected static final ITile EMPTY_TILE = new EmptyTile(-1, -1, -1);
    protected final CompressedInputStream reader;
    protected final POI[] pois;
    protected final Street[] streets;
    protected final Way[] ways;
    protected final Building[] buildings;
    protected final Area[] areas;
    protected final Label[] labels;

    public AbstractTileFactory(final CompressedInputStream reader, final POI[] pois, final Street[] streets,
            final Way[] ways, final Building[] buildings, final Area[] areas, final Label[] labels) {
        this.reader = reader;

        this.pois = pois;
        this.streets = streets;
        this.ways = ways;
        this.buildings = buildings;
        this.areas = areas;
        this.labels = labels;
    }

    protected <T> void fillElements(final T[] source, final T[] destination) throws IOException {
        int last = 0;
        for (int i = 0; i < destination.length; i++) {
            int current = reader.readCompressedInt() + last;
            destination[i] = source[current];
            last = current;
        }
    }
}
