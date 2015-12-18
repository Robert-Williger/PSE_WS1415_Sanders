package elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import model.elements.IStreet;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StreetTest {
    private IStreet iStreet;
    private int type;
    private String name;
    private int id;
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
        id = 1;
        iStreet = new IStreet(x, y, type, name, id);
    }

    @Test
    public void testLength() {
        assertEquals(iStreet.getLength(), 3);
    }

    @Test
    public void testId() {
        assertEquals(iStreet.getID(), id);
    }

    @Test
    public void testEquals() {
        assertEquals(iStreet, iStreet);
        assertFalse(iStreet.equals(null));
        assertFalse(iStreet.equals(new IStreet(x, y, type, name + "x", id)));
        assertFalse(iStreet.equals(new IStreet(x, y, type, name, 0)));
        assertEquals(iStreet, new IStreet(x, y, type, name, id));
    }

    @Test
    public void testHashCode() {
        assertEquals(iStreet.hashCode(), iStreet.hashCode());
        assertEquals(iStreet.hashCode(), new IStreet(x, y, type, name, id).hashCode());
    }
}
