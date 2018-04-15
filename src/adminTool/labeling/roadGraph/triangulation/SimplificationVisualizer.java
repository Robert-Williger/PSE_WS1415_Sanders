package adminTool.labeling.roadGraph.triangulation;

import java.awt.Color;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import adminTool.labeling.roadGraph.PathFormer;
import adminTool.labeling.roadGraph.PathVisualizer;
import adminTool.labeling.roadGraph.PolyVisualizer;

public class SimplificationVisualizer extends JFrame {
    private static final long serialVersionUID = 1L;

    public SimplificationVisualizer(final String filePath, final float lineWidth) {
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

        final JPanel triangle1 = new TriangulationVisualizer(triangulationReader.getTriangulation());
        final JPanel triangle2 = new TriangulationVisualizer(triangulationReader.getTriangulation());

        final PathFormer pathFormer = new PathFormer(50);
        pathFormer.formPaths(triangulationReader.getTriangulation(), lineWidth);
        final JPanel segment1 = new PathVisualizer(pathFormer.getPoints(), pathFormer.getPaths());
        final JPanel segment2 = new PathVisualizer(pathFormer.getPoints(), pathFormer.getPaths());

        poly.setLocation(20, 10);
        triangle1.setLocation(850, 10);
        segment1.setLocation(850, 340);
        segment2.setLocation(20, 340);
        triangle2.setLocation(20, 340);

        add(poly);
        add(triangle1);
        add(segment1);
        add(triangle2);
        add(segment2);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1700, 1040);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
