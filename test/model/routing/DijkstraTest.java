package model.routing;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import model.IProgressListener;

import org.junit.Before;
import org.junit.Test;

public class DijkstraTest {

    private Dijkstra routing;
    private boolean error;

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

        final IDirectedGraph directedGraph = new DirectedGraph(21, 0, convert(edges), weights);
        routing = new Dijkstra(directedGraph);
    }

    @Test
    public void testDistanceSameEdge() {
        final Path path = routing.calculateShortestPath(new InterNode(19, 19 | 0x80000000, 0.8f),
                new InterNode(19, 19 | 0x80000000, 0.2f));
        assertEquals(60, path.getLength());
    }

    @Test
    public void testPathSameEdge() {
        final Path path = routing.calculateShortestPath(new InterNode(19, 19 | 0x80000000, 0.8f),
                new InterNode(19, 19 | 0x80000000, 0.2f));
        assertEquals(0, path.getEdges().size());
    }

    @Test
    public void testDistance() {
        final Path path = routing.calculateShortestPath(new InterNode(19, 19 | 0x80000000, 1),
                new InterNode(10, 10 | 0x80000000, 1f));
        assertEquals(1000, path.getLength());
    }

    @Test
    public void testPath() {
        final Path path = routing.calculateShortestPath(new InterNode(19, 19 | 0x80000000, 1),
                new InterNode(10, 10 | 0x80000000, 1f));
        final Long[] route = { getEdge(0, 16), getEdge(0, 2), getEdge(1, 2), getEdge(1, 6), getEdge(6, 10) };
        assertArrayEquals(route, path.getEdges().toArray());
    }

    @Test
    public void testNotAccessible() {
        error = false;
        routing.addProgressListener(new IProgressListener() {

            @Override
            public void progressDone(final int progress) {
            }

            @Override
            public void errorOccured(final String message) {
                error = true;
            }

            @Override
            public void stepCommenced(String step) {
            }
        });

        routing.calculateShortestPath(new InterNode(19, 19 | 0x80000000, 1), new InterNode(20, 20 | 0x80000000, 1));
        assertTrue(error);
    }

    @Test
    public void testProgress() {
        error = true;
        routing.addProgressListener(new IProgressListener() {

            @Override
            public void progressDone(final int progress) {
                error = false;
            }

            @Override
            public void errorOccured(final String message) {
            }

            @Override
            public void stepCommenced(String step) {

            }
        });

        routing.calculateShortestPath(new InterNode(19, 19 | 0x80000000, 1), new InterNode(20, 20 | 0x80000000, 1));
        assertFalse(error);
    }

    private static int[][] convert(final long[] edges) {
        final int[][] ret = new int[2][edges.length];
        for (int i = 0; i < edges.length; i++) {
            ret[0][i] = (int) (edges[i] & 0xFFFFFFFF);
            ret[1][i] = (int) (edges[i] >> 32);
        }
        return ret;
    }
}
