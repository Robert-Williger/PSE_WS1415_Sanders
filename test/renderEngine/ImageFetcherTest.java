package renderEngine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.event.ChangeListener;

import model.elements.IArea;
import model.elements.IBuilding;
import model.elements.Label;
import model.elements.POI;
import model.elements.IStreet;
import model.elements.StreetNode;
import model.elements.IWay;
import model.map.AddressNode;
import model.map.IMapManager;
import model.map.IMapState;
import model.map.IPixelConverter;
import model.map.ITile;
import model.map.Tile;
import model.renderEngine.AbstractImageFetcher;
import model.renderEngine.HighlyCachedImageFetcher;
import model.renderEngine.IRenderer;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ImageFetcherTest {
    private static AbstractImageFetcher fetcher;
    private static volatile boolean isRendered = false;
    private static DummyRenderer renderer;
    private final long defaultID = -2;
    private Image defaultImage;

    private static class DummyRenderer implements IRenderer {

        public long lastTileID;

        public DummyRenderer() {
        }

        @Override
        public void addChangeListener(final ChangeListener listener) {
        }

        @Override
        public void removeChangeListener(final ChangeListener listener) {
        }

        @Override
        public boolean render(final ITile tile, final Image image) {
            if (tile != null) {
                lastTileID = tile.getID();
            }

            isRendered = true;
            return true;
        }

        @Override
        public void setConverter(final IPixelConverter converter) {
        }

    }

    private class DummyMapManager implements IMapManager {
        private final Dimension imageSize;

        public DummyMapManager(final Dimension size) {
            imageSize = size;
        }

        @Override
        public ITile getTile(final long tileID) {
            return new DummyTile(tileID);
        }

        @Override
        public ITile getTile(final Point coordinate, final int zoomStep) {
            return null;
        }

        @Override
        public ITile getTile(final int row, final int column, final int zoomStep) {
            return null;
        }

        @Override
        public Dimension getTileSize() {
            return imageSize;
        }

        @Override
        public AddressNode getAddress(final Point coordinate) {
            return null;
        }

        @Override
        public int getVisibleRows() {
            return 0;
        }

        @Override
        public int getVisibleColumns() {
            return 0;
        }

        @Override
        public Point getGridLocation() {
            return null;
        }

        @Override
        public Point getCoord(final Point pixelPoint) {
            return null;
        }

        @Override
        public Point getPixel(final Point coordinate) {
            return null;
        }

        @Override
        public IMapState getState() {
            return null;
        }

        @Override
        public IPixelConverter getConverter() {
            return null;
        }

    }

    private class DummyTile implements ITile {
        private final long ID;

        public DummyTile(final long ID) {
            this.ID = ID;
        }

        @Override
        public long getID() {
            return ID;
        }

        @Override
        public int getRow() {
            return 0;
        }

        @Override
        public int getColumn() {
            return 0;
        }

        @Override
        public int getZoomStep() {
            return 0;
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return null;
        }

        @Override
        public Iterator<IWay> getWays() {
            return null;
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return null;
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return null;
        }

        @Override
        public Iterator<POI> getPOIs() {
            return null;
        }

        @Override
        public StreetNode getStreetNode(final Point coordinate) {
            return null;
        }

        @Override
        public IBuilding getBuilding(final Point coordinate) {
            return null;
        }

        @Override
        public Iterator<Label> getLabels() {
            return null;
        }

    }

    @BeforeClass
    public static void setUpClass() {
        final Tile[][][] tileArray = new Tile[1][1][1];
        tileArray[0][0][0] = new Tile(0, 0, 0);
        renderer = new DummyRenderer();
    }

    @Before
    public void setUp() {
        renderer.lastTileID = defaultID;
        isRendered = false;
        fetcher = new HighlyCachedImageFetcher(renderer, new DummyMapManager(new Dimension(1, 1)));
        defaultImage = fetcher.getImage(0);
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

        while (!isRendered) {
        }

        assertEquals(tileID, renderer.lastTileID);
    }

    @Test(timeout = 10000)
    public void testFlush() {
        final long tileID = 16;
        fetcher.loadImage(tileID, 0);

        while (!isRendered) {
        }

        assertEquals(tileID, renderer.lastTileID);
        renderer.lastTileID = defaultID;
        fetcher.flush();
        assertTrue(imagesEqual((BufferedImage) defaultImage, (BufferedImage) fetcher.getImage(tileID)));
    }

    @Test
    public void testSetMapManager() {
        final BufferedImage image = (BufferedImage) fetcher.getImage(0);
        fetcher.setMapManager(new DummyMapManager(new Dimension(2, 2)));
        final BufferedImage image2 = (BufferedImage) fetcher.getImage(0);

        assertFalse(image.getHeight() != image2.getHeight() && image.getWidth() != image2.getWidth());
    }

    @Test
    public void testGetRenderer() {
        assertEquals(renderer, fetcher.getRenderer());
    }
}
