package elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import model.elements.IMultiElement;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultiElementTest {

    private IMultiElement multi;
    private static int[] x;
    private static int[] y;

    @BeforeClass
    public static void setUpClass() {
        x = new int[]{0, 0, 1, 1};
        y = new int[]{0, 1, 1, 0};
    }

    @Before
    public void setUp() {
        multi = new IMultiElement(x, y);
    }

    @Test
    public void testEquals() {
        assertEquals(multi, multi);

        assertFalse(multi.equals(new IMultiElement(new int[0], new int[0])));
        assertFalse(multi.equals(null));
        assertFalse(multi.equals(x));
        assertFalse(new IMultiElement(null, null).equals(multi));
        assertEquals(multi, new IMultiElement(x, y));
    }

    @Test
    public void testHashCode() {
        assertEquals(multi.hashCode(), multi.hashCode());
        assertEquals(multi.hashCode(), new IMultiElement(x, y).hashCode());
    }
}
