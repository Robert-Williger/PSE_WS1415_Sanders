package adminTool.labeling.roadGraph.visualizer;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import adminTool.elements.PointAccess;
import adminTool.labeling.roadGraph.Planarization;
import adminTool.labeling.roadGraph.Road;
import util.IntList;

public class PlanarizationVisualizer extends JFrame {
    private static final long serialVersionUID = 1L;

    public PlanarizationVisualizer() {
        setLayout(null);
        getContentPane().setBackground(Color.white);
        setTitle("Planarization Visualizer");

        final PointAccess points = createPoints();
        final List<Road> roads = createRoads();
        final Planarization planarization = new Planarization(10, 5, 1);
        planarization.planarize(roads, points, new Dimension(615, 270));

        final JPanel origPaths = new WayVisualizer(points, roads);
        final JPanel cutPaths = new WayVisualizer(points, planarization.getRoads());

        for (final Road road : planarization.getRoads()) {
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

    private static List<Road> createRoads() {
        final List<Road> ways = new ArrayList<>();
        final IntList indices0 = new IntList();
        for (int i = 0; i < 5; ++i) {
            indices0.add(i);
        }
        ways.add(new Road(indices0, 0, 1));

        return ways;
    }

    public static void main(final String[] args) {
        new PlanarizationVisualizer();
    }
}
