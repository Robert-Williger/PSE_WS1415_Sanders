package adminTool.labeling.roadGraph.visualizer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import adminTool.elements.IPointAccess;
import adminTool.elements.MultiElement;

public class WayVisualizer extends JPanel {
    private static final long serialVersionUID = 1L;

    private final BufferedImage image;

    public WayVisualizer(final IPointAccess points, final List<? extends MultiElement> paths) {
        image = new BufferedImage(800, 320, BufferedImage.TYPE_INT_ARGB);
        setSize(800, 320);
        draw(points, paths);
        setOpaque(false);
    }

    private void draw(final IPointAccess points, final List<? extends MultiElement> paths) {
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
        for (final MultiElement path : paths) {
            int last = path.getNode(0);

            g2.setColor(Color.BLACK);
            g2.fillOval(points.getX(last) - 3, points.getY(last) - 3, 6, 6);

            for (int i = 1; i < path.size(); ++i) {
                int cur = path.getNode(i);
                g2.setColor(path.getType() != -1 ? Color.blue : Color.RED);
                g2.drawLine(points.getX(last), points.getY(last), points.getX(cur), points.getY(cur));
                last = cur;
                g2.setColor(Color.BLACK);
                g2.fillOval(points.getX(last) - 3, points.getY(last) - 3, 6, 6);
            }
        }

        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, 799, 319);

        g2.dispose();
    }

    public void paint(final Graphics g) {
        g.drawImage(image, 0, 0, this);
    }
}
