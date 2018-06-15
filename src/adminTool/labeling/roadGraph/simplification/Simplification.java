package adminTool.labeling.roadGraph.simplification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import adminTool.IPointAccess;
import adminTool.UnboundedPointAccess;
import adminTool.VisvalingamWhyatt;
import adminTool.elements.MultiElement;
import adminTool.elements.Way;
import adminTool.labeling.roadGraph.DrawInfo;
import adminTool.labeling.roadGraph.ITypeMap;
import adminTool.labeling.roadGraph.simplification.hull.HullCreator;
import adminTool.labeling.roadGraph.simplification.hull.HullSimplifier;
import adminTool.labeling.roadGraph.simplification.triangulation.Triangulator;
import util.IntList;

public class Simplification {
    private List<MultiElement> paths;
    private UnboundedPointAccess points;
    private IntList types;
    private final DrawInfo info;
    private final HullSimplifier hullSimplifier;
    private final PathSimplifier pathSimplifier;
    private final VisvalingamWhyatt simplifier;

    public Simplification(final DrawInfo info, final int simplificationThreshold) {
        this.simplifier = new VisvalingamWhyatt(simplificationThreshold);
        // TODO own parameter for the 5 ?
        this.hullSimplifier = new HullSimplifier(5);// simplificationThreshold);
        this.pathSimplifier = new PathSimplifier(simplificationThreshold);
        this.info = info;
    }

    public UnboundedPointAccess getPoints() {
        return points;
    }

    public List<MultiElement> getPaths() {
        return paths;
    }

    public ITypeMap getTypeMap() {
        return new ITypeMap() {
            @Override
            public int getType(final int id) {
                return types.get(id);
            }
        };
    }

    public void simplify(final Collection<List<Way>> identifiedWays, final IPointAccess points) {
        this.points = new UnboundedPointAccess();
        this.paths = new ArrayList<MultiElement>();
        this.types = new IntList();

        final PathFormer pathFormer = new PathFormer();
        final Triangulator triangulator = new Triangulator();
        final HullCreator hullCreator = new HullCreator(info);
        final PathJoiner pathJoiner = new PathJoiner();
        int equalWayNr = 0;
        for (final List<Way> equalWays : identifiedWays) {
            if (equalWays.isEmpty() || info.getStrokeWidth(equalWays.get(0).getType()) == 0)
                continue;
            if (equalWays.size() > 1) {
                final int type = equalWays.get(0).getType();
                pathJoiner.join(equalWays);

                hullCreator.createHulls(pathJoiner.getProcessedPaths(), points);
                if (hullCreator.getHulls().size() == pathJoiner.getProcessedPaths().size()) {
                    appendOriginalPaths(pathJoiner.getProcessedPaths(), points, equalWayNr);
                } else {
                    hullSimplifier.simplify(hullCreator.getHulls());
                    triangulator.triangulate(hullSimplifier.getPoints(), hullSimplifier.getOutlines(),
                            hullSimplifier.getHoles());
                    pathFormer.formPaths(triangulator.getTriangulation(), info.getStrokeWidth(type));
                    pathSimplifier.simplify(pathFormer.getPaths(), pathFormer.getPoints());
                    appendSimplifiedPaths(equalWayNr, type);
                }
            } else {
                appendOriginalPaths(equalWays, points, equalWayNr);
            }

            ++equalWayNr;
        }
    }

    private void appendSimplifiedPaths(final int equalWayNr, final int type) {
        final IPointAccess pathPoints = pathSimplifier.getPoints();
        final int offset = points.getPoints();
        for (final IntList path : pathSimplifier.getPaths()) {
            final int[] indices = new int[path.size()];
            for (int i = 0; i < indices.length; ++i) {
                final int index = path.get(i);
                indices[i] = index + offset;
            }
            paths.add(new MultiElement(indices, equalWayNr));
        }
        for (int i = 0; i < pathPoints.getPoints(); ++i) {
            points.addPoint(pathPoints.getX(i), pathPoints.getY(i));
        }

        types.add(type);
    }

    private void appendOriginalPaths(final List<Way> ways, final IPointAccess origPoints, final int equalWayNr) {
        for (final Way way : ways) {
            final IntList list = simplifier.simplifyMultiline(origPoints, way.iterator());
            final int[] indices = new int[list.size()];
            for (int i = 0; i < indices.length - 1; ++i) {
                indices[i] = points.getPoints();
                points.addPoint(origPoints.getX(list.get(i)), origPoints.getY(list.get(i)));
            }
            if (list.get(0) != list.get(list.size() - 1)) {
                indices[indices.length - 1] = points.getPoints();
                points.addPoint(origPoints.getX(list.get(list.size() - 1)), origPoints.getY(list.get(list.size() - 1)));
            } else {
                indices[indices.length - 1] = indices[0];
            }
            paths.add(new MultiElement(indices, equalWayNr));
        }

        types.add(!ways.isEmpty() ? ways.get(0).getType() : 0); // TODO fix this
    }

}
