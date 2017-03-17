package model.routing;

import java.util.Iterator;

import util.AddressableBinaryHeap;
import util.IAddressablePriorityQueue;

public class JPMST {
    private IUndirectedGraph undirectedGraph;
    private final int[] distance;
    private final int[] parent;
    private final IAddressablePriorityQueue<Integer> queue;
    private final boolean ready;

    public JPMST(final IUndirectedGraph undirectedGraph) {
        this.undirectedGraph = undirectedGraph;
        final int nodes = undirectedGraph.getNodes();
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

            final Iterator<Integer> it = undirectedGraph.getAdjacentNodes(u);
            while (it.hasNext()) {
                relax(u, it.next());
            }
        }
    }

    private void relax(final int firstNode, final int secondNode) {
        final int weight = undirectedGraph.getWeight(undirectedGraph.getEdge(firstNode, secondNode));
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

    public IUndirectedGraph calculateMST() {

        initializeJP();
        executeJP();

        final int[] firstNodes = new int[parent.length - 1];
        final int[] secondNodes = new int[firstNodes.length];
        final int[] weights = new int[firstNodes.length];
        for (int i = 1; i < parent.length; i++) {
            firstNodes[i - 1] = i;
            secondNodes[i - 1] = parent[i];
            weights[i - 1] = undirectedGraph.getWeight(undirectedGraph.getEdge(i, parent[i]));
        }

        return new UndirectedGraph(undirectedGraph.getNodes(), firstNodes, secondNodes, weights);
    }

    public IAddressablePriorityQueue<Integer> createQueue() {
        return new AddressableBinaryHeap<>();
    }
}
