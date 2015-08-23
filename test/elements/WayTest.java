package elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import model.elements.Node;
import model.elements.Way;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class WayTest {
    private Way way;
    private int type;
    private String name;
    private static Node[] nodes;

    @BeforeClass
    public static void setUpClass() {
        nodes = new Node[]{new Node(0, 0), new Node(0, 1), new Node(1, 1), new Node(1, 0)};
    }

    @Before
    public void setUp() {
        type = 2;
        name = "Rhein";
        way = new Way(nodes, type, name);
    }

    @Test
    public void testType() {
        assertEquals(way.getType(), type);
    }

    @Test
    public void testName() {
        assertEquals(way.getName(), name);
    }

    @Test
    public void testEquals() {
        assertEquals(way, way);
        assertFalse(new Way(nodes, type, null).equals(way));
        assertFalse(way.equals(null));
        assertFalse(way.equals(nodes));
        assertFalse(way.equals(new Way(nodes, type, name + "x")));
        assertFalse(way.equals(new Way(nodes, 0, name)));
        assertEquals(way, new Way(nodes, type, name));
    }

    @Test
    public void testHashCode() {
        assertEquals(way.hashCode(), way.hashCode());
        assertEquals(way.hashCode(), new Way(nodes, type, name).hashCode());
    }
}
