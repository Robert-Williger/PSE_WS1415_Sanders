package renderEngine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics;
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
import model.map.PixelConverter;
import model.map.Tile;
import model.renderEngine.BackgroundRenderer;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BackgroundRendererTest {

    private static BufferedImage emptyImage;
    private static BufferedImage renderImage;
    private static BackgroundRenderer renderer;
    private static Area[] areas;
    private static Way[] ways;
    private static Street[] streets;
    private static Building[] buildings;

    private final static int IMAGE_HEIGHT = 256;
    private final static int IMAGE_WIDTH = 256;

    @BeforeClass
    public static void setUpClass() {
        renderer = new BackgroundRenderer(new PixelConverter(1));

        areas = new Area[]{new Area(new Node[]{new Node(2, 2), new Node(5, 5), new Node(10, 3), new Node(3, 0)}, 1)};
        ways = new Way[]{new Way(new Node[]{new Node(90, 0), new Node(120, 150), new Node(10, 300)}, 3, "Testweg")};

        final Street street = new Street(new Node[]{new Node(0, 0), new Node(5, 5), new Node(20, 10)}, 1,
                "Kaiserstrasse", 0);

        streets = new Street[]{
                street,
                new Street(new Node[]{new Node(60, 150), new Node(60, 150), new Node(210, 80), new Node(200, 30)}, 2,
                        "Waldstrasse", 1)};

        final StreetNode streetNode = new StreetNode(0.9f, street);

        buildings = new Building[]{new Building(new Node[]{new Node(2, 2), new Node(5, 5), new Node(10, 3),
                new Node(3, 0)}, "Teststraße 15", streetNode)};

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
        assertFalse(renderer.render(null, renderImage));
        assertFalse(renderer.render(new Tile(), null));
        assertFalse(renderer.render(null, null));
    }

    @Test
    public void testAreaRendering() {
        final Tile tile = new Tile(1, 1, 1, 1, 1, new Way[0], new Street[0], areas, new Building[0], new POI[0]);
        assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }

    @Test
    public void testNullAreaRendering() {
        final Tile tile = new Tile(1, 1, 1, 1, 1, new Way[0], new Street[0], null, new Building[0], new POI[0]);
        assertFalse(renderer.render(tile, renderImage));
        assertFalse(imageChanged());
    }

    @Test
    public void testWayRendering() {
        final Tile tile = new Tile(1, 1, 1, 1, 1, ways, new Street[0], new Area[0], new Building[0], new POI[0]);
        assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }

    @Test
    public void testNullWayRendering() {
        final Tile tile = new Tile(1, 1, 1, 1, 1, null, new Street[0], new Area[0], new Building[0], new POI[0]);
        assertFalse(renderer.render(tile, renderImage));
        assertFalse(imageChanged());
    }

    @Test
    public void testStreetRendering() {
        final Tile tile = new Tile(4, 1, 1, 1, 1, new Way[0], streets, new Area[0], new Building[0], new POI[0]);
        assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }

    @Test
    public void testNullStreetRendering() {
        final Tile tile = new Tile(4, 1, 1, 1, 1, new Way[0], null, new Area[0], new Building[0], new POI[0]);
        assertFalse(renderer.render(tile, renderImage));
        assertFalse(imageChanged());
    }

    @Test
    public void testBuildingRendering() {
        final Tile tile = new Tile(6, 1, 1, 1, 1, new Way[0], new Street[0], new Area[0], buildings, new POI[0]);
        assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }

    @Test
    public void testNullBuildingRendering() {
        final Tile tile = new Tile(6, 1, 1, 1, 1, new Way[0], new Street[0], new Area[0], null, new POI[0]);
        assertFalse(renderer.render(tile, renderImage));
        assertFalse(imageChanged());
    }

    @Test
    public void testFullTileRendering() {
        final Tile tile = new Tile(1, 1, 1, 1, 1, ways, streets, areas, buildings, new POI[0]);
        assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }
}
