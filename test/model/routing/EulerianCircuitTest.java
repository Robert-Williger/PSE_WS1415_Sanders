package model.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class EulerianCircuitTest {

    @Rule
    public Timeout globalTimeout = new Timeout(500); // 1/2 second max per
                                                     // method tested

    @Test
    public void testCircle() {
        final int nodes = 12;

        final List<Integer> weights = new ArrayList<Integer>();
        final List<Long> edges = new ArrayList<Long>();

        for (int i = 0; i < nodes; i++) {
            weights.add(0);
            edges.add(getEdge(i, (i + 1) % nodes));
        }
        final Graph graph = new Graph(nodes, edges, weights);

        checkTour(graph, new EulerianCircuitAlgorithm().getEulerianCurcuit(graph));
    }

    @Test
    public void testCircle2() {
        final int nodes = 13;

        final List<Integer> weights = new ArrayList<Integer>();
        final List<Long> edges = new ArrayList<Long>();

        int current = 0;
        for (int i = 0; i < nodes; i++) {
            weights.add(0);
            edges.add(getEdge(current % nodes, (current += 2) % nodes));
        }
        final Graph graph = new Graph(nodes, edges, weights);

        checkTour(graph, new EulerianCircuitAlgorithm().getEulerianCurcuit(graph));
    }

    @Test
    public void testHourglass() {
        final int nodes = 5;

        final List<Integer> weights = new ArrayList<Integer>();
        final List<Long> edges = new ArrayList<Long>();

        edges.add(getEdge(4, 2));
        edges.add(getEdge(2, 0));

        for (int i = nodes - 2; i >= 0; i--) {
            weights.add(0);
            edges.add(getEdge(i, i + 1));
        }
        weights.add(0);
        weights.add(0);

        final Graph graph = new Graph(nodes, edges, weights);

        checkTour(graph, new EulerianCircuitAlgorithm().getEulerianCurcuit(graph));
    }

    @Test
    public void testCircles() {
        final int nodes = 7;

        final List<Integer> weights = new ArrayList<Integer>();
        final List<Long> edges = new ArrayList<Long>();

        edges.add(getEdge(3, 4));
        edges.add(getEdge(4, 5));
        edges.add(getEdge(5, 6));
        edges.add(getEdge(6, 3));

        edges.add(getEdge(0, 1));
        edges.add(getEdge(1, 2));
        edges.add(getEdge(2, 3));
        edges.add(getEdge(3, 0));

        for (int i = 0; i < edges.size(); i++) {
            weights.add(0);
        }

        final Graph graph = new Graph(nodes, edges, weights);

        checkTour(graph, new EulerianCircuitAlgorithm().getEulerianCurcuit(graph));
    }

    @Test
    public void testComplexGraph() {
        final int nodes = 9;

        final List<Integer> weights = new ArrayList<Integer>();
        final List<Long> edges = new ArrayList<Long>();

        edges.add(getEdge(0, 1));
        edges.add(getEdge(0, 2));
        edges.add(getEdge(0, 6));
        edges.add(getEdge(0, 7));

        edges.add(getEdge(1, 2));

        edges.add(getEdge(2, 6));
        edges.add(getEdge(2, 3));

        edges.add(getEdge(3, 6));
        edges.add(getEdge(3, 8));
        edges.add(getEdge(3, 4));

        edges.add(getEdge(4, 8));

        edges.add(getEdge(5, 8));
        edges.add(getEdge(5, 6));

        edges.add(getEdge(6, 8));
        edges.add(getEdge(6, 7));

        for (int i = 0; i < edges.size(); i++) {
            weights.add(0);
        }

        final Graph graph = new Graph(nodes, edges, weights);

        checkTour(graph, new EulerianCircuitAlgorithm().getEulerianCurcuit(graph));
    }

    @Test
    public void test() {

        final List<Integer> weights = new ArrayList<Integer>();
        final List<Long> edges = Arrays.asList(1L, 34359738379L, 21474836498L, 8589934605L, 25769803791L, 42949672974L,
                12884901892L, 25769803783L, 17179869191L, 8L, 34359738377L, 38654705675L, 42949672972L, 34359738380L,
                4294967309L, 8589934605L, 13L, 14L, 8589934607L, 8589934608L, 68719476753L, 12884901906L, 21474836498L,
                73014444050L);

        for (int i = 0; i < edges.size(); i++) {
            weights.add(0);
        }
        final Graph graph = new Graph(19, edges, weights);

        checkTour(graph, new EulerianCircuitAlgorithm().getEulerianCurcuit(graph));

    }

    private void checkTour(final IGraph graph, final List<Integer> tour) {
        final Map<Long, Integer> availableEdges = new HashMap<Long, Integer>();
        int edges = 0;

        for (int node = 0; node < graph.getNodes(); node++) {
            for (final Iterator<Integer> iterator = graph.getAdjacentNodes(node); iterator.hasNext();) {
                final int other = iterator.next();
                if (node < other) {
                    ++edges;

                    final long edge = graph.getEdge(node, other);
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
            final long edge = graph.getEdge(lastNode, lastNode = iterator.next());
            final int occurances = availableEdges.get(edge);
            if (occurances == 1) {
                availableEdges.remove(edge);
            } else {
                availableEdges.put(edge, occurances - 1);
            }
        }

        assertTrue(availableEdges.isEmpty());
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
