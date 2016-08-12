package view;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import model.targets.IRoutePoint;
import model.targets.PointAdapter;
import model.targets.PointState;

public class RoutePointView extends JComponent implements IRoutePointView {

    private static final long serialVersionUID = 1L;
    private static final int DIAMETER = 19;

    private static final Ellipse2D circle;

    private final IRoutePoint point;
    private static Image[] images;

    public RoutePointView(final IRoutePoint point) {
        this.point = point;
        setSize(DIAMETER, DIAMETER);
        setOpaque(false);
        point.addPointListener(new PointAdapter() {

            @Override
            public void listIndexChanged() {
                repaint();
            }

            @Override
            public void locationChanged() {
                final Container component = getParent();
                if (component != null) {
                    component.repaint();
                }
            }

            @Override
            public void stateChanged() {
                repaint();
            }

            @Override
            public void targetIndexChanged() {
                repaint();
            }
        });
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public Point getLocation() {
        return new Point(getX(), getY());
    }

    @Override
    public int getWidth() {
        return DIAMETER;
    }

    @Override
    public int getHeight() {
        return DIAMETER;
    }

    @Override
    public int getX() {
        return point.getLocation() != null ? point.getLocation().x - DIAMETER / 2 : 0;
    }

    @Override
    public int getY() {
        return point.getLocation() != null ? point.getLocation().y - DIAMETER / 2 : 0;
    }

    @Override
    public void addRoutePointListener(final IDragListener listener) {
        addMouseListener(listener);
        addMouseMotionListener(listener);
    }

    @Override
    public IRoutePoint getRoutePoint() {
        return point;
    }

    @Override
    public void paint(final Graphics g) {
        g.drawImage(images[point.getState().getIndex()], 0, 0, this);
        g.setFont(g.getFont().deriveFont(Font.BOLD));
        g.setColor(Color.BLACK);
        // TODO getTargetIndex
        final String number = (point.getListIndex() + 1) + "";
        g.drawString(number + "", (DIAMETER - g.getFontMetrics(g.getFont()).stringWidth(number)) / 2, 14);
    }

    @Override
    public boolean contains(final int x, final int y) {
        return circle.contains(x - getX(), y - getY());
    }

    static {
        circle = new Ellipse2D.Double(0, 0, DIAMETER, DIAMETER);

        final float[] fractions = {0f, 1f};

        final Rectangle bounds = new Rectangle(0, 0, DIAMETER, DIAMETER);
        final Paint paints[] = new Paint[]{
                new RadialGradientPaint(bounds, fractions, new Color[]{Color.green, Color.green.darker()},
                        CycleMethod.NO_CYCLE),
                new RadialGradientPaint(bounds, fractions, new Color[]{Color.yellow, Color.yellow.darker()},
                        CycleMethod.NO_CYCLE),
                new RadialGradientPaint(bounds, fractions, new Color[]{Color.red, Color.red.darker()},
                        CycleMethod.NO_CYCLE)};
        images = new BufferedImage[3];
        for (final PointState state : PointState.values()) {
            final BufferedImage image = new BufferedImage(DIAMETER, DIAMETER, BufferedImage.TYPE_INT_ARGB);

            final Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setPaint(paints[state.getIndex()]);
            g2.fillOval(1, 1, DIAMETER - 2, DIAMETER - 2);

            g2.setColor(Color.DARK_GRAY);
            g2.drawOval(0, 0, DIAMETER - 1, DIAMETER - 1);

            g2.dispose();

            images[state.getIndex()] = image;
        }
    }

}