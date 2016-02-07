package model.routing;

import java.util.Iterator;

public class DirectedGraph implements IDirectedGraph {
    private final int[] nodeBorders;
    private final int[] outgoingEdges;
    private final int[] weights;
    private final int[][] nodes;

    public DirectedGraph() {
        this(0, new int[0], new int[0], new int[0], new int[0]);
    }

    public DirectedGraph(final int nodes, final int[] firstNodes, final int[] secondNodes, final int[] weights,
            final int[] oneways) {

        this.nodeBorders = new int[nodes + 1];

        this.weights = weights;
        this.nodes = new int[][]{firstNodes, secondNodes};

        int onewayIndex = 0;
        int oneway = getOneway(onewayIndex, oneways);

        for (int i = 0; i < firstNodes.length; i++) {
            this.nodeBorders[firstNodes[i]]++;
            if (i != oneway) {
                this.nodeBorders[secondNodes[i]]++;
            } else {
                ++onewayIndex;
                oneway = getOneway(onewayIndex, oneways);
            }
        }

        int lastBorder = this.nodeBorders[0];
        int currentBorder;
        this.nodeBorders[0] = 0;

        for (int i = 1; i < this.nodeBorders.length; i++) {
            currentBorder = this.nodeBorders[i];
            this.nodeBorders[i] = this.nodeBorders[i - 1] + lastBorder;
            lastBorder = currentBorder;
        }

        this.outgoingEdges = new int[nodeBorders[nodeBorders.length - 1]];

        final int[] fillLevel = this.nodeBorders.clone();

        onewayIndex = 0;
        oneway = getOneway(onewayIndex, oneways);

        for (int i = 0; i < firstNodes.length; i++) {
            this.outgoingEdges[fillLevel[firstNodes[i]]++] = i;
            if (i != oneway) {
                this.outgoingEdges[fillLevel[secondNodes[i]]++] = getReversedEdge(i);
            } else {
                ++onewayIndex;
                oneway = getOneway(onewayIndex, oneways);
            }
        }
    }

    private int getOneway(final int index, final int[] oneways) {
        return index < oneways.length ? oneways[index] : -1;
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
    public Iterator<Integer> getOutgoingEdges(final int node) {
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

    private class EdgeIterator implements Iterator<Integer> {
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
        public Integer next() {
            return outgoingEdges[currentElement++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }
}
