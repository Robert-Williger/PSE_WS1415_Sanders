package model.map.factories;

import java.util.Iterator;
import java.io.IOException;

import util.Arrays;
import model.CompressedInputStream;
import model.elements.Area;
import model.elements.Building;
import model.elements.POI;
import model.elements.Street;
import model.elements.Way;

import model.map.AbstractTile;
import model.map.ITile;

public class StorageTileFactory extends AbstractTileFactory implements ITileFactory {

    private final ITileFactory[] factories;

    public StorageTileFactory(final CompressedInputStream reader, final POI[] pois, final Street[] streets,
            final Way[] ways, final Building[] buildings, final Area[] areas) {
        super(reader, pois, streets, ways, buildings, areas);

        factories = new ITileFactory[32];
        for (int i = 0; i < 32; i++) {
            factories[0] = new TileFactory0();
            factories[1] = new TileFactory1();
            factories[2] = new TileFactory2();
            factories[3] = new TileFactory3();
            factories[4] = new TileFactory4();
            factories[5] = new TileFactory5();
            factories[6] = new TileFactory6();
            factories[7] = new TileFactory7();
            factories[8] = new TileFactory8();
            factories[9] = new TileFactory9();
            factories[10] = new TileFactory10();
            factories[11] = new TileFactory11();
            factories[12] = new TileFactory12();
            factories[13] = new TileFactory13();
            factories[14] = new TileFactory14();
            factories[15] = new TileFactory15();
            factories[16] = new TileFactory16();
            factories[17] = new TileFactory17();
            factories[18] = new TileFactory18();
            factories[19] = new TileFactory19();
            factories[20] = new TileFactory20();
            factories[21] = new TileFactory21();
            factories[22] = new TileFactory22();
            factories[23] = new TileFactory23();
            factories[24] = new TileFactory24();
            factories[25] = new TileFactory25();
            factories[26] = new TileFactory26();
            factories[27] = new TileFactory27();
            factories[28] = new TileFactory28();
            factories[29] = new TileFactory29();
            factories[30] = new TileFactory30();
            factories[31] = new TileFactory31();
        }
    }

    @Override
    public ITile createTile(int row, int column, int zoom) throws IOException {
        return factories[reader.readByte()].createTile(zoom, row, column);
    }

    private static class TileFactory0 implements ITileFactory {
        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            return EMPTY_TILE;
        }
    }

    private class TileFactory1 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            return new Tile1(tilepois, zoom, row, column);
        }
    }

    private static class Tile1 extends AbstractTile {

        private final POI[] pois;

        public Tile1(final POI[] pois, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator();
        }
    }

    private class TileFactory2 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            Street[] tilestreets = new Street[reader.readCompressedInt()];
            fillElements(streets, tilestreets);
            return new Tile2(tilestreets, zoom, row, column);
        }
    }

    private static class Tile2 extends AbstractTile {

        private final Street[] streets;

        public Tile2(final Street[] streets, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.streets = streets;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator(streets);
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator();
        }
    }

    private class TileFactory3 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            Street[] tilestreets = new Street[reader.readCompressedInt()];
            fillElements(streets, tilestreets);
            return new Tile3(tilepois, tilestreets, zoom, row, column);
        }
    }

    private static class Tile3 extends AbstractTile {

        private final POI[] pois;
        private final Street[] streets;

        public Tile3(final POI[] pois, final Street[] streets, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.streets = streets;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator(streets);
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator();
        }
    }

    private class TileFactory4 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            Way[] tileways = new Way[reader.readCompressedInt()];
            fillElements(ways, tileways);
            return new Tile4(tileways, zoom, row, column);
        }
    }

    private static class Tile4 extends AbstractTile {

        private final Way[] ways;

        public Tile4(final Way[] ways, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.ways = ways;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator();
        }
    }

    private class TileFactory5 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            Way[] tileways = new Way[reader.readCompressedInt()];
            fillElements(ways, tileways);
            return new Tile5(tilepois, tileways, zoom, row, column);
        }
    }

    private static class Tile5 extends AbstractTile {

        private final POI[] pois;
        private final Way[] ways;

        public Tile5(final POI[] pois, final Way[] ways, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.ways = ways;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator();
        }
    }

    private class TileFactory6 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            Street[] tilestreets = new Street[reader.readCompressedInt()];
            fillElements(streets, tilestreets);
            Way[] tileways = new Way[reader.readCompressedInt()];
            fillElements(ways, tileways);
            return new Tile6(tilestreets, tileways, zoom, row, column);
        }
    }

    private static class Tile6 extends AbstractTile {

        private final Street[] streets;
        private final Way[] ways;

        public Tile6(final Street[] streets, final Way[] ways, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.streets = streets;
            this.ways = ways;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator(streets);
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator();
        }
    }

    private class TileFactory7 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            Street[] tilestreets = new Street[reader.readCompressedInt()];
            fillElements(streets, tilestreets);
            Way[] tileways = new Way[reader.readCompressedInt()];
            fillElements(ways, tileways);
            return new Tile7(tilepois, tilestreets, tileways, zoom, row, column);
        }
    }

    private static class Tile7 extends AbstractTile {

        private final POI[] pois;
        private final Street[] streets;
        private final Way[] ways;

        public Tile7(final POI[] pois, final Street[] streets, final Way[] ways, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.streets = streets;
            this.ways = ways;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator(streets);
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator();
        }
    }

    private class TileFactory8 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            Building[] tilebuildings = new Building[reader.readCompressedInt()];
            fillElements(buildings, tilebuildings);
            return new Tile8(tilebuildings, zoom, row, column);
        }
    }

    private static class Tile8 extends AbstractTile {

        private final Building[] buildings;

        public Tile8(final Building[] buildings, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.buildings = buildings;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator(buildings);
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator();
        }
    }

    private class TileFactory9 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            Building[] tilebuildings = new Building[reader.readCompressedInt()];
            fillElements(buildings, tilebuildings);
            return new Tile9(tilepois, tilebuildings, zoom, row, column);
        }
    }

    private static class Tile9 extends AbstractTile {

        private final POI[] pois;
        private final Building[] buildings;

        public Tile9(final POI[] pois, final Building[] buildings, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.buildings = buildings;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator(buildings);
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator();
        }
    }

    private class TileFactory10 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            Street[] tilestreets = new Street[reader.readCompressedInt()];
            fillElements(streets, tilestreets);
            Building[] tilebuildings = new Building[reader.readCompressedInt()];
            fillElements(buildings, tilebuildings);
            return new Tile10(tilestreets, tilebuildings, zoom, row, column);
        }
    }

    private static class Tile10 extends AbstractTile {

        private final Street[] streets;
        private final Building[] buildings;

        public Tile10(final Street[] streets, final Building[] buildings, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.streets = streets;
            this.buildings = buildings;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator(streets);
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator(buildings);
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator();
        }
    }

    private class TileFactory11 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            Street[] tilestreets = new Street[reader.readCompressedInt()];
            fillElements(streets, tilestreets);
            Building[] tilebuildings = new Building[reader.readCompressedInt()];
            fillElements(buildings, tilebuildings);
            return new Tile11(tilepois, tilestreets, tilebuildings, zoom, row, column);
        }
    }

    private static class Tile11 extends AbstractTile {

        private final POI[] pois;
        private final Street[] streets;
        private final Building[] buildings;

        public Tile11(final POI[] pois, final Street[] streets, final Building[] buildings, final int zoom,
                final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.streets = streets;
            this.buildings = buildings;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator(streets);
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator(buildings);
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator();
        }
    }

    private class TileFactory12 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            Way[] tileways = new Way[reader.readCompressedInt()];
            fillElements(ways, tileways);
            Building[] tilebuildings = new Building[reader.readCompressedInt()];
            fillElements(buildings, tilebuildings);
            return new Tile12(tileways, tilebuildings, zoom, row, column);
        }
    }

    private static class Tile12 extends AbstractTile {

        private final Way[] ways;
        private final Building[] buildings;

        public Tile12(final Way[] ways, final Building[] buildings, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.ways = ways;
            this.buildings = buildings;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator(buildings);
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator();
        }
    }

    private class TileFactory13 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            Way[] tileways = new Way[reader.readCompressedInt()];
            fillElements(ways, tileways);
            Building[] tilebuildings = new Building[reader.readCompressedInt()];
            fillElements(buildings, tilebuildings);
            return new Tile13(tilepois, tileways, tilebuildings, zoom, row, column);
        }
    }

    private static class Tile13 extends AbstractTile {

        private final POI[] pois;
        private final Way[] ways;
        private final Building[] buildings;

        public Tile13(final POI[] pois, final Way[] ways, final Building[] buildings, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.ways = ways;
            this.buildings = buildings;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator(buildings);
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator();
        }
    }

    private class TileFactory14 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            Street[] tilestreets = new Street[reader.readCompressedInt()];
            fillElements(streets, tilestreets);
            Way[] tileways = new Way[reader.readCompressedInt()];
            fillElements(ways, tileways);
            Building[] tilebuildings = new Building[reader.readCompressedInt()];
            fillElements(buildings, tilebuildings);
            return new Tile14(tilestreets, tileways, tilebuildings, zoom, row, column);
        }
    }

    private static class Tile14 extends AbstractTile {

        private final Street[] streets;
        private final Way[] ways;
        private final Building[] buildings;

        public Tile14(final Street[] streets, final Way[] ways, final Building[] buildings, final int zoom,
                final int row, final int column) {
            super(zoom, row, column);
            this.streets = streets;
            this.ways = ways;
            this.buildings = buildings;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator(streets);
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator(buildings);
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator();
        }
    }

    private class TileFactory15 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            Street[] tilestreets = new Street[reader.readCompressedInt()];
            fillElements(streets, tilestreets);
            Way[] tileways = new Way[reader.readCompressedInt()];
            fillElements(ways, tileways);
            Building[] tilebuildings = new Building[reader.readCompressedInt()];
            fillElements(buildings, tilebuildings);
            return new Tile15(tilepois, tilestreets, tileways, tilebuildings, zoom, row, column);
        }
    }

    private static class Tile15 extends AbstractTile {

        private final POI[] pois;
        private final Street[] streets;
        private final Way[] ways;
        private final Building[] buildings;

        public Tile15(final POI[] pois, final Street[] streets, final Way[] ways, final Building[] buildings,
                final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.streets = streets;
            this.ways = ways;
            this.buildings = buildings;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator(streets);
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator(buildings);
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator();
        }
    }

    private class TileFactory16 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            Area[] tileareas = new Area[reader.readCompressedInt()];
            fillElements(areas, tileareas);
            return new Tile16(tileareas, zoom, row, column);
        }
    }

    private static class Tile16 extends AbstractTile {

        private final Area[] areas;

        public Tile16(final Area[] areas, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.areas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator(areas);
        }
    }

    private class TileFactory17 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            Area[] tileareas = new Area[reader.readCompressedInt()];
            fillElements(areas, tileareas);
            return new Tile17(tilepois, tileareas, zoom, row, column);
        }
    }

    private static class Tile17 extends AbstractTile {

        private final POI[] pois;
        private final Area[] areas;

        public Tile17(final POI[] pois, final Area[] areas, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.areas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator(areas);
        }
    }

    private class TileFactory18 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            Street[] tilestreets = new Street[reader.readCompressedInt()];
            fillElements(streets, tilestreets);
            Area[] tileareas = new Area[reader.readCompressedInt()];
            fillElements(areas, tileareas);
            return new Tile18(tilestreets, tileareas, zoom, row, column);
        }
    }

    private static class Tile18 extends AbstractTile {

        private final Street[] streets;
        private final Area[] areas;

        public Tile18(final Street[] streets, final Area[] areas, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.streets = streets;
            this.areas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator(streets);
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator(areas);
        }
    }

    private class TileFactory19 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            Street[] tilestreets = new Street[reader.readCompressedInt()];
            fillElements(streets, tilestreets);
            Area[] tileareas = new Area[reader.readCompressedInt()];
            fillElements(areas, tileareas);
            return new Tile19(tilepois, tilestreets, tileareas, zoom, row, column);
        }
    }

    private static class Tile19 extends AbstractTile {

        private final POI[] pois;
        private final Street[] streets;
        private final Area[] areas;

        public Tile19(final POI[] pois, final Street[] streets, final Area[] areas, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.streets = streets;
            this.areas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator(streets);
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator(areas);
        }
    }

    private class TileFactory20 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            Way[] tileways = new Way[reader.readCompressedInt()];
            fillElements(ways, tileways);
            Area[] tileareas = new Area[reader.readCompressedInt()];
            fillElements(areas, tileareas);
            return new Tile20(tileways, tileareas, zoom, row, column);
        }
    }

    private static class Tile20 extends AbstractTile {

        private final Way[] ways;
        private final Area[] areas;

        public Tile20(final Way[] ways, final Area[] areas, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.ways = ways;
            this.areas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator(areas);
        }
    }

    private class TileFactory21 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            Way[] tileways = new Way[reader.readCompressedInt()];
            fillElements(ways, tileways);
            Area[] tileareas = new Area[reader.readCompressedInt()];
            fillElements(areas, tileareas);
            return new Tile21(tilepois, tileways, tileareas, zoom, row, column);
        }
    }

    private static class Tile21 extends AbstractTile {

        private final POI[] pois;
        private final Way[] ways;
        private final Area[] areas;

        public Tile21(final POI[] pois, final Way[] ways, final Area[] areas, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.ways = ways;
            this.areas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator(areas);
        }
    }

    private class TileFactory22 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            Street[] tilestreets = new Street[reader.readCompressedInt()];
            fillElements(streets, tilestreets);
            Way[] tileways = new Way[reader.readCompressedInt()];
            fillElements(ways, tileways);
            Area[] tileareas = new Area[reader.readCompressedInt()];
            fillElements(areas, tileareas);
            return new Tile22(tilestreets, tileways, tileareas, zoom, row, column);
        }
    }

    private static class Tile22 extends AbstractTile {

        private final Street[] streets;
        private final Way[] ways;
        private final Area[] areas;

        public Tile22(final Street[] streets, final Way[] ways, final Area[] areas, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.streets = streets;
            this.ways = ways;
            this.areas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator(streets);
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator(areas);
        }
    }

    private class TileFactory23 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            Street[] tilestreets = new Street[reader.readCompressedInt()];
            fillElements(streets, tilestreets);
            Way[] tileways = new Way[reader.readCompressedInt()];
            fillElements(ways, tileways);
            Area[] tileareas = new Area[reader.readCompressedInt()];
            fillElements(areas, tileareas);
            return new Tile23(tilepois, tilestreets, tileways, tileareas, zoom, row, column);
        }
    }

    private static class Tile23 extends AbstractTile {

        private final POI[] pois;
        private final Street[] streets;
        private final Way[] ways;
        private final Area[] areas;

        public Tile23(final POI[] pois, final Street[] streets, final Way[] ways, final Area[] areas, final int zoom,
                final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.streets = streets;
            this.ways = ways;
            this.areas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator(streets);
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator(areas);
        }
    }

    private class TileFactory24 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            Building[] tilebuildings = new Building[reader.readCompressedInt()];
            fillElements(buildings, tilebuildings);
            Area[] tileareas = new Area[reader.readCompressedInt()];
            fillElements(areas, tileareas);
            return new Tile24(tilebuildings, tileareas, zoom, row, column);
        }
    }

    private static class Tile24 extends AbstractTile {

        private final Building[] buildings;
        private final Area[] areas;

        public Tile24(final Building[] buildings, final Area[] areas, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.buildings = buildings;
            this.areas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator(buildings);
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator(areas);
        }
    }

    private class TileFactory25 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            Building[] tilebuildings = new Building[reader.readCompressedInt()];
            fillElements(buildings, tilebuildings);
            Area[] tileareas = new Area[reader.readCompressedInt()];
            fillElements(areas, tileareas);
            return new Tile25(tilepois, tilebuildings, tileareas, zoom, row, column);
        }
    }

    private static class Tile25 extends AbstractTile {

        private final POI[] pois;
        private final Building[] buildings;
        private final Area[] areas;

        public Tile25(final POI[] pois, final Building[] buildings, final Area[] areas, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.buildings = buildings;
            this.areas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator(buildings);
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator(areas);
        }
    }

    private class TileFactory26 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            Street[] tilestreets = new Street[reader.readCompressedInt()];
            fillElements(streets, tilestreets);
            Building[] tilebuildings = new Building[reader.readCompressedInt()];
            fillElements(buildings, tilebuildings);
            Area[] tileareas = new Area[reader.readCompressedInt()];
            fillElements(areas, tileareas);
            return new Tile26(tilestreets, tilebuildings, tileareas, zoom, row, column);
        }
    }

    private static class Tile26 extends AbstractTile {

        private final Street[] streets;
        private final Building[] buildings;
        private final Area[] areas;

        public Tile26(final Street[] streets, final Building[] buildings, final Area[] areas, final int zoom,
                final int row, final int column) {
            super(zoom, row, column);
            this.streets = streets;
            this.buildings = buildings;
            this.areas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator(streets);
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator(buildings);
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator(areas);
        }
    }

    private class TileFactory27 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            Street[] tilestreets = new Street[reader.readCompressedInt()];
            fillElements(streets, tilestreets);
            Building[] tilebuildings = new Building[reader.readCompressedInt()];
            fillElements(buildings, tilebuildings);
            Area[] tileareas = new Area[reader.readCompressedInt()];
            fillElements(areas, tileareas);
            return new Tile27(tilepois, tilestreets, tilebuildings, tileareas, zoom, row, column);
        }
    }

    private static class Tile27 extends AbstractTile {

        private final POI[] pois;
        private final Street[] streets;
        private final Building[] buildings;
        private final Area[] areas;

        public Tile27(final POI[] pois, final Street[] streets, final Building[] buildings, final Area[] areas,
                final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.streets = streets;
            this.buildings = buildings;
            this.areas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator(streets);
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator(buildings);
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator(areas);
        }
    }

    private class TileFactory28 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            Way[] tileways = new Way[reader.readCompressedInt()];
            fillElements(ways, tileways);
            Building[] tilebuildings = new Building[reader.readCompressedInt()];
            fillElements(buildings, tilebuildings);
            Area[] tileareas = new Area[reader.readCompressedInt()];
            fillElements(areas, tileareas);
            return new Tile28(tileways, tilebuildings, tileareas, zoom, row, column);
        }
    }

    private static class Tile28 extends AbstractTile {

        private final Way[] ways;
        private final Building[] buildings;
        private final Area[] areas;

        public Tile28(final Way[] ways, final Building[] buildings, final Area[] areas, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.ways = ways;
            this.buildings = buildings;
            this.areas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator(buildings);
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator(areas);
        }
    }

    private class TileFactory29 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            Way[] tileways = new Way[reader.readCompressedInt()];
            fillElements(ways, tileways);
            Building[] tilebuildings = new Building[reader.readCompressedInt()];
            fillElements(buildings, tilebuildings);
            Area[] tileareas = new Area[reader.readCompressedInt()];
            fillElements(areas, tileareas);
            return new Tile29(tilepois, tileways, tilebuildings, tileareas, zoom, row, column);
        }
    }

    private static class Tile29 extends AbstractTile {

        private final POI[] pois;
        private final Way[] ways;
        private final Building[] buildings;
        private final Area[] areas;

        public Tile29(final POI[] pois, final Way[] ways, final Building[] buildings, final Area[] areas,
                final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.ways = ways;
            this.buildings = buildings;
            this.areas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator(buildings);
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator(areas);
        }
    }

    private class TileFactory30 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            Street[] tilestreets = new Street[reader.readCompressedInt()];
            fillElements(streets, tilestreets);
            Way[] tileways = new Way[reader.readCompressedInt()];
            fillElements(ways, tileways);
            Building[] tilebuildings = new Building[reader.readCompressedInt()];
            fillElements(buildings, tilebuildings);
            Area[] tileareas = new Area[reader.readCompressedInt()];
            fillElements(areas, tileareas);
            return new Tile30(tilestreets, tileways, tilebuildings, tileareas, zoom, row, column);
        }
    }

    private static class Tile30 extends AbstractTile {

        private final Street[] streets;
        private final Way[] ways;
        private final Building[] buildings;
        private final Area[] areas;

        public Tile30(final Street[] streets, final Way[] ways, final Building[] buildings, final Area[] areas,
                final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.streets = streets;
            this.ways = ways;
            this.buildings = buildings;
            this.areas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator(streets);
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator(buildings);
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator(areas);
        }
    }

    private class TileFactory31 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            Street[] tilestreets = new Street[reader.readCompressedInt()];
            fillElements(streets, tilestreets);
            Way[] tileways = new Way[reader.readCompressedInt()];
            fillElements(ways, tileways);
            Building[] tilebuildings = new Building[reader.readCompressedInt()];
            fillElements(buildings, tilebuildings);
            Area[] tileareas = new Area[reader.readCompressedInt()];
            fillElements(areas, tileareas);
            return new Tile31(tilepois, tilestreets, tileways, tilebuildings, tileareas, zoom, row, column);
        }
    }

    private static class Tile31 extends AbstractTile {

        private final POI[] pois;
        private final Street[] streets;
        private final Way[] ways;
        private final Building[] buildings;
        private final Area[] areas;

        public Tile31(final POI[] pois, final Street[] streets, final Way[] ways, final Building[] buildings,
                final Area[] areas, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.streets = streets;
            this.ways = ways;
            this.buildings = buildings;
            this.areas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<Street> getStreets() {
            return Arrays.iterator(streets);
        }

        @Override
        public Iterator<Way> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<Building> getBuildings() {
            return Arrays.iterator(buildings);
        }

        @Override
        public Iterator<Area> getTerrain() {
            return Arrays.iterator(areas);
        }
    }

}