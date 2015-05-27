package elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.awt.Point;

import model.elements.Node;

import org.junit.Before;
import org.junit.Test;

public class NodeTest {
    private Node node;
    private Point loc;

    @Before
    public void setUp() {
        loc = new Point(1, 2);
        node = new Node(loc.x, loc.y);
    }

    @Test
    public void testLocation() {
        assertEquals(node.getLocation(), loc);
        assertEquals(new Node(1, 2).getLocation(), loc);
    }

    @Test
    public void testEquals() {
        assertEquals(node, node);
        assertFalse(node.equals(new Node()));
        assertFalse(node.equals(null));
        assertFalse(node.equals(loc));
        assertEquals(node, new Node(loc.x, loc.y));
    }

    @Test
    public void testHashCode() {
        assertEquals(node.hashCode(), node.hashCode());
        assertEquals(node.hashCode(), new Node(loc.x, loc.y).hashCode());
    }
}
