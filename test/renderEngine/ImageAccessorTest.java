package renderEngine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Image;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.map.IMapManager;
import model.map.MapManager;
import model.renderEngine.IImageFetcher;
import model.renderEngine.ImageAccessor;

import org.junit.Before;
import org.junit.Test;

public class ImageAccessorTest {

    private static ImageAccessor accessor;
    private static MapManager manager;

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
        public void setMapManager(final IMapManager manager) {
        }

        @Override
        public void addChangeListener(ChangeListener listener) {

        }

        @Override
        public void removeChangeListener(ChangeListener listener) {

        }
    }

    private boolean changed;
    private DummyFetcher fetcher;

    @Before
    public void setUp() {
        manager = new MapManager();
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
        accessor.getImage(0, 0, 0);
        assertEquals(0, fetcher.lastGetImageId);
    }
}
