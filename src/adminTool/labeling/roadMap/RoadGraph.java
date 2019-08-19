package adminTool.labeling.roadMap;

public class RoadGraph {
    private final int[] firstOut;
    private final int[] head;
    private final int[] pathId;
    private final double[] length;
    private final int[] roads;
    private final int nodes;
    private final int edges;

    public RoadGraph(int[] firstOut, int[] head, double[] length, int[] sectionId, int[] roads) {
        this(firstOut, head, length, sectionId, roads, firstOut.length - 1, head.length);
    }

    public RoadGraph(int[] firstOut, int[] head, double[] length, int[] pathId, int[] roads, int nodes, int edges) {
        super();
        this.firstOut = firstOut;
        this.head = head;
        this.length = length;
        this.pathId = pathId;
        this.roads = roads;
        this.nodes = nodes;
        this.edges = edges;
    }

    public int getRoot() {
        return 0;
    }

    public boolean isRoadSection(final int u, final int v) {
        return isRegular(u) && isRegular(v);
    }

    public boolean isJunctionSection(final int u, final int v) {
        return isJunction(u) || isJunction(v);
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
        return firstOut[node];
    }

    public int endEdge(final int node) {
        return firstOut[node + 1];
    }

    public int edgeHead(final int edge) {
        return head[edge];
    }

    public int sectionId(final int edge) {
        return pathId[edge] >> 1;
    }

    public double edgeLength(final int edge) {
        return length[edge];
    }

    public int numNodes() {
        return nodes;
    }

    public int numEdges() {
        return edges;
    }

}
