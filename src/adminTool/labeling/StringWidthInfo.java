package adminTool.labeling;

import java.awt.Canvas;
import java.awt.Font;
import java.awt.FontMetrics;

public class StringWidthInfo implements IStringWidthInfo {
    private final FontMetrics fm;

    public StringWidthInfo() {
        Font font = new Font("Times New Roman", Font.PLAIN, 12);
        fm = new Canvas().getFontMetrics(font);
    }

    @Override
    public double getStringWidth(final String name) {
        final int zoom = 17;
        final int zoomOffset = 21 - zoom;
        final double shift = 1 << 29;
        return (fm.stringWidth(name) * (1 << zoomOffset)) / shift;
    }

}
