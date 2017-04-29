package model.routing;

import java.util.PrimitiveIterator.OfInt;

public class DirectedGraph implements IDirectedGraph {
    private final int[] nodeBorders;
    private final int[] outgoingEdges;
    private final int[] weights;
    private final int[][] nodes;

    public DirectedGraph() {
        this(0, 0, new int[2][0], new int[0]);
    }

    public DirectedGraph(final int nodeCount, final int onewayCount, final int[] firstNodes, final int[] secondNodes,
            final int[] weights) {
        this(nodeCount, onewayCount, new int[][] { firstNodes, secondNodes }, weights);
    }

    public DirectedGraph(final int nodeCount, final int onewayCount, final int[][] nodes, final int[] weights) {
        this.nodeBorders = new int[nodeCount + 1];
        this.weights = weights;
        this.nodes = nodes;

        final int edges = weights.length;

        int index = 0;
        for (; index < onewayCount; ++index) {
            nodeBorders[nodes[0][index]]++;
        }
        for (; index < edges; ++index) {
            nodeBorders[nodes[0][index]]++;
            nodeBorders[nodes[1][index]]++;
        }

        int lastBorder = this.nodeBorders[0];
        int currentBorder;
        this.nodeBorders[0] = 0;

        for (int i = 1; i < nodeBorders.length; i++) {
            currentBorder = nodeBorders[i];
            nodeBorders[i] = nodeBorders[i - 1] + lastBorder;
            lastBorder = currentBorder;
        }

        outgoingEdges = new int[nodeBorders[nodeBorders.length - 1]];

        final int[] fillLevel = nodeBorders.clone();

        index = 0;
        for (; index < onewayCount; ++index) {
            outgoingEdges[fillLevel[nodes[0][index]]++] = index;
        }
        for (; index < edges; ++index) {
            outgoingEdges[fillLevel[nodes[0][index]]++] = index;
            outgoingEdges[fillLevel[nodes[1][index]]++] = getReversedEdge(index);
        }
    }

    private int getReversedEdge(final int edge) {
        return edge | 0x80000000;
    }

    private int getEndIndex(final int edge) {
        return 1 - (edge >>> 31);
    }

    private int getStartIndex(final int edge) {
        return edge >>> 31;
    }

    private int getEdgeIndex(final int edge) {
        return edge & 0x7FFFFFFF;
    }

    @Override
    public int getNodes() {
        return nodeBorders.length - 1;
    }

    @Override
    public int getEdges() {
        return weights.length;
    }

    @Override
    public OfInt getOutgoingEdges(final int node) {
        return new EdgeIterator(node);
    }

    @Override
    public int getEndNode(final int edge) {
        return nodes[getEndIndex(edge)][getEdgeIndex(edge)];
    }

    @Override
    public int getStartNode(final int edge) {
        return nodes[getStartIndex(edge)][getEdgeIndex(edge)];
    }

    @Override
    public int getWeight(final int edge) {
        return weights[getEdgeIndex(edge)];
    }

    private class EdgeIterator implements OfInt {
        private int currentElement;
        private final int limit;

        public EdgeIterator(final int node) {
            currentElement = nodeBorders[node];
            limit = nodeBorders[node + 1];
        }

        @Override
        public boolean hasNext() {
            return currentElement < limit;

        }

        @Override
        public int nextInt() {
            return outgoingEdges[currentElement++];
        }

    }
}
