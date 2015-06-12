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
    private IGraph mst;

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

        edges.add(getEdge(0, 1));
        edges.add(getEdge(0, 2));
        edges.add(getEdge(0, 5));
        edges.add(getEdge(1, 2));
        edges.add(getEdge(1, 3));
        edges.add(getEdge(1, 5));
        edges.add(getEdge(2, 4));
        edges.add(getEdge(3, 4));
        edges.add(getEdge(3, 6));
        edges.add(getEdge(4, 5));
        edges.add(getEdge(6, 7));
        edges.add(getEdge(6, 8));
        edges.add(getEdge(6, 9));
        edges.add(getEdge(7, 8));
        edges.add(getEdge(8, 9));

        weights.add(1);
        weights.add(2);
        weights.add(3);
        weights.add(9);
        weights.add(3);
        weights.add(1);
        weights.add(2);
        weights.add(1);
        weights.add(3);
        weights.add(4);
        weights.add(3);
        weights.add(4);
        weights.add(5);
        weights.add(1);
        weights.add(2);

        final IGraph graph = new Graph(10, edges, weights);
        jp = new JPMST(graph);
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

        final Integer[][] adj = {{1, 2}, {0, 5}, {0, 4}, {4, 6}, {2, 3}, {1}, {3, 7}, {6, 8}, {7, 9}, {8}};

        for (int i = 0; i < mst.getNodes(); i++) {
            final Iterator<Integer> it = mst.getAdjacentNodes(i);
            final List<Integer> temp = new ArrayList<Integer>();

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

}
