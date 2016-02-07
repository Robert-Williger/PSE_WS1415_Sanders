package elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import model.elements.Area;
import model.elements.IArea;
import model.elements.IMultiElement;
import model.elements.MultiElement;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AreaTest {

    private IArea area;
    private static int[] x;
    private static int[] y;
    private static int type;

    @BeforeClass
    public static void setUpClass() {
        x = new int[]{0, 0, 1, 1};
        y = new int[]{0, 1, 1, 0};
        type = 5;
    }

    @Before
    public void setUp() {
        area = new Area(x, y, 5);
    }

    @Test
    public void testType() {
        assertEquals(type, area.getType());
    }

    @Test
    public void testEquals() {
        assertEquals(area, area);
        assertFalse(area.equals(new Area(x, y, type + 1)));
        assertEquals(area, new Area(x, y, type));
    }

    @Test
    public void testHashCode() {
        assertEquals(area.hashCode(), area.hashCode());
        assertEquals(area.hashCode(), new Area(x, y, type).hashCode());
    }
}
