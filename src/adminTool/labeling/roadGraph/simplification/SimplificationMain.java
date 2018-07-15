package adminTool.labeling.roadGraph.simplification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import adminTool.elements.BoundedPointAccess;
import adminTool.elements.Way;
import adminTool.labeling.roadGraph.DrawInfo;
import adminTool.labeling.roadGraph.simplification.hull.HullCreator;
import adminTool.labeling.roadGraph.simplification.hull.HullSimplifier;
import adminTool.labeling.roadGraph.simplification.triangulation.PolyWriter;
import adminTool.labeling.roadGraph.simplification.triangulation.TriangleAdapter;
import util.IntList;

public class SimplificationMain {
    private static int pathWidth = 40;
    private static int threshold = 50;

    public static void main(final String[] args) {
        final String filePath = "visualizer";
        final BoundedPointAccess points = createPoints();
        final List<Way> ways = createWays();

        final HullCreator hullCreator = new HullCreator(new DrawInfo(new int[] { pathWidth }, new int[] { 5 }));
        final HullSimplifier hullSimplifier = new HullSimplifier(threshold);
        hullCreator.createHulls(ways, points);

        hullSimplifier.simplify(hullCreator.getHulls());
        try {
            new PolyWriter(filePath).write(hullSimplifier.getPoints(), hullSimplifier.getOutlines(),
                    hullSimplifier.getHoles());
            new TriangleAdapter(filePath).triangulate();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new SimplificationVisualizer(filePath, ways, points, pathWidth, threshold);
    }

    private static BoundedPointAccess createPoints() {
        final BoundedPointAccess points = new BoundedPointAccess(15);
        points.setPoint(0, 50, 200);
        points.setPoint(1, 100, 195);
        points.setPoint(2, 340, 70);
        points.setPoint(3, 400, 20);
        points.setPoint(4, 470, 60);
        points.setPoint(5, 500, 80);
        points.setPoint(6, 600, 110);
        points.setPoint(7, 595, 210);

        points.setPoint(8, 615, 270);
        points.setPoint(9, 595, 210);
        points.setPoint(10, 580, 140);
        points.setPoint(11, 470, 100);
        points.setPoint(12, 360, 115);
        points.setPoint(13, 110, 210);
        points.setPoint(14, 80, 260);
        // points.setPoint(0, 50, 100);
        // points.setPoint(1, 100, 101);
        // points.setPoint(2, 150, 99);
        // points.setPoint(3, 200, 100);
        // points.setPoint(4, 250, 101);
        // points.setPoint(5, 300, 100);
        // points.setPoint(6, 350, 99);
        // points.setPoint(7, 400, 100);
        //
        // points.setPoint(8, 50, 140);
        // points.setPoint(9, 100, 139);
        // points.setPoint(10, 150, 140);
        // points.setPoint(11, 200, 141);
        // points.setPoint(12, 250, 140);
        // points.setPoint(13, 300, 139);
        // points.setPoint(14, 400, 141);

        return points;
    }

    private static List<Way> createWays() {
        final List<Way> ways = new ArrayList<Way>();
        final IntList indices1 = new IntList();
        for (int i = 0; i < 8; ++i) {
            indices1.add(i);
        }
        ways.add(new Way(indices1, 0, "Testweg", true));
        final IntList indices2 = new IntList();
        for (int i = 0; i < 7; ++i) {
            indices2.add(8 + i);
        }
        ways.add(new Way(indices2, 0, "Testweg2", true));
        return ways;
    }
}
