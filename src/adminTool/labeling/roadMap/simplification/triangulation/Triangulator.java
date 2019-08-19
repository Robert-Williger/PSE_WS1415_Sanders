package adminTool.labeling.roadMap.simplification.triangulation;

import java.io.IOException;
import java.util.List;

import adminTool.elements.IPointAccess;
import util.IntList;

public class Triangulator {
    private final PolyWriter writer;
    private final TriangleAdapter triangulator;
    private final TriangulationReader reader;

    private Triangulation triangulation;

    public Triangulator() {
        writer = new PolyWriter("test");
        triangulator = new TriangleAdapter("test");
        reader = new TriangulationReader("test");
    }

    public void triangulate(final IPointAccess points, final List<IntList> outlines, final List<IntList> holes) {
        try {
            writer.write(points, outlines, holes);
            triangulator.triangulate();
            reader.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        triangulation = reader.getTriangulation();
    }

    public Triangulation getTriangulation() {
        return triangulation;
    }
}
