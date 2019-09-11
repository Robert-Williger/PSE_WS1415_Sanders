package model.renderEngine.schemes.styles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;

public class LabelStyle {

    private final int minZoomStep;
    private final Font[] fonts;
    private final Color[] colors;
    private final Stroke[] strokes;

    private static final Font font = new Font("Arial", Font.BOLD, 20);

    public LabelStyle(final int minZoomStep, final int maxZoomStep, final int[] sizes, final Color color,
            final boolean outline) {
        this(minZoomStep, maxZoomStep, sizes, convertColor(color, sizes.length), outline);
    }

    public LabelStyle(final int minZoomStep, final int maxZoomStep, final int[] sizes, final Color[] colors,
            final boolean outline) {
        int zoomSteps = maxZoomStep - minZoomStep + 1;

        fonts = new Font[zoomSteps];

        for (int i = 0; i < zoomSteps; i++) {
            fonts[i] = font.deriveFont((float) sizes[i]);
        }

        if (outline) {
            strokes = new Stroke[zoomSteps];
            for (int i = 0; i < zoomSteps; i++) {
                strokes[i] = new BasicStroke(sizes[i] / 6f);
            }
        } else {
            strokes = null;
        }
        this.colors = colors;
        this.minZoomStep = minZoomStep;
    }

    public boolean mainStroke(final Graphics2D g, final int zoom) {
        int relativeZoom = zoom - minZoomStep;
        if (relativeZoom >= 0 && relativeZoom < fonts.length) {
            g.setColor(colors[relativeZoom]);
            g.setFont(fonts[relativeZoom]);
            return true;
        }

        return false;
    }

    public boolean isVisible(final int zoom) {
        int relativeZoom = zoom - minZoomStep;
        return (relativeZoom >= 0 && relativeZoom < fonts.length);
    }

    public Font getFont(final int zoom) {
        return fonts[zoom - minZoomStep];
    }

    public boolean outlineStroke(final Graphics2D g, final int zoom) {
        if (strokes != null) {
            int relativeZoom = zoom - minZoomStep;
            if (relativeZoom >= 0 && relativeZoom < fonts.length) {
                g.setColor(Color.white);
                g.setFont(fonts[relativeZoom]);
                g.setStroke(strokes[relativeZoom]);
                return true;
            }
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
