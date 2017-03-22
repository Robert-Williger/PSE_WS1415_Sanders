package map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.awt.Dimension;
import java.awt.Point;

import model.elements.IArea;
import model.elements.IBuilding;
import model.elements.Label;
import model.elements.POI;
import model.elements.Street;
import model.elements.StreetNode;
import model.elements.IWay;
import model.map.AddressPoint;
import model.map.DefaultTileSource;
import model.map.IMapManager;
import model.map.IPixelConverter;
import model.map.ITile;
import model.map.MapManager;
import model.map.MapState;
import model.map.PixelConverter;
import model.map.Tile;

import org.junit.Before;
import org.junit.Test;

public class MapManagerTest {

    private IMapManager manager;
    private ITile[][][] tiles;
    private IBuilding iBuilding;
    private Street street;
    private IPixelConverter converter;

    @Before
    public void setUp() {
        final Street[] streets = new Street[5];

        streets[0] = new Street(new int[]{0, 500, 1240, 2750}, new int[]{750, 750, 700, 500}, 3, "Haid und Neu Straße",
                0);
        streets[1] = new Street(new int[]{500, 550, 525, 505}, new int[]{750, 1350, 2000, 2750}, 4, "Tullastraße", 0);
        streets[2] = new Street(new int[]{1240, 1250, 1250, 1250}, new int[]{700, 1150, 1900, 2750}, 5,
                "Helmertstraße", 0);
        streets[3] = street = new Street(new int[]{550, 1250}, new int[]{1350, 1150}, 5, "Gaußstraße", 0);
        streets[4] = new Street(new int[]{525, 1250, 1250, 1900, 2750}, new int[]{525, 1250, 1250, 1900, 2750}, 5,
                "Jordanstraße", 0);

        final IWay[] ways = new IWay[0];
        final POI[] pois = new POI[0];
        final IArea[] areas = new IArea[0];
        final IBuilding[] buildings = new IBuilding[6];

        buildings[0] = iBuilding = IBuilding.create(new int[]{362, 362, 462, 462}, new int[]{945, 1046, 1046, 946},
                new StreetNode(0.125f, streets[1]), "10");
        buildings[1] = IBuilding.create(new int[]{358, 358, 458, 458}, new int[]{1626, 1726, 1726, 1626},
                new StreetNode(0.475f, streets[1]), "30");
        buildings[2] = IBuilding.create(new int[]{852, 852, 952, 952}, new int[]{1386, 1286, 1286, 1386},
                new StreetNode(0.5f, streets[3]), "21");
        buildings[3] = IBuilding.create(new int[]{682, 682, 782, 782}, new int[]{1826, 1926, 1926, 1826},
                new StreetNode(0.1f, streets[4]), "8");
        buildings[4] = IBuilding.create(new int[]{1274, 1274, 1374, 1374}, new int[]{850, 950, 950, 850},
                new StreetNode(0.1f, streets[2]), "7");
        buildings[5] = IBuilding.create(new int[]{1118, 1118, 1218, 1218}, new int[]{1474, 1574, 1574, 1474},
                new StreetNode(0.4f, streets[2]), "21");

        tiles = new Tile[3][][];
        for (int zoom = 0; zoom < 3; zoom++) {
            final int zoomFactor = 1 << zoom;
            tiles[zoom] = new Tile[4 * zoomFactor][4 * zoomFactor];
            for (int row = 0; row < 4 * zoomFactor; row++) {
                for (int column = 0; column < 4 * zoomFactor; column++) {
                    final Tile tile = new Tile(zoom, row, column, ways, streets, areas, buildings, pois, new Label[0]);
                    tiles[zoom][row][column] = tile;
                }
            }
        }

        converter = new PixelConverter(2);
        manager = new MapManager(new DefaultTileSource(tiles, 0), converter, new MapState(2048, 2048, 0, 2),
                new Dimension(256, 256));
    }

    @Test
    public void testTileSize() {
        assertEquals(manager.getTileSize(), new Dimension(256, 256));
    }

    @Test
    public void testConverter() {
        assertEquals(converter, manager.getConverter());
    }

    @Test
    public void testMapState() {
        assertNotNull(manager.getState());
    }

    @Test
    public void testRows() {
        manager.getState().setSectionSize(0, 1025);
        assertEquals(3, manager.getVisibleRows());
    }

    @Test
    public void testColumns() {
        manager.getState().setSectionSize(1025, 0);
        assertEquals(3, manager.getVisibleColumns());
    }

    @Test
    public void testGridLocation() {
        manager.getState().setLocation(512, 512);
        assertEquals(new Point(1, 1), manager.getGridLocation());
    }

    @Test
    public void testCoordConversion() {
        manager.getState().setLocation(280, 230);
        assertEquals(new Point(360, 310), manager.getCoord(new Point(40, 40)));
    }

    @Test
    public void testPixelConversion() {
        manager.getState().setLocation(280, 230);
        assertEquals(new Point(40, 40), manager.getPixel(new Point(360, 310)));
    }

    @Test
    public void testTileByID() {
        assertEquals(tiles[0][0][1], manager.getTile(1));
    }

    @Test
    public void testTileByCoord() {
        assertEquals(tiles[1][2][2], manager.getTile(new Point(512, 512), 1));
    }

    @Test
    public void testTileByZRC() {
        assertEquals(tiles[1][2][3], manager.getTile(2, 3, 1));
    }

    @Test
    public void testTileInvalid() {
        assertEquals(-1, manager.getTile(0, 0, 3).getID());
    }

    @Test
    public void testUnsuccessfulSearch() {
        assertNull(new MapManager().getAddress(new Point(200, 200)));
    }

    @Test
    public void testNormalSearch() {
        assertEquals(new AddressPoint("Gaußstraße 21", new StreetNode(0.5f, street)),
                manager.getAddress(new Point(900, 1250)));
    }

    @Test
    public void testSearchWithBuilding() {
        assertEquals(new AddressPoint(iBuilding.getAddress(), iBuilding.getStreetNode()),
                manager.getAddress(new Point(380, 1000)));
    }

}
