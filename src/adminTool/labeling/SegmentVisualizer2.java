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

public class SegmentVisualizer2 extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final int[] s = new int[] { 1, 1, 3 };

    private final BufferedImage image;

    public SegmentVisualizer2(final String name) {
        image = new BufferedImage(800, 320, BufferedImage.TYPE_INT_RGB);
        setSize(800, 320);

        try {
            read(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read(final String name) throws IOException {
        BufferedReader neighReader = new BufferedReader(new FileReader(name + ".1.neigh"));
        String[] data = neighReader.readLine().replaceAll("\\s+", " ").split(" ");
        int triangleCount = Integer.parseInt(data[0]);
        int[] triangles = new int[triangleCount * 3];
        int segmentCount = 0;
        int midpointCount = 0;
        for (int i = 0; i < triangleCount; ++i) {
            data = neighReader.readLine().trim().replaceAll("\\s+", " ").split(" ");
            int internals = 0;
            for (int j = 1; j < 3; ++j) {
                internals += Integer.parseInt(data[j]) != -1 ? 1 : 0;
            }
            midpointCount += s[internals] + 1;
            segmentCount += s[internals];
        }
        int[] segments = new int[segmentCount * 2];
        neighReader.close();

        BufferedReader nodeReader = new BufferedReader(new FileReader(name + ".1.node"));
        data = nodeReader.readLine().split(" ");
        int pointCount = Integer.parseInt(data[0]);
        int[] points = new int[(pointCount + midpointCount) * 2];

        for (int i = 0; i < pointCount; ++i) {
            data = nodeReader.readLine().trim().replaceAll("\\s+", " ").split(" ");
            points[i << 1] = (int) Double.parseDouble(data[1]);
            points[(i << 1) + 1] = (int) Double.parseDouble(data[2]);
        }
        nodeReader.close();

        BufferedReader eleReader = new BufferedReader(new FileReader(name + ".1.ele"));
        neighReader = new BufferedReader(new FileReader(name + ".1.neigh"));
        eleReader.readLine();
        neighReader.readLine();
        int[] tmp = new int[6];

        int index = 0;
        for (int i = 0; i < triangleCount; ++i) {
            data = eleReader.readLine().trim().replaceAll("\\s+", " ").split(" ");
            int t1 = Integer.parseInt(data[1]) - 1;
            int t2 = Integer.parseInt(data[2]) - 1;
            int t3 = Integer.parseInt(data[3]) - 1;
            triangles[index++] = t1;
            triangles[index++] = t2;
            triangles[index++] = t3;

            data = neighReader.readLine().trim().replaceAll("\\s+", " ").split(" ");
            int segCount = 0;
            tmp[segCount << 1] = t2;
            tmp[(segCount << 1) + 1] = t3;
            segCount += Integer.parseInt(data[1]) != -1 ? 1 : 0;
            tmp[segCount << 1] = t3;
            tmp[(segCount << 1) + 1] = t1;
            segCount += Integer.parseInt(data[2]) != -1 ? 1 : 0;
            tmp[segCount << 1] = t1;
            tmp[(segCount << 1) + 1] = t2;
            segCount += Integer.parseInt(data[3]) != -1 ? 1 : 0;

            if (segCount == 1 || segCount == 3) {
                // add centroid
                points[pointCount << 1] = (int) ((points[t1 << 1] + points[t2 << 1] + points[t3 << 1]) / 3.f);
                points[(pointCount << 1)
                        + 1] = (int) ((points[(t1 << 1) + 1] + points[(t2 << 1) + 1] + points[(t3 << 1) + 1]) / 3.f);
                int midPoint = pointCount;
                ++pointCount;

                // add edge midpoints
                for (int j = 0; j < 2 * segCount; j += 2) {
                    --segmentCount;
                    segments[segmentCount << 1] = pointCount;
                    segments[(segmentCount << 1) + 1] = midPoint;

                    points[pointCount << 1] = (points[tmp[j] << 1] + points[tmp[j + 1] << 1]) / 2;
                    points[(pointCount << 1) + 1] = (points[(tmp[j] << 1) + 1] + points[(tmp[j + 1] << 1) + 1]) / 2;
                    ++pointCount;
                }
            } else {
                --segmentCount;
                segments[segmentCount << 1] = pointCount;
                segments[(segmentCount << 1) + 1] = pointCount + 1;
                points[pointCount << 1] = (points[tmp[0] << 1] + points[tmp[1] << 1]) / 2;
                points[(pointCount << 1) + 1] = (points[(tmp[0] << 1) + 1] + points[(tmp[1] << 1) + 1]) / 2;
                ++pointCount;
                points[pointCount << 1] = (points[tmp[2] << 1] + points[tmp[3] << 1]) / 2;
                points[(pointCount << 1) + 1] = (points[(tmp[2] << 1) + 1] + points[(tmp[3] << 1) + 1]) / 2;
                ++pointCount;
            }

            segCount = 0;
        }

        eleReader.close();
        neighReader.close();

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
            g2.fillOval(points[i++] - 3, points[i++] - 3, 6, 6);
        }
        for (int i = 0; i < triangles.length;) {
            int f = triangles[i++];
            int s = triangles[i++];
            int t = triangles[i++];
            g2.drawLine(points[f << 1], points[(f << 1) + 1], points[s << 1], points[(s << 1) + 1]);
            g2.drawLine(points[s << 1], points[(s << 1) + 1], points[t << 1], points[(t << 1) + 1]);
            g2.drawLine(points[t << 1], points[(t << 1) + 1], points[f << 1], points[(f << 1) + 1]);
        }
        g2.setColor(Color.blue);
        for (int i = 0; i < segments.length;) {
            int f = segments[i++];
            int t = segments[i++];
            g2.drawLine(points[f << 1], points[(f << 1) + 1], points[t << 1], points[(t << 1) + 1]);
        }

        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, 799, 319);

        g2.dispose();
    }

    public void paint(final Graphics g) {
        g.drawImage(image, 0, 0, this);
    }
}
