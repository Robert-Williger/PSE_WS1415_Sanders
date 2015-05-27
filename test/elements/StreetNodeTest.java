package elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.util.LinkedList;

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
        final LinkedList<Node> nodes = new LinkedList<Node>();
        nodes.add(new Node(0, 0));
        nodes.add(new Node(5, 5));
        nodes.add(new Node(10, 10));
        nodes.add(new Node(20, 20));
        street = new Street(nodes, 0, "Kaiserstraße 5", 1);
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
    public void testStreet() {
        assertEquals(sNode.getStreet(), street);

        final Street newStreet = new Street(street.getNodes(), 0, "NewStreet 1", 2);
        assertFalse(sNode.getStreet().equals(newStreet));
        sNode.setStreet(newStreet);
        assertEquals(sNode.getStreet(), newStreet);
    }

    @Test
    public void testEquals() {
        assertEquals(sNode, sNode);
        assertFalse(sNode.equals(null));
        assertFalse(sNode.equals(new Node()));
        assertFalse(new StreetNode(0f, street).equals(sNode));
        assertFalse(new StreetNode(0.5f, null).equals(sNode));
        assertFalse(new StreetNode(0.5f, new Street(street.getNodes(), 0, "NewStreet 1", 2)).equals(sNode));
        assertEquals(sNode, new StreetNode(0.5f, street));
    }

    @Test
    public void testHashCode() {
        assertEquals(sNode.hashCode(), sNode.hashCode());
        assertEquals(sNode.hashCode(), new StreetNode(offset, street).hashCode());
    }
}
