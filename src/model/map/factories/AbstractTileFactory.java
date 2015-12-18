package model.map.factories;

import java.io.IOException;

import model.CompressedInputStream;
import model.elements.IArea;
import model.elements.IBuilding;
import model.elements.Label;
import model.elements.POI;
import model.elements.IStreet;
import model.elements.IWay;
import model.map.EmptyTile;
import model.map.ITile;

public abstract class AbstractTileFactory implements ITileFactory {

    protected static final ITile EMPTY_TILE = new EmptyTile(-1, -1, -1);
    protected final CompressedInputStream reader;
    protected final POI[] pois;
    protected final IStreet[] iStreets;
    protected final IWay[] ways;
    protected final IBuilding[] iBuildings;
    protected final IArea[] iAreas;
    protected final Label[] labels;

    public AbstractTileFactory(final CompressedInputStream reader, final POI[] pois, final IStreet[] streets,
            final IWay[] ways, final IBuilding[] buildings, final IArea[] areas, final Label[] labels) {
        this.reader = reader;

        this.pois = pois;
        this.iStreets = streets;
        this.ways = ways;
        this.iBuildings = buildings;
        this.iAreas = areas;
        this.labels = labels;
    }

    protected <T> void fillElements(final T[] source, final T[] destination) throws IOException {
        int id = 0;
        for (int i = 0; i < destination.length; i++) {
            id += reader.readCompressedInt();
            destination[i] = source[id];
        }
    }

    protected int[] readIntArray() throws IOException {
        final int[] ret = new int[reader.readCompressedInt()];

        int id = 0;
        for (int i = 0; i < ret.length; i++) {
            id += reader.readCompressedInt();
            ret[i] = id;
        }

        return ret;
    }
}
