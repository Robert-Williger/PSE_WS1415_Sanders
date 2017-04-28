package model.routing;

import static org.junit.Assert.assertNotNull;

import model.map.MapManager;

import org.junit.Before;
import org.junit.Test;

public class RouteManagerTest {

    private RouteManager rM;

    @Before
    public void setUp() {
        rM = new RouteManager(new DirectedGraph(0, new int[0], new int[0], new int[0], new int[0]), new MapManager());
    }

    @Test
    public void testGetPointList() {
        assertNotNull(rM.getPointList());
    }
}
