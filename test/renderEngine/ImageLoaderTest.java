package renderEngine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

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

        final Street[] streets = new Street[5];

        streets[0] = new Street(new Node[]{n0, n1, n2, n3}, 3, "Haid und Neu Straße", 0);
        streets[1] = new Street(new Node[]{n1, n4, n5, n6}, 4, "Tullastraße", 0);
        streets[2] = new Street(new Node[]{n2, n7, n8, n9}, 5, "Helmertstraße", 0);
        streets[3] = new Street(new Node[]{n4, n7}, 5, "Gaußstraße", 0);
        streets[4] = new Street(new Node[]{n5, n8, n9, n10, n11}, 5, "Jordanstraße", 0);

        final Way[] ways = new Way[0];
        final POI[] pois = new POI[0];
        final Area[] areas = new Area[0];
        final Building[] buildings = new Building[6];

        buildings[0] = new Building(new Node[]{new Node(362, 946), new Node(362, 1046), new Node(462, 1046),
                new Node(462, 946)}, "Tullastraße 10", new StreetNode(0.125f, streets[1]));
        buildings[1] = new Building(new Node[]{new Node(358, 1626), new Node(358, 1726), new Node(458, 1726),
                new Node(458, 1626)}, "Tullastraße 30", new StreetNode(0.475f, streets[1]));
        buildings[2] = new Building(new Node[]{new Node(852, 1386), new Node(852, 1286), new Node(952, 1286),
                new Node(952, 1386)}, "Gaußstraße 21", new StreetNode(0.5f, streets[3]));
        buildings[3] = new Building(new Node[]{new Node(682, 1826), new Node(682, 1926), new Node(782, 1926),
                new Node(782, 1826)}, "Jordanstraße 8", new StreetNode(0.1f, streets[4]));
        buildings[4] = new Building(new Node[]{new Node(1274, 850), new Node(1274, 950), new Node(1374, 950),
                new Node(1374, 850)}, "Helmertstraße 7", new StreetNode(0.1f, streets[2]));
        buildings[5] = new Building(new Node[]{new Node(1118, 1474), new Node(1118, 1574), new Node(1218, 1574),
                new Node(1218, 1474)}, "Helmertstraße 22", new StreetNode(0.4f, streets[2]));

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
