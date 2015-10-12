package map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.Point;
import java.util.LinkedList;

import model.elements.Area;
import model.elements.Building;
import model.elements.Label;
import model.elements.Node;
import model.elements.POI;
import model.elements.Street;
import model.elements.StreetNode;
import model.elements.Way;
import model.map.ITile;
import model.map.Tile;

import org.junit.Before;
import org.junit.Test;

public class TileTest {

    private ITile tile;
    private LinkedList<Street> streets;
    private LinkedList<Way> ways;
    private LinkedList<Building> buildings;
    private LinkedList<Area> areas;
    private LinkedList<POI> pois;

    @Before
    public void setUp() {
        streets = new LinkedList<Street>();

        final Street street = new Street(new Node[]{new Node(50, 0), new Node(80, 20), new Node(256, 156)}, 3,
                "Kaiserstrasse", 0);
        streets.add(street);

        streets.add(new Street(new Node[]{new Node(60, 150), new Node(60, 150), new Node(210, 80), new Node(200, 30)},
                2, "Waldstrasse", 1));

        final StreetNode streetNode = new StreetNode(0.9f, street);

        ways = new LinkedList<Way>();

        ways.add(new Way(new Node[]{new Node(90, 0), new Node(120, 150), new Node(10, 300)}, 3, "Testweg"));

        buildings = new LinkedList<Building>();

        buildings.add(Building.create(new Node[]{new Node(244, 119), new Node(235, 130), new Node(215, 115),
                new Node(224, 104)}, streetNode, "15"));

        areas = new LinkedList<Area>();

        pois = new LinkedList<POI>();
        pois.add(new POI(240, 150, 3));
        pois.add(new POI(120, 50, 2));
        pois.add(new POI(40, 170, 0));

        tile = new Tile(1, 3, 2, ways.toArray(new Way[1]), streets.toArray(new Street[2]), areas.toArray(new Area[0]),
                buildings.toArray(new Building[1]), pois.toArray(new POI[1]), new Label[0]);
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

    @Test
    public void testSuccessfulAddressSearch() {
        assertEquals("Waldstrasse", tile.getAddress(new Point(150, 150)));
    }

    @Test
    public void testUnsuccesfulAddressSearch() {
        assertNull(new Tile().getAddress(new Point(30, 30)));
    }
}
