package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import model.targets.PointState;

public class RoutePointViewFlyweight {

    private final Image[] images;
    private final FontMetrics fontMetrics;
    private final Font font;
    private final int diameter;

    public RoutePointViewFlyweight(final int diameter) {

        final float[] fractions = { 0f, 1f };

        final Paint[] paints = new Paint[PointState.STATES];
        paints[0] = new RadialGradientPaint(new Rectangle(0, 0, diameter, diameter), fractions,
                new Color[] { Color.green, Color.green.darker() }, CycleMethod.NO_CYCLE);
        paints[1] = new RadialGradientPaint(new Rectangle(0, 0, diameter, diameter), fractions,
                new Color[] { Color.yellow, Color.yellow.darker() }, CycleMethod.NO_CYCLE);
        paints[2] = new RadialGradientPaint(new Rectangle(0, 0, diameter, diameter), fractions,
                new Color[] { Color.red, Color.red.darker() }, CycleMethod.NO_CYCLE);
        final Color[] borderColor = new Color[] { new Color(94, 94, 94), new Color(94, 94, 94), Color.DARK_GRAY };

        images = new Image[PointState.STATES];
        for (int i = 0; i < PointState.STATES; i++) {
            final BufferedImage image = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g2 = image.createGraphics();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setPaint(paints[i]);
            g2.fillOval(1, 1, diameter - 2, diameter - 2);

            g2.setColor(borderColor[i]);
            g2.drawOval(0, 0, diameter - 1, diameter - 1);

            images[i] = image;

            g2.dispose();
        }

        final Graphics g = images[0].getGraphics();
        font = g.getFont().deriveFont(Font.BOLD);
        fontMetrics = g.getFontMetrics(font);

        this.diameter = diameter;
    }

    public void paint(final Graphics g, final PointState state, final int x, final int y) {
        g.drawImage(images[state.getIndex()], x, y, null);
    }

    public void paint(final Graphics g, final PointState state, final String index, final int x, final int y) {
        paint(g, state, x, y);
        g.setColor(Color.BLACK);

        g.setFont(font);
        g.drawString(index, (diameter - fontMetrics.stringWidth(index)) / 2 + x, 14 + y);
    }
}