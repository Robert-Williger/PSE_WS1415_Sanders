package elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import model.elements.MultiElement;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultiElementTest {

    private MultiElement multi;
    private static int[] x;
    private static int[] y;

    @BeforeClass
    public static void setUpClass() {
        x = new int[]{0, 0, 1, 1};
        y = new int[]{0, 1, 1, 0};
    }

    @Before
    public void setUp() {
        multi = new MultiElement(x, y);
    }

    @Test
    public void testEquals() {
        assertEquals(multi, multi);

        assertFalse(multi.equals(new MultiElement(new int[0], new int[0])));
        assertFalse(multi.equals(null));
        assertFalse(multi.equals(x));
        assertFalse(new MultiElement(null, null).equals(multi));
        assertEquals(multi, new MultiElement(x, y));
    }

    @Test
    public void testHashCode() {
        assertEquals(multi.hashCode(), multi.hashCode());
        assertEquals(multi.hashCode(), new MultiElement(x, y).hashCode());
    }
}
