package model.map.factories;

import java.io.IOException;

import model.CompressedInputStream;
import model.elements.Area;
import model.elements.Building;
import model.elements.Label;
import model.elements.POI;
import model.elements.Street;
import model.elements.Way;
import model.map.ITile;
import model.map.Tile;

public class DefaultTileFactory extends AbstractTileFactory implements ITileFactory {

    private static final POI[] EMPTY_POIS;
    private static final Street[] EMPTY_STREETS;
    private static final Way[] EMPTY_WAYS;
    private static final Building[] EMPTY_BUILDINGS;
    private static final Area[] EMPTY_AREAS;
    private static final Label[] EMPTY_LABELS;

    public DefaultTileFactory(final CompressedInputStream reader, final POI[] pois, final Street[] streets,
            final Way[] ways, final Building[] buildings, final Area[] areas, final Label[] labels) {
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
            final Street[] tileStreets;
            final Way[] tileWays;
            final Building[] tileBuildings;
            final Area[] tileAreas;
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
                tileStreets = new Street[reader.readCompressedInt()];
                fillElements(streets, tileStreets);
            }

            if ((flags >> 2 & 1) == 0) {
                tileWays = EMPTY_WAYS;
            } else {
                tileWays = new Way[reader.readCompressedInt()];
                fillElements(ways, tileWays);
            }

            if ((flags >> 3 & 1) == 0) {
                tileBuildings = EMPTY_BUILDINGS;
            } else {
                tileBuildings = new Building[reader.readCompressedInt()];
                fillElements(buildings, tileBuildings);
            }

            if ((flags >> 4 & 1) == 0) {
                tileAreas = EMPTY_AREAS;
            } else {
                tileAreas = new Area[reader.readCompressedInt()];
                fillElements(areas, tileAreas);
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
        EMPTY_STREETS = new Street[0];
        EMPTY_WAYS = new Way[0];
        EMPTY_BUILDINGS = new Building[0];
        EMPTY_AREAS = new Area[0];
        EMPTY_LABELS = new Label[0];
    }
}
