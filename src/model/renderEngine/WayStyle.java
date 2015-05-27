package model.renderEngine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class WayStyle extends ShapeStyle {
    protected final float middleLineWidth;
    protected final float maxMiddleLineWidth;
    protected final Color middleLineColor;
    protected final float dash;
    protected final float maxDash;
    protected final float dashSpacing;
    protected final float maxDashSpacing;

    public WayStyle(final float mainWidth, final float outlineWidth, final float middleLineWidth,
            final Color mainColor, final Color outlineColor, final Color middleLineColor, final int cap,
            final int join, final float dash, final float dash_spacing) {
        super(mainWidth, outlineWidth, mainColor, outlineColor, cap, join);
        maxMiddleLineWidth = Float.MAX_VALUE;
        this.middleLineWidth = middleLineWidth;
        this.middleLineColor = middleLineColor;
        this.dash = dash;
        maxDash = Float.MAX_VALUE;
        dashSpacing = dash_spacing;
        maxDashSpacing = Float.MAX_VALUE;
    }

    public WayStyle(final float mainWidth, final float outlineWidth, final float middleLineWidth,
            final Color mainColor, final Color outlineColor, final Color middleLineColor, final int cap,
            final int join, final float dash) {
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

    public WayStyle(final float[] mainWidth, final float[] outlineWidth, final float[] middleLineWidth,
            final Color mainColor, final Color outlineColor, final Color middleLineColor, final int cap,
            final int join, final float[] dash, final float[] dashSpacing) {
        super(mainWidth, outlineWidth, mainColor, outlineColor, cap, join);
        this.middleLineWidth = middleLineWidth[0];
        maxMiddleLineWidth = middleLineWidth[1];
        this.middleLineColor = middleLineColor;
        this.dash = dash[0];
        maxDash = dash[1];
        this.dashSpacing = dashSpacing[0];
        maxDashSpacing = dashSpacing[1];
    }

    public WayStyle(final float[] mainWidth, final float[] outlineWidth, final float[] middleLineWidth,
            final Color mainColor, final Color outlineColor, final Color middleLineColor, final int cap,
            final int join, final float[] dash) {
        this(mainWidth, outlineWidth, middleLineWidth, mainColor, outlineColor, middleLineColor, cap, join, dash,
                new float[]{-1f, -1f});
    }

    public WayStyle(final float[] mainWidth, final float[] outlineWidth, final Color mainColor,
            final Color outlineColor, final int cap, final int join) {
        this(mainWidth, outlineWidth, new float[]{0f, 0f}, mainColor, outlineColor, null, cap, join,
                new float[]{0f, 0f}, new float[]{0f, 0f});
    }

    public WayStyle(final float[] mainWidth, final float[] outlineWidth, final Color mainColor, final Color outlineColor) {
        this(mainWidth, outlineWidth, new float[]{0f, 0f}, mainColor, outlineColor, null, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, new float[]{0, 0}, new float[]{0, 0});
    }

    public WayStyle(final float[] mainWidth, final Color mainColor, final int cap, final int join) {
        this(mainWidth, new float[]{0f, 0f}, mainColor, null, cap, join);
    }

    public WayStyle(final float[] mainWidth, final Color mainColor) {
        this(mainWidth, mainColor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    public boolean middleLineStroke(final Graphics2D g, final int zoomStep) {
        if (middleLineColor != null) {
            float[] dashing;

            if (dashSpacing > 0f) {
                dashing = new float[]{
                        Math.min(maxDash, Math.max(0.1f, converter.getPixelDistancef((SCALE_FACTOR * dash), zoomStep))),
                        Math.min(maxDashSpacing,
                                Math.max(0.1f, converter.getPixelDistancef((SCALE_FACTOR * dashSpacing), zoomStep)))};
            } else {
                dashing = new float[]{Math.min(maxDash,
                        Math.max(0.1f, converter.getPixelDistancef((SCALE_FACTOR * dash), zoomStep)))};
            }

            g.setStroke(new BasicStroke(Math.min(maxMiddleLineWidth,
                    Math.max(0.1f, converter.getPixelDistancef((SCALE_FACTOR * middleLineWidth), zoomStep))),
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dashing, 0));
            g.setColor(middleLineColor);
            return true;
        }

        return false;
    }
}
