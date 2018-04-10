package adminTool.labeling;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

public class PolyVisualizer extends JPanel {
    private static final long serialVersionUID = 1L;
    private final BufferedImage image;

    public PolyVisualizer(final String name) {
        image = new BufferedImage(800, 320, BufferedImage.TYPE_INT_RGB);
        setSize(1000, 320);
        try {
            read(name);
        } catch (IOException e) {
        }
    }

    public void paint(final Graphics g) {
        g.drawImage(image, 0, 0, this);
    }

    private void read(final String name) throws IOException {
        BufferedReader polyReader = new BufferedReader(new FileReader(name + ".poly"));
        BufferedReader nodeReader;

        String[] data = polyReader.readLine().split(" ");
        int pointCount = Integer.parseInt(data[0]);
        if (pointCount == 0) {
            nodeReader = new BufferedReader(new FileReader(name + ".node"));
            pointCount = Integer.parseInt(nodeReader.readLine().split(" ")[0]);
        } else {
            nodeReader = polyReader;
        }

        int[] points = new int[pointCount * 2];
        for (int i = 0; i < pointCount; ++i) {
            data = nodeReader.readLine().split(" ");
            points[i << 1] = (int) Double.parseDouble(data[1]);
            points[(i << 1) + 1] = (int) Double.parseDouble(data[2]);
        }

        data = polyReader.readLine().split(" ");
        final int segmentCount = Integer.parseInt(data[0]);

        int[] segments = new int[segmentCount * 2];
        for (int i = 0; i < segmentCount; ++i) {
            data = polyReader.readLine().split(" ");
            segments[i << 1] = Integer.parseInt(data[1]) - 1;
            segments[(i << 1) + 1] = Integer.parseInt(data[2]) - 1;
        }

        data = polyReader.readLine().split(" ");
        final int holeCount = Integer.parseInt(data[0]);
        int[] holes = new int[holeCount * 2];
        for (int i = 0; i < holeCount; ++i) {
            data = polyReader.readLine().split(" ");
            holes[i << 1] = (int) Double.parseDouble(data[1]);
            holes[(i << 1) + 1] = (int) Double.parseDouble(data[2]);
        }

        polyReader.close();
        nodeReader.close();

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
        for (int i = 0; i < points.length;) {
            g2.fillOval(points[i] - 3, points[i + 1] - 3, 6, 6);
            i += 2;
        }
        for (int i = 0; i < segments.length;) {
            int from = segments[i++];
            int to = segments[i++];
            g2.drawLine(points[from << 1], points[(from << 1) + 1], points[to << 1], points[(to << 1) + 1]);
        }
        g2.setColor(Color.RED);
        for (int i = 0; i < holes.length;) {
            g2.fillOval(holes[i++] - 3, holes[i++] - 3, 6, 6);
        }
        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, 799, 319);
        g2.dispose();
    }
}
