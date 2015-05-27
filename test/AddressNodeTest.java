import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;

import model.elements.Node;
import model.elements.Street;
import model.elements.StreetNode;
import model.map.AddressNode;

import org.junit.Before;
import org.junit.Test;

public class AddressNodeTest {

    private AddressNode addressNode;
    private StreetNode streetNode;

    @Before
    public void setUp() {
        final LinkedList<Node> nodes = new LinkedList<Node>();
        nodes.add(new Node(0, 0));
        streetNode = new StreetNode(0.5f, new Street(nodes, 0, "Teststraße", 0));
        addressNode = new AddressNode("Teststraße 42", streetNode);
    }

    @Test
    public void testAddress() {
        assertEquals("Teststraße 42", addressNode.getAddress());
    }

    @Test
    public void testStreetNode() {
        assertEquals(streetNode, addressNode.getStreetNode());
    }

    @Test
    public void testEquals() {
        assertTrue(addressNode.equals(addressNode));
        assertFalse(addressNode.equals(null));
        assertFalse(addressNode.equals(streetNode));
        final AddressNode node2 = new AddressNode(null, streetNode);
        assertTrue(addressNode.equals(new AddressNode("Teststraße 42", streetNode)));
        assertFalse(addressNode.equals(node2));
        assertFalse(node2.equals(addressNode));
        assertFalse(new AddressNode("Teststraße 42", null).equals(addressNode));

        final LinkedList<Node> nodes = new LinkedList<Node>();
        nodes.add(new Node(0, 0));
        assertFalse(addressNode.equals(new AddressNode("Teststraße 42", new StreetNode(0.6f,
                new Street(nodes, 0, "", 0)))));
    }

    @Test
    public void testHashCode() {
        assertEquals(addressNode.hashCode(), addressNode.hashCode());
        assertEquals(new AddressNode("Teststraße 42", streetNode).hashCode(), addressNode.hashCode());
    }
}
