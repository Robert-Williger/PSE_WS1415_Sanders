package adminTool.labeling.roadGraph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import adminTool.UnboundedPointAccess;
import adminTool.elements.MultiElement;
import adminTool.elements.Way;

public class TransformationVisualizer extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final int WAY_WIDTH = 40;
    private static final int THRESHOLD = 2 * WAY_WIDTH;

    public TransformationVisualizer() {
        setLayout(null);
        getContentPane().setBackground(Color.white);
        setTitle("Transformation Visualizer");

        final UnboundedPointAccess points = createPoints();
        final List<? extends MultiElement> ways = createWays();

        final JPanel origPaths = new WayVisualizer(points, ways);

        final Transformation transformation = new Transformation(WAY_WIDTH, THRESHOLD);
        transformation.transform(ways, points);
        final JPanel cutPaths = new WayVisualizer(points, transformation.getProcessedPaths());

        origPaths.setLocation(20, 10);
        cutPaths.setLocation(850, 10);

        add(origPaths);
        add(cutPaths);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1700, 1040);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private static UnboundedPointAccess createPoints() {
        final UnboundedPointAccess points = new UnboundedPointAccess();
        points.addPoint(400, 200);

        points.addPoint(350, 100);
        points.addPoint(265, 220);

        points.addPoint(500, 250);
        points.addPoint(600, 270);

        points.addPoint(400, 100);
        points.addPoint(295, 50);
        return points;
    }

    private static List<Way> createWays() {
        final List<Way> ways = new ArrayList<Way>();
        final int[] indices0 = new int[3];
        indices0[0] = 0;
        for (int i = 1; i < 3; ++i) {
            indices0[i] = i;
        }
        ways.add(new Way(indices0, 0, "Testweg0", true));

        final int[] indices1 = new int[3];
        indices1[0] = 0;
        for (int i = 1; i < 3; ++i) {
            indices1[i] = i + 2;
        }
        ways.add(new Way(indices1, 1, "Testweg1", true));

        final int[] indices2 = new int[3];
        indices2[0] = 0;
        for (int i = 1; i < 3; ++i) {
            indices2[i] = i + 4;
        }
        ways.add(new Way(indices2, 2, "Testweg2", true));
        return ways;
    }

    public static void main(final String[] args) {
        new TransformationVisualizer();
    }
}
