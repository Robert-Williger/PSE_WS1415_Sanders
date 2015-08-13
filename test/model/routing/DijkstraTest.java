package model.routing;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

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

        final IGraph graph = new Graph(21, edges, weights);
        routing = new Dijkstra(graph);
    }

    @Test
    public void testDistanceSameEdge() {
        final Path path = routing.calculateShortestPath(new InterNode(getEdge(16, 15), 0.2F),
                new InterNode(getEdge(16, 15), 0.8F));
        assertEquals(60, path.getLength());
    }

    @Test
    public void testPathSameEdge() {
        final Path path = routing.calculateShortestPath(new InterNode(getEdge(16, 15), 0.2F),
                new InterNode(getEdge(16, 15), 0.8F));
        assertEquals(0, path.getEdges().toArray().length);
    }

    @Test
    public void testDistance() {
        final Path path = routing.calculateShortestPath(new InterNode(getEdge(16, 15), 0F), new InterNode(
                getEdge(6, 10), 1F));
        assertEquals(1000, path.getLength());
    }

    @Test
    public void testPath() {
        final Path path = routing.calculateShortestPath(new InterNode(getEdge(16, 15), 0F), new InterNode(
                getEdge(6, 10), 1F));
        final Long[] route = {getEdge(0, 16), getEdge(0, 2), getEdge(1, 2), getEdge(1, 6), getEdge(6, 10)};
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

        routing.calculateShortestPath(new InterNode(getEdge(16, 15), 0F), new InterNode(getEdge(19, 20), 1F));
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

        routing.calculateShortestPath(new InterNode(getEdge(16, 15), 0F), new InterNode(getEdge(19, 20), 1F));
        assertFalse(error);
    }

}
