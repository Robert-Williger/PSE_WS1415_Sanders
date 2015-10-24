package elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Point;

import model.elements.Node;
import model.elements.Street;
import model.elements.StreetNode;

import org.junit.Before;
import org.junit.Test;

public class StreetNodeTest {

    private StreetNode sNode;
    private Street street;
    private float offset;

    @Before
    public void setUp() {
        street = new Street(new int[]{0, 5, 10, 20}, new int[]{0, 5, 10, 20}, 0, "Kaiserstraße 5", 1);
        offset = 0.5f;
        sNode = new StreetNode(offset, street);
    }

    @Test
    public void testLocation() {
        assertEquals(sNode.getLocation(), new Point(10, 10));
    }

    @Test
    public void testOffset() {
        assertTrue(Math.abs(sNode.getOffset() - offset) < 0.00001f);
    }

    @Test
    public void testEquals() {
        assertEquals(sNode, sNode);
        assertFalse(sNode.equals(null));
        assertFalse(sNode.equals(new Node()));
        assertFalse(new StreetNode(0f, street).equals(sNode));
        assertEquals(sNode, new StreetNode(0.5f, street));
    }

    @Test
    public void testHashCode() {
        assertEquals(sNode.hashCode(), sNode.hashCode());
        assertEquals(sNode.hashCode(), new StreetNode(offset, street).hashCode());
    }
}
