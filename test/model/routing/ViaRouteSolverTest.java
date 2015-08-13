package model.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import model.IProgressListener;

import org.junit.Before;
import org.junit.Test;

public class ViaRouteSolverTest {

    private ViaRouteSolver routing;
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
        routing = new ViaRouteSolver(graph);
    }

    @Test
    public void testDistance() {
        final List<InterNode> edges = new ArrayList<InterNode>();
        edges.add(new InterNode(getEdge(1, 2), 0F));
        edges.add(new InterNode(getEdge(16, 15), 0.8F));
        edges.add(new InterNode(getEdge(12, 18), 1F));

        final List<Path> path = routing.calculateRoute(edges);
        int length = 0;

        final Iterator<Path> it = path.iterator();
        while (it.hasNext()) {
            length += it.next().getLength();
        }

        assertEquals(1400, length);
    }

    @Test
    public void testPath() {
        boolean error = false;

        final InterNode[] nodes = new InterNode[3];
        nodes[0] = new InterNode(getEdge(1, 2), 0F);
        nodes[1] = new InterNode(getEdge(16, 15), 0.8F);
        nodes[2] = new InterNode(getEdge(12, 18), 1F);

        final List<InterNode> edges = new ArrayList<InterNode>();
        edges.add(nodes[0]);
        edges.add(nodes[1]);
        edges.add(nodes[2]);

        final List<Path> paths = routing.calculateRoute(edges);

        final int[] num = new int[3];

        final Iterator<Path> it = paths.iterator();
        while (it.hasNext()) {
            final Path path = it.next();

            for (int i = 0; i < 3; i++) {
                if (path.getStartNode().equals(nodes[i])) {
                    num[i]++;
                }
                if (path.getEndNode().equals(nodes[i])) {
                    num[i]++;
                }
            }
        }

        for (int i = 0; i < 3; i++) {
            if (i == 0 || i == 2) {
                if (num[i] != 1) {
                    error = true;
                }
            } else {
                if (num[i] != 2) {
                    error = true;
                }
            }

        }

        assertFalse(error);
    }

    @Test
    public void testNotAccessible() {
        error = false;
        final List<InterNode> edges = new ArrayList<InterNode>();
        edges.add(new InterNode(getEdge(1, 2), 0F));
        edges.add(new InterNode(getEdge(16, 15), 0.8F));
        edges.add(new InterNode(getEdge(19, 20), 1F));

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

        routing.calculateRoute(edges);
        assertTrue(error);
    }

    @Test
    public void testProgress() {
        error = true;

        final List<InterNode> edges = new ArrayList<InterNode>();
        edges.add(new InterNode(getEdge(1, 2), 0F));
        edges.add(new InterNode(getEdge(16, 15), 0.8F));
        edges.add(new InterNode(getEdge(12, 18), 1F));

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

        routing.calculateRoute(edges);
        assertFalse(error);
    }

}
