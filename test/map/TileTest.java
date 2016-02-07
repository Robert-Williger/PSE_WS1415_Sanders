package map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.Point;
import java.util.LinkedList;

import model.elements.IArea;
import model.elements.IBuilding;
import model.elements.Label;
import model.elements.POI;
import model.elements.Street;
import model.elements.StreetNode;
import model.elements.IWay;
import model.elements.Way;
import model.map.ITile;
import model.map.Tile;

import org.junit.Before;
import org.junit.Test;

public class TileTest {

    private ITile tile;
    private LinkedList<Street> streets;
    private LinkedList<IWay> ways;
    private LinkedList<IBuilding> buildings;
    private LinkedList<IArea> areas;
    private LinkedList<POI> pois;

    @Before
    public void setUp() {
        streets = new LinkedList<Street>();

        final Street Street = new Street(new int[]{50, 80, 256}, new int[]{0, 20, 156}, 3, "Kaiserstrasse", 0);
        streets.add(Street);

        streets.add(new Street(new int[]{60, 60, 210, 200}, new int[]{150, 150, 80, 30}, 2, "Waldstrasse", 1));

        final StreetNode streetNode = new StreetNode(0.9f, Street);

        ways = new LinkedList<IWay>();

        ways.add(new Way(new int[]{90, 120, 10}, new int[]{0, 150, 300}, 3, "Testweg"));

        buildings = new LinkedList<IBuilding>();

        buildings.add(IBuilding.create(new int[]{244, 235, 215, 224}, new int[]{119, 130, 115, 104}, streetNode, "15"));

        areas = new LinkedList<IArea>();

        pois = new LinkedList<POI>();
        pois.add(new POI(240, 150, 3));
        pois.add(new POI(120, 50, 2));
        pois.add(new POI(40, 170, 0));

        tile = new Tile(1, 3, 2, ways.toArray(new IWay[1]), streets.toArray(new Street[2]), areas.toArray(new IArea[0]),
                buildings.toArray(new IBuilding[1]), pois.toArray(new POI[1]), new Label[0]);
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
        assertEquals(buildings.get(0), tile.getBuilding(new Point(225, 120)));
    }

    @Test
    public void testUnsuccessfulBuildingSearch() {
        assertNull(tile.getBuilding(new Point(20, 20)));
    }

    @Test
    public void testSuccessfulStreetNodeSearch() {
        assertEquals(new StreetNode(0, streets.get(0)), tile.getStreetNode(new Point(0, 0)));
    }

    @Test
    public void testUnsuccessfulStreetNodeSearch() {
        assertNull(new Tile().getStreetNode(new Point(20, 20)));
    }
}
