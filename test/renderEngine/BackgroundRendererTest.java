package renderEngine;

//import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

//import model.elements.Area;
//import model.elements.IArea;
//import model.elements.IBuilding;
//import model.elements.Label;
//import model.elements.POI;
//import model.elements.IStreet;
//import model.elements.Street;
//import model.elements.StreetNode;
//import model.elements.IWay;
//import model.elements.Way;
//import model.map.PixelConverter;
//import model.map.Tile;
//import model.renderEngine.BackgroundRenderer;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BackgroundRendererTest {

    private static BufferedImage emptyImage;
    private static BufferedImage renderImage;
    // private static BackgroundRenderer renderer;
    // private static IArea[] iAreas;
    // private static IWay[] ways;
    // private static IStreet[] iStreets;
    // private static IBuilding[] iBuildings;

    private final static int IMAGE_HEIGHT = 256;
    private final static int IMAGE_WIDTH = 256;

    @BeforeClass
    public static void setUpClass() {
        // renderer = new BackgroundRenderer(new PixelConverter(1));
        //
        // iAreas = new IArea[]{new Area(new int[]{2, 5, 10, 3}, new int[]{2, 5, 3, 0}, 1)};
        // ways = new IWay[]{new Way(new int[]{90, 120, 10}, new int[]{0, 150, 300}, 3, "Testweg")};
        //
        // final IStreet iStreet = new Street(new int[]{0, 5, 20}, new int[]{0, 5, 10}, 1, "Kaiserstrasse", 0);
        //
        // iStreets = new IStreet[]{iStreet,
        // new Street(new int[]{60, 60, 210, 200}, new int[]{150, 150, 80, 30}, 2, "Waldstrasse", 1)};
        //
        // final StreetNode streetNode = new StreetNode(0.9f, iStreet);
        //
        // iBuildings = new IBuilding[]{IBuilding.create(new int[]{2, 5, 10, 3}, new int[]{2, 5, 3, 0}, streetNode,
        // "15")};

        renderImage = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        emptyImage = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        final Graphics g = emptyImage.getGraphics();
        g.setColor(new Color(241, 238, 232));
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        g.dispose();
    }

    @Before
    public void setUp() {
        final Graphics g = renderImage.getGraphics();
        g.setColor(new Color(241, 238, 232));
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        g.dispose();
    }

    public boolean imageChanged() {
        final DataBufferInt dbActual = (DataBufferInt) renderImage.getRaster().getDataBuffer();
        final DataBufferInt dbExpected = (DataBufferInt) emptyImage.getRaster().getDataBuffer();

        boolean compare = true;

        for (int bank = 0; bank < dbActual.getNumBanks() && compare; bank++) {
            final int[] actual = dbActual.getData(bank);
            final int[] expected = dbExpected.getData(bank);
            compare = Arrays.equals(actual, expected);
        }

        return !compare;
    }

    @Test
    public void nullParameterTest() {
        // assertFalse(renderer.render(null, renderImage));
        // assertFalse(renderer.render(new Tile(), null));
        // assertFalse(renderer.render(null, null));
    }

    @Test
    public void testAreaRendering() {
        // final Tile tile = new Tile(1, 1, 1, new IWay[0], new IStreet[0], iAreas, new IBuilding[0], new POI[0], new
        // Label[0]);
        // assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }

    @Test
    public void testWayRendering() {
        // final Tile tile = new Tile(1, 1, 1, ways, new IStreet[0], new IArea[0], new IBuilding[0], new POI[0], new
        // Label[0]);
        // assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }

    @Test
    public void testStreetRendering() {
        // final Tile tile = new Tile(4, 1, 1, new IWay[0], iStreets, new IArea[0], new IBuilding[0], new POI[0], new
        // Label[0]);
        // assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }

    @Test
    public void testBuildingRendering() {
        // final Tile tile = new Tile(6, 1, 1, new IWay[0], new IStreet[0], new IArea[0], iBuildings, new POI[0], new
        // Label[0]);
        // assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }

    @Test
    public void testFullTileRendering() {
        // final Tile tile = new Tile(1, 1, 1, ways, iStreets, iAreas, iBuildings, new POI[0], new Label[0]);
        // assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }
}
