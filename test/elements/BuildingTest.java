package elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.LinkedList;

import model.elements.Area;
import model.elements.Building;
import model.elements.Node;
import model.elements.Street;
import model.elements.StreetNode;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BuildingTest {

    private Building building;
    private static LinkedList<Node> nodes;
    private static String address;
    private static StreetNode sNode;

    @BeforeClass
    public static void setUpClass() {
        nodes = new LinkedList<Node>();
        nodes.add(new Node(0, 0));
        nodes.add(new Node(0, 1));
        nodes.add(new Node(1, 1));
        nodes.add(new Node(1, 0));

        final LinkedList<Node> street = new LinkedList<Node>();
        street.add(new Node(0, 0));
        street.add(new Node(2, 2));

        address = "Kaiserstraße 5";
        sNode = new StreetNode(0.5f, new Street(street, 1, "Kaiserstraße", 1));
    }

    @Before
    public void setUp() {
        building = new Building(nodes, address, sNode);
    }

    @Test
    public void testAddress() {
        assertEquals(address, building.getAddress());
    }

    @Test
    public void testPolygon() {
        final int[] xpoints = building.getPolygon().xpoints;
        final int[] ypoints = building.getPolygon().ypoints;
        int i = 0;

        for (final Node node : nodes) {
            assertEquals(xpoints[i], node.getLocation().x);
            assertEquals(ypoints[i], node.getLocation().y);
            i++;
        }
    }

    @Test
    public void testStreetNode() {
        assertEquals(sNode, building.getStreetNode());

        final LinkedList<Node> n = new LinkedList<Node>();
        n.add(new Node(0, 0));
        n.add(new Node(2, 2));
        final StreetNode s = new StreetNode(0.5f, new Street(n, 1, "Test", 1));

        building.setStreetNode(s);
        assertEquals(s, building.getStreetNode());
        assertFalse(sNode.equals(building.getStreetNode()));
    }

    @Test
    public void testEquals() {
        assertEquals(building, building);
        assertFalse(building.equals(new Area(nodes, 0)));
        assertFalse(building.equals(new Building(nodes, address + "x", sNode)));
        assertFalse(building.equals(new Building(nodes, address, new StreetNode(0.5f, new Street(
                new LinkedList<Node>(), 1, "Test", 1)))));
        assertFalse(new Building(nodes, address, null).equals(building));
        assertFalse(new Building(nodes, null, sNode).equals(building));
        assertEquals(building, new Building(nodes, address, sNode));
    }

    @Test
    public void testHashCode() {
        assertEquals(building.hashCode(), building.hashCode());
        assertEquals(building.hashCode(), new Building(nodes, address, sNode).hashCode());
    }
}
