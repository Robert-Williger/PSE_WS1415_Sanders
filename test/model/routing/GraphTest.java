package model.routing;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class GraphTest {
    private IGraph graph;

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
        final List<Long> edges = new ArrayList<Long>();
        final List<Integer> weights = new ArrayList<Integer>();
        edges.add(getEdge(0, 16));
        edges.add(getEdge(0, 2));
        edges.add(getEdge(0, 3));
        edges.add(getEdge(1, 2));
        edges.add(getEdge(1, 6));
        edges.add(getEdge(2, 7));
        edges.add(getEdge(3, 4));
        edges.add(getEdge(4, 5));
        edges.add(getEdge(5, 11));
        edges.add(getEdge(6, 11));
        edges.add(getEdge(6, 10));
        edges.add(getEdge(7, 8));
        edges.add(getEdge(9, 13));
        edges.add(getEdge(9, 14));
        edges.add(getEdge(9, 15));
        edges.add(getEdge(10, 18));
        edges.add(getEdge(12, 13));
        edges.add(getEdge(12, 18));
        edges.add(getEdge(13, 17));
        edges.add(getEdge(15, 16));
        edges.add(getEdge(19, 20));

        weights.add(100);
        weights.add(200);
        weights.add(100);
        weights.add(100);
        weights.add(300);
        weights.add(300);
        weights.add(100);
        weights.add(100);
        weights.add(200);
        weights.add(100);
        weights.add(200);
        weights.add(100);
        weights.add(300);
        weights.add(200);
        weights.add(200);
        weights.add(100);
        weights.add(100);
        weights.add(300);
        weights.add(200);
        weights.add(100);
        weights.add(200);

        graph = new Graph(21, edges, weights);
    }

    @Test
    public void testNumberOfNodes() {
        assertEquals(21, graph.getNodes());
    }

    @Test
    public void testNumberOfEdges() {
        assertEquals(21, graph.getEdges());
    }

    @Test
    public void testEdgeMapping1() {
        assertEquals(51539607763L, graph.getEdge(211, 12));
    }

    @Test
    public void testEdgeMapping2() {
        assertEquals(51539607763L, graph.getEdge(12, 211));
    }

    @Test
    public void testEdgeMapping3() {
        assertEquals(1L, graph.getEdge(1, 0));
    }

    @Test
    public void testNode1() {
        assertEquals(12, graph.getFirstNode(51539607763L));
    }

    @Test
    public void testNode2() {
        assertEquals(211, graph.getSecondNode(51539607763L));
    }

    @Test
    public void testAdjacentNodes() {
        final List<Integer> ret = new ArrayList<Integer>();
        final List<Integer> nodes = new ArrayList<Integer>();
        nodes.add(9);
        nodes.add(12);
        nodes.add(17);

        final Iterator<Integer> it = graph.getAdjacentNode(13);
        while (it.hasNext()) {
            ret.add(it.next());
        }

        assertEquals(nodes, ret);
    }

    @Test
    public void testWeight() {
        assertEquals(200, graph.getWeight(graph.getEdge(5, 11)));
    }

}
