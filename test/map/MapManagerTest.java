package map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.awt.Dimension;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import model.elements.Area;
import model.elements.Building;
import model.elements.Node;
import model.elements.POI;
import model.elements.Street;
import model.elements.StreetNode;
import model.elements.Way;
import model.map.AddressNode;
import model.map.IMapManager;
import model.map.IPixelConverter;
import model.map.ITile;
import model.map.MapManager;
import model.map.PixelConverter;
import model.map.Tile;

import org.junit.Before;
import org.junit.Test;

public class MapManagerTest {

    private IMapManager manager;
    private ITile[][][] tiles;
    private Building building;
    private Street street;
    private IPixelConverter converter;

    @Before
    public void setUp() {
        final Node n0 = new Node(0, 750);
        final Node n1 = new Node(500, 750);
        final Node n2 = new Node(1240, 700);
        final Node n3 = new Node(2750, 500);
        final Node n4 = new Node(550, 1350);
        final Node n5 = new Node(525, 2000);
        final Node n6 = new Node(505, 2750);
        final Node n7 = new Node(1250, 1150);
        final Node n8 = new Node(1250, 1900);
        final Node n9 = new Node(1250, 2750);
        final Node n10 = new Node(1900, 1900);
        final Node n11 = new Node(2750, 1800);

        final Street[] streets = new Street[5];

        final LinkedList<Node> sn0 = new LinkedList<Node>();
        sn0.add(n0);
        sn0.add(n1);
        sn0.add(n2);
        sn0.add(n3);
        streets[0] = new Street(sn0, 3, "Haid und Neu Straße", 0);

        final LinkedList<Node> sn1 = new LinkedList<Node>();
        sn1.add(n1);
        sn1.add(n4);
        sn1.add(n5);
        sn1.add(n6);
        streets[1] = new Street(sn1, 4, "Tullastraße", 0);

        final LinkedList<Node> sn2 = new LinkedList<Node>();
        sn2.add(n2);
        sn2.add(n7);
        sn2.add(n8);
        sn2.add(n9);
        streets[2] = new Street(sn2, 5, "Helmertstraße", 0);

        final LinkedList<Node> sn3 = new LinkedList<Node>();
        sn3.add(n4);
        sn3.add(n7);
        streets[3] = street = new Street(sn3, 5, "Gaußstraße", 0);

        final LinkedList<Node> sn4 = new LinkedList<Node>();
        sn4.add(n5);
        sn4.add(n8);
        sn4.add(n8);
        sn4.add(n10);
        sn4.add(n11);
        streets[4] = new Street(sn4, 5, "Jordanstraße", 0);

        final Way[] ways = new Way[0];
        final POI[] pois = new POI[0];
        final Area[] areas = new Area[0];
        final Building[] buildings = new Building[6];

        final List<Node> bn1 = new LinkedList<Node>();
        bn1.add(new Node(362, 946));
        bn1.add(new Node(362, 1046));
        bn1.add(new Node(462, 1046));
        bn1.add(new Node(462, 946));
        buildings[0] = building = new Building(bn1, "Tullastraße 10", new StreetNode(0.125f, streets[1]));

        final List<Node> bn2 = new LinkedList<Node>();
        bn2.add(new Node(358, 1626));
        bn2.add(new Node(358, 1726));
        bn2.add(new Node(458, 1726));
        bn2.add(new Node(458, 1626));
        buildings[1] = new Building(bn2, "Tullastraße 30", new StreetNode(0.475f, streets[1]));

        final List<Node> bn4 = new LinkedList<Node>();
        bn4.add(new Node(852, 1386));
        bn4.add(new Node(852, 1286));
        bn4.add(new Node(952, 1286));
        bn4.add(new Node(952, 1386));
        buildings[2] = new Building(bn4, "Gaußstraße 21", new StreetNode(0.5f, streets[3]));

        final List<Node> bn5 = new LinkedList<Node>();
        bn5.add(new Node(682, 1826));
        bn5.add(new Node(682, 1926));
        bn5.add(new Node(782, 1926));
        bn5.add(new Node(782, 1826));
        buildings[3] = new Building(bn5, "Jordanstraße 8", new StreetNode(0.1f, streets[4]));

        final List<Node> bn7 = new LinkedList<Node>();
        bn7.add(new Node(1274, 850));
        bn7.add(new Node(1274, 950));
        bn7.add(new Node(1374, 950));
        bn7.add(new Node(1374, 850));
        buildings[4] = new Building(bn7, "Helmertstraße 7", new StreetNode(0.1f, streets[2]));

        final List<Node> bn8 = new LinkedList<Node>();
        bn8.add(new Node(1118, 1474));
        bn8.add(new Node(1118, 1574));
        bn8.add(new Node(1218, 1574));
        bn8.add(new Node(1218, 1474));
        buildings[5] = new Building(bn8, "Helmertstraße 22", new StreetNode(0.4f, streets[2]));

        tiles = new Tile[3][][];
        for (int zoom = 0; zoom < 3; zoom++) {
            final int zoomFactor = 1 << zoom;
            tiles[zoom] = new Tile[4 * zoomFactor][4 * zoomFactor];
            for (int row = 0; row < 4 * zoomFactor; row++) {
                for (int column = 0; column < 4 * zoomFactor; column++) {
                    final Tile tile = new Tile(zoom, row, column, 512 * column / zoomFactor, 512 * row / zoomFactor,
                            ways, streets, areas, buildings, pois);
                    tiles[zoom][row][column] = tile;
                }
            }
        }
        converter = new PixelConverter(2);
        manager = new MapManager(tiles, new Dimension(256, 256), converter, 0);// mmReader.readMapManager(reader);
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
        assertNotNull(manager.getMapState());
    }

    @Test
    public void testRows() {
        manager.getMapState().setSize(0, 1025);
        assertEquals(3, manager.getRows());
    }

    @Test
    public void testColumns() {
        manager.getMapState().setSize(1025, 0);
        assertEquals(3, manager.getColumns());
    }

    @Test
    public void testGridLocation() {
        manager.getMapState().setLocation(512, 512);
        assertEquals(new Point(1, 1), manager.getCurrentGridLocation());
    }

    @Test
    public void testCoordConversion() {
        manager.getMapState().setLocation(280, 230);
        assertEquals(new Point(360, 310), manager.getCoord(new Point(40, 40)));
    }

    @Test
    public void testPixelConversion() {
        manager.getMapState().setLocation(280, 230);
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
        assertNull(new MapManager().getAddressNode(new Point(200, 200)));
    }

    @Test
    public void testNormalSearch() {
        assertEquals(new AddressNode("Gaußstraße 21", new StreetNode(0.5f, street)),
                manager.getAddressNode(new Point(900, 1250)));
    }

    @Test
    public void testSearchWithBuilding() {
        assertEquals(new AddressNode(building.getAddress(), building.getStreetNode()),
                manager.getAddressNode(new Point(380, 1000)));
    }

}
