import static org.junit.Assert.assertEquals;

import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;

import model.MapManagerReader;
import model.elements.Area;
import model.elements.Building;
import model.elements.Node;
import model.elements.POI;
import model.elements.Street;
import model.elements.StreetNode;
import model.elements.Way;
import model.map.IMapManager;
import model.map.ITile;

import org.junit.BeforeClass;
import org.junit.Test;

public class MapManagerReaderTest {

    private static ITile tile;
    private static IMapManager manager;
    private static Street street;

    @BeforeClass
    public static void setUpClass() {
        final MapManagerReader reader = new MapManagerReader();
        try {
            manager = reader
                    .readMapManager(new DataInputStream(new BufferedInputStream(new FileInputStream("map.tst"))));
            tile = manager.getTile(0);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        final LinkedList<Node> nodes = new LinkedList<Node>();
        nodes.add(new Node(-10, -5));
        nodes.add(new Node(80, 90));
        nodes.add(new Node(400, 210));
        nodes.add(new Node(520, 325));
        street = new Street(nodes, 0, "Teststraße", 0);

    }

    @Test
    public void testPOIs() {
        assertEquals(1, tile.getPOIs().size());
        for (final POI poi : tile.getPOIs()) {
            assertEquals(new POI(new Point(125, 375), 2), poi);
        }
    }

    @Test
    public void testBuildings() {
        assertEquals(1, tile.getBuildings().size());
        for (final Building building : tile.getBuildings()) {
            final LinkedList<Node> nodes = new LinkedList<Node>();
            nodes.add(new Node(100, 100));
            nodes.add(new Node(150, 100));
            nodes.add(new Node(150, 150));
            nodes.add(new Node(100, 150));
            assertEquals(new Building(nodes, "Teststraße 2b", new StreetNode(0.2f, street)), building);
        }
    }

    @Test
    public void testStreets() {
        assertEquals(1, tile.getStreets().size());
        for (final Street street : tile.getStreets()) {
            assertEquals(street, MapManagerReaderTest.street);
        }
    }

    @Test
    public void testAreas() {
        assertEquals(1, tile.getTerrain().size());
        for (final Area terrain : tile.getTerrain()) {
            final LinkedList<Node> nodes = new LinkedList<Node>();
            nodes.add(new Node(250, 200));
            nodes.add(new Node(450, 200));
            nodes.add(new Node(450, 400));
            nodes.add(new Node(400, 400));
            assertEquals(new Area(nodes, 1), terrain);
        }
    }

    @Test
    public void testWays() {
        assertEquals(1, tile.getWays().size());
        for (final Way way : tile.getWays()) {
            final LinkedList<Node> nodes = new LinkedList<Node>();
            nodes.add(new Node(25, -5));
            nodes.add(new Node(150, 15));
            nodes.add(new Node(350, 540));
            assertEquals(new Way(nodes, 1, "Testweg"), way);
        }
    }
}
