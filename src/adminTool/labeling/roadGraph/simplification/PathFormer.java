package adminTool.labeling.roadGraph.simplification;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import adminTool.BoundedPointAccess;
import adminTool.IPointAccess;
import adminTool.labeling.roadGraph.simplification.triangulation.Triangulation;
import util.IntList;

public class PathFormer {
    private static final int[] mid = new int[] { 0, 0, 0, 1 };
    private static final int[][] index = new int[][] { new int[] { 1, 2 }, new int[] { 2, 0 }, new int[] { 0, 1 } };
    private static final int UNASSIGNED = -1;

    private Triangulation triangulation;
    private BoundedPointAccess points;
    private int pointCount;
    private int[] triangleToMidpoint;
    private boolean[] visited;
    private List<IntList> paths;
    private double minDistSq;

    public IPointAccess getPoints() {
        return points;
    }

    public List<IntList> getPaths() {
        return paths;
    }

    public void formPaths(final Triangulation triangulation, final float lineWidth) {
        this.minDistSq = 0.9 * 0.9 * lineWidth * lineWidth;
        this.triangulation = triangulation;
        pointCount = 0;

        createPoints(triangulation);

        paths = new ArrayList<IntList>();
        triangleToMidpoint = new int[triangulation.getTriangles()];
        for (int i = 0; i < triangulation.getTriangles(); ++i) {
            triangleToMidpoint[i] = UNASSIGNED;
        }
        visited = new boolean[triangulation.getTriangles() + 1];
        visit(-1);

        for (int triangle = 0; triangle < triangulation.getTriangles(); ++triangle) {
            if (triangulation.getNeighbors(triangle) != 2) {
                for (int index = 0; index < 3; ++index) {
                    final int neighbor = triangulation.getNeighbor(triangle, index);
                    if (!visited(neighbor)) {
                        final IntList connection = new IntList();
                        if (triangulation.getNeighbors(neighbor) == 2) {
                            tryAppendMidpoint(connection, triangle);
                            tryAppendEdgepoint(connection, triangle, index);
                            append(connection, neighbor, triangle);
                        } else if (triangle < neighbor) {// avoid generation of duplicates
                            tryAppendMidpoint(connection, triangle);
                            tryAppendEdgepoint(connection, triangle, index);
                            tryAppendMidpoint(connection, neighbor);
                        }
                        if (connection.size() > 1)
                            paths.add(connection);
                    }
                }
            }
        }
    }

    private void createPoints(final Triangulation triangulation) {
        int midpoints = 0;
        int edgepoints = 0;
        for (int i = 0; i < triangulation.getTriangles(); ++i) {
            midpoints += mid[triangulation.getNeighbors(i)];
            edgepoints += triangulation.getNeighbors(i);
        }
        points = new BoundedPointAccess(midpoints + edgepoints / 2);
    }

    // connection contains midpoint from previous and the midpoint of the edge between last and current triangle
    // current triangle has exactly two neighbors
    private void append(final IntList connection, int current, int last) {
        for (int index = 0; index < 3; ++index) {
            final int neighbor = triangulation.getNeighbor(current, index);
            if (!visited(neighbor) && neighbor != last) {
                visit(current);
                tryAppendEdgepoint(connection, current, index);
                current = neighbor;
                break;
            }
        }

        while (triangulation.getNeighbors(current) == 2) {
            for (int index = 0; index < 3; ++index) {
                final int neighbor = triangulation.getNeighbor(current, index);
                if (!visited(neighbor)) {
                    visit(current);
                    tryAppendEdgepoint(connection, current, index);
                    current = neighbor;
                    break;
                }
            }
        }

        tryAppendMidpoint(connection, current);
    }

    private int getEdgepoint(final int triangle, final int i) {
        int ex = (triangulation.getX(triangulation.getPoint(triangle, index[i][0]))
                + triangulation.getX(triangulation.getPoint(triangle, index[i][1]))) / 2;
        int ey = (triangulation.getY(triangulation.getPoint(triangle, index[i][0]))
                + triangulation.getY(triangulation.getPoint(triangle, index[i][1]))) / 2;

        if (Point.distanceSq(triangulation.getX(triangulation.getPoint(triangle, index[i][0])),
                triangulation.getY(triangulation.getPoint(triangle, index[i][0])),
                triangulation.getX(triangulation.getPoint(triangle, index[i][1])),
                triangulation.getY(triangulation.getPoint(triangle, index[i][1]))) > minDistSq) {
            points.setPoint(pointCount, ex, ey);
            return pointCount++;
        }

        return -1;
    }

    private int getMidpoint(final int triangle) {
        int ret = triangleToMidpoint[triangle];
        if (ret == UNASSIGNED) {
            final int mx = (int) ((triangulation.getX(triangulation.getPoint(triangle, 0))
                    + triangulation.getX(triangulation.getPoint(triangle, 1))
                    + triangulation.getX(triangulation.getPoint(triangle, 2))) / 3.f);
            final int my = (int) ((triangulation.getY(triangulation.getPoint(triangle, 0))
                    + triangulation.getY(triangulation.getPoint(triangle, 1))
                    + triangulation.getY(triangulation.getPoint(triangle, 2))) / 3.f);
            points.setPoint(pointCount, mx, my);
            triangleToMidpoint[triangle] = pointCount;
            ret = pointCount++;
        }
        return ret;
    }

    private void tryAppendMidpoint(final IntList connection, final int triangle) {
        if (triangulation.getNeighbors(triangle) == 3)
            connection.add(getMidpoint(triangle));
    }

    private void tryAppendEdgepoint(final IntList connection, final int triangle, final int index) {
        int edgepoint = getEdgepoint(triangle, index);
        if (edgepoint != -1)
            connection.add(edgepoint);
    }

    private void visit(final int triangle) {
        visited[triangle + 1] = true;
    }

    // triangle is visited iff it has exactly two neighbors and was already visited
    private boolean visited(final int triangle) {
        return visited[triangle + 1];
    }
}
