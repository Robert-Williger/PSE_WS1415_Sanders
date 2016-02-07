import java.util.Iterator;

import model.routing.DirectedGraph;
import model.routing.IDirectedGraph;

public class GraphTest {

    public static void main(String[] args) {
        final int nodes = 3;
        final int[] firstNodes = new int[]{0, 1, 2};
        final int[] secondNodes = new int[]{1, 2, 0};
        final int[] weights = new int[]{5, 5, 5};
        final int[] oneways = new int[]{1};
        final IDirectedGraph graph = new DirectedGraph(nodes, firstNodes, secondNodes, weights, oneways);

        for (int i = 0; i < nodes; i++) {
            System.out.println(getAdjacentNodes(graph, i));
        }
    }

    private static String getAdjacentNodes(final IDirectedGraph graph, final int node) {
        final Iterator<Integer> iterator = graph.getOutgoingEdges(node);
        String ret = node + ": [";
        while (iterator.hasNext()) {
            ret += graph.getEndNode(iterator.next());
            if (iterator.hasNext()) {
                ret += ", ";
            }
        }
        ret += "]";

        return ret;
    }
}
