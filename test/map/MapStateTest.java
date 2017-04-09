package map;

import static org.junit.Assert.assertEquals;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import model.map.IMapState;
import model.map.MapState;

import org.junit.Before;
import org.junit.Test;

public class MapStateTest {

    private IMapState state;

    @Before
    public void setUp() {
        state = new MapState(200, 200, 0, 15);
    }

    @Test
    public void testMaxZoomStep() {
        assertEquals(15, state.getMaxZoom());
    }

    @Test
    public void testMove() {
        state.setLocation(new Point(20, 20));
        state.move(10, 10);
        assertEquals(new Point(30, 30), state.getLocation());
    }

    @Test
    public void testLowZoomStep() {
        state.setZoom(-1);
        assertEquals(0, state.getZoom());
    }

    @Test
    public void testHighZoomStep() {
        state.setZoom(18);
        assertEquals(15, state.getZoom());
    }

    @Test
    public void testNormalZoomStep() {
        state.setZoom(12);
        assertEquals(12, state.getZoom());
    }

    @Test
    public void testSize() {
        state.setSize(new Dimension(15, 15));
        assertEquals(new Dimension(15, 15), state.getSize());
    }

    @Test
    public void testLowLocation() {
        state.setLocation(new Point(-10, -10));
        assertEquals(new Point(0, 0), state.getLocation());
    }

    @Test
    public void testHighLocation() {
        state.setSize(new Dimension(50, 50));
        state.setLocation(new Point(210, 210));
        assertEquals(new Point(150, 150), state.getLocation());
    }

    @Test
    public void testHighSize() {
        state.setSize(new Dimension(250, 250));
        // Size of cutting is higher than size of map -> cutting has to be
        // centered.
        assertEquals(new Point(-25, -25), state.getLocation());
    }

    @Test
    public void testBounds() {
        state.setCoordLocation(10, 10);
        state.setPixelSectionSize(20, 20);
        assertEquals(new Rectangle(10, 10, 20, 20), state.getBounds());
    }
}
