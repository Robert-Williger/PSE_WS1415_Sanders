package model.routing;

import java.util.Iterator;

public class JPMST {
    IGraph graph;
    private final int[] distance;
    private final int[] parent;
    private final IAddressablePriorityQueue<Integer> queue;
    private final boolean ready;

    public JPMST(final IGraph graph) {
        this.graph = graph;
        final int nodes = graph.getNodes();
        distance = new int[nodes];
        parent = new int[nodes];
        queue = createQueue();
        ready = false;

        for (int i = 0; i < nodes; i++) {
            distance[i] = Integer.MAX_VALUE;
            parent[i] = -1;
        }
    }

    private void initializeJP() {
        // Startnode is node 0;
        distance[0] = 0;
        parent[0] = 0;
        queue.insert(0, 0);
    }

    private void executeJP() {
        while (ready != true & queue.size() != 0) {
            final int u = queue.deleteMin();
            distance[u] = 0;
            scan(u);
        }

    }

    private void scan(final int u) {
        if (ready != true) {

            final Iterator<Integer> it = graph.getAdjacentNodes(u);
            while (it.hasNext()) {
                relax(u, it.next());
            }
        }
    }

    private void relax(final int firstNode, final int secondNode) {
        final int weight = graph.getWeight(graph.getEdge(firstNode, secondNode));
        if (weight < distance[secondNode]) {
            distance[secondNode] = weight;
            parent[secondNode] = firstNode;

            if (queue.contains(secondNode)) {
                queue.changeKey(secondNode, weight);
            } else {
                queue.insert(secondNode, weight);
            }
        }
    }

    public IGraph calculateMST() {

        initializeJP();
        executeJP();

        final long[] edges = new long[parent.length - 1];
        final int[] weights = new int[edges.length];
        for (int i = 1; i < parent.length; i++) {
            edges[i - 1] = graph.getEdge(i, parent[i]);
            weights[i - 1] = Integer.MAX_VALUE;
        }

        return new Graph(graph.getNodes(), edges, weights);
    }

    public IAddressablePriorityQueue<Integer> createQueue() {
        return new AddressableBinaryHeap<Integer>();
    }
}
