package adminTool.labeling.roadGraph.hull;

import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

import adminTool.IPointAccess;
import adminTool.UnboundedPointAccess;
import adminTool.VisvalingamWhyatt;
import util.IntList;

public class HullSimplifier {
    private static final double DEFAULT_FLATNESS = Double.POSITIVE_INFINITY;

    private List<IntList> holes;
    private List<IntList> outlines;
    private UnboundedPointAccess points;

    public HullSimplifier() {
        holes = new ArrayList<IntList>();
    }

    public void simplify(final List<Area> areas, final int threshold) {
        simplify(areas, threshold, DEFAULT_FLATNESS);
    }

    public void simplify(final List<Area> areas, final int threshold, final double flatness) {
        holes = new ArrayList<IntList>();
        outlines = new ArrayList<IntList>();
        points = new UnboundedPointAccess();

        final float[] segments = new float[2];

        int from = 0;
        for (final Area area : areas) {
            final PathIterator pathIterator = area.getPathIterator(null, flatness);
            final VisvalingamWhyatt simplifier = new VisvalingamWhyatt(points, threshold);

            int startHoles = holes.size();
            while (!pathIterator.isDone()) {
                int seg = pathIterator.currentSegment(segments);
                switch (seg) {
                    case PathIterator.SEG_MOVETO:
                    case PathIterator.SEG_LINETO:
                        points.addPoint((int) segments[0], (int) segments[1]);
                        break;
                    case PathIterator.SEG_CLOSE:
                        IntList list = new IntList();
                        for (int i = from; i < points.getPoints(); ++i) {
                            list.add(i);
                        }
                        final int to = points.getPoints();
                        final IntList hole = simplifier.simplifyPolygon(from, to - from);
                        if (!hole.isEmpty()) {
                            holes.add(hole);
                        }
                        from = to;
                        break;
                }
                pathIterator.next();
            }

            if (startHoles != holes.size()) {
                outlines.add(holes.get(holes.size() - 1));
                holes.remove(holes.size() - 1);
            }
        }
    }

    public List<IntList> getOutlines() {
        return outlines;
    }

    public List<IntList> getHoles() {
        return holes;
    }

    public IPointAccess getPoints() {
        return points;
    }
}
