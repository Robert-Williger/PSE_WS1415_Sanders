package model.map.factories;

import java.io.IOException;

import model.CompressedInputStream;
import model.elements.IArea;
import model.elements.IBuilding;
import model.elements.Label;
import model.elements.POI;
import model.elements.IStreet;
import model.elements.IWay;
import model.map.ITile;
import model.map.Tile;

public class DefaultTileFactory extends AbstractTileFactory implements ITileFactory {

    private static final POI[] EMPTY_POIS;
    private static final IStreet[] EMPTY_STREETS;
    private static final IWay[] EMPTY_WAYS;
    private static final IBuilding[] EMPTY_BUILDINGS;
    private static final IArea[] EMPTY_AREAS;
    private static final Label[] EMPTY_LABELS;

    public DefaultTileFactory(final CompressedInputStream reader, final POI[] pois, final IStreet[] streets,
            final IWay[] ways, final IBuilding[] buildings, final IArea[] areas, final Label[] labels) {
        super(reader, pois, streets, ways, buildings, areas, labels);
    }

    @Override
    public ITile createTile(final int row, final int column, final int zoom) throws IOException {
        byte flags = reader.readByte();

        final ITile tile;
        if (flags == 0) {
            tile = EMPTY_TILE;
        } else {
            final POI[] tilePOIs;
            final IStreet[] tileStreets;
            final IWay[] tileWays;
            final IBuilding[] tileBuildings;
            final IArea[] tileAreas;
            final Label[] tileLabels;

            if ((flags & 1) == 0) {
                tilePOIs = EMPTY_POIS;
            } else {
                tilePOIs = new POI[reader.readCompressedInt()];
                fillElements(pois, tilePOIs);
            }

            if ((flags >> 1 & 1) == 0) {
                tileStreets = EMPTY_STREETS;
            } else {
                tileStreets = new IStreet[reader.readCompressedInt()];
                fillElements(iStreets, tileStreets);
            }

            if ((flags >> 2 & 1) == 0) {
                tileWays = EMPTY_WAYS;
            } else {
                tileWays = new IWay[reader.readCompressedInt()];
                fillElements(ways, tileWays);
            }

            if ((flags >> 3 & 1) == 0) {
                tileBuildings = EMPTY_BUILDINGS;
            } else {
                tileBuildings = new IBuilding[reader.readCompressedInt()];
                fillElements(iBuildings, tileBuildings);
            }

            if ((flags >> 4 & 1) == 0) {
                tileAreas = EMPTY_AREAS;
            } else {
                tileAreas = new IArea[reader.readCompressedInt()];
                fillElements(iAreas, tileAreas);
            }

            if ((flags >> 5 & 1) == 0) {
                tileLabels = EMPTY_LABELS;
            } else {
                tileLabels = new Label[reader.readCompressedInt()];
                fillElements(labels, tileLabels);
            }

            tile = new Tile(zoom, row, column, tileWays, tileStreets, tileAreas, tileBuildings, tilePOIs, tileLabels);
        }

        return tile;
    }

    static {
        EMPTY_POIS = new POI[0];
        EMPTY_STREETS = new IStreet[0];
        EMPTY_WAYS = new IWay[0];
        EMPTY_BUILDINGS = new IBuilding[0];
        EMPTY_AREAS = new IArea[0];
        EMPTY_LABELS = new Label[0];
    }
}
