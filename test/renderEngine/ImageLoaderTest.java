package renderEngine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import model.elements.Area;
import model.elements.Building;
import model.elements.Node;
import model.elements.POI;
import model.elements.Street;
import model.elements.StreetNode;
import model.elements.Way;
import model.map.IMapManager;
import model.map.MapManager;
import model.map.PixelConverter;
import model.map.Tile;
import model.renderEngine.IImageLoader;
import model.renderEngine.ImageLoader;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ImageLoaderTest {

    private static IImageLoader loader;
    private static Tile[][][] tiles;
    private static IMapManager mapManager;
    private static BufferedImage defaultImage;

    @BeforeClass
    public static void setUpClass() {
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

        final ArrayList<Street> streets = new ArrayList<Street>();

        final LinkedList<Node> sn0 = new LinkedList<Node>();
        sn0.add(n0);
        sn0.add(n1);
        sn0.add(n2);
        sn0.add(n3);
        streets.add(new Street(sn0, 3, "Haid und Neu Straße", 0));

        final LinkedList<Node> sn1 = new LinkedList<Node>();
        sn1.add(n1);
        sn1.add(n4);
        sn1.add(n5);
        sn1.add(n6);
        streets.add(new Street(sn1, 4, "Tullastraße", 0));

        final LinkedList<Node> sn2 = new LinkedList<Node>();
        sn2.add(n2);
        sn2.add(n7);
        sn2.add(n8);
        sn2.add(n9);
        streets.add(new Street(sn2, 5, "Helmertstraße", 0));

        final LinkedList<Node> sn3 = new LinkedList<Node>();
        sn3.add(n4);
        sn3.add(n7);
        streets.add(new Street(sn3, 5, "Gaußstraße", 0));

        final LinkedList<Node> sn4 = new LinkedList<Node>();
        sn4.add(n5);
        sn4.add(n8);
        sn4.add(n8);
        sn4.add(n10);
        sn4.add(n11);
        streets.add(new Street(sn4, 5, "Jordanstraße", 0));

        final LinkedList<Way> ways = new LinkedList<Way>();
        final LinkedList<POI> pois = new LinkedList<POI>();
        final LinkedList<Area> areas = new LinkedList<Area>();
        final LinkedList<Building> buildings = new LinkedList<Building>();

        final List<Node> bn1 = new LinkedList<Node>();
        bn1.add(new Node(362, 946));
        bn1.add(new Node(362, 1046));
        bn1.add(new Node(462, 1046));
        bn1.add(new Node(462, 946));
        buildings.add(new Building(bn1, "Tullastraße 10", new StreetNode(0.125f, streets.get(1))));

        final List<Node> bn2 = new LinkedList<Node>();
        bn2.add(new Node(358, 1626));
        bn2.add(new Node(358, 1726));
        bn2.add(new Node(458, 1726));
        bn2.add(new Node(458, 1626));
        buildings.add(new Building(bn2, "Tullastraße 30", new StreetNode(0.475f, streets.get(1))));

        final List<Node> bn4 = new LinkedList<Node>();
        bn4.add(new Node(852, 1386));
        bn4.add(new Node(852, 1286));
        bn4.add(new Node(952, 1286));
        bn4.add(new Node(952, 1386));
        buildings.add(new Building(bn4, "Gaußstraße 21", new StreetNode(0.5f, streets.get(3))));

        final List<Node> bn5 = new LinkedList<Node>();
        bn5.add(new Node(682, 1826));
        bn5.add(new Node(682, 1926));
        bn5.add(new Node(782, 1926));
        bn5.add(new Node(782, 1826));
        buildings.add(new Building(bn5, "Jordanstraße 8", new StreetNode(0.1f, streets.get(4))));

        final List<Node> bn7 = new LinkedList<Node>();
        bn7.add(new Node(1274, 850));
        bn7.add(new Node(1274, 950));
        bn7.add(new Node(1374, 950));
        bn7.add(new Node(1374, 850));
        buildings.add(new Building(bn7, "Helmertstraße 7", new StreetNode(0.1f, streets.get(2))));

        final List<Node> bn8 = new LinkedList<Node>();
        bn8.add(new Node(1118, 1474));
        bn8.add(new Node(1118, 1574));
        bn8.add(new Node(1218, 1574));
        bn8.add(new Node(1218, 1474));
        buildings.add(new Building(bn8, "Helmertstraße 22", new StreetNode(0.4f, streets.get(2))));

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

        mapManager = new MapManager(tiles, new Dimension(256, 256), new PixelConverter(1), 0);
        mapManager.getMapState().setSize(10, 10);
        loader = new ImageLoader(mapManager);
    }

    @Before
    public void setUp() {
        mapManager.getMapState().setLocation(0, 0);
        loader.setMapManager(mapManager);
        defaultImage = (BufferedImage) loader.getBackgroundAccessor().getImage(-1, -1);
    }

    public boolean imagesEqual(final BufferedImage image1, final BufferedImage image2) {
        final DataBufferInt db1 = (DataBufferInt) image1.getRaster().getDataBuffer();
        final DataBufferInt db2 = (DataBufferInt) image2.getRaster().getDataBuffer();

        boolean compare = true;

        for (int bank = 0; bank < db1.getNumBanks() && compare; bank++) {
            final int[] actual = db1.getData(bank);
            final int[] expected = db2.getData(bank);
            compare = Arrays.equals(actual, expected);
        }

        return compare;
    }

    @Test
    public void testAccessorsNotNull() {
        assertNotNull(loader.getBackgroundAccessor());
        assertNotNull(loader.getPOIAccessor());
        assertNotNull(loader.getRouteAccessor());
    }

    @Test
    public void testImageLoading() {
        assertTrue(imagesEqual((BufferedImage) loader.getBackgroundAccessor().getImage(1, 1), defaultImage));

        loader.update();
        synchronized (this) {
            try {
                wait(500);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        assertFalse(imagesEqual((BufferedImage) loader.getBackgroundAccessor().getImage(1, 1), defaultImage));

        assertTrue(imagesEqual(
                (BufferedImage) loader.getBackgroundAccessor().getImage(
                        mapManager.getCurrentGridLocation().y + mapManager.getRows() + 1,
                        mapManager.getCurrentGridLocation().x + mapManager.getColumns() + 1), defaultImage));
    }

    @Test
    public void testMove() {
        loader.update();
        synchronized (this) {
            try {
                wait(500);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        final int row = mapManager.getCurrentGridLocation().y + mapManager.getRows() + 1;
        final int column = mapManager.getCurrentGridLocation().x + mapManager.getColumns() + 1;

        assertTrue(imagesEqual((BufferedImage) loader.getBackgroundAccessor().getImage(row, column), defaultImage));

        // move by 1 tile size in x and y direction
        mapManager.getMapState().move(256, 256);

        loader.update();
        synchronized (this) {
            try {
                wait(500);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        // substract 1 because we moved by 1 tilesize
        assertFalse(imagesEqual((BufferedImage) loader.getBackgroundAccessor().getImage(row - 1, column - 1),
                defaultImage));
    }
}
