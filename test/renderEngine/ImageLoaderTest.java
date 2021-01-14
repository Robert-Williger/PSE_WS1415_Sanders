package renderEngine;

//import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;

//import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

//import model.elements.IArea;
//import model.elements.IBuilding;
//import model.elements.Label;
//import model.elements.POI;
//import model.elements.Street;
//import model.elements.StreetNode;
//import model.elements.IWay;
//import model.map.DefaultTileSource;
import model.map.IMapManager;
import model.map.MapManager;
//import model.map.MapState;
//import model.map.PixelConverter;
//import model.map.Tile;
import model.renderEngine.IImageAccessor;
import model.renderEngine.IImageLoader;
import model.renderEngine.ImageLoader;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ImageLoaderTest {

    private static IImageLoader loader;
    // private static Tile[][][] tiles;
    private static IMapManager mapManager;
    // private static BufferedImage defaultImage;

    @BeforeClass
    public static void setUpClass() {
        // final Street[] streets = new Street[5];
        //
        // streets[0] = new Street(new int[]{0, 500, 1240, 2750}, new int[]{750, 750, 700, 500}, 3, "Haid und Neu
        // Straße",
        // 0);
        // streets[1] = new Street(new int[]{500, 550, 525, 505}, new int[]{750, 1350, 2000, 2750}, 4, "Tullastraße",
        // 0);
        // streets[2] = new Street(new int[]{1240, 1250, 1250, 1250}, new int[]{700, 1150, 1900, 2750}, 5,
        // "Helmertstraße", 0);
        // streets[3] = new Street(new int[]{550, 1250}, new int[]{1350, 1150}, 5, "Gaußstraße", 0);
        // streets[4] = new Street(new int[]{525, 1250, 1250, 1900, 2750}, new int[]{525, 1250, 1250, 1900, 2750}, 5,
        // "Jordanstraße", 0);
        //
        // final IWay[] ways = new IWay[0];
        // final POI[] pois = new POI[0];
        // final IArea[] areas = new IArea[0];
        // final IBuilding[] buildings = new IBuilding[6];
        //
        // buildings[0] = IBuilding.create(new int[]{362, 362, 462, 462}, new int[]{945, 1046, 1046, 946},
        // new StreetNode(0.125f, streets[1]), "10");
        // buildings[1] = IBuilding.create(new int[]{358, 358, 458, 458}, new int[]{1626, 1726, 1726, 1626},
        // new StreetNode(0.475f, streets[1]), "30");
        // buildings[2] = IBuilding.create(new int[]{852, 852, 952, 952}, new int[]{1386, 1286, 1286, 1386},
        // new StreetNode(0.5f, streets[3]), "21");
        // buildings[3] = IBuilding.create(new int[]{682, 682, 782, 782}, new int[]{1826, 1926, 1926, 1826},
        // new StreetNode(0.1f, streets[4]), "8");
        // buildings[4] = IBuilding.create(new int[]{1274, 1274, 1374, 1374}, new int[]{850, 950, 950, 850},
        // new StreetNode(0.1f, streets[2]), "7");
        // buildings[5] = IBuilding.create(new int[]{1118, 1118, 1218, 1218}, new int[]{1474, 1574, 1574, 1474},
        // new StreetNode(0.4f, streets[2]), "21");
        //
        // tiles = new Tile[3][][];
        // for (int zoom = 0; zoom < 3; zoom++) {
        // final int zoomFactor = 1 << zoom;
        // tiles[zoom] = new Tile[4 * zoomFactor][4 * zoomFactor];
        // for (int row = 0; row < 4 * zoomFactor; row++) {
        // for (int column = 0; column < 4 * zoomFactor; column++) {
        // final Tile tile = new Tile(zoom, row, column, ways, streets, areas, buildings, pois, new Label[0]);
        // tiles[zoom][row][column] = tile;
        // }
        // }
        // }
        //
        // mapManager = new MapManager(new DefaultTileSource(tiles, 0), new PixelConverter(1), new MapState(2048, 2048,
        // 0,
        // 2), new Dimension(256, 256));
        // mapManager.getState().setPixelSectionSize(10, 10);
        // loader = new ImageLoader(mapManager);
        mapManager = new MapManager();
        loader = new ImageLoader(mapManager);
    }

    @Before
    public void setUp() {
        mapManager.getMapSection().setMidpoint(0, 0);
        loader.setMapManager(mapManager);
        // defaultImage = (BufferedImage) loader.getImageAccessors().get(0).getImage(-1, -1);
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
        assertNotNull(loader.getImageAccessors());
        for (final IImageAccessor accessor : loader.getImageAccessors()) {
            assertNotNull(accessor);
        }
    }

    @Test
    public void testImageLoading() {
        // assertTrue(imagesEqual((BufferedImage) loader.getImageAccessors().get(0).getImage(1, 1), defaultImage));

        loader.update();
        synchronized (this) {
            try {
                wait(500);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        // assertFalse(imagesEqual((BufferedImage) loader.getImageAccessors().get(0).getImage(1, 1), defaultImage));

        // assertTrue(imagesEqual(
        // (BufferedImage) loader.getImageAccessors().get(0).getImage(
        // mapManager.getGridLocation().y + mapManager.getVisibleRows() + 1,
        // mapManager.getGridLocation().x + mapManager.getVisibleColumns() + 1),
        // defaultImage));
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

        // final int row = mapManager.getGridLocation().y + mapManager.getVisibleRows() + 1;
        // final int column = mapManager.getGridLocation().x + mapManager.getVisibleColumns() + 1;

        // assertTrue(imagesEqual((BufferedImage) loader.getImageAccessors().get(0).getImage(row, column),
        // defaultImage));

        // move by 1 tile size in x and y direction
        // mapManager.getState().move(256, 256);

        loader.update();
        synchronized (this) {
            try {
                wait(500);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        // substract 1 because we moved by 1 tilesize
        // assertFalse(imagesEqual((BufferedImage) loader.getImageAccessors().get(0).getImage(row - 1, column - 1),
        // defaultImage));
    }
}
