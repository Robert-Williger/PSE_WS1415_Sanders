package map;

import static org.junit.Assert.assertEquals;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import model.map.IMap;
import model.map.IMapManager;
import model.map.ITile;
import model.map.Map;
import model.map.MapManager;
import model.map.PixelConverter;
import model.map.Tile;

import org.junit.Before;
import org.junit.Test;

public class MapTest {

    private IMap map;
    private IMapManager manager;

    @Before
    public void setUp() {
        final ITile[][][] tiles = new ITile[4][][];
        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = new ITile[1 << i][1 << i];
            for (int j = 0; j < tiles[i].length; j++) {
                for (int k = 0; k < tiles[i][j].length; k++) {
                    tiles[i][j][k] = new Tile(i, j, k);
                }
            }
        }

        manager = new MapManager(tiles, new Dimension(256, 256), new PixelConverter(8), 0);
        map = new Map(manager);
    }

    @Test
    public void testViewSize() {
        map.setViewSize(new Dimension(500, 500));
        assertEquals(new Dimension(500 << 3, 500 << 3), manager.getMapState().getSize());
    }

    @Test
    public void testCenterPoint() {
        map.center(new Point(256, 256));
        assertEquals(new Point(256, 256), manager.getMapState().getLocation());
    }

    @Test
    public void testCenterRectangle() {
        map.center(new Rectangle(256, 256, 128, 128));
        assertEquals(new Point(320, 320), manager.getMapState().getLocation());
    }

    @Test
    public void testMove() {
        map.zoom(3, new Point(1, 1));
        map.moveView(257, 257);
        System.out.println(map.getViewLocation());
        assertEquals(new Point(8, 8), map.getViewLocation());
    }

    @Test
    public void testZoom() {
        map.zoom(2, new Point(50, 50));
        assertEquals(2, manager.getMapState().getZoomStep());
    }
}
