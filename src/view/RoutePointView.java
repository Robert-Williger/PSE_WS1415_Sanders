package view;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;

import javax.swing.JPanel;

import model.targets.IRoutePoint;
import model.targets.PointAdapter;
import model.targets.PointState;

public class RoutePointView extends JPanel implements IRoutePointView {

    private static final long serialVersionUID = 1L;
    private static final int DIAMETER = 19;

    private static final Ellipse2D circle = new Ellipse2D.Double(0, 0, DIAMETER, DIAMETER);

    private final IRoutePoint point;
    private static final HashMap<PointState, Paint> map;

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
        return point.getX() - DIAMETER / 2;
    }

    @Override
    public int getY() {
        return point.getY() - DIAMETER / 2;
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
        final Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setPaint(map.get(point.getState()));
        g2.fillOval(1, 1, DIAMETER - 2, DIAMETER - 2);

        g.setFont(g.getFont().deriveFont(Font.BOLD));
        g.setColor(point.getState() != PointState.unadded ? new Color(94, 94, 94) : Color.DARK_GRAY);
        g.drawOval(0, 0, DIAMETER - 1, DIAMETER - 1);
        g.setColor(Color.BLACK);

        final String number = (point.getTargetIndex() + 1) + "";
        g.drawString(number + "", (DIAMETER - g.getFontMetrics(g.getFont()).stringWidth(number)) / 2, 14);
    }

    @Override
    public boolean contains(final int x, final int y) {
        return circle.contains(x - getX(), y - getY());
    }

    static {
        final float[] fractions = {0f, 1f};

        map = new HashMap<>();
        map.put(PointState.added, new RadialGradientPaint(new Rectangle(0, 0, DIAMETER, DIAMETER), fractions,
                new Color[]{Color.green, Color.green.darker()}, CycleMethod.NO_CYCLE));
        map.put(PointState.editing, new RadialGradientPaint(new Rectangle(0, 0, DIAMETER, DIAMETER), fractions,
                new Color[]{Color.yellow, Color.yellow.darker()}, CycleMethod.NO_CYCLE));
        map.put(PointState.unadded, new RadialGradientPaint(new Rectangle(0, 0, DIAMETER, DIAMETER), fractions,
                new Color[]{Color.red, Color.red.darker()}, CycleMethod.NO_CYCLE));
    }

}