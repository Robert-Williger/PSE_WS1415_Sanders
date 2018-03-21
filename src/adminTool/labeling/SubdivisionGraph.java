package adminTool.labeling;

public class SubdivisionGraph {
    public static int JUNCTION = 0;
    public static int REGULAR = 1;

    public int getRoot() {
        return 0;
    }

    public boolean isRegular(final int node) {
        return roads[node] != -1;
    }

    public boolean isJunction(final int node) {
        return roads[node] == -1;
    }

    public int type(final int node) {
        return type[node];
    }

    public int road(final int node) {
        return roads[node];
    }

    public int beginEdge(final int node) {
        return index[node];
    }

    public int endEdge(final int node) {
        return index[node + 1];
    }

    public boolean isRoadSection(final int edge) {
        return roads[edge] != -1;
    }

    public boolean isJunctionSection(final int edge) {
        return roads[edge] == -1;
    }

    public int edgeHead(final int edge) {
        return edges[edge];
    }

    public double edgeWeight(final int edge) {
        return weights[edge];
    }

    /*
     * public int edgeRoad(final int edge) { return roads[edge]; }
     */

    public int numNodes() {
        return index.length - 1;
    }

    public int numEdges() {
        return edges.length - 1;
    }

    private byte[] type;
    private int[] index;
    private int[] edges;
    private double[] weights;
    private int[] roads;
}
