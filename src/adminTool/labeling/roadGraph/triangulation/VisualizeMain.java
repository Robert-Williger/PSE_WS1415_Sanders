package adminTool.labeling.roadGraph.triangulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import adminTool.BoundedPointAccess;
import adminTool.elements.Way;
import adminTool.labeling.roadGraph.hull.HullCreator;
import adminTool.labeling.roadGraph.hull.HullSimplifier;

public class VisualizeMain {
    private static float pathWidth = 35;
    private static int threshold = 35;

    public static void main(final String[] args) {
        final String filePath = "visualizer";
        final BoundedPointAccess points = createPoints();
        final List<Way> ways = createWays();

        final HullCreator hullCreator = new HullCreator(points);
        final HullSimplifier hullSimplifier = new HullSimplifier();
        hullCreator.createHulls(ways, pathWidth);

        hullSimplifier.simplify(hullCreator.getHulls(), threshold);
        try {
            new PolyWriter(filePath).write(hullSimplifier.getPoints(), hullSimplifier.getOutlines(),
                    hullSimplifier.getHoles());
            new TriangleAdapter(filePath).triangulate();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Visualizer(filePath, pathWidth);
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
        return points;
    }

    private static List<Way> createWays() {
        final List<Way> ways = new ArrayList<Way>();
        final int[] indices1 = new int[8];
        for (int i = 0; i < indices1.length; ++i) {
            indices1[i] = i;
        }
        ways.add(new Way(indices1, 0, "Testweg", true));
        final int[] indices2 = new int[7];
        for (int i = 0; i < indices2.length; ++i) {
            indices2[i] = indices1.length + i;
        }
        ways.add(new Way(indices2, 0, "Testweg2", true));
        return ways;
    }
}
