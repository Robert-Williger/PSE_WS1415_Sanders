import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
        streetNode = new StreetNode(0.5f, new Street(new int[]{0, 0}, 0, "Teststraße", 0, false));
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

        assertFalse(addressNode.equals(new AddressNode("Teststraße 42", new StreetNode(0.6f, new Street(
                new int[]{0, 0}, 0, "", 0, false)))));
    }

    @Test
    public void testHashCode() {
        assertEquals(addressNode.hashCode(), addressNode.hashCode());
        assertEquals(new AddressNode("Teststraße 42", streetNode).hashCode(), addressNode.hashCode());
    }
}
