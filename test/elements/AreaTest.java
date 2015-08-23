package elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import model.elements.Area;
import model.elements.MultiElement;
import model.elements.Node;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AreaTest {

    private Area area;
    private static Node[] nodes;
    private static int type;

    @BeforeClass
    public static void setUpClass() {
        nodes = new Node[]{new Node(0, 0), new Node(0, 1), new Node(1, 1), new Node(1, 0)};

        type = 5;
    }

    @Before
    public void setUp() {
        area = new Area(nodes, 5);
    }

    @Test
    public void testType() {
        assertEquals(type, area.getType());
    }

    @Test
    public void testPolygon() {
        final int[] xpoints = area.getPolygon().xpoints;
        final int[] ypoints = area.getPolygon().ypoints;
        int i = 0;

        for (final Node node : nodes) {
            assertEquals(xpoints[i], node.getLocation().x);
            assertEquals(ypoints[i], node.getLocation().y);
            i++;
        }
    }

    @Test
    public void testEquals() {
        assertEquals(area, area);
        assertFalse(area.equals(new MultiElement(nodes)));
        assertFalse(area.equals(new Area(nodes, type + 1)));
        assertEquals(area, new Area(nodes, type));
    }

    @Test
    public void testHashCode() {
        assertEquals(area.hashCode(), area.hashCode());
        assertEquals(area.hashCode(), new Area(nodes, type).hashCode());
    }
}
