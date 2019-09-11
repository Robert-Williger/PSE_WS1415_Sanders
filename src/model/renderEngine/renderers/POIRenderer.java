package model.renderEngine.renderers;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.PrimitiveIterator.OfInt;

import javax.imageio.ImageIO;

import model.map.IElementIterator;
import model.map.IMapManager;
import model.map.accessors.IPointAccessor;

public class POIRenderer extends AbstractRenderer implements IRenderer {
    private IElementIterator poiIterator;

    private static final Image[] poiImage;
    private static final int[] poiMinZoomStep;
    private static final Dimension imageSize;

    static {
        imageSize = new Dimension(20, 20);

        final BufferedImage defaultImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        final String[] amenities = { "viewpoint", "school", "library", "hospital", "bank", "cinema", "museum",
                "theatre", "courthouse", "playground", "restaurant", "cafe", "bar", "parking", "fuel" };
        poiMinZoomStep = new int[] { 16, 17, 16, 16, 17, 16, 16, 16, 16, 17, 17, 17, 17, 16, 17 };
        poiImage = new Image[amenities.length];

        for (int i = 0; i < amenities.length; i++) {
            final URL resource = POIRenderer.class.getResource(amenities[i] + ".png");
            if (resource != null) {
                try {
                    poiImage[i] = ImageIO.read(resource);
                } catch (final IOException e) {
                    poiImage[i] = defaultImage;
                }
            } else {
                poiImage[i] = defaultImage;
                System.err.println("WARNING: Failed to load image " + amenities[i] + ".png");
            }
        }
    }

    public POIRenderer(final IMapManager manager) {
        setMapManager(manager);
    }

    @Override
    public boolean render(final Graphics2D g, int row, int column, int zoom, int x, int y) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        return drawPOIs(g, row, column, zoom, x, y);
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        super.setMapManager(manager);

        poiIterator = manager.getElementIterator("poi");
    }

    private boolean drawPOIs(final Graphics2D g, int row, int column, int zoom, int x, int y) {
        boolean rendered = false;

        IPointAccessor poiAccessor = manager.createPointAccessor("poi");
        for (final OfInt it = poiIterator.iterator(row, column, zoom); it.hasNext();) {
            poiAccessor.setId(it.nextInt());

            final int type = poiAccessor.getType();
            if (zoom >= poiMinZoomStep[type]) {
                g.drawImage(poiImage[type],
                        converter.getPixelDistance(poiAccessor.getX() - x, zoom) - imageSize.width / 2,
                        converter.getPixelDistance(poiAccessor.getY() - y, zoom) - imageSize.height / 2, null);

                rendered = true;
            }

        }
        ;

        return rendered;
    }
}
