package adminTool.labeling;

public class TreeLabeler2 {
    private static double EPSILON = 1E-5;

    private static int UNIDENTIFIED = 0;
    private static int IDENTIFIED = 1;

    private static int UNCALCULATED = -1;

    public TreeLabeler2(final SubdivisionGraph graph) {
        this.graph = graph;
        this.values = new int[2][graph.numNodes()];
        init(graph.getRoot());
    }

    public int maxValue() {
        return maxValue(graph.getRoot(), UNIDENTIFIED);
    }

    private void init(final int node) {
        final int startEdges = graph.beginEdge(node);
        final int endEdges = graph.endEdge(node);
        if (startEdges == endEdges) {
            values[IDENTIFIED][node] = 0;
            values[UNIDENTIFIED][node] = 0;
        } else {
            values[IDENTIFIED][node] = UNCALCULATED;
            values[UNIDENTIFIED][node] = UNCALCULATED;
            for (int e = startEdges; e < endEdges; ++e) {
                init(graph.edgeHead(e));
            }
        }
    }

    private int maxValue(final int node, final int identified) {
        if (values[identified][node] == UNCALCULATED) {
            values[identified][node] = graph.isRegular(node) ? maxVlabel(node, identified) : maxHlabel(node);
        }

        return values[identified][node];

    }

    private int emptyValue(final int jNode) {
        final int endEdge = graph.endEdge(jNode);
        int edge = graph.beginEdge(jNode);
        int ret = maxValue(graph.edgeHead(edge++), UNIDENTIFIED);
        while (edge < endEdge) {
            ret += maxValue(graph.edgeHead(edge++), UNIDENTIFIED);
        }
        return ret;
    }

    private int maxVlabel(final int node, final int identified) {
        int labelValue = maxVlabelRec(node, graph.road(node), 0, 1 - identified);
        int emptyValue = maxValue(graph.edgeHead(graph.beginEdge(node)), identified);
        return Math.max(labelValue, emptyValue);
    }

    private int maxVlabelRec(final int node, final int road, final double length, final int value) {
        final int beginEdge = graph.beginEdge(node);
        final int endEdge = graph.endEdge(node);

        if (beginEdge != endEdge) { // node is not a leaf
            if (graph.isRegular(node)) {
                final int next = graph.edgeHead(beginEdge);
                final double newLength = length + graph.edgeWeight(beginEdge);
                if (labelLengths[road] - newLength > EPSILON) { // label not long enough yet
                    return maxVlabelRec(next, road, newLength, value);
                }
                if (graph.road(next) == road) { // label long enough and does not end on junction
                    return value + maxValue(next, IDENTIFIED);
                }
            } else { // node is junction node
                final int tval = value + 1 + emptyValue(node);

                int maxValue = 0;
                for (int e = beginEdge; e < endEdge; ++e) {
                    double newLength = length + graph.edgeWeight(e);
                    if (labelLengths[road] - newLength > EPSILON) { // label not long enough yet
                        int next = graph.edgeHead(e);
                        int newValue = maxVlabelRec(next, road, newLength, tval - maxValue(next, UNIDENTIFIED));
                        maxValue = Math.max(maxValue, newValue);
                    }
                }
                return maxValue;
            }
        }
        return 0;
    }

    private int maxHlabel(final int node) {
        final int beginEdge = graph.beginEdge(node);
        final int endEdge = graph.endEdge(node);

        int maxValue = emptyValue(node);
        final int tVal = 1 + maxValue;
        for (int e = beginEdge; e < endEdge; ++e) {
            final int next = graph.edgeHead(beginEdge);
            final int v = maxHlabelRec(node, next, next, graph.edgeWeight(next), tVal - maxValue(next, UNIDENTIFIED));
            maxValue = Math.max(maxValue, v);
        }

        return maxValue;
    }

    // jNode = junction node
    // fnode = first node of horizontal label
    // cNode = current node of horizontal label
    private int maxHlabelRec(final int jNode, final int fNode, final int cNode, final double length, final int value) {
        final int cBeginEdge = graph.beginEdge(cNode);
        final int cEndEdge = graph.endEdge(cNode);
        final int road = graph.road(fNode);

        int maxValue = 0;
        if (cBeginEdge != cEndEdge) { // cNode is not a leaf
            if (graph.isRegular(cNode)) {
                final double newLength = length + graph.edgeWeight(cBeginEdge);
                final int next = graph.edgeHead(cBeginEdge);
                if (labelLengths[road] - newLength > EPSILON) {
                    maxValue = maxHlabelRec(jNode, fNode, next, newLength, value);
                }
            } else { // cNode is a junction node
                final int tval = value + 1 + emptyValue(cNode);
                for (int e = cBeginEdge; e < cEndEdge; ++e) {
                    double newLength = length + graph.edgeWeight(e);
                    if (labelLengths[road] - newLength > EPSILON) {
                        final int next = graph.edgeHead(e);
                        int cValue = maxHlabelRec(jNode, fNode, next, newLength, tval - maxValue(next, UNIDENTIFIED));
                        maxValue = Math.max(maxValue, cValue);
                    }
                }
            }
        }

        final int jEndEdge = graph.endEdge(jNode);
        for (int e = graph.beginEdge(jNode); e < jEndEdge; ++e) { // TODO missing length instead of length
            final int next = graph.edgeHead(e);
            if (next != fNode) {
                final double newLength = length + graph.edgeWeight(e);
                if (labelLengths[road] - newLength > EPSILON) {
                    int v = maxVlabelRec(next, road, newLength, value + 1 - maxValue(next, UNIDENTIFIED));
                    maxValue = Math.max(maxValue, v);
                }
            }
        }

        return maxValue;
    }

    private final SubdivisionGraph graph;
    private final int[][] values;
    private int[] labelLengths;
}
