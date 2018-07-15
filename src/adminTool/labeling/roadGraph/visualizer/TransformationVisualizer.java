package adminTool.labeling.roadGraph.visualizer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import adminTool.elements.UnboundedPointAccess;
import adminTool.labeling.IDrawInfo;
import adminTool.labeling.roadGraph.Road;
import adminTool.labeling.roadGraph.Transformation;

public class TransformationVisualizer extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final int WAY_WIDTH = 15;
    private static final int THRESHOLD = 2 * WAY_WIDTH;

    public TransformationVisualizer() {
        setLayout(null);
        getContentPane().setBackground(Color.white);
        setTitle("Transformation Visualizer");

        final UnboundedPointAccess points = createPoints();
        final List<Road> roads = createRoads();

        final JPanel origPaths = new WayVisualizer(points, roads);
        final IDrawInfo info = new IDrawInfo() {

            @Override
            public int getStrokeWidth(int type) {
                return WAY_WIDTH;
            }

            @Override
            public int getFontSize(int type) {
                return WAY_WIDTH;
            }
        };
        final Transformation transformation = new Transformation(info, THRESHOLD);
        transformation.transform(roads, points);
        final JPanel cutPaths = new WayVisualizer(points, transformation.getProcessedRoads());

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
        // points.addPoint(100, 90);
        // points.addPoint(245, 100);
        // points.addPoint(260, 100);
        // points.addPoint(400, 90);
        // points.addPoint(245, 150);
        // points.addPoint(260, 150);
        points.addPoint(400, 10);
        points.addPoint(450, 220);
        points.addPoint(470, 270);
        points.addPoint(450, 310);
        points.addPoint(400, 340);
        points.addPoint(350, 310);
        points.addPoint(330, 270);
        points.addPoint(350, 220);
        points.addPoint(300, 200);

        points.addPoint(400, 300);
        return points;
    }

    private static List<Road> createRoads() {
        final List<Road> ways = new ArrayList<>();

        final int[] indices0 = new int[9];
        for (int i = 0; i < indices0.length - 1; ++i) {
            indices0[i] = i;
        }
        ways.add(new Road(indices0, 0, "Testweg0", 1));

        final int[] indices1 = new int[2];
        indices1[0] = 0;
        indices1[1] = 9;
        ways.add(new Road(indices1, 1, "Testweg1", 2));

        // final int[] indices0 = new int[2];
        // indices0[0] = 0;
        // indices0[1] = 1;
        // ways.add(new Way(indices0, 0, "Testweg0", true));
        //
        // final int[] indices1 = new int[2];
        // indices1[0] = 1;
        // indices1[1] = 2;
        // ways.add(new Way(indices1, 1, "Testweg1", true));
        //
        // final int[] indices2 = new int[2];
        // indices2[0] = 2;
        // indices2[1] = 3;
        // ways.add(new Way(indices2, 2, "Testweg2", true));
        //
        // final int[] indices3 = new int[2];
        // indices3[0] = 1;
        // indices3[1] = 4;
        // ways.add(new Way(indices3, 3, "Testweg3", true));
        //
        // final int[] indices4 = new int[2];
        // indices4[0] = 2;
        // indices4[1] = 5;
        // ways.add(new Way(indices4, 4, "Testweg4", true));

        return ways;
    }

    public static void main(final String[] args) {
        new TransformationVisualizer();
    }
}
