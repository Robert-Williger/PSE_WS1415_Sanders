package elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
    private static Node[] nodes;
    private static String address;
    private static StreetNode sNode;

    @BeforeClass
    public static void setUpClass() {
        nodes = new Node[]{new Node(0, 0), new Node(0, 1), new Node(1, 1), new Node(1, 0)};

        address = "Kaiserstraße 5";
        sNode = new StreetNode(0.5f, new Street(new Node[]{new Node(0, 0), new Node(2, 2)}, 1, "Kaiserstraße", 1));
    }

    @Before
    public void setUp() {
        building = Building.create(nodes, sNode, "5");
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
    }

    @Test
    public void testEquals() {
        assertEquals(building, building);
        assertFalse(building.equals(new Area(nodes, 0)));
        assertFalse(building.equals(Building.create(nodes, sNode, "x")));
        assertFalse(building.equals(Building.create(nodes, new StreetNode(0.5f, new Street(new Node[0], 1, "Test", 1)),
                "5")));
        assertFalse(Building.create(nodes, "Kaiserstraße", "5").equals(building));
        assertEquals(building, Building.create(nodes, sNode, "5"));
    }

    @Test
    public void testHashCode() {
        assertEquals(building.hashCode(), building.hashCode());
        assertEquals(building.hashCode(), Building.create(nodes, sNode, "5").hashCode());
    }
}
