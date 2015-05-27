package model.routing;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import model.map.MapManager;

import org.junit.Before;
import org.junit.Test;

public class RouteManagerTest {

    private RouteManager rM;

    @Before
    public void setUp() {
        rM = new RouteManager(new Graph(0, new ArrayList<Long>(), new ArrayList<Integer>()), new MapManager());
    }

    @Test
    public void testCreatRoutePoint() {
        assertNotNull(rM.createPoint());
    }

    @Test
    public void testGetPointList() {
        assertNotNull(rM.getPointList());
    }
}
