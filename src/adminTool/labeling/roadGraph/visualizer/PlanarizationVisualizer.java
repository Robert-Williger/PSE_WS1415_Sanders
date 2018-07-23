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
        final JPanel cutPaths = new WayVisualizer(points, planarization.getProcessedRoads());
        System.out.println(planarization.getProcessedRoads().size());
        for (final Road road : planarization.getProcessedRoads()) {
            System.out.println(road.size());
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
        // points.addPoint(50, 200);
        // points.addPoint(100, 195);
        // points.addPoint(280, 170);
        // points.addPoint(420, 20);
        // points.addPoint(670, 300);
        //
        // points.addPoint(500, 80);
        // points.addPoint(500, 210);
        // points.addPoint(595, 100);
        //
        // points.addPoint(615, 270);
        // points.addPoint(595, 210);
        // points.addPoint(580, 140);
        // points.addPoint(470, 100);
        // points.addPoint(360, 115);
        // points.addPoint(110, 210);
        // points.addPoint(80, 260);
        points.addPoint(200, 200);
        points.addPoint(300, 200);
        points.addPoint(400, 200);

        points.addPoint(300, 100);
        points.addPoint(300, 200);
        points.addPoint(300, 300);

        // points.addPoint(615, 270);
        // points.addPoint(595, 210);
        // points.addPoint(580, 140);
        // points.addPoint(470, 100);
        // points.addPoint(360, 115);
        // points.addPoint(110, 210);
        // points.addPoint(80, 260);
        return points;
    }

    private static List<Road> createRoads() {
        final List<Road> ways = new ArrayList<>();
        final int[] indices0 = new int[3];
        for (int i = 0; i < indices0.length; ++i) {
            indices0[i] = i;
        }
        ways.add(new Road(indices0, 0, "Testweg0", 1));

        final int[] indices1 = new int[3];
        for (int i = 0; i < indices1.length; ++i) {
            indices1[i] = i + 3;
        }
        ways.add(new Road(indices1, 1, "Testweg1", 2));

        return ways;
    }

    public static void main(final String[] args) {
        new PlanarizationVisualizer();
    }
}
