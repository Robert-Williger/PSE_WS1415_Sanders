package adminTool.labeling.roadMap.visualizer;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import adminTool.elements.PointAccess;
import adminTool.labeling.roadMap.LabelSection;
import adminTool.labeling.roadMap.Planarization;
import util.IntList;

public class PlanarizationVisualizer extends JFrame {
    private static final long serialVersionUID = 1L;

    public PlanarizationVisualizer() {
        setLayout(null);
        getContentPane().setBackground(Color.white);
        setTitle("Planarization Visualizer");

        final PointAccess points = createPoints();
        final List<LabelSection> roads = createRoads();
        final Planarization planarization = new Planarization(10, 5, 1);
        planarization.planarize(roads, points, new Rectangle2D.Double(0, 0, 615, 270));

        final JPanel origPaths = new WayVisualizer(points, roads);
        final JPanel cutPaths = new WayVisualizer(points, planarization.getRoads());

        System.out.println("orig:");
        for (final LabelSection road : roads) {
            System.out.println(road.toList());
        }

        System.out.println("planarized:");
        for (final LabelSection road : planarization.getRoads()) {
            System.out.println(road.toList());
        }

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
        points.addPoint(200, 200);
        points.addPoint(400, 200);
        points.addPoint(400, 300);

        points.addPoint(300, 300);
        points.addPoint(300, 100);
        return points;
    }

    private static List<LabelSection> createRoads() {
        final List<LabelSection> ways = new ArrayList<>();
        final IntList indices0 = new IntList();
        for (int i = 0; i < 5; ++i) {
            indices0.add(i);
        }
        ways.add(new LabelSection(indices0, 0, 1));

        return ways;
    }

    public static void main(final String[] args) {
        new PlanarizationVisualizer();
    }
}
