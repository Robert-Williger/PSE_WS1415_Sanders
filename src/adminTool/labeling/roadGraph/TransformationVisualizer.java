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

    private static final int WAY_WIDTH = 50;
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
        points.addPoint(100, 100);
        points.addPoint(180, 100);
        points.addPoint(220, 100);
        points.addPoint(300, 100);
        points.addPoint(180, 150);
        points.addPoint(220, 50);
        return points;
    }

    private static List<Way> createWays() {
        final List<Way> ways = new ArrayList<Way>();
        final int[] indices0 = new int[2];
        indices0[0] = 0;
        indices0[1] = 1;
        ways.add(new Way(indices0, 0, "Testweg0", true));

        final int[] indices1 = new int[2];
        indices1[0] = 1;
        indices1[1] = 2;
        ways.add(new Way(indices1, 1, "Testweg1", true));

        final int[] indices2 = new int[2];
        indices2[0] = 2;
        indices2[1] = 3;
        ways.add(new Way(indices2, 2, "Testweg2", true));

        final int[] indices3 = new int[2];
        indices3[0] = 1;
        indices3[1] = 4;
        ways.add(new Way(indices3, 3, "Testweg3", true));

        final int[] indices4 = new int[2];
        indices4[0] = 2;
        indices4[1] = 5;
        ways.add(new Way(indices4, 4, "Testweg4", true));

        return ways;
    }

    public static void main(final String[] args) {
        new TransformationVisualizer();
    }
}
