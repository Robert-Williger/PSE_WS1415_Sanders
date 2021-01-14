package adminTool.labeling;

public class DrawInfo implements IDrawInfo {
    private final double[] strokeWidths;
    private final double[] fontSizes;
    private final boolean[] visible;

    public DrawInfo(final double[] strokeWidths, final double[] fontSizes, final boolean[] visible) {
        this.strokeWidths = strokeWidths;
        this.fontSizes = fontSizes;
        this.visible = visible;
    }

    public double getStrokeWidth(int type) {
        return strokeWidths[type];
    }

    public double getFontSize(int type) {
        return fontSizes[type];
    }

    @Override
    public boolean isVisible(int type) {
        return visible[type];
    }
}
