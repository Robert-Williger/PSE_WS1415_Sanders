package elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import model.elements.MultiElement;
import model.elements.Node;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultiElementTest {

    private MultiElement multi;
    private static Node[] nodes;

    @BeforeClass
    public static void setUpClass() {
        nodes = new Node[]{new Node(0, 0), new Node(0, 1), new Node(1, 1), new Node(1, 0)};
    }

    @Before
    public void setUp() {
        multi = new MultiElement(nodes);
    }

    @Test
    public void testEquals() {
        assertEquals(multi, multi);

        assertFalse(multi.equals(new MultiElement(new Node[0])));
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
