package model.routing;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class BlossomAlgorithmTest {

    @Test
    public void testOnlyAugmentions() {
        int nodes = 6;
        final List<Integer> weights = Arrays.asList(62, 22, 2, 80, 36, 6, 30, 64, 50, 28, 40, 72, 46, 48, 20);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        checkMatching(matching, graph, 28);
    }

    @Test
    public void testAugmentedGrow() {
        int nodes = 6;
        final List<Integer> weights = Arrays.asList(42, 4, 6, 14, 8, 34, 58, 68, 64, 68, 36, 26, 62, 50, 6);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        checkMatching(matching, graph, 46);
    }

    @Test
    public void testUnaugmentedGrow() {
        int nodes = 6;
        final List<Integer> weights = Arrays.asList(40, 52, 50, 46, 70, 36, 46, 34, 54, 28, 64, 20, 6, 28, 34);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        checkMatching(matching, graph, 66);
    }

    @Test
    public void testShrinkAndExpand() {
        int nodes = 8;
        final List<Integer> weights = Arrays.asList(40, 52, 50, 46, 70, 36, 46, 34, 54, 28, 64, 20, 6, 28, 34, 24, 2,
                30, 42, 18, 36, 8, 14, 80, 22, 22, 64, 80);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        checkMatching(matching, graph, 64);
    }

    @Test
    public void testSmallShrink() {
        int nodes = 6;
        final List<Integer> weights = Arrays.asList(30, 38, 36, 2, 58, 4, 28, 28, 80, 68, 40, 14, 18, 70, 36);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        checkMatching(matching, graph, 44);
    }

    @Test
    public void testNormalShrink() {
        int nodes = 12;
        final List<Integer> weights = Arrays.asList(62, 22, 2, 80, 36, 6, 30, 64, 50, 28, 40, 72, 46, 48, 20, 28, 18,
                64, 48, 48, 10, 30, 80, 10, 8, 50, 60, 50, 78, 72, 10, 76, 4, 8, 46, 6, 74, 20, 18, 14, 62, 2, 10, 16,
                70, 24, 52, 56, 2, 72, 24, 28, 60, 70, 32, 66, 74, 30, 38, 14, 36, 54, 66, 12, 72, 78);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        checkMatching(matching, graph, 48);
    }

    @Test
    public void testGreatShrink1() {
        int nodes = 16;
        final List<Integer> weights = Arrays.asList(62, 22, 2, 80, 36, 6, 30, 64, 50, 28, 40, 72, 46, 48, 20, 28, 18,
                64, 48, 48, 10, 30, 80, 10, 8, 50, 60, 50, 78, 72, 10, 76, 4, 8, 46, 6, 74, 20, 18, 14, 62, 2, 10, 16,
                70, 24, 52, 56, 2, 72, 24, 28, 60, 70, 32, 66, 74, 30, 38, 14, 36, 54, 66, 12, 72, 78, 44, 10, 58, 6,
                26, 14, 32, 2, 24, 42, 30, 42, 50, 18, 72, 16, 18, 52, 40, 36, 34, 30, 18, 40, 38, 12, 20, 20, 42, 62,
                64, 54, 14, 52, 62, 78, 58, 58, 64, 54, 18, 50, 80, 58, 78, 76, 34, 30, 50, 6, 76, 32, 30, 24);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        checkMatching(matching, graph, 74);
    }

    @Test
    public void testGreatShrink2() {
        int nodes = 16;
        final List<Integer> weights = Arrays.asList(80, 66, 22, 42, 44, 30, 52, 38, 38, 74, 62, 50, 36, 48, 72, 72, 46,
                76, 34, 4, 76, 30, 66, 6, 10, 24, 16, 22, 14, 52, 36, 50, 44, 62, 66, 36, 14, 32, 50, 46, 28, 74, 34,
                70, 12, 10, 70, 72, 8, 64, 4, 60, 60, 12, 8, 32, 16, 42, 32, 18, 58, 28, 26, 32, 56, 42, 56, 22, 74,
                66, 56, 42, 26, 52, 56, 52, 8, 76, 28, 44, 36, 78, 60, 74, 24, 72, 42, 52, 40, 50, 52, 50, 80, 68, 44,
                28, 48, 42, 26, 24, 8, 48, 12, 26, 8, 26, 78, 14, 16, 42, 56, 28, 54, 54, 68, 66, 16, 56, 72, 58);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        checkMatching(matching, graph, 120);
    }

    @Test
    public void testComplexGraph() {
        int nodes = 16;
        final List<Integer> weights = Arrays.asList(30, 38, 36, 2, 58, 4, 28, 28, 80, 68, 40, 14, 18, 70, 36, 72, 12,
                38, 30, 44, 38, 12, 58, 50, 42, 28, 78, 22, 74, 62, 32, 36, 6, 52, 2, 66, 60, 64, 58, 10, 42, 52, 12,
                46, 68, 42, 70, 6, 16, 40, 16, 42, 22, 14, 56, 20, 50, 24, 44, 18, 42, 46, 8, 80, 16, 2, 72, 10, 70,
                64, 10, 48, 50, 74, 46, 44, 78, 50, 60, 80, 6, 44, 62, 2, 46, 36, 50, 60, 30, 54, 18, 48, 14, 62, 24,
                58, 20, 72, 24, 72, 58, 40, 4, 60, 2, 70, 64, 30, 24, 56, 30, 36, 20, 64, 46, 4, 28, 34, 48, 32);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        checkMatching(matching, graph, 76);
    }

    private void checkMatching(final Set<Long> matching, final IGraph graph, final int expectedWeight) {
        int actualWeight = 0;
        int[] actualOccurances = new int[graph.getNodes()];
        int[] expectedOccurances = new int[graph.getNodes()];
        for (int i = 0; i < expectedOccurances.length; i++) {
            expectedOccurances[i] = 1;
        }

        for (final long edge : matching) {
            actualWeight += graph.getWeight(edge);
            final int firstNode = graph.getFirstNode(edge);
            final int secondNode = graph.getSecondNode(edge);

            assertTrue(firstNode >= 0 && firstNode < graph.getNodes());
            assertTrue(secondNode >= 0 && secondNode < graph.getNodes());

            ++actualOccurances[firstNode];
            ++actualOccurances[secondNode];
        }

        assertEquals(expectedWeight, actualWeight);
        assertArrayEquals(expectedOccurances, actualOccurances);
    }

    private List<Long> createEdges(final int nodes) {
        final List<Long> edges = new ArrayList<Long>(nodes * (nodes - 1) / 2);

        for (int i = 0; i < nodes; i++) {
            for (int j = i + 1; j < nodes; j++) {
                edges.add(getEdge(i, j));
            }
        }

        return edges;
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
