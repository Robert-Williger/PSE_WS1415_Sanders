package elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.LinkedList;

import model.elements.MultiElement;
import model.elements.Node;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultiElementTest {

    private MultiElement multi;
    private static LinkedList<Node> nodes;

    @BeforeClass
    public static void setUpClass() {
        nodes = new LinkedList<Node>();
        nodes.add(new Node(0, 0));
        nodes.add(new Node(0, 1));
        nodes.add(new Node(1, 1));
        nodes.add(new Node(1, 0));
    }

    @Before
    public void setUp() {
        multi = new MultiElement(nodes);
    }

    @Test
    public void testNodes() {
        assertEquals(multi.getNodes(), nodes);
        final LinkedList<Node> newNodes = new LinkedList<Node>(nodes);
        newNodes.add(new Node());
        assertFalse(multi.getNodes().equals(newNodes));
    }

    @Test
    public void testEquals() {
        assertEquals(multi, multi);

        final LinkedList<Node> newNodes = new LinkedList<Node>(nodes);
        newNodes.add(new Node());
        assertFalse(multi.equals(new MultiElement(newNodes)));
        assertFalse(multi.equals(null));
        assertFalse(multi.equals(nodes));
        assertFalse(new MultiElement(null).equals(multi));
        assertEquals(multi, new MultiElement(nodes));
    }

    @Test
    public void testHashCode() {
        assertEquals(multi.hashCode(), multi.hashCode());
        assertEquals(multi.hashCode(), new MultiElement(nodes).hashCode());
    }
}
