package adminTool.labeling;

public class TreeLabeler {
    private static double EPSILON = 1E-5;

    private static int UNIDENTIFIED = 0;
    private static int IDENTIFIED = 1;

    private static int UNCALCULATED = -1;

    public TreeLabeler(final SubdivisionGraph graph) {
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
            values[identified][node] = graph.isRegular(node) ? maxV(node, identified) : maxH(node);
        }
        return values[identified][node];
    }

    private int maxV(final int node, final int identified) {
        final int road = graph.road(node);
        final int edge = graph.beginEdge(node);
        final int next = graph.edgeHead(edge);
        final double ml = labelLengths[road] - graph.edgeWeight(edge);
        final int value = 1 - identified;
        final int lValue = graph.isRegular(next) ? maxVR(next, road, ml, value) : maxVJJ(next, road, ml, value);
        final int eValue = maxValue(next, identified);

        return Math.max(lValue, eValue);
    }

    // construct vertical label, incoming edge is a road section
    private int maxVR(final int node, final int road, double missingLength, final int value) {
        if (missingLength < EPSILON) // label fully constructed
            return value + maxValue(node, IDENTIFIED);

        final int edge = graph.beginEdge(node);
        if (edge == graph.endEdge(node))
            return 0;

        final int n = graph.edgeHead(edge);
        if (graph.isRegular(n))
            return maxVR(n, road, missingLength - graph.edgeWeight(edge), value);
        return maxVJJ(n, road, missingLength - graph.edgeWeight(edge), value);
    }

    // construct vertical label, incoming edge is a junction section
    private int maxVJ(final int node, final int road, double missingLength, final int value) {
        if (graph.isJunction(node))
            return maxVJJ(node, road, missingLength, value);
        else if (graph.road(node) == road)
            return maxVJR(node, road, missingLength, value + 1);
        return 0;
    }

    // construct vertical label, incoming edge is a junction section and its end is a junction node
    private int maxVJJ(final int node, final int road, double missingLength, final int value) {
        if (missingLength < EPSILON) // label fully constructed
            return 0;

        int max = 0;
        final int val = value + emptyValue(node);
        final int endEdge = graph.endEdge(node);
        for (int e = graph.beginEdge(node); e < endEdge; ++e) {
            final int n = graph.edgeHead(e);
            max = Math.max(max, maxVJ(n, road, missingLength - graph.edgeWeight(e), val - maxValue(n, UNIDENTIFIED)));
        }
        return max;
    }

    // construct vertical label, incoming edge is a junction section and its end is a regular node from the road
    private int maxVJR(final int node, final int road, double missingLength, final int value) {
        if (missingLength < EPSILON) // label fully constructed
            return 0;

        final int edge = graph.beginEdge(node);
        return maxVR(graph.edgeHead(edge), road, missingLength - graph.edgeWeight(edge), value);
    }

    private int maxH(final int node) {
        final int endEdge = graph.endEdge(node);
        final int emptyValue = emptyValue(node);
        int max = emptyValue;
        for (int e = graph.beginEdge(node); e < endEdge; ++e) {
            final int n = graph.edgeHead(e);
            final int road = graph.road(n);
            final double ml = labelLengths[road] - graph.edgeWeight(e);
            final int v = maxHJ(n, road, ml, emptyValue - maxValue(n, UNIDENTIFIED), node, e);
            max = Math.max(max, v);
        }

        return max;
    }

    // construct horizontal label, incoming edge is a road section
    // j = junction node / lowest node of horizontal label
    // f = first edge of horizontal label (beginning at j)
    private int maxHR(final int node, final int road, double ml, final int value, final int j, final int f) {
        if (ml < EPSILON) // constructed label is vertical starting on a junction (j)
            return 0;

        int max = 0;
        final int edge = graph.beginEdge(node);
        if (edge != graph.endEdge(node)) {
            final int n = graph.edgeHead(edge);
            final double l = ml - graph.edgeWeight(edge);
            max = graph.isRegular(node) ? maxHR(n, road, l, value, j, f) : maxHJJ(n, road, l, value, j, f);
        }

        final int val = value + maxValue(node, IDENTIFIED);
        final int endEdge = graph.endEdge(j);
        for (int e = graph.beginEdge(j); e < endEdge; ++e) {
            if (e != f) {
                final int n = graph.edgeHead(e);
                max = Math.max(max, maxVJ(n, road, ml - graph.edgeWeight(e), val - maxValue(n, UNIDENTIFIED)));
            }
        }
        return max;
    }

    // construct horizontal label, incoming edge is a junction section
    // j = junction node / lowest node of horizontal label
    // f = first edge of horizontal label (beginning at j)
    private int maxHJ(final int node, final int road, double ml, int value, final int j, final int f) {
        if (graph.isJunction(node))
            return maxHJJ(node, road, ml, value, j, f);
        else if (graph.road(node) == road)
            return maxHJR(node, road, ml, value + 1, j, f);
        return 0;
    }

    // construct horizontal label, incoming edge is a junction section and its end is a junction node
    // j = junction node / lowest node of horizontal label
    // f = first edge of horizontal label (beginning at j)
    private int maxHJJ(final int node, final int road, double ml, int value, final int j, final int f) {
        if (ml < EPSILON) // constructed label is vertical starting on a junction
            return 0;

        int max = 0;

        final int val = emptyValue(node);
        final int endEdge = graph.endEdge(node);
        for (int e = graph.beginEdge(node); e < endEdge; ++e) {
            final int n = graph.edgeHead(e);
            max = Math.max(max, maxHJ(n, road, ml - graph.edgeWeight(e), val - maxValue(n, UNIDENTIFIED), j, f));
        }
        return max;
    }

    // construct horizontal label, incoming edge is a junction section and its end is a regular node from the road
    // j = junction node / lowest node of horizontal label
    // f = first edge of horizontal label (beginning at j)
    private int maxHJR(final int node, final int road, double ml, final int value, final int j, final int f) {
        if (ml < EPSILON) // constructed label is vertical starting on a junction
            return 0;

        final int edge = graph.beginEdge(node);
        int max = maxHR(graph.edgeHead(edge), road, ml - graph.edgeWeight(edge), value, j, f);

        final int val = value + maxValue(node, IDENTIFIED);
        final int endEdge = graph.endEdge(j);
        for (int e = graph.beginEdge(j); e < endEdge; ++e) {
            if (e != f) {
                final int n = graph.edgeHead(e);
                max = Math.max(max, maxVJ(n, road, ml - graph.edgeWeight(e), val - maxValue(n, UNIDENTIFIED)));
            }
        }
        return max;
    }

    private int emptyValue(final int jNode) {
        final int endEdge = graph.endEdge(jNode);
        int edge = graph.beginEdge(jNode);
        int ret = maxValue(graph.edgeHead(edge), UNIDENTIFIED);
        while (++edge < endEdge) {
            ret += maxValue(graph.edgeHead(edge), UNIDENTIFIED);
        }
        return ret;
    }

    private final SubdivisionGraph graph;
    private final int[][] values;
    private int[] labelLengths;
}
