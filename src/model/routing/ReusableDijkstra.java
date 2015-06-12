package model.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ReusableDijkstra extends AbstractProgressable implements ISPSPSolver {
    private final IGraph graph;
    private final int[] distance;
    private final int[] parent;
    private IAddressablePriorityQueue<Integer> queue;
    private boolean canceled;
    private InterNode start;

    public ReusableDijkstra(final IGraph graph) {
        this.graph = graph;
        final int nodes = graph.getNodes();
        distance = new int[nodes];
        parent = new int[nodes];
    }

    public IAddressablePriorityQueue<Integer> createQueue() {
        return new AddressableBinaryHeap<Integer>();
    }

    @Override
    public void cancelCalculation() {
        start = null;
        canceled = true;
    }

    @Override
    public Path calculateShortestPath(final InterNode start, final InterNode end) {
        initializeDijkstra(start, end);
        execute();

        return !canceled ? createPath(start, end) : null;
    }

    private void initializeDijkstra(final InterNode start, final InterNode end) {
        if (!start.equals(this.start)) {
            this.start = start;

            queue = createQueue();

            canceled = false;

            for (int i = 0; i < graph.getNodes(); i++) {
                distance[i] = Integer.MAX_VALUE;
                parent[i] = -1;
            }

            final int[] startNodes = {graph.getFirstNode(start.getEdge()), graph.getSecondNode(start.getEdge())};
            final int weight = graph.getWeight(start.getEdge());
            final float offset = start.getOffset();

            distance[startNodes[0]] = (int) (weight * offset);
            distance[startNodes[1]] = (int) (weight * (1 - offset));

            for (int i = 0; i < 2; i++) {
                parent[startNodes[i]] = startNodes[i];
            }

            queue.insert(startNodes[0], (int) (weight * offset));
            queue.insert(startNodes[1], (int) (weight * (1 - offset)));
        }
    }

    private void execute() {

        while (!canceled && !queue.isEmpty()) {
            final int u = queue.deleteMin();

            scan(u);
        }

    }

    private final void scan(final int u) {
        final Iterator<Integer> it = graph.getAdjacentNodes(u);
        while (it.hasNext()) {
            relax(u, it.next());
        }
    }

    private final void relax(final int firstNode, final int secondNode) {
        final int totalDistance = distance[firstNode] + graph.getWeight(graph.getEdge(firstNode, secondNode));
        if (totalDistance < distance[secondNode]) {
            distance[secondNode] = totalDistance;
            parent[secondNode] = firstNode;

            if (!queue.contains(secondNode)) {
                queue.insert(secondNode, totalDistance);
            } else {
                queue.changeKey(secondNode, totalDistance);
            }
        }
    }

    private List<Integer> createListOfPoints(final int endNode) {
        final List<Integer> ret = new ArrayList<Integer>();

        int node = endNode;
        ret.add(node);

        while (parent[node] != node) {
            ret.add(parent[node]);
            node = parent[node];
        }
        Collections.reverse(ret);
        return ret;
    }

    private List<Long> createListOfEdges(final List<Integer> points) {
        final List<Long> edges = new ArrayList<Long>();
        for (int i = 1; i < points.size(); i++) {
            final long edge = graph.getEdge(points.get(i - 1), points.get(i));
            edges.add(edge);
        }
        return edges;
    }

    private Path createPath(final InterNode start, final InterNode end) {
        final int[] weights = {0, 0};

        final int[] endNodes = {graph.getFirstNode(end.getEdge()), graph.getSecondNode(end.getEdge())};

        weights[0] = distance[endNodes[0]];
        weights[1] = distance[endNodes[1]];

        if (weights[0] == Integer.MAX_VALUE && weights[1] == Integer.MAX_VALUE) {
            fireNoRouteError();
            return null;
        }

        if (endNodes[0] < endNodes[1]) {
            weights[0] += graph.getWeight(end.getEdge()) * end.getOffset();
            weights[1] += graph.getWeight(end.getEdge()) * (1 - end.getOffset());
        } else {
            weights[0] += graph.getWeight(end.getEdge()) * (1 - end.getOffset());
            weights[1] += graph.getWeight(end.getEdge()) * end.getOffset();
        }

        int weight;

        List<Long> edges;
        if (weights[0] < weights[1]) {
            weight = weights[0];
            edges = createListOfEdges(createListOfPoints(endNodes[0]));
        } else {
            weight = weights[1];
            edges = createListOfEdges(createListOfPoints(endNodes[1]));
        }

        fireReadyEvent();

        if (start.getEdge() == end.getEdge()) {
            final int temp = (int) (Math.abs(start.getOffset() - end.getOffset()) * graph.getWeight(start.getEdge()));
            if (temp <= weight) {
                return new Path(temp, new ArrayList<Long>(), start, end);
            }
        }
        return new Path(weight, edges, start, end);
    }

    private void fireNoRouteError() {
        fireErrorOccured("Es existiert keine Verbindung zwischen den angegebenen Punkten.");
    }

    private void fireReadyEvent() {
        fireProgressDone(100);
    }

}