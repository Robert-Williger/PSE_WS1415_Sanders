package elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import model.elements.Street;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StreetTest {
    private Street street;
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
        street = new Street(x, y, type, name, id);
    }

    @Test
    public void testLength() {
        assertEquals(street.getLength(), 3);
    }

    @Test
    public void testId() {
        assertEquals(street.getID(), id);
    }

    @Test
    public void testEquals() {
        assertEquals(street, street);
        assertFalse(street.equals(null));
        assertFalse(street.equals(new Street(x, y, type, name + "x", id)));
        assertFalse(street.equals(new Street(x, y, type, name, 0)));
        assertEquals(street, new Street(x, y, type, name, id));
    }

    @Test
    public void testHashCode() {
        assertEquals(street.hashCode(), street.hashCode());
        assertEquals(street.hashCode(), new Street(x, y, type, name, id).hashCode());
    }
}
