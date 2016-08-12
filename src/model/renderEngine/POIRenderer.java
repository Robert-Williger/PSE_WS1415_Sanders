package model.renderEngine;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.ImageIO;

import model.elements.dereferencers.IPOIDereferencer;
import model.map.IMapManager;

public class POIRenderer extends AbstractRenderer implements IRenderer {
    private Graphics2D g;
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

    public POIRenderer(final IMapManager manager) {
        super(manager);
    }

    @Override
    public boolean render(final long tile, final Image image) {
        setTileID(tile);

        g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        if (drawPOIs()) {
            g.dispose();
            fireChange();
            return true;
        }

        g.dispose();
        return false;
    }

    private boolean drawPOIs() {
        final Iterator<Integer> iterator = tile.getPOIs();

        boolean ret = false;

        final IPOIDereferencer dereferencer = tile.getPOIDereferencer();
        while (iterator.hasNext()) {
            final int poi = iterator.next();
            dereferencer.setID(poi);

            final int type = dereferencer.getType();
            if (zoom >= poiMinZoomStep[type]) {
                ret = true;
                g.drawImage(poiImage[type], converter.getPixelDistance(dereferencer.getX() - x, zoom) - imageSize.width
                        / 2, converter.getPixelDistance(dereferencer.getY() - y, zoom) - imageSize.height / 2, null);
            }

        }

        return ret;
    }
}