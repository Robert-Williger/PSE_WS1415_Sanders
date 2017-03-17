package model.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import util.AddressableBinaryHeap;
import util.IAddressablePriorityQueue;

public class ReusableDijkstra extends AbstractProgressable implements ISPSPSolver {
    private final IDirectedGraph               graph;
    private final int[]                        distance;
    private final int[]                        parent;
    // TODO
    // private final Set<Integer> endNodeSet;
    private int[]                              endNodes;
    private IAddressablePriorityQueue<Integer> queue;
    private boolean                            canceled;

    private InterNode                          start;

    public ReusableDijkstra(final IDirectedGraph graph) {
        this.graph = graph;
        final int nodes = graph.getNodes();
        // endNodeSet = new HashSet<Integer>(2);
        distance = new int[nodes];
        parent = new int[nodes];
    }

    public IAddressablePriorityQueue<Integer> createQueue() {
        return new AddressableBinaryHeap<>();
    }

    @Override
    public void cancelCalculation() {
        start = null;
        canceled = true;
    }

    @Override
    public Path calculateShortestPath(final InterNode start, final InterNode end) {
        initializeDijkstra(start, end);
        if (parent[endNodes[0]] == -1 || parent[endNodes[1]] == -1) {
            execute();
        }

        return !canceled ? createPath(start, end) : null;
    }

    private void initializeDijkstra(final InterNode start, final InterNode end) {
        if (!start.equals(this.start)) {
            this.start = start;

            queue = createQueue();

            for (int i = 0; i < graph.getNodes(); i++) {
                distance[i] = Integer.MAX_VALUE;
                parent[i] = -1;
            }

            final int weight = graph.getWeight(start.getEdge());
            final float offset = start.getOffset();

            initializeStartNode(start.getEdge(), (int) (weight * offset));
            initializeStartNode(start.getCorrespondingEdge(), (int) (weight * (1 - offset)));
        }

        canceled = false;
        // TODO handle one-ways!
        endNodes = new int[]{graph.getStartNode(end.getEdge()), graph.getEndNode(end.getEdge())};
    }

    private void initializeStartNode(final int edge, final int weight) {
        if (edge != -1) {
            final int node = graph.getStartNode(edge);
            distance[node] = weight;
            parent[node] = edge;
            queue.insert(node, weight);
        }
    }

    // TODO
    // private void initializeEndNode(final int edge, final int index) {
    // final int node = graph.getStartNode(edge);
    // endNodeSet.add(node);
    // endNodes[index] = node;
    // }

    private void execute() {

        while (!canceled && !queue.isEmpty()) {
            final int u = queue.deleteMin();

            scan(u);

            if (u == endNodes[0]) {
                if (parent[endNodes[1]] != -1) {
                    break;
                }
            } else if (u == endNodes[1]) {
                if (parent[endNodes[0]] != -1) {
                    break;
                }
            }

        }

    }

    private final void scan(final int u) {
        final Iterator<Integer> iterator = graph.getOutgoingEdges(u);
        while (iterator.hasNext()) {
            relax(u, iterator.next());
        }
    }

    private final void relax(final int startNode, final int edge) {
        final int endNode = graph.getEndNode(edge);
        final int totalDistance = distance[startNode] + graph.getWeight(edge);
        if (totalDistance < distance[endNode]) {
            distance[endNode] = totalDistance;
            parent[endNode] = edge;

            if (!queue.contains(endNode)) {
                queue.insert(endNode, totalDistance);
            } else {
                queue.changeKey(endNode, totalDistance);
            }
        }
    }

    private List<Integer> createListOfEdges(final int endNode) {
        final List<Integer> edges = new ArrayList<>();

        int node = endNode;
        int edge = parent[node];
        node = graph.getStartNode(edge);

        while (parent[node] != edge) {
            edges.add(edge);
            edge = parent[node];
            node = graph.getStartNode(edge);
        }

        Collections.reverse(edges);

        return edges;
    }

    private Path createPath(final InterNode start, final InterNode end) {

        if (distance[endNodes[0]] == Integer.MAX_VALUE && distance[endNodes[1]] == Integer.MAX_VALUE) {
            fireNoRouteError();
            return null;
        }

        final int[] weights = new int[endNodes.length];
        final float[] offsets = new float[]{end.getOffset(), 1 - end.getOffset()};
        int minIndex = 0;

        for (int i = 0; i < weights.length; i++) {
            weights[i] = distance[endNodes[i]] + Math.round(graph.getWeight(end.getEdge()) * offsets[i]);
            if (weights[i] < weights[minIndex]) {
                minIndex = i;
            }
        }

        fireReadyEvent();

        if (start.getEdge() == end.getEdge()) {
            final float temp = (Math.abs(start.getOffset() - end.getOffset()) * graph.getWeight(start.getEdge()));
            if (temp <= weights[minIndex]) {
                return new Path(Math.round(temp), new ArrayList<Integer>(), start, end);
            }
        }

        return new Path(weights[minIndex], createListOfEdges(endNodes[minIndex]), start, end);
    }

    private void fireNoRouteError() {
        fireErrorOccured("Es existiert keine Verbindung zwischen den angegebenen Punkten.");
    }

    private void fireReadyEvent() {
        fireProgressDone(100);
    }

}