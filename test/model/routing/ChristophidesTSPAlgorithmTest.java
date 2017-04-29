package model.routing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import model.IProgressListener;

import org.junit.Before;
import org.junit.Test;

public class ChristophidesTSPAlgorithmTest {

    private IRouteSolver routing;
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
        routing = new ChristofidesTSPSolver(directedGraph);
    }

    @Test
    public void testDistance() {
        final List<InterNode> edges = new ArrayList<>();
        edges.add(new InterNode(0, 0 | 0x80000000, 0));
        edges.add(new InterNode(17, 17 | 0x80000000, 0));
        edges.add(new InterNode(13, 13 | 0x80000000, 0));
        edges.add(new InterNode(12, 12 | 0x80000000, 0));

        final Route route = routing.calculateRoute(edges);
        int length = 0;

        for (final Path path : route.getPaths()) {
            length += path.getLength();
        }

        // Die kürzeste Rundroute beträgt 2000 Einheiten. Da unser Programm die
        // 1.5-Approximation verwendet darf die Route maximal 1,5 so lange
        // sein.
        assertTrue(length < 2000 * 1.5 ? true : false);
    }

    @Test
    public void testPath() {
        boolean error = false;

        final InterNode[] nodes = new InterNode[3];
        nodes[0] = new InterNode(3, 3 | 0x80000000, 0);
        nodes[1] = new InterNode(19, 19 | 0x80000000, 0.2f);
        nodes[2] = new InterNode(17, 17 | 0x80000000, 1f);

        final List<InterNode> edges = new ArrayList<>();
        edges.add(nodes[0]);
        edges.add(nodes[1]);
        edges.add(nodes[2]);

        final Route route = routing.calculateRoute(edges);

        final int[] num = new int[3];

        for (final Path path : route.getPaths()) {
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
            if (num[i] != 2) {
                error = true;
            }
        }

        assertFalse(error);
    }

    @Test
    public void testNotAccessible() {
        error = false;
        final List<InterNode> edges = new ArrayList<>();
        edges.add(new InterNode(3, 3 | 0x80000000, 0));
        edges.add(new InterNode(19, 19 | 0x80000000, 0.2f));
        edges.add(new InterNode(20, 20 | 0x80000000, 1));

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

        final List<InterNode> edges = new ArrayList<>();
        edges.add(new InterNode(3, 3 | 0x80000000, 0));
        edges.add(new InterNode(19, 19 | 0x80000000, 0.2f));
        edges.add(new InterNode(17, 17 | 0x80000000, 1F));

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

    private static int[][] convert(final long[] edges) {
        final int[][] ret = new int[2][edges.length];
        for (int i = 0; i < edges.length; i++) {
            ret[0][i] = (int) (edges[i] & 0xFFFFFFFF);
            ret[1][i] = (int) (edges[i] >> 32);
        }
        return ret;
    }
}
