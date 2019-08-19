package adminTool.labeling.roadMap.visualizer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import adminTool.elements.PointAccess;
import adminTool.labeling.IDrawInfo;
import adminTool.labeling.roadMap.LabelSection;
import adminTool.labeling.roadMap.Transformation;
import util.IntList;

public class TransformationVisualizer extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final int WAY_WIDTH = 15;
    private static final int THRESHOLD = 2 * WAY_WIDTH;

    public TransformationVisualizer() {
        setLayout(null);
        getContentPane().setBackground(Color.white);
        setTitle("Transformation Visualizer");

        final PointAccess points = createPoints();
        final List<LabelSection> roads = createRoads();

        final JPanel origPaths = new WayVisualizer(points, roads);
        final IDrawInfo info = new IDrawInfo() {

            @Override
            public double getStrokeWidth(int type) {
                return WAY_WIDTH;
            }

            @Override
            public double getFontSize(int type) {
                return WAY_WIDTH;
            }
        };
        final Transformation transformation = new Transformation(info, THRESHOLD, 0);
        transformation.transform(roads, points);
        final JPanel cutPaths = new WayVisualizer(points, transformation.getRoadSections());

        origPaths.setLocation(20, 10);
        cutPaths.setLocation(850, 10);

        add(origPaths);
        add(cutPaths);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1700, 1040);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private static PointAccess createPoints() {
        final PointAccess points = new PointAccess();
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

    private static List<LabelSection> createRoads() {
        final List<LabelSection> ways = new ArrayList<>();

        final int[] indices0 = new int[9];
        for (int i = 0; i < indices0.length - 1; ++i) {
            indices0[i] = i;
        }
        ways.add(new LabelSection(new IntList(indices0), 0, 1));

        final int[] indices1 = new int[2];
        indices1[0] = 0;
        indices1[1] = 9;
        ways.add(new LabelSection(new IntList(indices1), 1, 2));

        return ways;
    }

    public static void main(final String[] args) {
        new TransformationVisualizer();
    }
}
