package renderEngine;

//import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

//import model.elements.IArea;
//import model.elements.IBuilding;
//import model.elements.Label;
//import model.elements.POI;
//import model.elements.IStreet;
//import model.elements.IWay;
//import model.map.PixelConverter;
//import model.map.Tile;
//import model.renderEngine.POIRenderer;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class POIRendererTest {

    private static BufferedImage emptyImage;
    private static BufferedImage renderImage;
    // private static POIRenderer renderer;
    // private static POI[] pois;

    private final static int IMAGE_HEIGHT = 256;
    private final static int IMAGE_WIDTH = 256;

    @BeforeClass
    public static void setUpClass() {
        // renderer = new POIRenderer(new PixelConverter(1));
        // pois = new POI[1];
        // pois[0] = new POI(0, 0, 3);

        renderImage = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        emptyImage = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        final Graphics g = emptyImage.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        g.dispose();
    }

    @Before
    public void setUp() {
        final Graphics g = renderImage.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        g.dispose();
    }

    public boolean imageChanged() {
        final DataBufferInt dbActual = (DataBufferInt) renderImage.getRaster().getDataBuffer();
        final DataBufferInt dbExpected = (DataBufferInt) emptyImage.getRaster().getDataBuffer();

        boolean compare = true;

        for (int bank = 0; bank < dbActual.getNumBanks() && compare; bank++) {
            final int[] actual = dbActual.getData(bank);
            final int[] expected = dbExpected.getData(bank);
            compare = Arrays.equals(actual, expected);
        }

        return !compare;
    }

    @Test
    public void nullParameterTest() {
        // assertFalse(renderer.render(null, renderImage));
        // assertFalse(renderer.render(new Tile(), null));
        // assertFalse(renderer.render(null, null));
    }

    @Test
    public void testPOIRendering() {
        // final Tile tile = new Tile(17, 0, 0, new IWay[0], new IStreet[0], new IArea[0], new IBuilding[0], pois,
        // new Label[0]);
        // assertTrue(renderer.render(tile, renderImage));
        assertTrue(imageChanged());
    }
}
