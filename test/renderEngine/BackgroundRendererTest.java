package renderEngine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
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
    private static LinkedList<Area> areas;
    private static LinkedList<Way> ways;
    private static LinkedList<Street> streets;
    private static LinkedList<Building> buildings;

    private final static int IMAGE_HEIGHT = 256;
    private final static int IMAGE_WIDTH = 256;

    @BeforeClass
    public static void setUpClass() {
        renderer = new BackgroundRenderer(new PixelConverter(1));

        areas = new LinkedList<Area>();
        final List<Node> areaNodes = new LinkedList<Node>();
        areaNodes.add(new Node(2, 2));
        areaNodes.add(new Node(5, 5));
        areaNodes.add(new Node(10, 3));
        areaNodes.add(new Node(3, 0));
        areas.add(new Area(areaNodes, 1));

        ways = new LinkedList<Way>();
        final List<Node> wayNodes = new LinkedList<Node>();
        wayNodes.add(new Node(90, 0));
        wayNodes.add(new Node(120, 150));
        wayNodes.add(new Node(10, 300));
        ways.add(new Way(wayNodes, 3, "Testweg"));

        streets = new LinkedList<Street>();
        final List<Node> streetNodes = new LinkedList<Node>();
        streetNodes.add(new Node(0, 0));
        streetNodes.add(new Node(5, 5));
        streetNodes.add(new Node(20, 10));
        final Street street = new Street(streetNodes, 1, "Kaiserstrasse", 0);
        streets.add(street);

        final LinkedList<Node> streetNodes2 = new LinkedList<Node>();
        streetNodes2.add(new Node(60, 150));
        streetNodes2.add(new Node(60, 150));
        streetNodes2.add(new Node(210, 80));
        streetNodes2.add(new Node(200, 30));
        streets.add(new Street(streetNodes2, 2, "Waldstrasse", 1));

        final StreetNode streetNode = new StreetNode(0.9f, street);

        buildings = new LinkedList<Building>();
        final List<Node> buildingNodes = new LinkedList<Node>();
        buildingNodes.add(new Node(2, 2));
        buildingNodes.add(new Node(5, 5));
        buildingNodes.add(new Node(10, 3));
        buildingNodes.add(new Node(3, 0));
        buildings.add(new Building(buildingNodes, "Teststraße 15", streetNode));

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
        final Tile tile = new Tile(1, 1, 1, 1, 1, new LinkedList<Way>(), new LinkedList<Street>(), areas,
                new LinkedList<Building>(), new LinkedList<POI>());
        assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }

    @Test
    public void testNullAreaRendering() {
        final Tile tile = new Tile(1, 1, 1, 1, 1, new LinkedList<Way>(), new LinkedList<Street>(), null,
                new LinkedList<Building>(), new LinkedList<POI>());
        assertFalse(renderer.render(tile, renderImage));
        assertFalse(imageChanged());
    }

    @Test
    public void testWayRendering() {
        final Tile tile = new Tile(1, 1, 1, 1, 1, ways, new LinkedList<Street>(), new LinkedList<Area>(),
                new LinkedList<Building>(), new LinkedList<POI>());
        assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }

    @Test
    public void testNullWayRendering() {
        final Tile tile = new Tile(1, 1, 1, 1, 1, null, new LinkedList<Street>(), new LinkedList<Area>(),
                new LinkedList<Building>(), new LinkedList<POI>());
        assertFalse(renderer.render(tile, renderImage));
        assertFalse(imageChanged());
    }

    @Test
    public void testStreetRendering() {
        final Tile tile = new Tile(4, 1, 1, 1, 1, new LinkedList<Way>(), streets, new LinkedList<Area>(),
                new LinkedList<Building>(), new LinkedList<POI>());
        assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }

    @Test
    public void testNullStreetRendering() {
        final Tile tile = new Tile(4, 1, 1, 1, 1, new LinkedList<Way>(), null, new LinkedList<Area>(),
                new LinkedList<Building>(), new LinkedList<POI>());
        assertFalse(renderer.render(tile, renderImage));
        assertFalse(imageChanged());
    }

    @Test
    public void testBuildingRendering() {
        final Tile tile = new Tile(6, 1, 1, 1, 1, new LinkedList<Way>(), new LinkedList<Street>(),
                new LinkedList<Area>(), buildings, new LinkedList<POI>());
        assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }

    @Test
    public void testNullBuildingRendering() {
        final Tile tile = new Tile(6, 1, 1, 1, 1, new LinkedList<Way>(), new LinkedList<Street>(),
                new LinkedList<Area>(), null, new LinkedList<POI>());
        assertFalse(renderer.render(tile, renderImage));
        assertFalse(imageChanged());
    }

    @Test
    public void testFullTileRendering() {
        final Tile tile = new Tile(1, 1, 1, 1, 1, ways, streets, areas, buildings, new LinkedList<POI>());
        assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }
}
