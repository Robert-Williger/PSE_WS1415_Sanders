package model.routing;

import java.util.HashMap;
import java.util.Iterator;

public class UndirectedGraph implements IUndirectedGraph {
    private final int[] nodes;
    private final int[] edges;
    private final HashMap<Long, Integer> weights;

    private class NodeIterator implements Iterator<Integer> {
        private int currentElement;
        private final int limit;

        public NodeIterator(final int node) {
            currentElement = nodes[node];
            limit = nodes[node + 1];
        }

        @Override
        public boolean hasNext() {
            return currentElement < limit;

        }

        @Override
        public Integer next() {
            return edges[currentElement++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    public UndirectedGraph(final int nodes, final int[] firstNodes, final int[] secondNodes, final int[] weights) {
        this.nodes = new int[nodes + 1];
        this.edges = new int[firstNodes.length * 2];
        this.weights = new HashMap<Long, Integer>();

        for (int i = 0; i < firstNodes.length; i++) {
            final long edge = getEdge(firstNodes[i], secondNodes[i]);
            final int weight = weights[i];

            this.nodes[firstNodes[i]]++;
            this.nodes[secondNodes[i]]++;

            this.weights.put(edge, weight);
        }

        int temp = this.nodes[0];

        int temp2;
        this.nodes[0] = 0;

        for (int i = 1; i < this.nodes.length; i++) {
            temp2 = this.nodes[i];
            this.nodes[i] = this.nodes[i - 1] + temp;
            temp = temp2;
        }

        final int[] clone = this.nodes.clone();

        for (int i = 0; i < firstNodes.length; i++) {
            final int node1 = firstNodes[i];
            final int node2 = secondNodes[i];

            this.edges[clone[node1]] = node2;
            clone[node1]++;

            this.edges[clone[node2]] = node1;
            clone[node2]++;
        }
    }

    @Override
    public int getNodes() {
        return nodes.length - 1;
    }

    @Override
    public int getEdges() {
        return edges.length / 2;
    }

    @Override
    public Iterator<Integer> getAdjacentNodes(final int node) {
        return new NodeIterator(node);
    }

    @Override
    public int getFirstNode(final long edge) {
        return (int) (edge >> 32);
    }

    @Override
    public int getSecondNode(final long edge) {
        return (int) (edge & 0xFFFFFFFF);
    }

    @Override
    public long getEdge(final int node1, final int node2) {
        long ret;
        if (node1 < node2) {
            ret = ((long) node1 << 32) | (node2);
        } else {
            ret = ((long) node2 << 32) | (node1);
        }
        return ret;
    }

    @Override
    public int getWeight(final long edge) {
        return weights.get(edge) == null ? Integer.MAX_VALUE : weights.get(edge);
    }

}
