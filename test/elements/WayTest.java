package elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import model.elements.Way;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class WayTest {
    private Way way;
    private int type;
    private String name;
    private static int[] x;
    private static int[] y;

    @BeforeClass
    public static void setUpClass() {
        x = new int[]{0, 0, 1, 1};
        y = new int[]{0, 1, 1, 0};
    }

    @Before
    public void setUp() {
        type = 2;
        name = "Rhein";
        way = new Way(x, y, type, name);
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
        assertFalse(new Way(x, y, type, null).equals(way));
        assertFalse(way.equals(null));
        assertFalse(way.equals(x));
        assertFalse(way.equals(new Way(x, y, type, name + "x")));
        assertFalse(way.equals(new Way(x, y, 0, name)));
        assertEquals(way, new Way(x, y, type, name));
    }

    @Test
    public void testHashCode() {
        assertEquals(way.hashCode(), way.hashCode());
        assertEquals(way.hashCode(), new Way(x, y, type, name).hashCode());
    }
}
