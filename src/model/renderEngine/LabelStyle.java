package model.renderEngine;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class LabelStyle {

    private final int minZoomStep;
    private final int maxZoomStep;
    private final Font[] fonts;
    private final Color[] colors;

    private static final Font font = new Font("Times New Roman", Font.BOLD, 20);

    public LabelStyle(final int minZoomStep, final int maxZoomStep, final int[] size, final Color color) {
        this(minZoomStep, maxZoomStep, size, convertColor(color, size.length));
    }

    public LabelStyle(final int minZoomStep, final int maxZoomStep, final int[] size, final Color[] colors) {
        int zoomSteps = maxZoomStep - minZoomStep + 1;

        fonts = new Font[zoomSteps];
        for (int i = 0; i < zoomSteps; i++) {
            fonts[i] = font.deriveFont((float) size[i]);
        }
        this.colors = colors;
        this.minZoomStep = minZoomStep;
        this.maxZoomStep = maxZoomStep;
    }

    public boolean draw(final Graphics2D g, final int zoom) {
        if (zoom >= minZoomStep && zoom <= maxZoomStep) {
            int relativeZoom = zoom - minZoomStep;
            g.setColor(colors[relativeZoom]);
            g.setFont(fonts[relativeZoom]);
            return true;
        }

        return false;
    }

    private static Color[] convertColor(final Color color, final int length) {
        final Color[] colors = new Color[length];
        for (int i = 0; i < length; i++) {
            colors[i] = color;
        }
        return colors;
    }
}
