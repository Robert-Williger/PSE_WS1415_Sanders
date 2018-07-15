package adminTool.labeling.roadGraph;

import adminTool.labeling.IDrawInfo;

public class DrawInfo implements IDrawInfo {
    private final int[] hullWidths;
    private final int[] strokeWidths;
    private final int[] fontSizes;

    public DrawInfo() {
        final float[] pixelWidths = new float[] { 13.0f, 4.0f, 17.0f, 17.0f, 13.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 13.0f,
                3.0f, 4.0f, 0.6f, 17.0f, 18.0f, 18.0f, 12.0f, 12.0f, 12.0f, 1.0f, 4.0f, 1.0f, 2.0f, 1.125f, 1.125f,
                1.125f, };
        this.fontSizes = new int[pixelWidths.length];
        this.strokeWidths = new int[pixelWidths.length];
        this.hullWidths = new int[pixelWidths.length];

        for (int i = 0; i < strokeWidths.length; ++i) {
            this.strokeWidths[i] = (int) (pixelWidths[i] * (1 << 5));
            this.hullWidths[i] = (int) ((pixelWidths[i] + 1) * (1 << 5));
        }
    }

    public DrawInfo(final int[] strokeWidths, final int[] fontSizes) {
        this.strokeWidths = strokeWidths;
        this.hullWidths = strokeWidths;
        this.fontSizes = fontSizes;
    }

    public int getStrokeWidth(int type) {
        return strokeWidths[type];
    }

    public int getHullWidth(int type) {
        return hullWidths[type];
    }

    public int getFontSize(int type) {
        return fontSizes[type];
    }
}
