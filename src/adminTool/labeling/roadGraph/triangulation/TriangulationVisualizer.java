package adminTool.labeling.roadGraph.triangulation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

public class TriangulationVisualizer extends JPanel {
    private static final long serialVersionUID = 1L;

    private final BufferedImage image;

    public TriangulationVisualizer(final Triangulation triangulation) {
        setSize(800, 320);
        setOpaque(false);
        image = new BufferedImage(800, 320, BufferedImage.TYPE_INT_ARGB);
        draw(triangulation);
    }

    public TriangulationVisualizer(final String name) {
        setSize(800, 320);
        image = new BufferedImage(800, 320, BufferedImage.TYPE_INT_ARGB);

        TriangulationReader reader = new TriangulationReader(name);
        try {
            reader.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        draw(reader.getTriangulation());
    }

    private void draw(final Triangulation triangulation) {
        Map<RenderingHints.Key, Object> hints = new HashMap<>();
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHints(hints);
        g2.setColor(new Color(255, 255, 255, 0));
        g2.fillRect(0, 0, 1000, 1000);

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
