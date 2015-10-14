package renderEngine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Dimension;
import java.awt.Image;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.map.DefaultTileSource;
import model.map.IMapManager;
import model.map.IPixelConverter;
import model.map.ITile;
import model.map.MapManager;
import model.map.MapState;
import model.map.PixelConverter;
import model.map.Tile;
import model.renderEngine.IImageFetcher;
import model.renderEngine.IRenderer;
import model.renderEngine.ImageAccessor;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ImageAccessorTest {

    private static ImageAccessor accessor;
    private static MapManager manager;

    private class DummyRenderer implements IRenderer {

        @Override
        public void addChangeListener(final ChangeListener listener) {
        }

        @Override
        public void removeChangeListener(final ChangeListener listener) {
        }

        @Override
        public boolean render(final ITile tile, final Image image) {
            return false;
        }

        @Override
        public void setConverter(final IPixelConverter converter) {
        }

    }

    private class DummyFetcher implements IImageFetcher {
        long lastGetImageId = -1;

        @Override
        public void flush() {
        }

        @Override
        public Image getImage(final long id) {
            lastGetImageId = id;
            return null;
        }

        @Override
        public void loadImage(final long id, final int priority) {
        }

        @Override
        public IRenderer getRenderer() {
            return new DummyRenderer();
        }

        @Override
        public void setMapManager(final IMapManager manager) {
        }
    }

    @BeforeClass
    public static void setUpClass() {
        final Tile[][][] tiles = new Tile[4][][];
        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = new Tile[1 << i][1 << i];
            for (int j = 0; j < tiles[i].length; j++) {
                for (int k = 0; k < tiles[i][j].length; k++) {
                    tiles[i][j][k] = new Tile(i, j, k);
                }
            }
        }

        manager = new MapManager(new DefaultTileSource(tiles, 0), new PixelConverter(1),
                new MapState(2048, 2048, 0, 2), new Dimension(256, 256));
    }

    private boolean changed;
    private DummyFetcher fetcher;

    @Before
    public void setUp() {
        fetcher = new DummyFetcher();
        accessor = new ImageAccessor(manager, fetcher);
        accessor.setMapManager(manager);
        accessor.setVisible(false);
        changed = false;
    }

    @Test
    public void testVisibility() {
        accessor.setVisible(true);
        assertTrue(accessor.isVisible());
        accessor.setVisible(false);
        assertFalse(accessor.isVisible());
    }

    @Test
    public void testRows() {
        assertEquals(manager.getRows(), accessor.getRows());
    }

    @Test
    public void testColumns() {
        assertEquals(manager.getColumns(), accessor.getColumns());
    }

    @Test
    public void testListener() {
        final ChangeListener listener = new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                changed = true;
            }
        };
        accessor.addChangeListener(listener);

        accessor.setVisible(true);
        assertTrue(changed);
        changed = false;

        accessor.setVisible(true);
        assertFalse(changed);
        changed = false;

        accessor.removeChangeListener(listener);
        accessor.setVisible(false);
        assertFalse(changed);
    }

    @Test
    public void testGetImage() {
        accessor.getImage(0, 0);
        assertEquals(0, fetcher.lastGetImageId);
    }
}
