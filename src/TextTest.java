import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class TextTest extends JPanel {
    private static final long serialVersionUID = 1L;

    final BufferedImage image;

    public TextTest() {
        final Shape path = getPath();
        final String text = getText();
        final Font font = new Font("Dialog", Font.PLAIN, 36);
        final FontMetrics metrics = getFontMetrics(font);
        final int offset = (metrics.getAscent() - metrics.getDescent()) / 2;
        // collect token widths

        image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);

        final Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.white);
        g2.fillRect(0, 0, 400, 400);
        g2.setColor(Color.black);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Map<?, ?> map = (Map<?, ?>) (tk.getDesktopProperty("awt.font.desktophints"));
        if (map != null) {
            g2.addRenderingHints(map);
        }

        g2.setFont(font);

        g2.draw(path);

        final double[] tokenWidths = new double[text.length()];

        for (int j = 0; j < text.length(); j++) {
            final float width = metrics.charWidth(text.charAt(j));
            tokenWidths[j] = width;
        }

        // collect path points
        final List<Point2D.Double> p = getPathPoints(path);

        Iterator<Point2D.Double> it = p.iterator();
        Point2D.Double last = it.next();
        int index = 0;
        double currentDistance = 0;
        final List<String> partList = new LinkedList<String>();
        for (; it.hasNext();) {
            final Point2D.Double current = it.next();
            currentDistance += last.distance(current);
            String part = "";
            while (index < text.length() && currentDistance > 0) {
                final char character = text.charAt(index);
                currentDistance -= metrics.charWidth(character);
                part += character;
                ++index;
            }

            partList.add(part);
            last = current;
        }

        if (index == text.length()) {
            it = p.iterator();
            last = it.next();
            final Iterator<String> parts = partList.iterator();
            for (; it.hasNext();) {
                final Point2D.Double current = it.next();
                final String part = parts.next();

                if (!part.isEmpty()) {
                    final double theta = getAngle(last, current);
                    final AffineTransform at = AffineTransform.getTranslateInstance(last.x - Math.sin(theta) * offset,
                            last.y + Math.cos(theta) * offset);
                    at.rotate(theta);
                    g2.setFont(font.deriveFont(at));
                    g2.drawString(part, 0, 0);
                }

                last = current;
            }
        }
    }

    private String getText() {
        return "Sinsheimerstraße";
    }

    private Shape getPath() {
        final int w = 200;
        final int h = 200;

        // circle
        final double x1 = w / 4.0;
        final double y1 = h / 8.0;

        final double r = Math.min(w, h) / 3.0;
        final Ellipse2D circle = new Ellipse2D.Double(x1, y1, 2 * r, 2 * r);

        return new GeneralPath(circle);
        // final GeneralPath ret = new GeneralPath();
        // ret.moveTo(20, 20);
        // ret.lineTo(60, 40);
        // ret.lineTo(100, 200);
        // ret.lineTo(200, 400);
        // return ret;
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this);
    }

    private double getAngle(final Point2D.Double from, final Point2D.Double to) {
        final double dy = to.y - from.y;
        final double dx = to.x - from.x;
        return Math.atan2(dy, dx);
    }

    private List<Point2D.Double> getPathPoints(final Shape path) {
        final double flatness = 0.01;

        final List<Point2D.Double> points = new ArrayList<Point2D.Double>();
        final double[] coords = new double[6];
        final PathIterator pit = path.getPathIterator(null, flatness);
        while (!pit.isDone()) {
            final int type = pit.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                case PathIterator.SEG_LINETO:
                    points.add(new Point2D.Double(coords[0], coords[1]));
                    break;
                case PathIterator.SEG_CLOSE:
                    break;
                default:
                    System.out.println("unexpected type: " + type);
            }
            pit.next();
        }
        return points;
    }

    public static void main(final String[] args) {
        final TextTest ct = new TextTest();
        final JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(ct);
        f.setSize(400, 400);
        f.setLocation(200, 200);
        f.setVisible(true);
    }
}