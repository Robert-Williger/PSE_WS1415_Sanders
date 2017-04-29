package model.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PrimitiveIterator.OfInt;

import util.AddressableBinaryHeap;
import util.IAddressablePriorityQueue;

public class Dijkstra extends AbstractProgressable implements ISPSPSolver {
    private final IDirectedGraph graph;
    private final int[] distance;
    private final int[] parentNode;
    private final int[] parentEdge;
    private IAddressablePriorityQueue<Integer> queue;
    private int[] endNodes;
    private int achievedNodes;

    // 0 working, 1 ready, 2 cancel/error
    private int state;

    public Dijkstra(final IDirectedGraph graph) {
        this.graph = graph;
        final int nodes = graph.getNodes();
        distance = new int[nodes];
        parentNode = new int[nodes];
        parentEdge = new int[nodes];
    }

    public IAddressablePriorityQueue<Integer> createQueue() {
        return new AddressableBinaryHeap<>();
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
        state = 0;
        achievedNodes = 0;

        for (int i = 0; i < graph.getNodes(); i++) {
            distance[i] = Integer.MAX_VALUE;
            parentNode[i] = -1;
            parentEdge[i] = -1;
        }

        final int startEdgeWeight = graph.getWeight(start.getEdge());
        final float startEdgeOffset = start.getOffset();

        initializeStartNode(start.getEdge(), (int) (startEdgeWeight * startEdgeOffset));
        if (!start.isOneway()) {
            initializeStartNode(start.getCorrespondingEdge(), (int) (startEdgeWeight * (1 - startEdgeOffset)));
        }

        if (!end.isOneway()) {
            endNodes = new int[] { graph.getStartNode(end.getEdge()), graph.getEndNode(end.getEdge()) };
        } else {
            endNodes = new int[] { graph.getStartNode(end.getEdge()) };
        }
    }

    private void initializeStartNode(final int edge, final int weight) {
        final int node = graph.getEndNode(edge);
        distance[node] = weight;
        parentNode[node] = node;
        parentEdge[node] = edge;
        queue.insert(node, weight);
    }

    private void executeDijkstra() {
        while (state == 0 && !queue.isEmpty()) {
            final int u = queue.deleteMin();

            // endNodes.length is 1 or 2 ..
            if ((u == endNodes[0] || u == endNodes[endNodes.length - 1]) && ++achievedNodes >= endNodes.length) {
                state = 1;
            } else {
                scan(u);
            }
        }

    }

    private void scan(final int u) {
        final OfInt iterator = graph.getOutgoingEdges(u);
        while (iterator.hasNext()) {
            relax(u, iterator.nextInt());
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
        final List<Integer> edges = new ArrayList<>();

        int node = endNode;

        while (parentNode[node] != node) {
            edges.add(parentEdge[node]);
            node = parentNode[node];
        }
        Collections.reverse(edges);

        return edges;
    }

    private Path createPath(final InterNode start, final InterNode end) {
        final int endEdgeWeight = graph.getWeight(end.getEdge());
        int endNode = endNodes[0];
        int weight = distance[endNode] + Math.round(endEdgeWeight * end.getOffset());

        if (!end.isOneway()) {
            final int weight2 = distance[endNodes[1]] + Math.round(endEdgeWeight * (1 - end.getOffset()));

            if (weight2 < weight) {
                endNode = endNodes[1];
                weight = weight2;
            }
        }

        fireReadyEvent();

        if (start.getEdge() == end.getEdge()) {
            float temp = Float.MAX_VALUE;

            if (!end.isOneway()) {
                temp = (Math.abs(start.getOffset() - end.getOffset()) * graph.getWeight(start.getEdge()));
            } else if (start.getOffset() < end.getOffset()) {
                temp = (end.getOffset() - start.getOffset()) * graph.getWeight(start.getEdge());
            }

            if (temp <= weight) {
                return new Path(Math.round(temp), Collections.emptyList(), start, end);
            }
        }

        return new Path(weight, createListOfEdges(endNode), start, end);
    }

    private Path fireNoRouteError() {
        fireErrorOccured("Es existiert keine Verbindung zwischen den angegebenen Punkten.");
        return null;
    }

    private void fireReadyEvent() {
        fireProgressDone(100);
    }

}