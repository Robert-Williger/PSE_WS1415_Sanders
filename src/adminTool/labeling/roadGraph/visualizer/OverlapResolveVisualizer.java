package adminTool.labeling.roadGraph.visualizer;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import adminTool.elements.PointAccess;
import adminTool.labeling.IDrawInfo;
import adminTool.labeling.roadGraph.OverlapResolve;
import adminTool.labeling.roadGraph.Road;
import util.IntList;

public class OverlapResolveVisualizer extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final int WAY_WIDTH = 60;

    public OverlapResolveVisualizer() {
        setLayout(null);
        getContentPane().setBackground(Color.white);
        setTitle("Overlap-Resolve Visualizer");

        final PointAccess points = createPoints();
        final List<Road> ways = createRoads();

        final IDrawInfo info = new IDrawInfo() {

            @Override
            public double getStrokeWidth(final int type) {
                return WAY_WIDTH;
            }

            @Override
            public double getFontSize(final int type) {
                return WAY_WIDTH;
            }
        };

        final OverlapResolve resolve = new OverlapResolve(info);
        resolve.resolve(ways, points, new Dimension(615, 270));

        final JPanel origPaths = new WayVisualizer(points, ways);
        final JPanel cutPaths = new WayVisualizer(points, resolve.getRoads());

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
        points.addPoint(100, 200);
        // points.addPoint(200, 200);
        // points.addPoint(300, 200);
        // points.addPoint(400, 200);
        points.addPoint(500, 200);

        // points.addPoint(100, 250);
        points.addPoint(100, 200);
        // points.addPoint(300, 250);
        points.addPoint(400, 250);
        // points.addPoint(500, 250);
        return points;
    }

    private static List<Road> createRoads() {
        final List<Road> ways = new ArrayList<>();
        final IntList indices0 = new IntList(2);
        for (int i = 0; i < 2; ++i) {
            indices0.add(i);
        }

        final IntList indices1 = new IntList(2);
        for (int i = 0; i < 2; ++i) {
            indices1.add(i + 2);
        }
        ways.add(new Road(indices0, 0, 1));
        ways.add(new Road(indices1, 1, 1));

        return ways;
    }

    public static void main(final String[] args) {
        new OverlapResolveVisualizer();
    }
}
