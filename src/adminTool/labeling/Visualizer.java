package adminTool.labeling;

import java.awt.Color;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Visualizer extends JFrame {
    private static final long serialVersionUID = 1L;

    public Visualizer(final List<String> filePaths) {
        setLayout(null);
        getContentPane().setBackground(Color.white);
        setTitle("Visualizer");

        final JPanel poly = new PolyVisualizer(filePaths.get(0));
        final JPanel triangle = new TriangulationVisualizer(filePaths.get(0));
        final JPanel segment = new SegmentVisualizer(filePaths.get(0));

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
