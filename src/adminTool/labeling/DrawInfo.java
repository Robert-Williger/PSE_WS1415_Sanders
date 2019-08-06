package adminTool.labeling;

public class DrawInfo implements IDrawInfo {
    private final double[] strokeWidths;
    private final double[] fontSizes;

    public DrawInfo() {
        final double[] pixelWidths = new double[] { 13.0f, 4.0f, 17.0f, 17.0f, 13.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f,
                13.0f, 3.0f, 4.0f, 0.6f, 17.0f, 18.0f, 18.0f, 12.0f, 12.0f, 12.0f, 1.0f, 4.0f, 1.0f, 2.0f, 1.125f,
                1.125f, 1.125f, };
        this.fontSizes = new double[pixelWidths.length];
        this.strokeWidths = new double[pixelWidths.length];

        final int zoom = 17;
        final int zoomOffset = 21 - zoom;
        final double shift = 1 << 29;
        for (int i = 0; i < strokeWidths.length; ++i) {
            this.strokeWidths[i] = (pixelWidths[i] * (1 << zoomOffset)) / shift;
            this.fontSizes[i] = this.strokeWidths[i];
        }
    }

    public DrawInfo(final double[] strokeWidths, final double[] fontSizes) {
        this.strokeWidths = strokeWidths;
        this.fontSizes = fontSizes;
    }

    public double getStrokeWidth(int type) {
        return strokeWidths[type];
    }

    public double getFontSize(int type) {
        return fontSizes[type];
    }
}
