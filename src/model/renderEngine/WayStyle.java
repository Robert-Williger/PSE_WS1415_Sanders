package model.renderEngine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class WayStyle extends ShapeStyle {
    protected final float middleLineWidth;
    protected final float maxMiddleLineWidth;
    protected final float minMiddleLineWidth;
    protected final Color middleLineColor;
    protected final float dash;
    protected final float maxDash;
    protected final float minDash;
    protected final float dashSpacing;
    protected final float maxDashSpacing;
    protected final float minDashSpacing;

    public WayStyle(final float mainWidth, final float outlineWidth, final float middleLineWidth,
            final Color mainColor, final Color outlineColor, final Color middleLineColor, final int cap,
            final int join, final float dash, final float dash_spacing) {
        super(mainWidth, outlineWidth, mainColor, outlineColor, cap, join);
        maxMiddleLineWidth = Float.MAX_VALUE;
        minMiddleLineWidth = Float.MIN_VALUE;
        this.middleLineWidth = SCALE_FACTOR * middleLineWidth;
        this.middleLineColor = middleLineColor;
        this.dash = SCALE_FACTOR * dash;
        maxDash = Float.MAX_VALUE;
        minDash = 0;
        dashSpacing = SCALE_FACTOR * dash_spacing;
        maxDashSpacing = Float.MAX_VALUE;
        minDashSpacing = 0;
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
        this.middleLineWidth = SCALE_FACTOR * middleLineWidth[1];
        maxMiddleLineWidth = middleLineWidth[2];
        minMiddleLineWidth = middleLineWidth[0];
        this.middleLineColor = middleLineColor;
        this.dash = SCALE_FACTOR * dash[1];
        maxDash = dash[2];
        minDash = dash[0];
        this.dashSpacing = SCALE_FACTOR * dashSpacing[1];
        maxDashSpacing = dashSpacing[2];
        minDashSpacing = dashSpacing[0];
    }

    public WayStyle(final float[] mainWidth, final float[] outlineWidth, final float[] middleLineWidth,
            final Color mainColor, final Color outlineColor, final Color middleLineColor, final int cap,
            final int join, final float[] dash) {
        this(mainWidth, outlineWidth, middleLineWidth, mainColor, outlineColor, middleLineColor, cap, join, dash,
                new float[]{-1f, -1f, -1f});
    }

    public WayStyle(final float[] mainWidth, final float[] outlineWidth, final Color mainColor,
            final Color outlineColor, final int cap, final int join) {
        this(mainWidth, outlineWidth, new float[]{0f, 0f, 0f}, mainColor, outlineColor, null, cap, join, new float[]{
                0f, 0f, 0f}, new float[]{0f, 0f, 0f});
    }

    public WayStyle(final float[] mainWidth, final float[] outlineWidth, final Color mainColor, final Color outlineColor) {
        this(mainWidth, outlineWidth, new float[]{0, 0, 0}, mainColor, outlineColor, null, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, new float[]{0, 0, 0}, new float[]{0, 0, 0});
    }

    public WayStyle(final float[] mainWidth, final Color mainColor, final int cap, final int join) {
        this(mainWidth, new float[]{0, 0, 0}, mainColor, null, cap, join);
    }

    public WayStyle(final float[] mainWidth, final Color mainColor) {
        this(mainWidth, mainColor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    public boolean middleLineStroke(final Graphics2D g, final int zoomStep) {
        if (middleLineColor != null) {
            float[] dashing;

            if (dashSpacing > 0f) {
                dashing = new float[]{getValue(minDash, dash, maxDash, zoomStep),
                        getValue(minDashSpacing, dashSpacing, maxDashSpacing, zoomStep)};
            } else {
                dashing = new float[]{getValue(minDash, dash, maxDash, zoomStep)};
            }

            g.setStroke(new BasicStroke(getValue(minMiddleLineWidth, middleLineWidth, maxMiddleLineWidth, zoomStep),
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dashing, 0));
            g.setColor(middleLineColor);
            return true;
        }

        return false;
    }

}
