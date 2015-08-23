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
import model.map.MapManager;
import model.map.PixelConverter;
import model.map.Tile;
import model.renderEngine.RenderRoute;
import model.renderEngine.RouteRenderer;
import model.targets.PointList;
import model.targets.RoutePoint;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RouteRendererTest {

    private static Street[] streets;
    private static BufferedImage emptyImage;
    private static BufferedImage renderImage;
    private static RouteRenderer renderer;
    private static Street street;
    private static PointList pList;
    private static Street[] doubleStreets;
    private RenderRoute route;

    private final static int streetId = 7;
    private final static int doubleStreetID = 9;
    private final static int IMAGE_HEIGHT = 256;
    private final static int IMAGE_WIDTH = 256;

    @BeforeClass
    public static void setUpClass() {
        renderer = new RouteRenderer(new PixelConverter(1));

        streets = new Street[1];
        street = new Street(new Node[]{new Node(0, 0), new Node(5, 5), new Node(0, 1)}, 1, "Kaiserstrasse", streetId);
        streets[0] = street;

        doubleStreets = new Street[2];
        final Street newStreet = new Street(new Node[]{new Node(0, 0), new Node(2, 2), new Node(0, 1)}, 1,
                "Teststraße", doubleStreetID);
        doubleStreets[0] = newStreet;
        doubleStreets[1] = new Street(new Node[]{new Node(0, 0), new Node(5, 5), new Node(0, 1)}, 1, "Testgasse",
                doubleStreetID);

        final MapManager emptyMapManager = new MapManager();
        final RoutePoint rPoint1 = new RoutePoint(emptyMapManager);
        rPoint1.setStreetNode(new StreetNode(0.2f, newStreet));
        final RoutePoint rPoint2 = new RoutePoint(emptyMapManager);
        rPoint2.setStreetNode(new StreetNode(0.8f, newStreet));

        pList = new PointList();
        pList.add(rPoint1);
        pList.add(rPoint2);

        renderImage = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        emptyImage = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        final Graphics g = emptyImage.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        g.dispose();
    }

    @Before
    public void setUp() {
        final Graphics g = renderImage.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        g.dispose();

        route = new RenderRoute(0, null, pList);
        renderer.setRenderRoute(route);
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
    public void testFullStreetRouteRendering() {
        route.addStreet(streetId);

        final Tile tile = new Tile(6, 1, 1, 1, 1, new Way[0], new Street[0], new Area[0], new Building[0], new POI[0]);
        assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }

    @Test
    public void testStreetPartRouteRendering() {
        route.addStreetPart(streetId, 0.1f, 0.4f);

        final Tile tile = new Tile(6, 1, 1, 1, 1, new Way[0], streets, new Area[0], new Building[0], new POI[0]);
        assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }

    @Test
    public void testMultiStreetPartRouteRendering() {
        route.addStreetPart(streetId, 0.3f, 0.4f);
        route.addStreetPart(streetId, 0.4f, 0.5f);
        route.addStreetPart(streetId, 0.3f, 0.2f);
        route.addStreetPart(streetId, 0.7f, 0.8f);

        final Tile tile = new Tile(6, 1, 1, 1, 1, new Way[0], streets, new Area[0], new Building[0], new POI[0]);
        assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }

    @Test
    public void testInvalidRouteRendering() {
        route.addStreet(streetId + 3);

        final Tile tile = new Tile(6, 1, 1, 1, 1, new Way[0], streets, new Area[0], new Building[0], new POI[0]);
        assertTrue(renderer.render(tile, renderImage));
        assertFalse(imageChanged());
    }

    @Test
    public void testNullRouteRendering() {
        renderer.setRenderRoute(null);
        final Tile tile = new Tile(6, 1, 1, 1, 1, new Way[0], streets, new Area[0], new Building[0], new POI[0]);
        assertTrue(renderer.render(tile, renderImage));
        assertFalse(imageChanged());
    }

    @Test
    public void testNullSteetsRendering() {
        route.addStreet(streetId);

        final Tile tile = new Tile(6, 1, 1, 1, 1, new Way[0], null, new Area[0], new Building[0], new POI[0]);
        assertFalse(renderer.render(tile, renderImage));
        assertFalse(imageChanged());
    }

    @Test
    public void testFullSameStreetIDRendering() {
        route.addStreet(doubleStreetID);

        final Tile tile = new Tile(6, 1, 1, 1, 1, new Way[0], doubleStreets, new Area[0], new Building[0], new POI[0]);
        assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }

    @Test
    public void testPartSameStreetIDRendering() {
        route.addStreetPart(doubleStreetID, 0.2f, 0.8f);

        final Tile tile = new Tile(6, 1, 1, 1, 1, new Way[0], doubleStreets, new Area[0], new Building[0], new POI[0]);
        assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }

    @Test
    public void testMultiPartSameStreetIDRendering() {
        route.addStreetPart(doubleStreetID, 0.2f, 0.3f);
        route.addStreetPart(doubleStreetID, 0.5f, 0.8f);

        final Tile tile = new Tile(6, 1, 1, 1, 1, new Way[0], doubleStreets, new Area[0], new Building[0], new POI[0]);
        assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }
}
