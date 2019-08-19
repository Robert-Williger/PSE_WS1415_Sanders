package adminTool.labeling.roadMap.visualizer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import adminTool.elements.PointAccess;
import adminTool.elements.Way;
import adminTool.labeling.INameInfo;
import adminTool.labeling.roadMap.Identification;
import util.IntList;

public class IdentificationVisualizer extends JFrame {
    private static final long serialVersionUID = 1L;

    public IdentificationVisualizer() {
        setLayout(null);
        getContentPane().setBackground(Color.white);
        setTitle("Planarization Visualizer");

        final PointAccess points = createPoints();
        final List<Way> ways = createWays();
        final Identification identification = new Identification();
        identification.identify(ways, points);

        final JPanel origPaths = new WayVisualizer(points, ways);
        final JPanel identifiedPaths = new WayVisualizer(identification.getPoints(), identification.getRoads());

        origPaths.setLocation(20, 10);
        identifiedPaths.setLocation(850, 10);

        System.out.println(identification.getRoads().size());

        final INameInfo info = identification.getNameInfo();
        for (int i = 0; i < identification.getRoadIds(); ++i) {
            System.out.println(info.getName(i));
        }

        add(origPaths);
        add(identifiedPaths);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1700, 1040);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private static PointAccess createPoints() {
        final PointAccess points = new PointAccess();

        points.addPoint(400, 200);
        points.addPoint(300, 200);
        points.addPoint(500, 200);
        points.addPoint(200, 200);
        points.addPoint(100, 200);

        return points;
    }

    private static List<Way> createWays() {
        final List<Way> ways = new ArrayList<>();

        for (int way = 0; way < 3; ++way) {
            final int[] indices = new int[2];
            for (int i = 0; i < indices.length; ++i) {
                indices[i] = i + way;
            }
            System.out.println(Arrays.toString(indices));
            ways.add(new Way(new IntList(indices), 0, "Testweg", false));
        }
        for (int way = 0; way < 3; ++way) {
            final int[] indices = new int[2];
            indices[0] = way;
            indices[1] = way + 2;
            System.out.println(Arrays.toString(indices));
            ways.add(new Way(new IntList(indices), 0, "Teststraße", false));
        }
        System.out.println("init done");
        return ways;
    }

    public static void main(final String[] args) {
        new IdentificationVisualizer();
    }
}
