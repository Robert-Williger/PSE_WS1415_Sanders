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
import model.targets.IRoutePoint.State;

public class RoutePointViewFlyweight {

    private final Image[] images;
    private final FontMetrics fontMetrics;
    private final Font font;
    private final int diameter;

    public RoutePointViewFlyweight(final int diameter) {

        final float[] fractions = { 0f, 1f };

        final Paint[] paints = new Paint[State.STATES];
        paints[0] = new RadialGradientPaint(new Rectangle(0, 0, diameter, diameter), fractions,
                new Color[] { Color.green, Color.green.darker() }, CycleMethod.NO_CYCLE);
        paints[1] = new RadialGradientPaint(new Rectangle(0, 0, diameter, diameter), fractions,
                new Color[] { Color.yellow, Color.yellow.darker() }, CycleMethod.NO_CYCLE);
        paints[2] = new RadialGradientPaint(new Rectangle(0, 0, diameter, diameter), fractions,
                new Color[] { Color.red, Color.red.darker() }, CycleMethod.NO_CYCLE);
        final Color[] borderColor = new Color[] { new Color(94, 94, 94), new Color(94, 94, 94), Color.DARK_GRAY };

        images = new Image[State.STATES];
        for (int i = 0; i < State.STATES; i++) {
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

    public void paint(final Graphics2D g, final State state, final double x, final double y) {
        g.translate(x, y);
        g.drawImage(images[state.getIndex()], 0, 0, null);
        g.translate(-x, -y);
    }

    public void paint(final Graphics2D g, final State state, final String index, final double x, final double y) {
        g.translate(x, y);
        g.drawImage(images[state.getIndex()], 0, 0, null);

        g.setColor(Color.BLACK);

        g.setFont(font);
        g.drawString(index, (diameter - fontMetrics.stringWidth(index)) / 2, 14);

        g.translate(-x, -y);
    }
}