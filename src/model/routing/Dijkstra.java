package model.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Dijkstra extends AbstractProgressable implements ISPSPSolver {
    private final IDirectedGraph graph;
    private final int[] distance;
    private final int[] parentNode;
    private final int[] parentEdge;
    private IAddressablePriorityQueue<Integer> queue;
    private int[] endNodes;
    private int achievedNodes;
    private int state;

    public Dijkstra(final IDirectedGraph graph) {
        this.graph = graph;
        final int nodes = graph.getNodes();
        distance = new int[nodes];
        parentNode = new int[nodes];
        parentEdge = new int[nodes];
    }

    public IAddressablePriorityQueue<Integer> createQueue() {
        return new AddressableBinaryHeap<Integer>();
    }

    @Override
    public void cancelCalculation() {
        state = 2;
    }

    @Override
    public Path calculateShortestPath(final InterNode start, final InterNode end) {
        initializeDijkstra(start, end);
        executeDijkstra();
        fireReadyEvent();

        return state == 1 ? createPath(start, end) : state == 2 ? null : fireNoRouteError();
    }

    private void initializeDijkstra(final InterNode start, final InterNode end) {
        queue = createQueue();
        endNodes = new int[2];
        // 0 working, 1 ready, 2 cancel/error
        state = 0;
        achievedNodes = 0;

        for (int i = 0; i < graph.getNodes(); i++) {
            distance[i] = Integer.MAX_VALUE;
            parentNode[i] = -1;
            parentEdge[i] = -1;
        }

        final int weight = graph.getWeight(start.getEdge());
        final float offset = start.getOffset();

        initializeStartNode(start.getEdge(), (int) (weight * offset));
        initializeStartNode(start.getCorrespondingEdge(), (int) (weight * (1 - offset)));

        // TODO handle one-ways!
        endNodes = new int[]{graph.getStartNode(end.getEdge()), graph.getEndNode(end.getEdge())};
    }

    private void initializeStartNode(final int edge, final int weight) {
        if (edge != -1) {
            final int node = graph.getEndNode(edge);
            distance[node] = weight;
            parentNode[node] = node;
            parentEdge[node] = edge;
            queue.insert(node, weight);
        }
    }

    private void executeDijkstra() {
        while (state == 0 && !queue.isEmpty()) {
            final int u = queue.deleteMin();

            if (u == endNodes[0] || u == endNodes[1]) {
                if (++achievedNodes >= 2) {
                    state = 1;
                }
            }

            scan(u);
        }

    }

    private void scan(final int u) {
        if (state == 0) {
            final Iterator<Integer> iterator = graph.getOutgoingEdges(u);
            while (iterator.hasNext()) {
                relax(u, iterator.next());
            }
        }

    }

    private void relax(final int startNode, final int edge) {
        final int endNode = graph.getEndNode(edge);
        final int totalDistance = distance[startNode] + graph.getWeight(edge);
        if (totalDistance < distance[endNode]) {
            distance[endNode] = totalDistance;
            parentNode[endNode] = startNode;
            parentEdge[endNode] = edge;

            if (queue.contains(endNode)) {
                queue.changeKey(endNode, totalDistance);
            } else {
                queue.insert(endNode, totalDistance);
            }
        }
    }

    private List<Integer> createListOfEdges(final int endNode) {
        final List<Integer> edges = new ArrayList<Integer>();

        int node = endNode;

        while (parentNode[node] != node) {
            edges.add(parentEdge[node]);
            node = parentNode[node];
        }
        Collections.reverse(edges);

        return edges;
    }

    private Path createPath(final InterNode start, final InterNode end) {
        final int[] weights = {0, 0};
        weights[0] = distance[endNodes[0]] + Math.round(graph.getWeight(end.getEdge()) * end.getOffset());
        weights[1] = distance[endNodes[1]] + Math.round(graph.getWeight(end.getEdge()) * (1 - end.getOffset()));

        final int minIndex;
        if (weights[0] < weights[1]) {
            minIndex = 0;
        } else {
            minIndex = 1;
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

    private Path fireNoRouteError() {
        fireErrorOccured("Es existiert keine Verbindung zwischen den angegebenen Punkten.");
        return null;
    }

    private void fireReadyEvent() {
        fireProgressDone(100);
    }

}