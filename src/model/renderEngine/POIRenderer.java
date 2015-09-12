package model.renderEngine;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.ImageIO;

import model.elements.POI;
import model.map.IPixelConverter;
import model.map.ITile;

public class POIRenderer extends AbstractRenderer implements IRenderer {

    private static final Image[] poiImage;
    private static final int[] poiMinZoomStep;
    private static final Dimension imageSize;

    static {
        imageSize = new Dimension(20, 20);

        final BufferedImage defaultImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        final String[] amenities = {"viewpoint", "school", "library", "hospital", "bank", "cinema", "museum",
                "theatre", "courthouse", "playground", "restaurant", "cafe", "bar", "parking", "fuel"};
        poiMinZoomStep = new int[]{16, 17, 16, 16, 17, 16, 16, 16, 16, 17, 17, 17, 17, 16, 17};
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

    public POIRenderer(final IPixelConverter converter) {
        setConverter(converter);
    }

    @Override
    public boolean render(final ITile tile, final Image image) {
        if (tile == null || image == null) {
            return false;
        }

        final Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        final Point location = getTileLocation(tile, image);

        if (drawPOIs(tile, location, g)) {
            g.dispose();
            fireChange();
            return true;
        }

        g.dispose();
        return false;
    }

    private boolean drawPOIs(final ITile tile, final Point location, final Graphics2D g) {
        final Iterator<POI> iterator = tile.getPOIs();
        if (tile.getPOIs() == null) {
            return false;
        }

        boolean ret = false;

        final int zoom = tile.getZoomStep();

        while (iterator.hasNext()) {
            final POI poi = iterator.next();
            if (poi == null) {
                return false;
            }

            if (zoom >= poiMinZoomStep[poi.getType()]) {
                ret = true;
                g.drawImage(poiImage[poi.getType()], converter.getPixelDistance(poi.getX() - location.x, zoom)
                        - imageSize.width / 2, converter.getPixelDistance(poi.getY() - location.y, zoom)
                        - imageSize.height / 2, null);
            }

        }

        return ret;
    }
}