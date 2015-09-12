package model.map;

import java.util.Iterator;

import util.Arrays;
import model.elements.Area;
import model.elements.Building;
import model.elements.POI;
import model.elements.Street;
import model.elements.Way;

public class TileFactory {

    private final ITileFactory[] factories;

    public TileFactory() {
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

    public ITile create(final byte flags, final POI[] pois, final Street[] streets, final Way[] ways,
            final Building[] buildings, final Area[] areas, final int zoom, final int row, final int column) {
        return factories[flags].create(pois, streets, ways, buildings, areas, zoom, row, column);
    }

    private static interface ITileFactory {
        ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom, int row,
                int column);
    }

    private static class TileFactory0 implements ITileFactory {
        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new EmptyTile(zoom, row, column);
        }
    }

    private static class TileFactory1 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(pois, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final POI[] pois;

            public Tile(final POI[] pois, final int zoom, final int row, final int column) {
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
    }

    private static class TileFactory2 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(streets, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final Street[] streets;

            public Tile(final Street[] streets, final int zoom, final int row, final int column) {
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
    }

    private static class TileFactory3 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(pois, streets, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final POI[] pois;
            private final Street[] streets;

            public Tile(final POI[] pois, final Street[] streets, final int zoom, final int row, final int column) {
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
    }

    private static class TileFactory4 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(ways, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final Way[] ways;

            public Tile(final Way[] ways, final int zoom, final int row, final int column) {
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
    }

    private static class TileFactory5 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(pois, ways, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final POI[] pois;
            private final Way[] ways;

            public Tile(final POI[] pois, final Way[] ways, final int zoom, final int row, final int column) {
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
    }

    private static class TileFactory6 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(streets, ways, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final Street[] streets;
            private final Way[] ways;

            public Tile(final Street[] streets, final Way[] ways, final int zoom, final int row, final int column) {
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
    }

    private static class TileFactory7 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(pois, streets, ways, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final POI[] pois;
            private final Street[] streets;
            private final Way[] ways;

            public Tile(final POI[] pois, final Street[] streets, final Way[] ways, final int zoom, final int row,
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
    }

    private static class TileFactory8 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(buildings, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final Building[] buildings;

            public Tile(final Building[] buildings, final int zoom, final int row, final int column) {
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
    }

    private static class TileFactory9 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(pois, buildings, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final POI[] pois;
            private final Building[] buildings;

            public Tile(final POI[] pois, final Building[] buildings, final int zoom, final int row, final int column) {
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
    }

    private static class TileFactory10 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(streets, buildings, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final Street[] streets;
            private final Building[] buildings;

            public Tile(final Street[] streets, final Building[] buildings, final int zoom, final int row,
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
    }

    private static class TileFactory11 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(pois, streets, buildings, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final POI[] pois;
            private final Street[] streets;
            private final Building[] buildings;

            public Tile(final POI[] pois, final Street[] streets, final Building[] buildings, final int zoom,
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
    }

    private static class TileFactory12 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(ways, buildings, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final Way[] ways;
            private final Building[] buildings;

            public Tile(final Way[] ways, final Building[] buildings, final int zoom, final int row, final int column) {
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
    }

    private static class TileFactory13 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(pois, ways, buildings, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final POI[] pois;
            private final Way[] ways;
            private final Building[] buildings;

            public Tile(final POI[] pois, final Way[] ways, final Building[] buildings, final int zoom, final int row,
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
    }

    private static class TileFactory14 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(streets, ways, buildings, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final Street[] streets;
            private final Way[] ways;
            private final Building[] buildings;

            public Tile(final Street[] streets, final Way[] ways, final Building[] buildings, final int zoom,
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
    }

    private static class TileFactory15 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(pois, streets, ways, buildings, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final POI[] pois;
            private final Street[] streets;
            private final Way[] ways;
            private final Building[] buildings;

            public Tile(final POI[] pois, final Street[] streets, final Way[] ways, final Building[] buildings,
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
    }

    private static class TileFactory16 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(areas, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final Area[] areas;

            public Tile(final Area[] areas, final int zoom, final int row, final int column) {
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
    }

    private static class TileFactory17 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(pois, areas, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final POI[] pois;
            private final Area[] areas;

            public Tile(final POI[] pois, final Area[] areas, final int zoom, final int row, final int column) {
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
    }

    private static class TileFactory18 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(streets, areas, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final Street[] streets;
            private final Area[] areas;

            public Tile(final Street[] streets, final Area[] areas, final int zoom, final int row, final int column) {
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
    }

    private static class TileFactory19 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(pois, streets, areas, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final POI[] pois;
            private final Street[] streets;
            private final Area[] areas;

            public Tile(final POI[] pois, final Street[] streets, final Area[] areas, final int zoom, final int row,
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
    }

    private static class TileFactory20 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(ways, areas, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final Way[] ways;
            private final Area[] areas;

            public Tile(final Way[] ways, final Area[] areas, final int zoom, final int row, final int column) {
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
    }

    private static class TileFactory21 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(pois, ways, areas, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final POI[] pois;
            private final Way[] ways;
            private final Area[] areas;

            public Tile(final POI[] pois, final Way[] ways, final Area[] areas, final int zoom, final int row,
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
    }

    private static class TileFactory22 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(streets, ways, areas, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final Street[] streets;
            private final Way[] ways;
            private final Area[] areas;

            public Tile(final Street[] streets, final Way[] ways, final Area[] areas, final int zoom, final int row,
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
    }

    private static class TileFactory23 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(pois, streets, ways, areas, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final POI[] pois;
            private final Street[] streets;
            private final Way[] ways;
            private final Area[] areas;

            public Tile(final POI[] pois, final Street[] streets, final Way[] ways, final Area[] areas, final int zoom,
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
    }

    private static class TileFactory24 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(buildings, areas, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final Building[] buildings;
            private final Area[] areas;

            public Tile(final Building[] buildings, final Area[] areas, final int zoom, final int row, final int column) {
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
    }

    private static class TileFactory25 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(pois, buildings, areas, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final POI[] pois;
            private final Building[] buildings;
            private final Area[] areas;

            public Tile(final POI[] pois, final Building[] buildings, final Area[] areas, final int zoom,
                    final int row, final int column) {
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
    }

    private static class TileFactory26 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(streets, buildings, areas, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final Street[] streets;
            private final Building[] buildings;
            private final Area[] areas;

            public Tile(final Street[] streets, final Building[] buildings, final Area[] areas, final int zoom,
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
    }

    private static class TileFactory27 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(pois, streets, buildings, areas, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final POI[] pois;
            private final Street[] streets;
            private final Building[] buildings;
            private final Area[] areas;

            public Tile(final POI[] pois, final Street[] streets, final Building[] buildings, final Area[] areas,
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
    }

    private static class TileFactory28 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(ways, buildings, areas, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final Way[] ways;
            private final Building[] buildings;
            private final Area[] areas;

            public Tile(final Way[] ways, final Building[] buildings, final Area[] areas, final int zoom,
                    final int row, final int column) {
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
    }

    private static class TileFactory29 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(pois, ways, buildings, areas, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final POI[] pois;
            private final Way[] ways;
            private final Building[] buildings;
            private final Area[] areas;

            public Tile(final POI[] pois, final Way[] ways, final Building[] buildings, final Area[] areas,
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
    }

    private static class TileFactory30 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(streets, ways, buildings, areas, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final Street[] streets;
            private final Way[] ways;
            private final Building[] buildings;
            private final Area[] areas;

            public Tile(final Street[] streets, final Way[] ways, final Building[] buildings, final Area[] areas,
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
    }

    private static class TileFactory31 implements ITileFactory {

        @Override
        public ITile create(POI[] pois, Street[] streets, Way[] ways, Building[] buildings, Area[] areas, int zoom,
                int row, int column) {
            return new Tile(pois, streets, ways, buildings, areas, zoom, row, column);
        }

        private static class Tile extends AbstractTile {

            private final POI[] pois;
            private final Street[] streets;
            private final Way[] ways;
            private final Building[] buildings;
            private final Area[] areas;

            public Tile(final POI[] pois, final Street[] streets, final Way[] ways, final Building[] buildings,
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

}