package model.renderEngine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;

import model.map.IPixelConverter;

public class ShapeStyle {

    public class CompositeStroke implements Stroke {
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

    public static final float SCALE_FACTOR = 120f;

    protected static IPixelConverter converter = null;

    protected final float mainWidth;
    protected final float outlineWidth;
    protected final Color mainColor;
    protected final Color outlineColor;
    protected final int cap;
    protected final int join;

    protected final float maxMainWidth;
    protected final float maxOutlineWidth;

    // add minZoomstep & maxZoomstep -> zoom only in range (-> width)
    public ShapeStyle(final float mainWidth, final float outlineWidth, final Color mainColor, final Color outlineColor,
            final int cap, final int join) {
        this.mainWidth = mainWidth;
        this.outlineWidth = outlineWidth;

        this.mainColor = mainColor;
        this.outlineColor = outlineColor;

        this.cap = cap;
        this.join = join;

        maxMainWidth = Float.MAX_VALUE;
        maxOutlineWidth = Float.MAX_VALUE;
    }

    public ShapeStyle(final float mainWidth, final float outlineWidth, final Color mainColor, final Color outlineColor) {
        this(mainWidth, outlineWidth, mainColor, outlineColor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    public ShapeStyle(final float mainWidth, final Color mainColor, final int cap, final int join) {
        this(mainWidth, 0f, mainColor, null, cap, join);
    }

    public ShapeStyle(final float mainWidth, final Color mainColor) {
        this(mainWidth, mainColor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    public ShapeStyle(final float[] mainWidth, final float[] outlineWidth, final Color mainColor,
            final Color outlineColor, final int cap, final int join) {
        this.mainWidth = mainWidth[0];
        maxMainWidth = mainWidth[1];
        this.outlineWidth = outlineWidth[0];
        maxOutlineWidth = outlineWidth[1];

        this.mainColor = mainColor;
        this.outlineColor = outlineColor;

        this.cap = cap;
        this.join = join;
    }

    public ShapeStyle(final float[] mainWidth, final float[] outlineWidth, final Color mainColor,
            final Color outlineColor) {
        this(mainWidth, outlineWidth, mainColor, outlineColor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    public ShapeStyle(final float[] mainWidth, final Color mainColor, final int cap, final int join) {
        this(mainWidth, new float[]{0f, 0f}, mainColor, null, cap, join);
    }

    public ShapeStyle(final float[] mainWidth, final Color mainColor) {
        this(mainWidth, mainColor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    public static void setConverter(final IPixelConverter converter) {
        ShapeStyle.converter = converter;
    }

    public boolean mainStroke(final Graphics2D g, final int zoomStep) {
        if (mainColor != null) {
            g.setStroke(new BasicStroke(Math.min(maxMainWidth,
                    converter.getPixelDistancef((SCALE_FACTOR * mainWidth), zoomStep)), cap, join));
            g.setColor(mainColor);
            return true;
        }

        return false;
    }

    public boolean outlineStroke(final Graphics2D g, final int zoomStep) {
        if (outlineColor != null) {
            g.setStroke(new BasicStroke(Math.min(maxOutlineWidth,
                    converter.getPixelDistancef((SCALE_FACTOR * outlineWidth), zoomStep)), cap, join));
            g.setColor(outlineColor);
            return true;
        }

        return false;
    }

    public boolean outlineCompositeStroke(final Graphics2D g, final int zoomStep) {
        if (mainColor == null) {
            g.setStroke(new CompositeStroke(new BasicStroke(Math.min(maxMainWidth,
                    converter.getPixelDistancef((SCALE_FACTOR * mainWidth), zoomStep)), cap, join), new BasicStroke(
                    Math.min(maxOutlineWidth, converter.getPixelDistancef((SCALE_FACTOR * outlineWidth), zoomStep)),
                    cap, join)));
            g.setColor(outlineColor);
            return true;
        }

        return false;

    }
}