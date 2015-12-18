package model.map.factories;

import java.util.Iterator;
import java.io.IOException;

import util.Arrays;
import model.CompressedInputStream;
import model.elements.Label;
import model.elements.IArea;
import model.elements.IBuilding;
import model.elements.POI;
import model.elements.IStreet;
import model.elements.IWay;

import model.map.AbstractTile;
import model.map.ITile;

public class StorageTileFactory extends AbstractTileFactory implements ITileFactory {

    private final ITileFactory[] factories;

    public StorageTileFactory(final CompressedInputStream reader, POI[] pois, IStreet[] streets, IWay[] ways,
            IBuilding[] buildings, IArea[] areas, Label[] labels) {
        super(reader, pois, streets, ways, buildings, areas, labels);

        factories = new ITileFactory[64];
        for (int i = 0; i < 64; i++) {
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
            factories[32] = new TileFactory32();
            factories[33] = new TileFactory33();
            factories[34] = new TileFactory34();
            factories[35] = new TileFactory35();
            factories[36] = new TileFactory36();
            factories[37] = new TileFactory37();
            factories[38] = new TileFactory38();
            factories[39] = new TileFactory39();
            factories[40] = new TileFactory40();
            factories[41] = new TileFactory41();
            factories[42] = new TileFactory42();
            factories[43] = new TileFactory43();
            factories[44] = new TileFactory44();
            factories[45] = new TileFactory45();
            factories[46] = new TileFactory46();
            factories[47] = new TileFactory47();
            factories[48] = new TileFactory48();
            factories[49] = new TileFactory49();
            factories[50] = new TileFactory50();
            factories[51] = new TileFactory51();
            factories[52] = new TileFactory52();
            factories[53] = new TileFactory53();
            factories[54] = new TileFactory54();
            factories[55] = new TileFactory55();
            factories[56] = new TileFactory56();
            factories[57] = new TileFactory57();
            factories[58] = new TileFactory58();
            factories[59] = new TileFactory59();
            factories[60] = new TileFactory60();
            factories[61] = new TileFactory61();
            factories[62] = new TileFactory62();
            factories[63] = new TileFactory63();
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
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory2 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            return new Tile2(tilestreets, zoom, row, column);
        }
    }

    private static class Tile2 extends AbstractTile {

        private final IStreet[] iStreets;

        public Tile2(final IStreet[] streets, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.iStreets = streets;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory3 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            return new Tile3(tilepois, tilestreets, zoom, row, column);
        }
    }

    private static class Tile3 extends AbstractTile {

        private final POI[] pois;
        private final IStreet[] iStreets;

        public Tile3(final POI[] pois, final IStreet[] streets, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iStreets = streets;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory4 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            return new Tile4(tileways, zoom, row, column);
        }
    }

    private static class Tile4 extends AbstractTile {

        private final IWay[] ways;

        public Tile4(final IWay[] ways, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.ways = ways;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory5 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            return new Tile5(tilepois, tileways, zoom, row, column);
        }
    }

    private static class Tile5 extends AbstractTile {

        private final POI[] pois;
        private final IWay[] ways;

        public Tile5(final POI[] pois, final IWay[] ways, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.ways = ways;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory6 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            return new Tile6(tilestreets, tileways, zoom, row, column);
        }
    }

    private static class Tile6 extends AbstractTile {

        private final IStreet[] iStreets;
        private final IWay[] ways;

        public Tile6(final IStreet[] streets, final IWay[] ways, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.iStreets = streets;
            this.ways = ways;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory7 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            return new Tile7(tilepois, tilestreets, tileways, zoom, row, column);
        }
    }

    private static class Tile7 extends AbstractTile {

        private final POI[] pois;
        private final IStreet[] iStreets;
        private final IWay[] ways;

        public Tile7(final POI[] pois, final IStreet[] streets, final IWay[] ways, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iStreets = streets;
            this.ways = ways;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory8 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            return new Tile8(tilebuildings, zoom, row, column);
        }
    }

    private static class Tile8 extends AbstractTile {

        private final IBuilding[] iBuildings;

        public Tile8(final IBuilding[] buildings, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.iBuildings = buildings;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory9 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            return new Tile9(tilepois, tilebuildings, zoom, row, column);
        }
    }

    private static class Tile9 extends AbstractTile {

        private final POI[] pois;
        private final IBuilding[] iBuildings;

        public Tile9(final POI[] pois, final IBuilding[] buildings, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iBuildings = buildings;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory10 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            return new Tile10(tilestreets, tilebuildings, zoom, row, column);
        }
    }

    private static class Tile10 extends AbstractTile {

        private final IStreet[] iStreets;
        private final IBuilding[] iBuildings;

        public Tile10(final IStreet[] streets, final IBuilding[] buildings, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.iStreets = streets;
            this.iBuildings = buildings;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory11 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            return new Tile11(tilepois, tilestreets, tilebuildings, zoom, row, column);
        }
    }

    private static class Tile11 extends AbstractTile {

        private final POI[] pois;
        private final IStreet[] iStreets;
        private final IBuilding[] iBuildings;

        public Tile11(final POI[] pois, final IStreet[] streets, final IBuilding[] buildings, final int zoom,
                final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iStreets = streets;
            this.iBuildings = buildings;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory12 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            return new Tile12(tileways, tilebuildings, zoom, row, column);
        }
    }

    private static class Tile12 extends AbstractTile {

        private final IWay[] ways;
        private final IBuilding[] iBuildings;

        public Tile12(final IWay[] ways, final IBuilding[] buildings, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.ways = ways;
            this.iBuildings = buildings;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory13 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            return new Tile13(tilepois, tileways, tilebuildings, zoom, row, column);
        }
    }

    private static class Tile13 extends AbstractTile {

        private final POI[] pois;
        private final IWay[] ways;
        private final IBuilding[] iBuildings;

        public Tile13(final POI[] pois, final IWay[] ways, final IBuilding[] buildings, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.ways = ways;
            this.iBuildings = buildings;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory14 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            return new Tile14(tilestreets, tileways, tilebuildings, zoom, row, column);
        }
    }

    private static class Tile14 extends AbstractTile {

        private final IStreet[] iStreets;
        private final IWay[] ways;
        private final IBuilding[] iBuildings;

        public Tile14(final IStreet[] streets, final IWay[] ways, final IBuilding[] buildings, final int zoom,
                final int row, final int column) {
            super(zoom, row, column);
            this.iStreets = streets;
            this.ways = ways;
            this.iBuildings = buildings;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory15 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            return new Tile15(tilepois, tilestreets, tileways, tilebuildings, zoom, row, column);
        }
    }

    private static class Tile15 extends AbstractTile {

        private final POI[] pois;
        private final IStreet[] iStreets;
        private final IWay[] ways;
        private final IBuilding[] iBuildings;

        public Tile15(final POI[] pois, final IStreet[] streets, final IWay[] ways, final IBuilding[] buildings,
                final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iStreets = streets;
            this.ways = ways;
            this.iBuildings = buildings;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory16 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            return new Tile16(tileareas, zoom, row, column);
        }
    }

    private static class Tile16 extends AbstractTile {

        private final IArea[] iAreas;

        public Tile16(final IArea[] areas, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.iAreas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory17 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            return new Tile17(tilepois, tileareas, zoom, row, column);
        }
    }

    private static class Tile17 extends AbstractTile {

        private final POI[] pois;
        private final IArea[] iAreas;

        public Tile17(final POI[] pois, final IArea[] areas, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iAreas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory18 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            return new Tile18(tilestreets, tileareas, zoom, row, column);
        }
    }

    private static class Tile18 extends AbstractTile {

        private final IStreet[] iStreets;
        private final IArea[] iAreas;

        public Tile18(final IStreet[] streets, final IArea[] areas, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.iStreets = streets;
            this.iAreas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory19 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            return new Tile19(tilepois, tilestreets, tileareas, zoom, row, column);
        }
    }

    private static class Tile19 extends AbstractTile {

        private final POI[] pois;
        private final IStreet[] iStreets;
        private final IArea[] iAreas;

        public Tile19(final POI[] pois, final IStreet[] streets, final IArea[] areas, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iStreets = streets;
            this.iAreas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory20 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            return new Tile20(tileways, tileareas, zoom, row, column);
        }
    }

    private static class Tile20 extends AbstractTile {

        private final IWay[] ways;
        private final IArea[] iAreas;

        public Tile20(final IWay[] ways, final IArea[] areas, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.ways = ways;
            this.iAreas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory21 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            return new Tile21(tilepois, tileways, tileareas, zoom, row, column);
        }
    }

    private static class Tile21 extends AbstractTile {

        private final POI[] pois;
        private final IWay[] ways;
        private final IArea[] iAreas;

        public Tile21(final POI[] pois, final IWay[] ways, final IArea[] areas, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.ways = ways;
            this.iAreas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory22 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            return new Tile22(tilestreets, tileways, tileareas, zoom, row, column);
        }
    }

    private static class Tile22 extends AbstractTile {

        private final IStreet[] iStreets;
        private final IWay[] ways;
        private final IArea[] iAreas;

        public Tile22(final IStreet[] streets, final IWay[] ways, final IArea[] areas, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.iStreets = streets;
            this.ways = ways;
            this.iAreas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory23 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            return new Tile23(tilepois, tilestreets, tileways, tileareas, zoom, row, column);
        }
    }

    private static class Tile23 extends AbstractTile {

        private final POI[] pois;
        private final IStreet[] iStreets;
        private final IWay[] ways;
        private final IArea[] iAreas;

        public Tile23(final POI[] pois, final IStreet[] streets, final IWay[] ways, final IArea[] areas, final int zoom,
                final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iStreets = streets;
            this.ways = ways;
            this.iAreas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory24 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            return new Tile24(tilebuildings, tileareas, zoom, row, column);
        }
    }

    private static class Tile24 extends AbstractTile {

        private final IBuilding[] iBuildings;
        private final IArea[] iAreas;

        public Tile24(final IBuilding[] buildings, final IArea[] areas, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.iBuildings = buildings;
            this.iAreas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory25 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            return new Tile25(tilepois, tilebuildings, tileareas, zoom, row, column);
        }
    }

    private static class Tile25 extends AbstractTile {

        private final POI[] pois;
        private final IBuilding[] iBuildings;
        private final IArea[] iAreas;

        public Tile25(final POI[] pois, final IBuilding[] buildings, final IArea[] areas, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iBuildings = buildings;
            this.iAreas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory26 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            return new Tile26(tilestreets, tilebuildings, tileareas, zoom, row, column);
        }
    }

    private static class Tile26 extends AbstractTile {

        private final IStreet[] iStreets;
        private final IBuilding[] iBuildings;
        private final IArea[] iAreas;

        public Tile26(final IStreet[] streets, final IBuilding[] buildings, final IArea[] areas, final int zoom,
                final int row, final int column) {
            super(zoom, row, column);
            this.iStreets = streets;
            this.iBuildings = buildings;
            this.iAreas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory27 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            return new Tile27(tilepois, tilestreets, tilebuildings, tileareas, zoom, row, column);
        }
    }

    private static class Tile27 extends AbstractTile {

        private final POI[] pois;
        private final IStreet[] iStreets;
        private final IBuilding[] iBuildings;
        private final IArea[] iAreas;

        public Tile27(final POI[] pois, final IStreet[] streets, final IBuilding[] buildings, final IArea[] areas,
                final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iStreets = streets;
            this.iBuildings = buildings;
            this.iAreas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory28 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            return new Tile28(tileways, tilebuildings, tileareas, zoom, row, column);
        }
    }

    private static class Tile28 extends AbstractTile {

        private final IWay[] ways;
        private final IBuilding[] iBuildings;
        private final IArea[] iAreas;

        public Tile28(final IWay[] ways, final IBuilding[] buildings, final IArea[] areas, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.ways = ways;
            this.iBuildings = buildings;
            this.iAreas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory29 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            return new Tile29(tilepois, tileways, tilebuildings, tileareas, zoom, row, column);
        }
    }

    private static class Tile29 extends AbstractTile {

        private final POI[] pois;
        private final IWay[] ways;
        private final IBuilding[] iBuildings;
        private final IArea[] iAreas;

        public Tile29(final POI[] pois, final IWay[] ways, final IBuilding[] buildings, final IArea[] areas,
                final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.ways = ways;
            this.iBuildings = buildings;
            this.iAreas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory30 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            return new Tile30(tilestreets, tileways, tilebuildings, tileareas, zoom, row, column);
        }
    }

    private static class Tile30 extends AbstractTile {

        private final IStreet[] iStreets;
        private final IWay[] ways;
        private final IBuilding[] iBuildings;
        private final IArea[] iAreas;

        public Tile30(final IStreet[] streets, final IWay[] ways, final IBuilding[] buildings, final IArea[] areas,
                final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.iStreets = streets;
            this.ways = ways;
            this.iBuildings = buildings;
            this.iAreas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory31 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            return new Tile31(tilepois, tilestreets, tileways, tilebuildings, tileareas, zoom, row, column);
        }
    }

    private static class Tile31 extends AbstractTile {

        private final POI[] pois;
        private final IStreet[] iStreets;
        private final IWay[] ways;
        private final IBuilding[] iBuildings;
        private final IArea[] iAreas;

        public Tile31(final POI[] pois, final IStreet[] streets, final IWay[] ways, final IBuilding[] buildings,
                final IArea[] areas, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iStreets = streets;
            this.ways = ways;
            this.iBuildings = buildings;
            this.iAreas = areas;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }
    }

    private class TileFactory32 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile32(tilelabels, zoom, row, column);
        }
    }

    private static class Tile32 extends AbstractTile {

        private final Label[] labels;

        public Tile32(final Label[] labels, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory33 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile33(tilepois, tilelabels, zoom, row, column);
        }
    }

    private static class Tile33 extends AbstractTile {

        private final POI[] pois;
        private final Label[] labels;

        public Tile33(final POI[] pois, final Label[] labels, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory34 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile34(tilestreets, tilelabels, zoom, row, column);
        }
    }

    private static class Tile34 extends AbstractTile {

        private final IStreet[] iStreets;
        private final Label[] labels;

        public Tile34(final IStreet[] streets, final Label[] labels, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.iStreets = streets;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory35 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile35(tilepois, tilestreets, tilelabels, zoom, row, column);
        }
    }

    private static class Tile35 extends AbstractTile {

        private final POI[] pois;
        private final IStreet[] iStreets;
        private final Label[] labels;

        public Tile35(final POI[] pois, final IStreet[] streets, final Label[] labels, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iStreets = streets;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory36 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile36(tileways, tilelabels, zoom, row, column);
        }
    }

    private static class Tile36 extends AbstractTile {

        private final IWay[] ways;
        private final Label[] labels;

        public Tile36(final IWay[] ways, final Label[] labels, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.ways = ways;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory37 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile37(tilepois, tileways, tilelabels, zoom, row, column);
        }
    }

    private static class Tile37 extends AbstractTile {

        private final POI[] pois;
        private final IWay[] ways;
        private final Label[] labels;

        public Tile37(final POI[] pois, final IWay[] ways, final Label[] labels, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.ways = ways;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory38 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile38(tilestreets, tileways, tilelabels, zoom, row, column);
        }
    }

    private static class Tile38 extends AbstractTile {

        private final IStreet[] iStreets;
        private final IWay[] ways;
        private final Label[] labels;

        public Tile38(final IStreet[] streets, final IWay[] ways, final Label[] labels, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.iStreets = streets;
            this.ways = ways;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory39 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile39(tilepois, tilestreets, tileways, tilelabels, zoom, row, column);
        }
    }

    private static class Tile39 extends AbstractTile {

        private final POI[] pois;
        private final IStreet[] iStreets;
        private final IWay[] ways;
        private final Label[] labels;

        public Tile39(final POI[] pois, final IStreet[] streets, final IWay[] ways, final Label[] labels, final int zoom,
                final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iStreets = streets;
            this.ways = ways;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory40 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile40(tilebuildings, tilelabels, zoom, row, column);
        }
    }

    private static class Tile40 extends AbstractTile {

        private final IBuilding[] iBuildings;
        private final Label[] labels;

        public Tile40(final IBuilding[] buildings, final Label[] labels, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.iBuildings = buildings;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory41 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile41(tilepois, tilebuildings, tilelabels, zoom, row, column);
        }
    }

    private static class Tile41 extends AbstractTile {

        private final POI[] pois;
        private final IBuilding[] iBuildings;
        private final Label[] labels;

        public Tile41(final POI[] pois, final IBuilding[] buildings, final Label[] labels, final int zoom,
                final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iBuildings = buildings;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory42 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile42(tilestreets, tilebuildings, tilelabels, zoom, row, column);
        }
    }

    private static class Tile42 extends AbstractTile {

        private final IStreet[] iStreets;
        private final IBuilding[] iBuildings;
        private final Label[] labels;

        public Tile42(final IStreet[] streets, final IBuilding[] buildings, final Label[] labels, final int zoom,
                final int row, final int column) {
            super(zoom, row, column);
            this.iStreets = streets;
            this.iBuildings = buildings;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory43 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile43(tilepois, tilestreets, tilebuildings, tilelabels, zoom, row, column);
        }
    }

    private static class Tile43 extends AbstractTile {

        private final POI[] pois;
        private final IStreet[] iStreets;
        private final IBuilding[] iBuildings;
        private final Label[] labels;

        public Tile43(final POI[] pois, final IStreet[] streets, final IBuilding[] buildings, final Label[] labels,
                final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iStreets = streets;
            this.iBuildings = buildings;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory44 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile44(tileways, tilebuildings, tilelabels, zoom, row, column);
        }
    }

    private static class Tile44 extends AbstractTile {

        private final IWay[] ways;
        private final IBuilding[] iBuildings;
        private final Label[] labels;

        public Tile44(final IWay[] ways, final IBuilding[] buildings, final Label[] labels, final int zoom,
                final int row, final int column) {
            super(zoom, row, column);
            this.ways = ways;
            this.iBuildings = buildings;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory45 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile45(tilepois, tileways, tilebuildings, tilelabels, zoom, row, column);
        }
    }

    private static class Tile45 extends AbstractTile {

        private final POI[] pois;
        private final IWay[] ways;
        private final IBuilding[] iBuildings;
        private final Label[] labels;

        public Tile45(final POI[] pois, final IWay[] ways, final IBuilding[] buildings, final Label[] labels,
                final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.ways = ways;
            this.iBuildings = buildings;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory46 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile46(tilestreets, tileways, tilebuildings, tilelabels, zoom, row, column);
        }
    }

    private static class Tile46 extends AbstractTile {

        private final IStreet[] iStreets;
        private final IWay[] ways;
        private final IBuilding[] iBuildings;
        private final Label[] labels;

        public Tile46(final IStreet[] streets, final IWay[] ways, final IBuilding[] buildings, final Label[] labels,
                final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.iStreets = streets;
            this.ways = ways;
            this.iBuildings = buildings;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory47 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile47(tilepois, tilestreets, tileways, tilebuildings, tilelabels, zoom, row, column);
        }
    }

    private static class Tile47 extends AbstractTile {

        private final POI[] pois;
        private final IStreet[] iStreets;
        private final IWay[] ways;
        private final IBuilding[] iBuildings;
        private final Label[] labels;

        public Tile47(final POI[] pois, final IStreet[] streets, final IWay[] ways, final IBuilding[] buildings,
                final Label[] labels, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iStreets = streets;
            this.ways = ways;
            this.iBuildings = buildings;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory48 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile48(tileareas, tilelabels, zoom, row, column);
        }
    }

    private static class Tile48 extends AbstractTile {

        private final IArea[] iAreas;
        private final Label[] labels;

        public Tile48(final IArea[] areas, final Label[] labels, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.iAreas = areas;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory49 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile49(tilepois, tileareas, tilelabels, zoom, row, column);
        }
    }

    private static class Tile49 extends AbstractTile {

        private final POI[] pois;
        private final IArea[] iAreas;
        private final Label[] labels;

        public Tile49(final POI[] pois, final IArea[] areas, final Label[] labels, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iAreas = areas;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory50 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile50(tilestreets, tileareas, tilelabels, zoom, row, column);
        }
    }

    private static class Tile50 extends AbstractTile {

        private final IStreet[] iStreets;
        private final IArea[] iAreas;
        private final Label[] labels;

        public Tile50(final IStreet[] streets, final IArea[] areas, final Label[] labels, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.iStreets = streets;
            this.iAreas = areas;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory51 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile51(tilepois, tilestreets, tileareas, tilelabels, zoom, row, column);
        }
    }

    private static class Tile51 extends AbstractTile {

        private final POI[] pois;
        private final IStreet[] iStreets;
        private final IArea[] iAreas;
        private final Label[] labels;

        public Tile51(final POI[] pois, final IStreet[] streets, final IArea[] areas, final Label[] labels,
                final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iStreets = streets;
            this.iAreas = areas;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory52 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile52(tileways, tileareas, tilelabels, zoom, row, column);
        }
    }

    private static class Tile52 extends AbstractTile {

        private final IWay[] ways;
        private final IArea[] iAreas;
        private final Label[] labels;

        public Tile52(final IWay[] ways, final IArea[] areas, final Label[] labels, final int zoom, final int row,
                final int column) {
            super(zoom, row, column);
            this.ways = ways;
            this.iAreas = areas;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory53 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile53(tilepois, tileways, tileareas, tilelabels, zoom, row, column);
        }
    }

    private static class Tile53 extends AbstractTile {

        private final POI[] pois;
        private final IWay[] ways;
        private final IArea[] iAreas;
        private final Label[] labels;

        public Tile53(final POI[] pois, final IWay[] ways, final IArea[] areas, final Label[] labels, final int zoom,
                final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.ways = ways;
            this.iAreas = areas;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory54 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile54(tilestreets, tileways, tileareas, tilelabels, zoom, row, column);
        }
    }

    private static class Tile54 extends AbstractTile {

        private final IStreet[] iStreets;
        private final IWay[] ways;
        private final IArea[] iAreas;
        private final Label[] labels;

        public Tile54(final IStreet[] streets, final IWay[] ways, final IArea[] areas, final Label[] labels,
                final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.iStreets = streets;
            this.ways = ways;
            this.iAreas = areas;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory55 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile55(tilepois, tilestreets, tileways, tileareas, tilelabels, zoom, row, column);
        }
    }

    private static class Tile55 extends AbstractTile {

        private final POI[] pois;
        private final IStreet[] iStreets;
        private final IWay[] ways;
        private final IArea[] iAreas;
        private final Label[] labels;

        public Tile55(final POI[] pois, final IStreet[] streets, final IWay[] ways, final IArea[] areas,
                final Label[] labels, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iStreets = streets;
            this.ways = ways;
            this.iAreas = areas;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory56 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile56(tilebuildings, tileareas, tilelabels, zoom, row, column);
        }
    }

    private static class Tile56 extends AbstractTile {

        private final IBuilding[] iBuildings;
        private final IArea[] iAreas;
        private final Label[] labels;

        public Tile56(final IBuilding[] buildings, final IArea[] areas, final Label[] labels, final int zoom,
                final int row, final int column) {
            super(zoom, row, column);
            this.iBuildings = buildings;
            this.iAreas = areas;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory57 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile57(tilepois, tilebuildings, tileareas, tilelabels, zoom, row, column);
        }
    }

    private static class Tile57 extends AbstractTile {

        private final POI[] pois;
        private final IBuilding[] iBuildings;
        private final IArea[] iAreas;
        private final Label[] labels;

        public Tile57(final POI[] pois, final IBuilding[] buildings, final IArea[] areas, final Label[] labels,
                final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iBuildings = buildings;
            this.iAreas = areas;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory58 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile58(tilestreets, tilebuildings, tileareas, tilelabels, zoom, row, column);
        }
    }

    private static class Tile58 extends AbstractTile {

        private final IStreet[] iStreets;
        private final IBuilding[] iBuildings;
        private final IArea[] iAreas;
        private final Label[] labels;

        public Tile58(final IStreet[] streets, final IBuilding[] buildings, final IArea[] areas, final Label[] labels,
                final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.iStreets = streets;
            this.iBuildings = buildings;
            this.iAreas = areas;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory59 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile59(tilepois, tilestreets, tilebuildings, tileareas, tilelabels, zoom, row, column);
        }
    }

    private static class Tile59 extends AbstractTile {

        private final POI[] pois;
        private final IStreet[] iStreets;
        private final IBuilding[] iBuildings;
        private final IArea[] iAreas;
        private final Label[] labels;

        public Tile59(final POI[] pois, final IStreet[] streets, final IBuilding[] buildings, final IArea[] areas,
                final Label[] labels, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iStreets = streets;
            this.iBuildings = buildings;
            this.iAreas = areas;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory60 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile60(tileways, tilebuildings, tileareas, tilelabels, zoom, row, column);
        }
    }

    private static class Tile60 extends AbstractTile {

        private final IWay[] ways;
        private final IBuilding[] iBuildings;
        private final IArea[] iAreas;
        private final Label[] labels;

        public Tile60(final IWay[] ways, final IBuilding[] buildings, final IArea[] areas, final Label[] labels,
                final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.ways = ways;
            this.iBuildings = buildings;
            this.iAreas = areas;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory61 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile61(tilepois, tileways, tilebuildings, tileareas, tilelabels, zoom, row, column);
        }
    }

    private static class Tile61 extends AbstractTile {

        private final POI[] pois;
        private final IWay[] ways;
        private final IBuilding[] iBuildings;
        private final IArea[] iAreas;
        private final Label[] labels;

        public Tile61(final POI[] pois, final IWay[] ways, final IBuilding[] buildings, final IArea[] areas,
                final Label[] labels, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.ways = ways;
            this.iBuildings = buildings;
            this.iAreas = areas;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory62 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile62(tilestreets, tileways, tilebuildings, tileareas, tilelabels, zoom, row, column);
        }
    }

    private static class Tile62 extends AbstractTile {

        private final IStreet[] iStreets;
        private final IWay[] ways;
        private final IBuilding[] iBuildings;
        private final IArea[] iAreas;
        private final Label[] labels;

        public Tile62(final IStreet[] streets, final IWay[] ways, final IBuilding[] buildings, final IArea[] areas,
                final Label[] labels, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.iStreets = streets;
            this.ways = ways;
            this.iBuildings = buildings;
            this.iAreas = areas;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

    private class TileFactory63 implements ITileFactory {

        @Override
        public ITile createTile(int zoom, int row, int column) throws IOException {
            POI[] tilepois = new POI[reader.readCompressedInt()];
            fillElements(pois, tilepois);
            IStreet[] tilestreets = new IStreet[reader.readCompressedInt()];
            fillElements(iStreets, tilestreets);
            IWay[] tileways = new IWay[reader.readCompressedInt()];
            fillElements(ways, tileways);
            IBuilding[] tilebuildings = new IBuilding[reader.readCompressedInt()];
            fillElements(iBuildings, tilebuildings);
            IArea[] tileareas = new IArea[reader.readCompressedInt()];
            fillElements(iAreas, tileareas);
            Label[] tilelabels = new Label[reader.readCompressedInt()];
            fillElements(labels, tilelabels);
            return new Tile63(tilepois, tilestreets, tileways, tilebuildings, tileareas, tilelabels, zoom, row, column);
        }
    }

    private static class Tile63 extends AbstractTile {

        private final POI[] pois;
        private final IStreet[] iStreets;
        private final IWay[] ways;
        private final IBuilding[] iBuildings;
        private final IArea[] iAreas;
        private final Label[] labels;

        public Tile63(final POI[] pois, final IStreet[] streets, final IWay[] ways, final IBuilding[] buildings,
                final IArea[] areas, final Label[] labels, final int zoom, final int row, final int column) {
            super(zoom, row, column);
            this.pois = pois;
            this.iStreets = streets;
            this.ways = ways;
            this.iBuildings = buildings;
            this.iAreas = areas;
            this.labels = labels;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator(pois);
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(iStreets);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator(ways);
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator(iBuildings);
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator(iAreas);
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator(labels);
        }
    }

}