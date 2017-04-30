package routing;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import model.routing.IUndirectedGraph;
import model.routing.UndirectedGraph;

public class GraphTest {
    private IUndirectedGraph undirectedGraph;

    public static long getEdge(final int node1, final int node2) {
        long ret;
        if (node1 < node2) {
            ret = ((long) node1 << 32) | (node2);
        } else {
            ret = ((long) node2 << 32) | (node1);
        }
        return ret;
    }

    @Before
    public void setUp() {
        final long[] edges = new long[21];
        final int[] weights = new int[21];

        int count = -1;
        edges[++count] = getEdge(0, 16);
        edges[++count] = getEdge(0, 2);
        edges[++count] = getEdge(0, 3);
        edges[++count] = getEdge(1, 2);
        edges[++count] = getEdge(1, 6);
        edges[++count] = getEdge(2, 7);
        edges[++count] = getEdge(3, 4);
        edges[++count] = getEdge(4, 5);
        edges[++count] = getEdge(5, 11);
        edges[++count] = getEdge(6, 11);
        edges[++count] = getEdge(6, 10);
        edges[++count] = getEdge(7, 8);
        edges[++count] = getEdge(9, 13);
        edges[++count] = getEdge(9, 14);
        edges[++count] = getEdge(9, 15);
        edges[++count] = getEdge(10, 18);
        edges[++count] = getEdge(12, 13);
        edges[++count] = getEdge(12, 18);
        edges[++count] = getEdge(13, 17);
        edges[++count] = getEdge(15, 16);
        edges[++count] = getEdge(19, 20);

        count = -1;
        weights[++count] = 100;
        weights[++count] = 200;
        weights[++count] = 100;
        weights[++count] = 100;
        weights[++count] = 300;
        weights[++count] = 300;
        weights[++count] = 100;
        weights[++count] = 100;
        weights[++count] = 200;
        weights[++count] = 100;
        weights[++count] = 200;
        weights[++count] = 100;
        weights[++count] = 300;
        weights[++count] = 200;
        weights[++count] = 200;
        weights[++count] = 100;
        weights[++count] = 100;
        weights[++count] = 300;
        weights[++count] = 200;
        weights[++count] = 100;
        weights[++count] = 200;

        undirectedGraph = new UndirectedGraph(21, convert(edges), weights);
    }

    @Test
    public void testNumberOfNodes() {
        assertEquals(21, undirectedGraph.getNodes());
    }

    @Test
    public void testNumberOfEdges() {
        assertEquals(21, undirectedGraph.getEdges());
    }

    @Test
    public void testEdgeMapping1() {
        assertEquals(51539607763L, undirectedGraph.getEdge(211, 12));
    }

    @Test
    public void testEdgeMapping2() {
        assertEquals(51539607763L, undirectedGraph.getEdge(12, 211));
    }

    @Test
    public void testEdgeMapping3() {
        assertEquals(1L, undirectedGraph.getEdge(1, 0));
    }

    @Test
    public void testNode1() {
        assertEquals(12, undirectedGraph.getFirstNode(51539607763L));
    }

    @Test
    public void testNode2() {
        assertEquals(211, undirectedGraph.getSecondNode(51539607763L));
    }

    @Test
    public void testAdjacentNodes() {
        final List<Integer> ret = new ArrayList<>();
        final List<Integer> nodes = new ArrayList<>();
        nodes.add(9);
        nodes.add(12);
        nodes.add(17);

        final Iterator<Integer> it = undirectedGraph.getAdjacentNodes(13);
        while (it.hasNext()) {
            ret.add(it.next());
        }

        assertEquals(nodes, ret);
    }

    @Test
    public void testWeight() {
        assertEquals(200, undirectedGraph.getWeight(undirectedGraph.getEdge(5, 11)));
    }

    private static int[][] convert(final long[] edges) {
        final int[][] ret = new int[2][edges.length];
        for (int i = 0; i < edges.length; i++) {
            ret[1][i] = (int) (edges[i] & 0xFFFFFFFF);
            ret[0][i] = (int) (edges[i] >> 32);
        }
        return ret;
    }

}
