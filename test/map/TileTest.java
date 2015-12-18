package map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.Point;
import java.util.LinkedList;

import model.elements.IArea;
import model.elements.IBuilding;
import model.elements.Label;
import model.elements.POI;
import model.elements.IStreet;
import model.elements.StreetNode;
import model.elements.IWay;
import model.map.ITile;
import model.map.Tile;

import org.junit.Before;
import org.junit.Test;

public class TileTest {

    private ITile tile;
    private LinkedList<IStreet> iStreets;
    private LinkedList<IWay> ways;
    private LinkedList<IBuilding> iBuildings;
    private LinkedList<IArea> iAreas;
    private LinkedList<POI> pois;

    @Before
    public void setUp() {
        iStreets = new LinkedList<IStreet>();

        final IStreet iStreet = new IStreet(new int[]{50, 80, 256}, new int[]{0, 20, 156}, 3, "Kaiserstrasse", 0);
        iStreets.add(iStreet);

        iStreets.add(new IStreet(new int[]{60, 60, 210, 200}, new int[]{150, 150, 80, 30}, 2, "Waldstrasse", 1));

        final StreetNode streetNode = new StreetNode(0.9f, iStreet);

        ways = new LinkedList<IWay>();

        ways.add(new IWay(new int[]{90, 120, 10}, new int[]{0, 150, 300}, 3, "Testweg"));

        iBuildings = new LinkedList<IBuilding>();

        iBuildings.add(IBuilding.create(new int[]{244, 235, 215, 224}, new int[]{119, 130, 115, 104}, streetNode, "15"));

        iAreas = new LinkedList<IArea>();

        pois = new LinkedList<POI>();
        pois.add(new POI(240, 150, 3));
        pois.add(new POI(120, 50, 2));
        pois.add(new POI(40, 170, 0));

        tile = new Tile(1, 3, 2, ways.toArray(new IWay[1]), iStreets.toArray(new IStreet[2]), iAreas.toArray(new IArea[0]),
                iBuildings.toArray(new IBuilding[1]), pois.toArray(new POI[1]), new Label[0]);
    }

    @Test
    public void testID() {
        assertEquals(288230377762324482L, tile.getID());
    }

    @Test
    public void testRow() {
        assertEquals(3, tile.getRow());
    }

    @Test
    public void testColumn() {
        assertEquals(2, tile.getColumn());
    }

    @Test
    public void testZoomStep() {
        assertEquals(1, tile.getZoomStep());
    }

    // @Test
    // public void testLocation() {
    // assertEquals(new Point(1, 1), tile.getLocation());
    // }

    // @Test
    // public void testStreets() {
    // assertEquals(streets, tile.getStreets());
    // }
    //
    // @Test
    // public void testWays() {
    // assertEquals(ways, tile.getWays());
    // }
    //
    // @Test
    // public void testAreas() {
    // assertEquals(areas, tile.getTerrain());
    // }
    //
    // @Test
    // public void testPOIs() {
    // assertEquals(pois, tile.getPOIs());
    // }
    //
    // @Test
    // public void testBuildings() {
    // assertEquals(buildings, tile.getBuildings());
    // }

    @Test
    public void testSuccessfulBuildingSearch() {
        assertEquals(iBuildings.get(0), tile.getBuilding(new Point(225, 120)));
    }

    @Test
    public void testUnsuccessfulBuildingSearch() {
        assertNull(tile.getBuilding(new Point(20, 20)));
    }

    @Test
    public void testSuccessfulStreetNodeSearch() {
        assertEquals(new StreetNode(0, iStreets.get(0)), tile.getStreetNode(new Point(0, 0)));
    }

    @Test
    public void testUnsuccessfulStreetNodeSearch() {
        assertNull(new Tile().getStreetNode(new Point(20, 20)));
    }

    @Test
    public void testSuccessfulAddressSearch() {
        assertEquals("Waldstrasse", tile.getAddress(new Point(150, 150)));
    }

    @Test
    public void testUnsuccesfulAddressSearch() {
        assertNull(new Tile().getAddress(new Point(30, 30)));
    }
}
