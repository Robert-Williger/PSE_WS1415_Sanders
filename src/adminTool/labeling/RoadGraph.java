package adminTool.labeling;

public class RoadGraph {
    public int getRoot() {
        return 0;
    }

    public boolean isRegular(final int node) {
        return roads[node] != -1;
    }

    public boolean isJunction(final int node) {
        return roads[node] == -1;
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

    public int edgeHead(final int edge) {
        return edges[edge];
    }

    public double edgeWeight(final int edge) {
        return weights[edge];
    }

    public int numNodes() {
        return index.length - 1;
    }

    public int numEdges() {
        return edges.length - 1;
    }

    private int[] index;
    private int[] edges;
    private double[] weights;
    private int[] roads;
}
