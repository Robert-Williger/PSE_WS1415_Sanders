package model.targets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.awt.Point;

import model.elements.Street;
import model.elements.StreetNode;
import model.map.MapManager;

import org.junit.Before;
import org.junit.Test;

public class RoutePointTest {
    private IRoutePoint routePoint;
    private boolean error;

    @Before
    public void setUp() {
        routePoint = new RoutePoint(new MapManager());
    }

    @Test
    public void addressTest() {
        final String adress = "Test 1234";
        routePoint.setAddress(adress);
        assertEquals(adress, routePoint.getAddress());
    }

    @Test
    public void addressChangeListenerTest() {
        error = true;

        routePoint.addPointListener(new IPointListener() {

            @Override
            public void stateChanged() {

            }

            @Override
            public void locationChanged() {

            }

            @Override
            public void listIndexChanged() {

            }

            @Override
            public void addressChanged() {
                error = false;
            }
        });

        routePoint.setAddress("Test");
        assertFalse(error);
    }

    @Test
    public void streetNodeTest() {
        final StreetNode node = new StreetNode(12, new Street(new int[0], 1, "Test", 142313));

        routePoint.setStreetNode(node);
        assertEquals(node, routePoint.getStreetNode());
    }

    @Test
    public void streetNodeChangeListenerTest() {
        error = true;

        routePoint.addPointListener(new IPointListener() {

            @Override
            public void stateChanged() {

            }

            @Override
            public void locationChanged() {
                error = false;
            }

            @Override
            public void listIndexChanged() {

            }

            @Override
            public void addressChanged() {

            }
        });

        routePoint.setStreetNode(new StreetNode(12, new Street(new int[0], 1, "Test", 142313)));
        assertFalse(error);
    }

    @Test
    public void indexTest() {
        final int index = 2;

        routePoint.setListIndex(index);
        assertEquals(index, routePoint.getListIndex());
    }

    @Test
    public void indexChangeListenerTest() {
        error = true;

        routePoint.addPointListener(new IPointListener() {

            @Override
            public void stateChanged() {

            }

            @Override
            public void locationChanged() {

            }

            @Override
            public void listIndexChanged() {
                error = false;
            }

            @Override
            public void addressChanged() {

            }
        });

        routePoint.setListIndex(2);
        assertFalse(error);
    }

    @Test
    public void initialStateTest() {
        assertEquals(PointState.unadded, routePoint.getState());
    }

    @Test
    public void stateTest() {
        final PointState state = PointState.added;

        routePoint.setState(state);
        assertEquals(state, routePoint.getState());
    }

    @Test
    public void stateChangeListenerTest() {
        error = true;

        routePoint.addPointListener(new IPointListener() {

            @Override
            public void stateChanged() {
                error = false;

            }

            @Override
            public void locationChanged() {

            }

            @Override
            public void listIndexChanged() {

            }

            @Override
            public void addressChanged() {

            }
        });

        routePoint.setState(PointState.added);
        assertFalse(error);
    }

    @Test
    public void noLocationTest() {
        assertEquals(null, routePoint.getLocation());
    }

    @Test
    public void locationTest() {
        final Point point = new Point(123, 2314);

        routePoint.setLocation(point);
        assertEquals(point, routePoint.getLocation());
    }

    @Test
    public void locationChangeListenerTest() {
        error = true;

        routePoint.addPointListener(new IPointListener() {

            @Override
            public void stateChanged() {

            }

            @Override
            public void locationChanged() {
                error = false;
            }

            @Override
            public void listIndexChanged() {

            }

            @Override
            public void addressChanged() {

            }
        });

        routePoint.setLocation(new Point(123, 2314));
        assertFalse(error);
    }

}
