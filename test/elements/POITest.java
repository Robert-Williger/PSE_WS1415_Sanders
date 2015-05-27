package elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.awt.Point;

import model.elements.Node;
import model.elements.POI;

import org.junit.Before;
import org.junit.Test;

public class POITest {

    private POI poi;
    private Point loc;
    private int type;

    @Before
    public void setUp() {
        loc = new Point(1, 2);
        type = 5;
        poi = new POI(loc, type);
    }

    @Test
    public void testLocation() {
        assertEquals(poi.getLocation(), loc);
        assertEquals(new Node(1, 2).getLocation(), loc);
    }

    @Test
    public void testType() {
        assertEquals(poi.getType(), type);
    }

    @Test
    public void testEquals() {
        assertEquals(poi, poi);
        assertFalse(poi.equals(null));
        assertFalse(new POI(loc, 0).equals(poi));
        assertFalse(poi.equals(new Node()));
        assertEquals(poi, new POI(loc, type));
    }

    @Test
    public void testHashCode() {
        assertEquals(poi.hashCode(), poi.hashCode());
        assertEquals(poi.hashCode(), new POI(loc, type).hashCode());
    }
}
