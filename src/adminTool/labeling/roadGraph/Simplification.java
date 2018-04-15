package adminTool.labeling.roadGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import adminTool.IPointAccess;
import adminTool.UnboundedPointAccess;
import adminTool.elements.MultiElement;
import adminTool.elements.Way;
import adminTool.labeling.roadGraph.hull.HullCreator;
import adminTool.labeling.roadGraph.hull.HullSimplifier;
import adminTool.labeling.roadGraph.triangulation.Triangulator;
import util.IntList;

public class Simplification {

    private final int maxWayCoordWidth;
    private final int threshold;
    private final List<MultiElement> simplifiedPaths;
    private final UnboundedPointAccess simplifiedPoints;

    public Simplification(final int maxWayCoordWidth, final int simplifyThreshold) {
        this.maxWayCoordWidth = maxWayCoordWidth;
        this.threshold = simplifyThreshold;
        this.simplifiedPaths = new ArrayList<MultiElement>();
        this.simplifiedPoints = new UnboundedPointAccess();
    }

    public void simplify(final Collection<List<Way>> identifiedWays, final IPointAccess points) {
        final HullCreator hullCreator = new HullCreator(points);
        final HullSimplifier hullSimplifier = new HullSimplifier(threshold);
        final Triangulator triangulator = new Triangulator();
        final PathFormer pathFormer = new PathFormer(threshold);

        int equalWayNr = 0;
        for (final List<Way> equalWays : identifiedWays) {
            hullCreator.createHulls(equalWays, maxWayCoordWidth);
            hullSimplifier.simplify(hullCreator.getHulls());
            triangulator.triangulate(hullSimplifier.getPoints(), hullSimplifier.getOutlines(),
                    hullSimplifier.getHoles());
            pathFormer.formPaths(triangulator.getTriangulation(), maxWayCoordWidth);
            appendPaths(pathFormer, simplifiedPoints, equalWayNr);
            ++equalWayNr;

            if (equalWayNr % 1000 == 0) {
                System.out.println(equalWayNr);
            }
        }
    }

    public UnboundedPointAccess getPoints() {
        return simplifiedPoints;
    }

    public List<MultiElement> getPaths() {
        return simplifiedPaths;
    }

    private void appendPaths(final PathFormer pathFormer, final UnboundedPointAccess points, final int equalWayNr) {
        final IPointAccess pathPoints = pathFormer.getPoints();
        for (final IntList path : pathFormer.getPaths()) {
            final int[] indices = path.toArray();
            for (int i = 0; i < indices.length; ++i) {
                indices[i] += points.getPoints();
            }
            simplifiedPaths.add(new MultiElement(indices, equalWayNr));
        }
        for (int i = 0; i < pathPoints.getPoints(); ++i) {
            points.addPoint(pathPoints.getX(i), pathPoints.getY(i));
        }
    }
}
