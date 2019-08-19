package adminTool.labeling;

import java.awt.Canvas;
import java.awt.Font;
import java.awt.FontMetrics;

import adminTool.metrics.IDistanceMap;

public class StringWidthInfo implements IStringWidthInfo {
    private final FontMetrics fm;
    private final IDistanceMap map;

    public StringWidthInfo() {
        this(d -> d);
    }

    public StringWidthInfo(final IDistanceMap map) {
        Font font = new Font("Times New Roman", Font.PLAIN, 12);
        fm = new Canvas().getFontMetrics(font);
        this.map = map;
    }

    @Override
    public double getStringWidth(final String name) {
        return map.map(fm.stringWidth(name));
    }

}
