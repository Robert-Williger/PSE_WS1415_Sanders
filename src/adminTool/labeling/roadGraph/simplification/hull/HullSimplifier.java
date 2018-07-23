package adminTool.labeling.roadGraph.simplification.hull;

import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

import adminTool.VisvalingamWhyatt;
import adminTool.elements.IPointAccess;
import adminTool.elements.PointAccess;
import util.IntList;

public class HullSimplifier {
    private static final double DEFAULT_FLATNESS = Double.POSITIVE_INFINITY;

    private final VisvalingamWhyatt simplifier;
    private final double flatness;

    private List<IntList> holes;
    private List<IntList> outlines;
    private PointAccess points;

    public HullSimplifier() {
        this(0, DEFAULT_FLATNESS);
    }

    public HullSimplifier(final int threshold) {
        this(threshold, DEFAULT_FLATNESS);
    }

    public HullSimplifier(final int simplificationThreshold, final double flatness) {
        simplifier = new VisvalingamWhyatt(simplificationThreshold);
        this.flatness = flatness;
    }

    public void simplify(final List<Area> areas) {
        holes = new ArrayList<IntList>();
        outlines = new ArrayList<IntList>();
        points = new PointAccess();

        final float[] coords = new float[2];

        int from = 0;
        for (final Area area : areas) {
            final PathIterator pathIterator = area.getPathIterator(null, flatness);

            int startHoles = holes.size();
            while (!pathIterator.isDone()) {
                int seg = pathIterator.currentSegment(coords);
                switch (seg) {
                    case PathIterator.SEG_MOVETO:
                    case PathIterator.SEG_LINETO:
                        points.addPoint((int) coords[0], (int) coords[1]);
                        break;
                    case PathIterator.SEG_CLOSE:
                        final IntList hole = simplifier.simplifyPolygon(points, from, points.size() - from);
                        from = points.size();
                        if (!hole.isEmpty()) {
                            holes.add(hole);
                        }
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
