package adminTool.labeling.roadGraph.triangulation;

import java.awt.Color;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import adminTool.labeling.PathVisualizer;
import adminTool.labeling.PolyVisualizer;

public class Visualizer extends JFrame {
    private static final long serialVersionUID = 1L;

    public Visualizer(final String filePath, final float lineWidth) {
        setLayout(null);
        getContentPane().setBackground(Color.white);
        setTitle("Visualizer");

        final JPanel poly = new PolyVisualizer(filePath);

        final TriangulationReader triangulationReader = new TriangulationReader(filePath);
        try {
            triangulationReader.read();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        final JPanel triangle = new TriangulationVisualizer(triangulationReader.getTriangulation());
        final JPanel segment = new PathVisualizer(triangulationReader.getTriangulation(), lineWidth);

        poly.setLocation(20, 10);
        triangle.setLocation(850, 10);
        segment.setLocation(850, 340);

        add(poly);
        add(triangle);
        add(segment);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1700, 1040);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
