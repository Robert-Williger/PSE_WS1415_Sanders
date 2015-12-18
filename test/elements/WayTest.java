package elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import model.elements.IWay;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class WayTest {
    private IWay iWay;
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
        iWay = new IWay(x, y, type, name);
    }

    @Test
    public void testType() {
        assertEquals(iWay.getType(), type);
    }

    @Test
    public void testName() {
        assertEquals(iWay.getName(), name);
    }

    @Test
    public void testEquals() {
        assertEquals(iWay, iWay);
        assertFalse(new IWay(x, y, type, null).equals(iWay));
        assertFalse(iWay.equals(null));
        assertFalse(iWay.equals(x));
        assertFalse(iWay.equals(new IWay(x, y, type, name + "x")));
        assertFalse(iWay.equals(new IWay(x, y, 0, name)));
        assertEquals(iWay, new IWay(x, y, type, name));
    }

    @Test
    public void testHashCode() {
        assertEquals(iWay.hashCode(), iWay.hashCode());
        assertEquals(iWay.hashCode(), new IWay(x, y, type, name).hashCode());
    }
}
