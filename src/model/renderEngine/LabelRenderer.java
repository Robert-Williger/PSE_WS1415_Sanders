package model.renderEngine;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Map;

import model.elements.Label;
import model.map.IPixelConverter;
import model.map.ITile;

public class LabelRenderer extends AbstractRenderer implements IRenderer {

    private static final LabelStyle[] styles;

    static {
        styles = new LabelStyle[19];

        styles[2] = new LabelStyle(8, 14, new int[]{12, 12, 12, 14, 14, 16, 16}, Color.DARK_GRAY, true);
        styles[4] = new LabelStyle(8, 14, new int[]{11, 12, 14, 16, 16, 16, 16}, Color.DARK_GRAY, true);
        styles[5] = new LabelStyle(13, 16, new int[]{11, 12, 13, 14, 14}, new Color[]{Color.GRAY, Color.GRAY,
                Color.GRAY, Color.DARK_GRAY, Color.DARK_GRAY}, true);
        styles[7] = new LabelStyle(10, 15, new int[]{10, 11, 12, 13, 14, 16}, new Color[]{Color.GRAY, Color.GRAY,
                Color.DARK_GRAY, Color.DARK_GRAY, Color.DARK_GRAY, Color.DARK_GRAY}, true);
        styles[8] = new LabelStyle(12, 16, new int[]{10, 11, 12, 13, 14}, new Color[]{Color.GRAY, Color.GRAY,
                Color.GRAY, Color.DARK_GRAY, Color.DARK_GRAY}, true);
        // case "continent":
        // return 0;
        // case "country":
        // return 1;
        // case "state":
        // return 2;
        // case "county":
        // return 3;
        // case "city":
        // return 4;
        // case "suburb":
        // return 5;
        // case "neighbourhood":
        // return 6;
        // case "town":
        // return 7;
        // case "village":
        // return 8;
        // case "hamlet":
        // return 9;
        //
        // case "region":
        // return 10;
        // case "province":
        // return 11;
        // case "district":
        // return 12;
        // case "municipality":
        // return 13;
        // case "borough":
        // return 14;
        // case "quarter":
        // return 15;
        // case "city_block":
        // return 16;
        // case "plot":
        // return 17;
        // case "isolated_dwelling":
        // return 18;

    }

    public LabelRenderer(final IPixelConverter converter) {
        setConverter(converter);
    }

    @Override
    public boolean render(final ITile tile, final Image image) {
        final Iterator<Label> iterator = tile.getLabels();

        final Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Map<?, ?> map = (Map<?, ?>) (tk.getDesktopProperty("awt.font.desktophints"));
        if (map != null) {
            g.addRenderingHints(map);
        }

        final Point tileLocation = getTileLocation(tile, image);
        final int zoom = tile.getZoomStep();

        boolean rendered = false;
        while (iterator.hasNext()) {
            rendered = true;
            final Label label = iterator.next();

            if (styles[label.getType()] != null && styles[label.getType()].mainStroke(g, zoom)) {
                // TODO avoid object generation
                if (styles[label.getType()].outlineStroke(g, zoom)) {
                    TextLayout text = new TextLayout(label.getName(), g.getFont(), g.getFontRenderContext());
                    final Shape shape = text.getOutline(g.getTransform());
                    final Rectangle2D rect = text.getBounds();

                    double x = converter.getPixelDistancef(label.getX() - tileLocation.x, tile.getZoomStep())
                            - rect.getWidth() / 2.0;
                    double y = converter.getPixelDistancef(label.getY() - tileLocation.y, tile.getZoomStep())
                            - text.getDescent() + rect.getHeight() / 2.0;

                    g.translate(x, y);
                    g.draw(shape);
                    styles[label.getType()].mainStroke(g, zoom);
                    g.drawString(label.getName(), 0, 0);
                    g.translate(-x, -y);
                } else if (styles[label.getType()].mainStroke(g, zoom)) {
                    final FontMetrics fontMetrics = g.getFontMetrics(g.getFont());
                    final int width = fontMetrics.stringWidth(label.getName());
                    final int height = fontMetrics.getHeight();
                    float x = converter.getPixelDistancef(label.getX() - tileLocation.x, tile.getZoomStep()) - width
                            / 2.0f;
                    float y = converter.getPixelDistancef(label.getY() - tileLocation.y, tile.getZoomStep())
                            - fontMetrics.getDescent() + height / 2.0f;
                    g.drawString(label.getName(), x, y);
                }

            }
        }

        g.dispose();
        if (rendered) {
            fireChange();
        }

        return rendered;
    }
}
