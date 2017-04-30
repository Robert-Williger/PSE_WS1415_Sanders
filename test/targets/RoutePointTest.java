package targets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import model.targets.IPointListener;
import model.targets.IRoutePoint;
import model.targets.RoutePoint;

public class RoutePointTest {
    private IRoutePoint routePoint;
    private boolean error;

    @Before
    public void setUp() {
        routePoint = new RoutePoint();
    }

    @Test
    public void indexTest() {
        final int index = 2;

        routePoint.setListIndex(index);
        assertEquals(index, routePoint.getListIndex());
    }

    @Test
    public void listIndexChangeListenerTest() {
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

            @Override
            public void targetIndexChanged() {

            }
        });

        routePoint.setListIndex(2);
        assertFalse(error);
    }

    @Test
    public void targetIndexChangeListenerTest() {
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

            }

            @Override
            public void targetIndexChanged() {
                error = false;
            }
        });

        routePoint.setTargetIndex(2);
        assertFalse(error);
    }

    @Test
    public void initialStateTest() {
        assertEquals(IRoutePoint.State.unadded, routePoint.getState());
    }

    @Test
    public void stateTest() {
        final IRoutePoint.State state = IRoutePoint.State.added;

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

            @Override
            public void targetIndexChanged() {

            }
        });

        routePoint.setState(IRoutePoint.State.added);
        assertFalse(error);
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

            @Override
            public void targetIndexChanged() {

            }
        });

        routePoint.setLocation(123, 2314);
        assertFalse(error);
    }

}
