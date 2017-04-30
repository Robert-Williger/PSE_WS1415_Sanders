package model.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class JPMSTTest {

    private JPMST jp;
    private IUndirectedGraph mst;

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
        final long[] edges = new long[15];
        final int[] weights = new int[edges.length];

        int count = -1;

        edges[++count] = getEdge(0, 1);
        edges[++count] = getEdge(0, 2);
        edges[++count] = getEdge(0, 5);
        edges[++count] = getEdge(1, 2);
        edges[++count] = getEdge(1, 3);
        edges[++count] = getEdge(1, 5);
        edges[++count] = getEdge(2, 4);
        edges[++count] = getEdge(3, 4);
        edges[++count] = getEdge(3, 6);
        edges[++count] = getEdge(4, 5);
        edges[++count] = getEdge(6, 7);
        edges[++count] = getEdge(6, 8);
        edges[++count] = getEdge(6, 9);
        edges[++count] = getEdge(7, 8);
        edges[++count] = getEdge(8, 9);

        count = -1;
        weights[++count] = 1;
        weights[++count] = 2;
        weights[++count] = 3;
        weights[++count] = 9;
        weights[++count] = 3;
        weights[++count] = 1;
        weights[++count] = 2;
        weights[++count] = 1;
        weights[++count] = 3;
        weights[++count] = 4;
        weights[++count] = 3;
        weights[++count] = 4;
        weights[++count] = 5;
        weights[++count] = 1;
        weights[++count] = 2;

        final IUndirectedGraph undirectedGraph = new UndirectedGraph(10, convert(edges), weights);
        jp = new JPMST(undirectedGraph);
        mst = jp.calculateMST();
    }

    @Test
    public void testNodes() {
        assertEquals(10, mst.getNodes());
    }

    @Test
    public void testEdges() {
        assertEquals(9, mst.getEdges());
    }

    @Test
    public void testStructure() {
        boolean error = false;

        final Integer[][] adj = { { 1, 2 }, { 0, 5 }, { 0, 4 }, { 4, 6 }, { 2, 3 }, { 1 }, { 3, 7 }, { 6, 8 }, { 7, 9 },
                { 8 } };

        for (int i = 0; i < mst.getNodes(); i++) {
            final Iterator<Integer> it = mst.getAdjacentNodes(i);
            final List<Integer> temp = new ArrayList<>();

            while (it.hasNext()) {
                temp.add(it.next());
            }

            if (temp.size() != adj[i].length) {
                error = true;
            } else {
                Collections.sort(temp);
                if (!temp.equals(Arrays.asList(adj[i]))) {
                    error = true;
                }
            }

        }
        assertFalse(error);
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
