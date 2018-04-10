package adminTool.labeling.roadGraph;

import java.util.ArrayList;
import java.util.List;

import adminTool.BoundedPointAccess;
import adminTool.IPointAccess;
import adminTool.VisvalingamWhyatt;
import adminTool.labeling.roadGraph.Triangulation;
import util.IntList;

public class PathFormer {
    private static final int[] mid = new int[] { 0, 1, 0, 1 };
    private static final int[] edge = new int[] { 0, 2, 2, 3 };
    private static final int[][] index = new int[][] { new int[] { 1, 2 }, new int[] { 2, 0 }, new int[] { 0, 1 } };
    private static final int UNASSIGNED = -1;

    private Triangulation triangulation;
    private BoundedPointAccess points;
    private int pointCount;
    private int[] triangleToMidpoint;
    private boolean[] marks;
    private List<IntList> paths;
    private VisvalingamWhyatt simplifier;

    public IPointAccess getPoints() {
        return points;
    }

    public List<IntList> getPaths() {
        return paths;
    }

    public void formPaths(final Triangulation triangulation) {
        this.triangulation = triangulation;
        pointCount = 0;

        createPoints(triangulation);

        paths = new ArrayList<IntList>();
        simplifier = new VisvalingamWhyatt(points, 50);
        triangleToMidpoint = new int[triangulation.getTriangles()];
        for (int i = 0; i < triangulation.getTriangles(); ++i) {
            triangleToMidpoint[i] = UNASSIGNED;
        }
        marks = new boolean[triangulation.getTriangles()];
        for (int triangle = 0; triangle < triangulation.getTriangles(); ++triangle) {
            if (triangulation.getNeighbors(triangle) != 2 && triangleToMidpoint[triangle] == -1) {
                connect(-1, triangle);
            }
        }
    }

    private void createPoints(final Triangulation triangulation) {
        int midpoints = 0;
        int edgepoints = 0;
        for (int i = 0; i < triangulation.getTriangles(); ++i) {
            midpoints += mid[triangulation.getNeighbors(i)];
            edgepoints += edge[triangulation.getNeighbors(i)];
        }
        points = new BoundedPointAccess(midpoints + edgepoints / 2);
    }

    private IntList connect(final int prevTriangle, final int triangle) {
        marks[triangle] = true;
        if (triangulation.getNeighbors(triangle) == 2) {
            for (int i = 0; i < 3; ++i) {
                int neighbor = triangulation.getNeighbor(triangle, i);
                if (neighbor != -1 && neighbor != prevTriangle) {
                    final IntList path = connect(triangle, neighbor);
                    if (path == null)
                        return null;

                    addEdgePoint(triangle, i);
                    path.add(pointCount++);
                    return path;
                }
            }
        } else {
            int mid = triangleToMidpoint[triangle];
            if (mid == UNASSIGNED) {
                final int mx = (int) ((triangulation.getX(triangulation.getPoint(triangle, 0))
                        + triangulation.getX(triangulation.getPoint(triangle, 1))
                        + triangulation.getX(triangulation.getPoint(triangle, 2))) / 3.f);
                final int my = (int) ((triangulation.getY(triangulation.getPoint(triangle, 0))
                        + triangulation.getY(triangulation.getPoint(triangle, 1))
                        + triangulation.getY(triangulation.getPoint(triangle, 2))) / 3.f);
                points.setPoint(pointCount, mx, my);
                triangleToMidpoint[triangle] = pointCount;
                mid = pointCount++;
                for (int i = 0; i < 3; ++i) {
                    final int neighbor = triangulation.getNeighbor(triangle, i);
                    if (neighbor != -1 && !marks[neighbor]) {
                        final IntList path = connect(triangle, neighbor);
                        if (path != null) {
                            addEdgePoint(triangle, i);
                            path.add(pointCount++);
                            path.add(mid);
                            // TODO remove unused points from points
                            paths.add(simplifier.simplifyMultiline(path));
                        }
                    }
                }
            }
            final IntList ret = new IntList();
            ret.add(mid);
            return ret;
        }

        return null;
    }

    private void addEdgePoint(final int triangle, final int i) {
        final int ex = (triangulation.getX(triangulation.getPoint(triangle, index[i][0]))
                + triangulation.getX(triangulation.getPoint(triangle, index[i][1]))) / 2;
        final int ey = (triangulation.getY(triangulation.getPoint(triangle, index[i][0]))
                + triangulation.getY(triangulation.getPoint(triangle, index[i][1]))) / 2;

        points.setPoint(pointCount, ex, ey);
    }
}
