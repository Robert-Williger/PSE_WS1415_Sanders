package targets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import model.targets.IPointList;
import model.targets.IPointListListener;
import model.targets.IRoutePoint;
import model.targets.PointList;
import model.targets.RoutePoint;

public class PointListTest {
    private IPointList pointList;
    private boolean error;

    @Before
    public void setUp() {
        pointList = new PointList();
    }

    @Test
    public void sizeNull() {
        assertEquals(0, pointList.size());
    }

    @Test
    public void sizeTest() {
        pointList.add(new RoutePoint());
        pointList.add(new RoutePoint());
        pointList.add(new RoutePoint());

        assertEquals(3, pointList.size());
    }

    @Test
    public void sizeTestRemove() {
        pointList.add(new RoutePoint());
        pointList.add(new RoutePoint());
        pointList.add(new RoutePoint());

        pointList.remove(1);

        assertEquals(2, pointList.size());
    }

    @Test
    public void sizeTestReset() {
        pointList.add(new RoutePoint());
        pointList.add(new RoutePoint());
        pointList.add(new RoutePoint());

        pointList.clear();

        assertEquals(0, pointList.size());
    }

    @Test
    public void removeTest() {
        final RoutePoint rp = new RoutePoint();

        pointList.add(rp);

        assertTrue(pointList.remove(rp));
    }

    @Test
    public void getTest() {
        final RoutePoint rp0 = new RoutePoint();
        final RoutePoint rp1 = new RoutePoint();
        final RoutePoint rp2 = new RoutePoint();
        final RoutePoint rp3 = new RoutePoint();

        pointList.add(rp0);
        pointList.add(rp1);
        pointList.add(rp2);
        pointList.add(rp3);

        assertEquals(rp2, pointList.get(2));
    }

    @Test
    public void changeOrderTest() {
        boolean error = false;
        final RoutePoint rp0 = new RoutePoint();
        final RoutePoint rp1 = new RoutePoint();
        final RoutePoint rp2 = new RoutePoint();
        final RoutePoint rp3 = new RoutePoint();

        pointList.add(rp0);
        pointList.add(rp1);
        pointList.add(rp2);
        pointList.add(rp3);

        pointList.changeOrder(0, 2);

        if (!pointList.get(0).equals(rp1)) {
            error = true;
        } else if (!pointList.get(1).equals(rp2)) {
            error = true;
        } else if (!pointList.get(2).equals(rp0)) {
            error = true;
        } else if (!pointList.get(3).equals(rp3)) {
            error = true;
        }

        final Iterator<IRoutePoint> it = pointList.iterator();
        int i = 0;
        while (it.hasNext()) {
            if (it.next().getListIndex() != i) {
                error = true;
            }
            i++;
        }

        assertFalse(error);
    }

    @Test
    public void removeChangeListenerTest() {
        error = true;
        pointList.add(new RoutePoint());
        pointList.addPointListListener(new IPointListListener() {

            @Override
            public void pointRemoved(final IRoutePoint point) {
                error = false;

            }

            @Override
            public void pointAdded(final IRoutePoint point) {

            }

            @Override
            public void listCleared(int oldSize) {

            }
        });

        pointList.remove(0);

        assertFalse(error);

    }

    @Test
    public void addChangeListenerTest() {
        error = true;

        pointList.addPointListListener(new IPointListListener() {

            @Override
            public void pointRemoved(final IRoutePoint point) {

            }

            @Override
            public void pointAdded(final IRoutePoint point) {
                error = false;

            }

            @Override
            public void listCleared(int oldSize) {

            }
        });

        pointList.add(new RoutePoint());

        assertFalse(error);

    }

    @Test
    public void resetChangeListenerTest() {
        error = true;
        pointList.add(new RoutePoint());
        pointList.addPointListListener(new IPointListListener() {

            @Override
            public void pointRemoved(final IRoutePoint point) {

            }

            @Override
            public void pointAdded(final IRoutePoint point) {

            }

            @Override
            public void listCleared(int oldSize) {
                error = false;
            }
        });

        pointList.clear();

        assertFalse(error);

    }

}
