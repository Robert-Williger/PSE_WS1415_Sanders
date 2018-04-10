package adminTool.labeling;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import static adminTool.Util.lineIntersection;

public class Cross extends JFrame {
    private static final long serialVersionUID = 1L;

    public Cross() {
        setTitle("Intersection Test");
        setLocationRelativeTo(null);
        setSize(400, 400);
        add(new ImagePanel());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private class ImagePanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private final BufferedImage image;

        public ImagePanel() {
            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);

            Map<RenderingHints.Key, Object> hints = new HashMap<>();
            hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

            int x1 = 60;
            int y1 = 60;
            int x2 = 240;
            int y2 = 240;
            int x3 = 50;
            int y3 = 150;
            int x4 = 240;
            int y4 = 60;

            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHints(hints);
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, 1000, 1000);

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(40, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            g2.drawLine(x1, y1, x2, y2);
            g2.drawLine(x3, y3, x4, y4);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(36, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            g2.drawLine(x1, y1, x2, y2);
            g2.drawLine(x3, y3, x4, y4);

            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            g2.setColor(Color.black);
            g2.drawLine(x1, y1, x2, y2);
            g2.drawLine(x3, y3, x4, y4);

            Line2D[] lines = new Line2D.Double[] { new Line2D.Double(x1, y1, x2, y2),
                    new Line2D.Double(x3, y3, x4, y4) };
            for (int i = 0; i < 2; ++i) {
                Shape shape = new BasicStroke(36, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)
                        .createStrokedShape(lines[i]);
                PathIterator iterator = shape.getPathIterator(null);
                double[] coords1 = new double[2];
                double[] coords2 = new double[2];
                for (int j = 0; j < 2; ++j) {
                    iterator.currentSegment(coords1);
                    iterator.next();
                    iterator.currentSegment(coords2);
                    iterator.next();
                    g2.setColor(Color.BLACK);
                    Point2D point = lineIntersection(coords1[0], coords1[1], coords2[0], coords2[1],
                            lines[1 - i].getX1(), lines[1 - i].getY1(), lines[1 - i].getX2(), lines[1 - i].getY2());
                    if (point != null) {
                        g2.fillOval((int) point.getX() - 5, (int) point.getY() - 5, 10, 10);
                    }
                }
            }
            g2.fillOval(x1 - 5, y1 - 5, 10, 10);
            g2.fillOval(x2 - 5, y2 - 5, 10, 10);
            g2.fillOval(x3 - 5, y3 - 5, 10, 10);
            g2.fillOval(x4 - 5, y4 - 5, 10, 10);

            Point2D intersection = lineIntersection(x1, y1, x2, y2, x3, y3, x4, y4);
            if (intersection != null) {
                g2.fillOval((int) intersection.getX() - 5, (int) intersection.getY() - 5, 10, 10);
                g2.setColor(Color.WHITE);
                g2.fillOval((int) intersection.getX() - 3, (int) intersection.getY() - 3, 6, 6);
            }

            g2.dispose();
        }

        public void paint(final Graphics g) {
            g.drawImage(image, 0, 0, this);
        }

    }

    public static void main(String[] args) {
        new Cross();
    }

}
