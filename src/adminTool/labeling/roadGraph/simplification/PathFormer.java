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
    private static final int WILDCARD_NEIGHBOR = 3;
    private static final int UNASSIGNED = -1;

    private Triangulation triangulation;
    private BoundedPointAccess points;
    private int pointCount;
    private int[] triangleToMidpoint;
    private int[] visited;
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
        visited = new int[triangulation.getTriangles()];

        for (int triangle = 0; triangle < triangulation.getTriangles(); ++triangle) {
            if (triangulation.getNeighbors(triangle) != 2) {
                for (int index = 0; index < 3; ++index) {
                    if (!visited(triangle, index) && triangulation.getNeighbor(triangle, index) != -1) {
                        final IntList connection = formPath(triangle, index);
                        if (connection.size() > 1)
                            paths.add(connection);
                    }
                }
            }
        }

        for (int triangle = 0; triangle < triangulation.getTriangles(); ++triangle) {
            if (!isVisited(triangle)) {// found cycle of two-neighbor-triangles
                paths.add(formCycle(triangle));
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

    private IntList formPath(final int triangle, final int index) {
        final IntList connection = new IntList();

        visit(triangle, index);
        tryAppendMidpoint(connection, triangle);
        tryAppendEdgepoint(connection, triangle, index);

        int last = triangle;
        int current = triangulation.getNeighbor(triangle, index);

        while (triangulation.getNeighbors(current) == 2) {
            visit(current, WILDCARD_NEIGHBOR);
            int nextIndex = nextIndex(current, last);
            tryAppendEdgepoint(connection, current, nextIndex);
            last = current;
            current = triangulation.getNeighbor(current, nextIndex);
        }

        tryAppendMidpoint(connection, current);
        visit(current, getNeighborIndex(current, last));

        return connection;
    }

    private IntList formCycle(final int triangle) {
        final IntList connection = new IntList();

        int nextIndex = nextIndex(triangle, -1);
        final int first = getEdgepoint(triangle, nextIndex);
        connection.add(first);

        int last = triangle;
        int current = triangulation.getNeighbor(triangle, nextIndex);

        do {
            visit(current, WILDCARD_NEIGHBOR);
            nextIndex = nextIndex(current, last);
            last = current;
            current = triangulation.getNeighbor(current, nextIndex);
            connection.add(getEdgepoint(last, nextIndex));
        } while (current != triangle);
        connection.add(first);

        return connection;
    }

    private int nextIndex(final int current, final int last) {
        for (int index = 0; index < 3; ++index) {
            final int neighbor = triangulation.getNeighbor(current, index);
            if (neighbor != -1 && neighbor != last) {
                return index;
            }
        }

        return -1;
    }

    private int tryGetEdgepoint(final int triangle, final int i) {
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

    private int getEdgepoint(final int triangle, final int i) {
        int ex = (triangulation.getX(triangulation.getPoint(triangle, index[i][0]))
                + triangulation.getX(triangulation.getPoint(triangle, index[i][1]))) / 2;
        int ey = (triangulation.getY(triangulation.getPoint(triangle, index[i][0]))
                + triangulation.getY(triangulation.getPoint(triangle, index[i][1]))) / 2;

        points.setPoint(pointCount, ex, ey);
        return pointCount++;
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
        int edgepoint = tryGetEdgepoint(triangle, index);
        if (edgepoint != -1)
            connection.add(edgepoint);
    }

    private int getNeighborIndex(final int triangle, final int neighbor) {
        return triangulation.getNeighbor(triangle, 0) == neighbor ? 0
                : triangulation.getNeighbor(triangle, 1) == neighbor ? 1 : 2;
    }

    private void visit(final int triangle, final int neighborIndex) {
        visited[triangle] |= (1 << neighborIndex);
    }

    private boolean visited(final int triangle, final int neighborIndex) {
        return (visited[triangle] & (1 << neighborIndex)) != 0;
    }

    private boolean isVisited(final int triangle) {
        return visited[triangle] != 0;
    }
}
