package renderEngine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;


import model.map.IMapManager;
import model.map.MapManager;
import model.renderEngine.ImageFetcher;
import model.renderEngine.renderers.IRenderer;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ImageFetcherTest {
    private static ImageFetcher fetcher;
    private static volatile boolean isRendered = false;
    private static DummyRenderer renderer;
    private final long defaultID = -2;

    private static class DummyRenderer implements IRenderer {

        public long lastTileID;

        public DummyRenderer() {}

        @Override
        public boolean render(long tileID, Image image) {
            lastTileID = tileID;

            isRendered = true;
            return true;
        }

        @Override
        public void setMapManager(IMapManager manager) {

        }

    }

    @BeforeClass
    public static void setUpClass() {
        renderer = new DummyRenderer();
    }

    @Before
    public void setUp() {
        renderer.lastTileID = defaultID;
        isRendered = false;
        fetcher = new ImageFetcher(renderer, new MapManager());
    }

    public boolean imagesEqual(final BufferedImage image1, final BufferedImage image2) {
        final DataBufferInt db1 = (DataBufferInt) image1.getRaster().getDataBuffer();
        final DataBufferInt db2 = (DataBufferInt) image2.getRaster().getDataBuffer();

        boolean compare = true;

        for (int bank = 0; bank < db1.getNumBanks() && compare; bank++) {
            final int[] actual = db1.getData(bank);
            final int[] expected = db2.getData(bank);
            compare = Arrays.equals(actual, expected);
        }

        return compare;
    }

    @Test(timeout = 10000)
    public void testLoadImage() {
        final long tileID = 35;
        fetcher.loadImage(tileID, 0);

        while (!isRendered) {}

        assertEquals(tileID, renderer.lastTileID);
    }

    @Test(timeout = 10000)
    public void testFlush() {
        final long tileID = 16;
        fetcher.loadImage(tileID, 0);

        while (!isRendered) {}

        assertEquals(tileID, renderer.lastTileID);
        renderer.lastTileID = defaultID;
        fetcher.flush();
        assertNull(fetcher.getImage(tileID));
    }

}
