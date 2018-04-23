package adminTool.labeling.roadGraph.simplification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import adminTool.IPointAccess;
import adminTool.UnboundedPointAccess;
import adminTool.VisvalingamWhyatt;
import adminTool.elements.MultiElement;
import adminTool.elements.Way;
import adminTool.labeling.roadGraph.simplification.hull.HullCreator;
import adminTool.labeling.roadGraph.simplification.hull.HullSimplifier;
import adminTool.labeling.roadGraph.simplification.triangulation.Triangulator;
import util.IntList;

public class Simplification {
    private final int maxWayCoordWidth;
    private final List<MultiElement> paths;
    private UnboundedPointAccess points;
    private final HullCreator hullCreator;
    private final HullSimplifier hullSimplifier;
    private final Triangulator triangulator;
    private final PathFormer pathFormer;
    private final PathSimplifier pathSimplifier;
    private final VisvalingamWhyatt simplifier;

    public Simplification(final int maxWayCoordWidth, final int simplifyThreshold) {
        this.paths = new ArrayList<MultiElement>();
        this.simplifier = new VisvalingamWhyatt(simplifyThreshold);
        this.hullCreator = new HullCreator();
        this.hullSimplifier = new HullSimplifier(5);//simplifyThreshold);
        this.triangulator = new Triangulator();
        this.pathFormer = new PathFormer();
        this.pathSimplifier = new PathSimplifier(simplifyThreshold);
        this.maxWayCoordWidth = maxWayCoordWidth;
    }

    public UnboundedPointAccess getPoints() {
        return points;
    }

    public List<MultiElement> getPaths() {
        return paths;
    }

    public void simplify(final Collection<List<Way>> identifiedWays, final UnboundedPointAccess points) {
        this.points = points;
        int equalWayNr = 0;

        for (final List<Way> equalWays : identifiedWays) {
            if (equalWays.size() > 1) {
                hullCreator.createHulls(equalWays, points, maxWayCoordWidth);

                if (hullCreator.getHulls().size() == equalWays.size()) {
                    appendOriginalPaths(equalWays, points, equalWayNr);
                } else {
                    hullSimplifier.simplify(hullCreator.getHulls());
                    triangulator.triangulate(hullSimplifier.getPoints(), hullSimplifier.getOutlines(),
                            hullSimplifier.getHoles());
                    pathFormer.formPaths(triangulator.getTriangulation(), maxWayCoordWidth);
                    pathSimplifier.simplify(pathFormer.getPaths(), pathFormer.getPoints());

                    appendSimplifiedPaths(equalWayNr);
                }
            } else {
                appendOriginalPaths(equalWays, points, equalWayNr);
            }

            ++equalWayNr;

            if (equalWayNr % 1000 == 0)
                System.out.println(
                        "ways: " + equalWayNr + ", points: " + this.points.getPoints() + ", paths: " + paths.size());

        }
    }

    private void appendSimplifiedPaths(final int equalWayNr) {
        final IPointAccess pathPoints = pathSimplifier.getPoints();
        final int offset = points.getPoints();
        for (final IntList path : pathSimplifier.getPaths()) {
            final int[] indices = new int[path.size()];
            for (int i = 0; i < indices.length; ++i) {
                final int index = path.get(i);
                indices[i] = index + offset;
            }
            this.paths.add(new MultiElement(indices, equalWayNr));
        }
        for (int i = 0; i < pathPoints.getPoints(); ++i) {
            this.points.addPoint(pathPoints.getX(i), pathPoints.getY(i));
        }
    }

    private void appendOriginalPaths(final List<Way> ways, final IPointAccess origPoints, final int equalWayNr) {
        for (final Way way : ways) {
            final IntList list = simplifier.simplifyMultiline(origPoints, way.iterator());
            final int[] indices = new int[list.size()];
            for (int i = 0; i < indices.length; ++i) {
                indices[i] = points.getPoints();
                points.addPoint(origPoints.getX(list.get(i)), origPoints.getY(list.get(i)));
            }
            this.paths.add(new MultiElement(indices, equalWayNr));
        }
    }
}
