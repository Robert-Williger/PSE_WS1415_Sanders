package adminTool.labeling.roadGraph;

import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.PrimitiveIterator;

import adminTool.IPointAccess;
import util.IntList;

import static adminTool.Util.calculatePointInPolygon;

public class PolyWriter {
    private final String filepath;
    private BufferedWriter polyWriter;
    private int node;
    private int segment;
    private IPointAccess points;

    public PolyWriter(final String filepath) {
        this.filepath = filepath;
    }

    public void write(final IPointAccess points, final IntList outline, final List<IntList> holes) throws IOException {
        this.points = points;
        polyWriter = new BufferedWriter(new FileWriter(filepath + ".poly"));

        final int nodes = countNodes(outline, holes);
        writeNodes(outline, holes, nodes);
        writeSegments(outline, holes, nodes);
        writeHoles(points, holes);

        polyWriter.close();
    }

    private int countNodes(final IntList outline, final List<IntList> holes) {
        int nodes = outline.size();
        for (final IntList list : holes) {
            nodes += list.size();
        }
        return nodes;
    }

    private void writeNodes(final IntList outline, final List<IntList> holes, final int nodes) throws IOException {
        node = 1;
        polyWriter.write(nodes + " 2 0 0");
        polyWriter.newLine();

        writeNodes(outline);
        for (final IntList list : holes) {
            writeNodes(list);
        }
    }

    private void writeNodes(final IntList nodes) throws IOException {

        for (final PrimitiveIterator.OfInt it = nodes.iterator(); it.hasNext(); ++node) {
            int index = it.nextInt();
            polyWriter.write(node + " " + points.getX(index) + " " + points.getY(index));
            polyWriter.newLine();
        }
    }

    private void writeSegments(final IntList outline, final List<IntList> holes, final int nodes) throws IOException {
        segment = 1;
        polyWriter.write(nodes + " 0");
        polyWriter.newLine();

        writeSegments(outline.size());
        for (final IntList list : holes) {
            writeSegments(list.size());
        }
    }

    private void writeSegments(final int segments) throws IOException {
        final int from = segment;
        final int to = segment + segments - 1;
        for (; segment < to; ++segment) {
            polyWriter.write(segment + " " + segment + " " + (segment + 1));
            polyWriter.newLine();
        }
        polyWriter.write(segment + " " + segment + " " + from);
        polyWriter.newLine();
        ++segment;
    }

    private void writeHoles(final IPointAccess points, final List<IntList> holes) throws IOException {
        polyWriter.write("" + holes.size());
        polyWriter.newLine();
        int holeCount = 1;
        for (final IntList hole : holes) {
            final Point2D pointInHole = calculatePointInPolygon(hole, points);
            polyWriter.write(holeCount + " " + pointInHole.getX() + " " + pointInHole.getY());
            polyWriter.newLine();
            ++holeCount;
        }
    }

}
