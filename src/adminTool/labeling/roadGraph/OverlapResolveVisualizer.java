package adminTool.labeling.roadGraph;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import adminTool.UnboundedPointAccess;
import adminTool.elements.MultiElement;
import adminTool.elements.Way;
import adminTool.labeling.IDrawInfo;

public class OverlapResolveVisualizer extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final int WAY_WIDTH = 60;

    public OverlapResolveVisualizer() {
        setLayout(null);
        getContentPane().setBackground(Color.white);
        setTitle("Overlap-Resolve Visualizer");

        final UnboundedPointAccess points = createPoints();
        final List<? extends MultiElement> ways = createWays();

        final ITypeMap map = new ITypeMap() {
            @Override
            public int getType(final int id) {
                return id;
            }
        };
        final IDrawInfo info = new IDrawInfo() {

            @Override
            public int getStrokeWidth(final int type) {
                return WAY_WIDTH;
            }

            @Override
            public int getFontSize(final int type) {
                return WAY_WIDTH;
            }
        };

        final OverlapResolve resolve = new OverlapResolve(map, info, 0);
        resolve.resolve(ways, points, new Dimension(615, 270));

        final JPanel origPaths = new WayVisualizer(points, ways);
        final JPanel cutPaths = new WayVisualizer(points, resolve.getProcessedPaths());

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
        points.addPoint(200, 250);
        // points.addPoint(300, 250);
        points.addPoint(400, 250);
        // points.addPoint(500, 250);
        return points;
    }

    private static List<Way> createWays() {
        final List<Way> ways = new ArrayList<Way>();
        final int[] indices0 = new int[2];
        for (int i = 0; i < indices0.length; ++i) {
            indices0[i] = i;
        }

        final int[] indices1 = new int[2];
        for (int i = 0; i < indices1.length; ++i) {
            indices1[i] = i + indices0.length;
        }
        ways.add(new Way(indices0, 0, "Testweg0", true));
        ways.add(new Way(indices1, 1, "Testweg1", true));

        return ways;
    }

    public static void main(final String[] args) {
        new OverlapResolveVisualizer();
    }
}
