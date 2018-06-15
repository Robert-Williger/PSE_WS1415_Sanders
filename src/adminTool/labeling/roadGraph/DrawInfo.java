package adminTool.labeling.roadGraph;

import adminTool.labeling.IDrawInfo;

public class DrawInfo implements IDrawInfo {
    private final int[] strokeWidths;
    private final int[] fontSizes;

    public DrawInfo() {
        final float[] pixelWidths = new float[] { 10.0f, 2.0f, 12.0f, 12.0f, 10.0f, 2.0f, 2.0f, 2.0f, 2.0f, 2.0f, 13.0f,
                2.0f, 2.0f, 0.3f, 12.0f, 14.0f, 14.0f, 8.0f, 8.0f, 8.0f, 0.0f, 0.0f, 0.5f, 1.5f, 1.0f, 1.0f, 1.0f, };
        this.fontSizes = new int[pixelWidths.length];
        this.strokeWidths = new int[pixelWidths.length];
        for (int i = 0; i < strokeWidths.length; ++i) {
            this.strokeWidths[i] = (int) (pixelWidths[i] * (1 << 5));
        }
    }

    public DrawInfo(final int[] strokeWidths, final int[] fontSizes) {
        this.strokeWidths = strokeWidths;
        this.fontSizes = fontSizes;
    }

    public int getStrokeWidth(int type) {
        return strokeWidths[type];
    }

    public int getFontSize(int type) {
        return fontSizes[type];
    }
}
