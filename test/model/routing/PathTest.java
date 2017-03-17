package model.routing;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class PathTest {
    private Path path;
    private List<Long> edges;
    private InterNode iNode1;
    private InterNode iNode2;

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
        edges = new ArrayList<>();
        iNode1 = new InterNode(getEdge(0, 16), 1F);
        iNode2 = new InterNode(getEdge(2, 6), 1F);

        edges.add(getEdge(0, 16));
        edges.add(getEdge(16, 2));
        edges.add(getEdge(6, 2));

        path = new Path(3, edges, iNode1, iNode2);
    }

    @Test
    public void testLength() {
        assertEquals(3, path.getLength());
    }

    @Test
    public void testStartNodeTest() {
        assertEquals(iNode1, path.getStartNode());
    }

    @Test
    public void testEndNodeTest() {
        assertEquals(iNode2, path.getEndNode());
    }

    @Test
    public void testEdgesTest() {
        assertEquals(edges, path.getEdges());
    }

}
