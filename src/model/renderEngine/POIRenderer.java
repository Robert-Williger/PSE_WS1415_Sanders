package model.renderEngine;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import model.AbstractModel;
import model.elements.POI;
import model.map.IPixelConverter;
import model.map.ITile;

public class POIRenderer extends AbstractModel implements IRenderer {

    private static final float REFERENCE_DISTANCE_COORD;
    private static final float REFERENCE_DISTANCE_PIXEL;
    private static int minZoomstepOffset;

    private static final Image[] poiImage;
    private static final int[] poiMinZoomStep;
    private static final Dimension imageSize;

    private IPixelConverter converter;

    static {
        REFERENCE_DISTANCE_COORD = 10000f;
        REFERENCE_DISTANCE_PIXEL = 39f;
        imageSize = new Dimension(20, 20);

        final BufferedImage defaultImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        final String[] amenities = {"viewpoint", "school", "library", "hospital", "bank", "cinema", "museum",
                "theatre", "courthouse", "playground", "restaurant", "cafe", "bar", "parking", "fuel"};
        poiMinZoomStep = new int[]{0, 6, 7, 6, 6, 7, 6, 6, 6, 6, 7, 7, 7, 7, 6, 7};
        poiImage = new Image[amenities.length + 1];
        poiImage[0] = defaultImage;

        for (int i = 0; i < amenities.length; i++) {
            final URL resource = POIRenderer.class.getResource(amenities[i] + ".png");
            if (resource != null) {
                try {
                    poiImage[i + 1] = ImageIO.read(resource);
                } catch (final IOException e) {
                    poiImage[i + 1] = defaultImage;
                }
            } else {
                poiImage[i + 1] = defaultImage;
                System.err.println("WARNING: Failed to load image " + amenities[i] + ".png");
            }
        }
    }

    public POIRenderer(final IPixelConverter converter) {
        setConverter(converter);
    }

    private static void calculateZoomOffset(final IPixelConverter converter) {
        minZoomstepOffset = 9;
        while (converter.getPixelDistancef(REFERENCE_DISTANCE_COORD, 5 + minZoomstepOffset) - REFERENCE_DISTANCE_PIXEL > 10f
                && Math.abs(minZoomstepOffset) < 10) {
            minZoomstepOffset--;
        }
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

        if (drawPOIs(tile, g)) {
            g.dispose();
            fireChange();
            return true;
        }

        g.dispose();
        return false;
    }

    private boolean drawPOIs(final ITile tile, final Graphics2D g) {
        if (tile.getPOIs() == null) {
            return false;
        }

        boolean ret = false;

        final Point location = tile.getLocation();
        final int zoom = tile.getZoomStep();

        for (final POI poi : tile.getPOIs()) {
            if (poi == null) {
                return false;
            }

            if (zoom >= poiMinZoomStep[poi.getType()] + minZoomstepOffset) {
                ret = true;
                g.drawImage(poiImage[poi.getType()], converter.getPixelDistance(poi.getX() - location.x, zoom)
                        - imageSize.width / 2, converter.getPixelDistance(poi.getY() - location.y, zoom)
                        - imageSize.height / 2, null);
            }

        }

        return ret;
    }

    @Override
    public void setConverter(final IPixelConverter converter) {
        this.converter = converter;
        calculateZoomOffset(converter);
    }
}