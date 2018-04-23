package adminTool.labeling.roadGraph.simplification;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import adminTool.IPointAccess;
import adminTool.elements.MultiElement;
import adminTool.labeling.roadGraph.PathVisualizer;
import adminTool.labeling.roadGraph.WayVisualizer;
import adminTool.labeling.roadGraph.simplification.triangulation.PolyVisualizer;
import adminTool.labeling.roadGraph.simplification.triangulation.TriangulationReader;
import adminTool.labeling.roadGraph.simplification.triangulation.TriangulationVisualizer;

public class SimplificationVisualizer extends JFrame {
    private static final long serialVersionUID = 1L;

    public SimplificationVisualizer(final String filePath, final List<? extends MultiElement> paths,
            final IPointAccess points, final float lineWidth, final int simplificationThreshold) {
        setLayout(null);
        getContentPane().setBackground(Color.white);
        setTitle("Simplification Visualizer");

        final JPanel poly1 = new PolyVisualizer(filePath);
        final JPanel poly2 = new PolyVisualizer(filePath);

        final TriangulationReader triangulationReader = new TriangulationReader(filePath);
        try {
            triangulationReader.read();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        final JPanel way1 = new WayVisualizer(points, paths);
        final JPanel way2 = new WayVisualizer(points, paths);

        final JPanel triangle1 = new TriangulationVisualizer(triangulationReader.getTriangulation());
        final JPanel triangle2 = new TriangulationVisualizer(triangulationReader.getTriangulation());

        final PathFormer pathFormer = new PathFormer();
        pathFormer.formPaths(triangulationReader.getTriangulation(), lineWidth);
        final PathSimplifier pathSimplifier = new PathSimplifier(simplificationThreshold);
        pathSimplifier.simplify(pathFormer.getPaths(), pathFormer.getPoints());
        final JPanel path1 = new PathVisualizer(pathFormer.getPoints(), pathFormer.getPaths());
        final JPanel path2 = new PathVisualizer(pathFormer.getPoints(), pathFormer.getPaths());
        final JPanel path3 = new PathVisualizer(pathSimplifier.getPoints(), pathSimplifier.getPaths());

        way1.setLocation(20, 10);
        poly1.setLocation(850, 10);
        way2.setLocation(850, 10);
        triangle1.setLocation(20, 340);
        triangle2.setLocation(850, 340);
        path1.setLocation(20, 670);
        path2.setLocation(850, 340);
        path3.setLocation(850, 670);
        poly2.setLocation(850, 670);

        add(way1);
        add(way2);
        add(poly1);
        add(triangle1);
        add(path1);
        add(triangle2);
        add(path2);
        add(path3);
        add(poly2);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1700, 1040);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
