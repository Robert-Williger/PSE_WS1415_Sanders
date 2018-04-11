package adminTool.labeling;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator;

import javax.swing.JPanel;

import adminTool.IPointAccess;
import adminTool.labeling.roadGraph.PathFormer;
import adminTool.labeling.roadGraph.triangulation.Triangulation;
import util.IntList;

public class PathVisualizer extends JPanel {
    private static final long serialVersionUID = 1L;

    private final BufferedImage image;

    public PathVisualizer(final Triangulation triangulation, final float lineWidth) {
        image = new BufferedImage(800, 320, BufferedImage.TYPE_INT_RGB);
        setSize(800, 320);

        PathFormer pathFormer = new PathFormer();
        pathFormer.formPaths(triangulation, lineWidth);

        draw(pathFormer.getPoints(), pathFormer.getPaths(), triangulation);
    }

    private void draw(final IPointAccess points, final List<IntList> paths, final Triangulation triangulation) {
        Map<RenderingHints.Key, Object> hints = new HashMap<>();
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHints(hints);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, 1000, 1000);

        g2.setColor(Color.BLACK);

        for (final IntList path : paths) {
            final PrimitiveIterator.OfInt it = path.iterator();
            int last = it.nextInt();

            g2.setColor(Color.BLACK);
            g2.fillOval(points.getX(last) - 3, points.getY(last) - 3, 6, 6);

            while (it.hasNext()) {
                int cur = it.nextInt();
                g2.setColor(Color.blue);
                g2.drawLine(points.getX(last), points.getY(last), points.getX(cur), points.getY(cur));
                last = cur;
                g2.setColor(Color.BLACK);
                g2.fillOval(points.getX(last) - 3, points.getY(last) - 3, 6, 6);
            }
        }

        g2.setColor(Color.BLACK);
        for (int i = 0; i < triangulation.getPoints(); ++i) {
            g2.fillOval(triangulation.getX(i) - 3, triangulation.getY(i) - 3, 6, 6);
        }
        for (int i = 0; i < triangulation.getTriangles(); ++i) {
            int f = triangulation.getPoint(i, 0);
            int s = triangulation.getPoint(i, 1);
            int t = triangulation.getPoint(i, 2);
            g2.drawLine(triangulation.getX(f), triangulation.getY(f), triangulation.getX(s), triangulation.getY(s));
            g2.drawLine(triangulation.getX(s), triangulation.getY(s), triangulation.getX(t), triangulation.getY(t));
            g2.drawLine(triangulation.getX(t), triangulation.getY(t), triangulation.getX(f), triangulation.getY(f));
        }

        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, 799, 319);

        g2.dispose();
    }

    public void paint(final Graphics g) {
        g.drawImage(image, 0, 0, this);
    }
}
