package routing;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import model.routing.InterNode;
import model.routing.Path;

public class PathTest {
    private Path path;
    private List<Integer> edges;
    private InterNode node1;
    private InterNode node2;

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
        node1 = new InterNode(0, 0 | 0x80000000, 1);
        node2 = new InterNode(1, 1 | 0x80000000, 1);

        edges.add(0);
        edges.add(1);
        edges.add(2);

        path = new Path(3, edges, node1, node2);
    }

    @Test
    public void testLength() {
        assertEquals(3, path.getLength());
    }

    @Test
    public void testStartNodeTest() {
        assertEquals(node1, path.getStartNode());
    }

    @Test
    public void testEndNodeTest() {
        assertEquals(node2, path.getEndNode());
    }

    @Test
    public void testEdgesTest() {
        assertEquals(edges, path.getEdges());
    }

}
