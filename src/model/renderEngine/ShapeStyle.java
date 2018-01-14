package model.renderEngine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;

public class ShapeStyle {
    protected int minZoomStep;
    protected Color mainColor;
    protected Color outlineColor;

    protected Stroke[] mainStrokes;
    protected Stroke[] outlineStrokes;

    public ShapeStyle() {
        this(Integer.MAX_VALUE, 0, Color.black);
    }

    public ShapeStyle(final int minZoomStep, final float mainWidth, final Color mainColor) {
        this(minZoomStep, mainWidth, 0, mainColor, null);
    }

    public ShapeStyle(final int minZoomStep, final float mainWidth, final float outlineWidth, final Color mainColor,
            final Color outlineColor) {
        this(minZoomStep, new float[] { mainWidth }, new float[] { outlineWidth }, mainColor, outlineColor);
    }

    public ShapeStyle(final int minZoomStep, final float[] mainWidth, final float[] outlineWidth, final Color mainColor,
            final Color outlineColor) {
        this(minZoomStep, mainWidth, outlineWidth, mainColor, outlineColor, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND);
    }

    public ShapeStyle(final int minZoomStep, final float[] mainWidth, final float[] outlineWidth, final Color mainColor,
            final Color outlineColor, final int cap, final int join) {
        this.minZoomStep = minZoomStep;

        this.mainColor = mainColor;
        this.outlineColor = outlineColor;

        mainStrokes = createMainStrokes(mainWidth, cap, join);
        if (mainColor != null) {
            outlineStrokes = createOutlineStrokes(outlineWidth, mainWidth, cap, join);
        } else {
            outlineStrokes = createOutlineCompositeStrokes(outlineWidth, mainWidth, cap, join);
        }
    }

    private Stroke[] createMainStrokes(final float[] mainWidth, final int cap, final int join) {
        final Stroke[] mainStroke;
        if (mainColor != null) {
            mainStroke = new Stroke[mainWidth.length];
            for (int i = 0; i < mainWidth.length; i++) {
                mainStroke[i] = new BasicStroke(mainWidth[i], cap, join);
            }
        } else {
            mainStroke = new Stroke[1];
        }
        return mainStroke;
    }

    private Stroke[] createOutlineStrokes(final float[] outlineWidths, final float[] mainWidths, final int cap,
            final int join) {
        final Stroke[] ret;
        if (outlineColor != null) {
            ret = new Stroke[outlineWidths.length];
            for (int i = 0; i < outlineWidths.length; i++) {
                if (outlineWidths[i] > mainWidths[Math.min(i, mainWidths.length - 1)]) {
                    ret[i] = new BasicStroke(outlineWidths[i], cap, join);
                }
            }
        } else {
            ret = new Stroke[1];
        }

        return ret;
    }

    private Stroke[] createOutlineCompositeStrokes(final float[] outlineWidths, final float[] mainWidths, final int cap,
            final int join) {
        final Stroke[] ret;
        if (outlineColor != null) {
            ret = new Stroke[outlineWidths.length];
            for (int i = 0; i < outlineWidths.length; i++) {
                ret[i] = new CompositeStroke(new BasicStroke(mainWidths[i], cap, join),
                        new BasicStroke(outlineWidths[i], cap, join));
            }
        } else {
            ret = new Stroke[1];
        }

        return ret;
    }

    public boolean mainStroke(final Graphics2D g, final int zoomStep) {
        final Stroke stroke = getStroke(mainStrokes, zoomStep);
        if (stroke != null) {
            g.setStroke(stroke);
            g.setColor(mainColor);
            return true;
        }

        return false;
    }

    public boolean isVisible(final int zoomStep) {
        return zoomStep >= minZoomStep;
    }

    public boolean outlineStroke(final Graphics2D g, final int zoomStep) {
        final Stroke stroke = getStroke(outlineStrokes, zoomStep);
        if (stroke != null) {
            g.setStroke(stroke);
            g.setColor(outlineColor);
            return true;
        }

        return false;
    }

    protected final Stroke getStroke(final Stroke[] strokes, final int zoom) {
        if (zoom < minZoomStep) {
            return null;
        }
        return strokes[Math.min(strokes.length - 1, zoom - minZoomStep)];
    }

    private class CompositeStroke implements Stroke {
        private final Stroke mainStroke, outlineStroke;

        public CompositeStroke(final Stroke mainStroke, final Stroke outlineStroke) {
            this.mainStroke = mainStroke;
            this.outlineStroke = outlineStroke;
        }

        @Override
        public Shape createStrokedShape(final Shape shape) {
            return outlineStroke.createStrokedShape(mainStroke.createStrokedShape(shape));
        }
    }
}