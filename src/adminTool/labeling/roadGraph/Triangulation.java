package adminTool.labeling.roadGraph;

import adminTool.BoundedPointAccess;

public class Triangulation extends BoundedPointAccess {
    private int[] triangles;
    private int[] neighbors;
    private int[] internals;

    public Triangulation(final int triangles, final int nodes) {
        super(nodes);
        this.triangles = new int[3 * triangles];
        this.neighbors = new int[3 * triangles];
        this.internals = new int[triangles];
    }

    public void setNeighbors(final int triangle, final int first, final int second, final int third) {
        final int index = 3 * triangle;
        neighbors[index + 0] = first;
        neighbors[index + 1] = second;
        neighbors[index + 2] = third;
        internals[triangle] = internal(first) + internal(second) + internal(third);
    }

    public void setPoints(final int triangle, final int first, final int second, final int third) {
        final int index = 3 * triangle;
        triangles[index + 0] = first;
        triangles[index + 1] = second;
        triangles[index + 2] = third;
    }

    public int getNeighbor(final int triangle, final int index) {
        return neighbors[3 * triangle + index];
    }

    public int getPoint(final int triangle, final int index) {
        return triangles[3 * triangle + index];
    }

    public int getNeighbors(final int triangle) {
        return internals[triangle];
    }

    public int getTriangles() {
        return internals.length;
    }

    private static int internal(final int neighbor) {
        return neighbor != -1 ? 1 : 0;
    }
}
