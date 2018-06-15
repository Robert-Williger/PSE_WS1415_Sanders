package model.renderEngine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

public class WayStyle extends ShapeStyle {
    protected final Color middleLineColor;

    protected final Stroke[] middleLineStrokes;

    public WayStyle(final float mainWidth, final float outlineWidth, final float middleLineWidth, final Color mainColor,
            final Color outlineColor, final Color middleLineColor, final int cap, final int join, final float dash,
            final float dashSpacing) {

        this(0, new float[] { mainWidth }, new float[] { outlineWidth }, new float[] { middleLineWidth }, mainColor,
                outlineColor, middleLineColor, cap, join, new float[] { dash }, new float[] { dashSpacing });
    }

    public WayStyle(final float mainWidth, final float outlineWidth, final float middleLineWidth, final Color mainColor,
            final Color outlineColor, final Color middleLineColor, final int cap, final int join, final float dash) {
        this(mainWidth, outlineWidth, middleLineWidth, mainColor, outlineColor, middleLineColor, cap, join, dash, -1f);
    }

    public WayStyle(final float mainWidth, final float outlineWidth, final Color mainColor, final Color outlineColor,
            final int cap, final int join) {
        this(mainWidth, outlineWidth, 0f, mainColor, outlineColor, null, cap, join, 0f, 0f);
    }

    public WayStyle(final float mainWidth, final float outlineWidth, final Color mainColor, final Color outlineColor) {
        this(mainWidth, outlineWidth, 0f, mainColor, outlineColor, null, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                0, 0);
    }

    public WayStyle(final float mainWidth, final Color mainColor, final int cap, final int join) {
        this(mainWidth, 0f, mainColor, null, cap, join);
    }

    public WayStyle(final float mainWidth, final Color mainColor) {
        this(mainWidth, mainColor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    public WayStyle(final int minZoomStep, final float[] mainWidth, final float[] outlineWidth,
            final float[] middleLineWidth, final Color mainColor, final Color outlineColor, final Color middleLineColor,
            final int cap, final int join, final float[] dash, final float[] dashSpacing) {
        super(minZoomStep, mainWidth, outlineWidth, mainColor, outlineColor, cap, join);

        this.middleLineColor = middleLineColor;
        middleLineStrokes = createMiddleLineStrokes(middleLineWidth, dash, dashSpacing);

        float max = 0.f;
        final int zoom = 16;
        if (minZoomStep <= zoom) {
            max = Math.max(max, mainWidth[Math.min(mainWidth.length - 1, zoom - minZoomStep)]);
            max = Math.max(max, outlineWidth[Math.min(outlineWidth.length - 1, zoom - minZoomStep)]);
            max = Math.max(max, middleLineWidth[Math.min(middleLineWidth.length - 1, zoom - minZoomStep)]);
        }
        System.out.print(max + "f, ");
    }

    public WayStyle(final int minZoomStep, final float[] mainWidth, final float[] outlineWidth,
            final float[] middleLineWidth, final Color mainColor, final Color outlineColor, final Color middleLineColor,
            final int cap, final int join, final float[] dash) {
        this(minZoomStep, mainWidth, outlineWidth, middleLineWidth, mainColor, outlineColor, middleLineColor, cap, join,
                dash, new float[] { 0 });
    }

    public WayStyle(final int minZoomStep, final float[] mainWidth, final float[] outlineWidth, final Color mainColor,
            final Color outlineColor, final int cap, final int join) {
        this(minZoomStep, mainWidth, outlineWidth, new float[] { 0f }, mainColor, outlineColor, null, cap, join,
                new float[] { 0f }, new float[] { 0f });
    }

    public WayStyle(final int minZoomStep, final float[] mainWidth, final float[] outlineWidth, final Color mainColor,
            final Color outlineColor) {
        this(minZoomStep, mainWidth, outlineWidth, new float[] { 0 }, mainColor, outlineColor, null,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, new float[] { 0 }, new float[] { 0 });
    }

    public WayStyle(final int minZoomStep, final float[] mainWidth, final Color mainColor, final int cap,
            final int join) {
        this(minZoomStep, mainWidth, new float[] { 0 }, mainColor, null, cap, join);
    }

    public WayStyle(final int minZoomStep, final float[] mainWidth, final Color mainColor) {
        this(minZoomStep, mainWidth, mainColor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    private Stroke[] createMiddleLineStrokes(final float[] middleLineWidths, final float[] dash,
            final float[] dashSpacing) {

        final Stroke[] ret;
        if (middleLineColor != null) {
            ret = new Stroke[middleLineWidths.length];
            for (int i = 0; i < middleLineWidths.length; i++) {
                float[] dashing;
                if (dashSpacing[Math.min(i, dashSpacing.length - 1)] > 0) {
                    dashing = new float[] { dash[Math.min(i, dash.length - 1)],
                            dashSpacing[Math.min(i, dashSpacing.length - 1)] };
                } else {
                    dashing = null;
                }

                ret[i] = new BasicStroke(middleLineWidths[i], BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dashing,
                        0);
            }
        } else {
            ret = new Stroke[1];
        }

        return ret;
    }

    public boolean middleLineStroke(final Graphics2D g, final int zoomStep) {
        final Stroke stroke = getStroke(middleLineStrokes, zoomStep);
        if (stroke != null) {
            g.setStroke(stroke);
            g.setColor(middleLineColor);
            return true;
        }

        return false;
    }

}
