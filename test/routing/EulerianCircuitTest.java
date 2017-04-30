package routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import model.routing.EulerianCircuitAlgorithm;
import model.routing.IUndirectedGraph;
import model.routing.UndirectedGraph;

public class EulerianCircuitTest {

    @Rule
    public Timeout globalTimeout = new Timeout(500, TimeUnit.MILLISECONDS); // 1/2 second max per
    // method tested

    @Test
    public void testCircle() {
        final int nodes = 12;

        final int[] weights = new int[nodes];
        final int[][] edges = new int[2][nodes];

        for (int i = 0; i < nodes; i++) {
            edges[0][i] = i;
            edges[1][i] = (i + 1) % nodes;
        }
        final UndirectedGraph undirectedGraph = new UndirectedGraph(nodes, edges, weights);

        checkTour(undirectedGraph, new EulerianCircuitAlgorithm().getEulerianCurcuit(undirectedGraph));
    }

    @Test
    public void testCircle2() {
        final int nodes = 13;

        final int[] weights = new int[nodes];
        final int[][] edges = new int[2][nodes];

        int current = 0;
        for (int i = 0; i < nodes; i++) {
            edges[0][i] = current % nodes;
            edges[1][i] = (current += 2) % nodes;
        }
        final UndirectedGraph undirectedGraph = new UndirectedGraph(nodes, edges, weights);

        checkTour(undirectedGraph, new EulerianCircuitAlgorithm().getEulerianCurcuit(undirectedGraph));
    }

    @Test
    public void testHourglass() {
        final int nodes = 5;

        final int[] weights = new int[nodes + 1];
        final int[][] edges = new int[2][nodes + 1];

        edges[0][nodes - 1] = 4;
        edges[1][nodes - 1] = 2;

        edges[0][nodes] = 2;
        edges[1][nodes] = 0;

        for (int i = nodes - 2; i >= 0; i--) {
            edges[0][i] = i;
            edges[1][i] = i + 1;
        }

        final UndirectedGraph undirectedGraph = new UndirectedGraph(nodes, edges, weights);

        checkTour(undirectedGraph, new EulerianCircuitAlgorithm().getEulerianCurcuit(undirectedGraph));
    }

    @Test
    public void testCircles() {
        final int nodes = 7;

        final int[] weights = new int[nodes + 1];
        final int[][] edges = new int[2][nodes + 1];

        edges[0][0] = 3;
        edges[1][0] = 4;

        edges[0][1] = 4;
        edges[1][1] = 5;

        edges[0][2] = 5;
        edges[1][2] = 6;

        edges[0][3] = 6;
        edges[1][3] = 3;

        ///

        edges[0][4] = 0;
        edges[1][4] = 1;

        edges[0][5] = 1;
        edges[1][5] = 2;

        edges[0][6] = 2;
        edges[1][6] = 3;

        edges[0][7] = 3;
        edges[1][7] = 0;

        final UndirectedGraph undirectedGraph = new UndirectedGraph(nodes, edges, weights);

        checkTour(undirectedGraph, new EulerianCircuitAlgorithm().getEulerianCurcuit(undirectedGraph));
    }

    @Test
    public void testComplexGraph() {
        final int nodes = 9;

        final int[] weights = new int[15];
        final long[] edges = new long[15];

        edges[0] = getEdge(0, 1);
        edges[1] = getEdge(0, 2);
        edges[2] = getEdge(0, 6);
        edges[3] = getEdge(0, 7);

        edges[4] = getEdge(1, 2);

        edges[5] = getEdge(2, 6);
        edges[6] = getEdge(2, 3);

        edges[7] = getEdge(3, 6);
        edges[8] = getEdge(3, 8);
        edges[9] = getEdge(3, 4);

        edges[10] = getEdge(4, 8);

        edges[11] = getEdge(5, 8);
        edges[12] = getEdge(5, 6);

        edges[13] = getEdge(6, 8);
        edges[14] = getEdge(6, 7);

        final UndirectedGraph undirectedGraph = new UndirectedGraph(nodes, convert(edges), weights);

        checkTour(undirectedGraph, new EulerianCircuitAlgorithm().getEulerianCurcuit(undirectedGraph));
    }

    @Test
    public void test() {
        final long[] edges = new long[] { 1L, 34359738379L, 21474836498L, 8589934605L, 25769803791L, 42949672974L,
                12884901892L, 25769803783L, 17179869191L, 8L, 34359738377L, 38654705675L, 42949672972L, 34359738380L,
                4294967309L, 8589934605L, 13L, 14L, 8589934607L, 8589934608L, 68719476753L, 12884901906L, 21474836498L,
                73014444050L };
        final int[] weights = new int[edges.length];

        final UndirectedGraph undirectedGraph = new UndirectedGraph(19, convert(edges), weights);

        checkTour(undirectedGraph, new EulerianCircuitAlgorithm().getEulerianCurcuit(undirectedGraph));

    }

    private void checkTour(final IUndirectedGraph undirectedGraph, final List<Integer> tour) {
        final Map<Long, Integer> availableEdges = new HashMap<>();
        int edges = 0;

        for (int node = 0; node < undirectedGraph.getNodes(); node++) {
            for (final Iterator<Integer> iterator = undirectedGraph.getAdjacentNodes(node); iterator.hasNext();) {
                final int other = iterator.next();
                if (node < other) {
                    ++edges;

                    final long edge = undirectedGraph.getEdge(node, other);
                    final Integer occurances = availableEdges.get(edge);
                    if (occurances == null) {
                        availableEdges.put(edge, 1);
                    } else {
                        availableEdges.put(edge, occurances + 1);
                    }
                }
            }
        }

        assertEquals(edges, tour.size() - 1);

        final Iterator<Integer> iterator = tour.iterator();
        int lastNode = iterator.next();
        while (iterator.hasNext()) {
            final long edge = undirectedGraph.getEdge(lastNode, lastNode = iterator.next());
            final int occurances = availableEdges.get(edge);
            if (occurances == 1) {
                availableEdges.remove(edge);
            } else {
                availableEdges.put(edge, occurances - 1);
            }
        }

        assertTrue(availableEdges.isEmpty());
    }

    private static int[][] convert(final long[] edges) {
        final int[][] ret = new int[2][edges.length];
        for (int i = 0; i < edges.length; i++) {
            ret[1][i] = (int) (edges[i] & 0xFFFFFFFF);
            ret[0][i] = (int) (edges[i] >> 32);
        }
        return ret;
    }

    public static long getEdge(final int node1, final int node2) {
        long ret;
        if (node1 < node2) {
            ret = ((long) node1 << 32) | (node2);
        } else {
            ret = ((long) node2 << 32) | (node1);
        }
        return ret;
    }
}
