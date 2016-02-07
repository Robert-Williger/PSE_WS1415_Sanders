package elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import model.elements.Area;
import model.elements.IArea;
import model.elements.IBuilding;
import model.elements.IStreet;
import model.elements.Street;
import model.elements.StreetNode;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BuildingTest {

    private IBuilding building;
    private static int[] x;
    private static int[] y;
    private static String address;
    private static StreetNode sNode;

    @BeforeClass
    public static void setUpClass() {
        x = new int[]{0, 0, 1, 1};
        y = new int[]{0, 1, 1, 0};
        address = "Kaiserstraße 5";

        sNode = new StreetNode(0.5f, new Street(new int[]{0, 2}, new int[]{0, 2}, 1, "Kaiserstraße", 1));
    }

    @Before
    public void setUp() {
        building = IBuilding.create(x, y, sNode, "5");
    }

    @Test
    public void testAddress() {
        assertEquals(address, building.getAddress());
    }

    @Test
    public void testStreetNode() {
        assertEquals(sNode, building.getStreetNode());
    }

    @Test
    public void testEquals() {
        assertEquals(building, building);
        assertFalse(building.equals(new Area(x, y, 0)));
        assertFalse(building.equals(IBuilding.create(x, y, sNode, "x")));
        assertFalse(building.equals(IBuilding.create(x, y, new StreetNode(0.5f, new Street(new int[0], new int[0], 1,
                "Test", 1)), "5")));
        assertFalse(IBuilding.create(x, y, "Kaiserstraße", "5").equals(building));
        assertEquals(building, IBuilding.create(x, y, sNode, "5"));
    }

    @Test
    public void testHashCode() {
        assertEquals(building.hashCode(), building.hashCode());
        assertEquals(building.hashCode(), IBuilding.create(x, y, sNode, "5").hashCode());
    }
}
