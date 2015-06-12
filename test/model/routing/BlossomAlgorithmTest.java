package model.routing;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class BlossomAlgorithmTest {

    @Rule
    public Timeout globalTimeout = new Timeout(500); // 1/2 second max per
                                                     // method tested

    @Test
    public void testVerySmallAugmention() {
        final int nodes = 6;
        final List<Integer> weights = Arrays.asList(62, 22, 2, 80, 36, 6, 30, 64, 50, 28, 40, 72, 46, 48, 20);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        checkMatching(matching, graph, 28);
    }

    @Test
    public void testVerySmallGrow1() {
        final int nodes = 6;
        final List<Integer> weights = Arrays.asList(42, 4, 6, 14, 8, 34, 58, 68, 64, 68, 36, 26, 62, 50, 6);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        checkMatching(matching, graph, 46);
    }

    @Test
    public void testVerySmallGrow2() {
        final int nodes = 6;
        final List<Integer> weights = Arrays.asList(40, 52, 50, 46, 70, 36, 46, 34, 54, 28, 64, 20, 6, 28, 34);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        checkMatching(matching, graph, 66);
    }

    @Test
    public void testVerySmallShrink1() {
        final int nodes = 6;
        final List<Integer> weights = Arrays.asList(30, 38, 36, 2, 58, 4, 28, 28, 80, 68, 40, 14, 18, 70, 36);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        checkMatching(matching, graph, 44);
    }

    @Test
    public void testVerySmallShrink2() {
        final int nodes = 6;
        final List<Integer> weights = Arrays.asList(80, 66, 22, 42, 44, 30, 52, 38, 38, 74, 62, 50, 36, 48, 72);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        checkMatching(matching, graph, 110);
    }

    @Test
    public void testVerySmallShrink3() {
        final int nodes = 6;
        final List<Integer> weights = Arrays.asList(50, 42, 22, 2, 72, 14, 56, 80, 38, 50, 58, 50, 8, 54, 64);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        checkMatching(matching, graph, 70);
    }

    @Test
    public void testSmallShrink() {
        final int nodes = 8;
        final List<Integer> weights = Arrays.asList(42, 4, 6, 14, 8, 45, 58, 68, 64, 58, 36, 26, 62, 50, 6, 52, 24, 44,
                30, 26, 4, 32, 60, 40, 2, 60, 42, 44);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        checkMatching(matching, graph, 46);
    }

    @Test
    public void testSmallShrinkAndExpand() {
        final int nodes = 8;
        final List<Integer> weights = Arrays.asList(40, 52, 50, 46, 70, 36, 46, 34, 54, 28, 64, 20, 6, 28, 34, 24, 2,
                30, 42, 18, 36, 8, 14, 80, 22, 22, 64, 80);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        checkMatching(matching, graph, 64);
    }

    @Test
    public void testNormalShrink() {
        final int nodes = 12;
        final List<Integer> weights = Arrays.asList(62, 22, 2, 80, 36, 6, 30, 64, 50, 28, 40, 72, 46, 48, 20, 28, 18,
                64, 48, 48, 10, 30, 80, 10, 8, 50, 60, 50, 78, 72, 10, 76, 4, 8, 46, 6, 74, 20, 18, 14, 62, 2, 10, 16,
                70, 24, 52, 56, 2, 72, 24, 28, 60, 70, 32, 66, 74, 30, 38, 14, 36, 54, 66, 12, 72, 78);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        checkMatching(matching, graph, 48);
    }

    @Test
    public void testGreatShrink1() {
        final int nodes = 16;
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
        final int nodes = 16;
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
    public void testGreatShrinking3() {
        final int nodes = 16;
        final List<Integer> weights = Arrays.asList(30, 38, 36, 2, 58, 4, 28, 28, 80, 68, 40, 14, 18, 70, 36, 72, 12,
                38, 30, 44, 38, 12, 58, 50, 42, 28, 78, 22, 74, 62, 32, 36, 6, 52, 2, 66, 60, 64, 58, 10, 42, 52, 12,
                46, 68, 42, 70, 6, 16, 40, 16, 42, 22, 14, 56, 20, 50, 24, 44, 18, 42, 46, 8, 80, 16, 2, 72, 10, 70,
                64, 10, 48, 50, 74, 46, 44, 78, 50, 60, 80, 6, 44, 62, 2, 46, 36, 50, 60, 30, 54, 18, 48, 14, 62, 24,
                58, 20, 72, 24, 72, 58, 40, 4, 60, 2, 70, 64, 30, 24, 56, 30, 36, 20, 64, 46, 4, 28, 34, 48, 32);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        checkMatching(matching, graph, 76);
    }

    @Test
    public void testGreatGraph() {
        final int nodes = 16;
        final List<Integer> weights = Arrays.asList(76, 2, 30, 43, 20, 28, 3, 75, 60, 77, 58, 29, 16, 32, 52, 98, 63,
                21, 19, 18, 69, 32, 96, 70, 72, 6, 32, 6, 68, 98, 61, 81, 33, 48, 41, 48, 97, 33, 68, 82, 24, 78, 26,
                90, 15, 63, 73, 44, 80, 76, 86, 68, 74, 86, 88, 24, 78, 27, 90, 68, 65, 66, 85, 7, 39, 48, 6, 29, 85,
                31, 28, 63, 96, 7, 80, 30, 63, 8, 75, 21, 42, 37, 16, 6, 99, 1, 75, 71, 54, 89, 75, 73, 93, 99, 16, 40,
                52, 16, 2, 64, 72, 68, 22, 93, 56, 56, 38, 96, 46, 67, 99, 57, 25, 53, 89, 36, 42, 86, 74, 91);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        checkMatching(matching, graph, 111);
    }

    @Test
    public void computerGeneratedTest1() {
        final int nodes = 6;
        final List<Integer> weights = Arrays.asList(68, 63, 71, 85, 50, 37, 91, 83, 38, 53, 57, 9, 42, 16, 56);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        // matching [(3,4)(2,5)(0,1)]
        checkMatching(matching, graph, 119);
    }

    @Test
    public void computerGeneratedTest2() {
        final int nodes = 6;
        final List<Integer> weights = Arrays.asList(0, 10, 77, 46, 58, 25, 29, 64, 34, 91, 53, 29, 19, 49, 6);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        // matching [(4,5)(1,3)(0,2)]
        checkMatching(matching, graph, 45);
    }

    @Test
    public void computerGeneratedTest3() {
        final int nodes = 8;
        final List<Integer> weights = Arrays.asList(33, 65, 76, 59, 56, 52, 17, 59, 71, 96, 98, 53, 3, 4, 49, 8, 12,
                99, 72, 29, 2, 43, 44, 50, 88, 24, 69, 15);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        // matching [(3,6)(2,5)(1,7)(0,4)]
        checkMatching(matching, graph, 72);
    }

    @Test
    public void computerGeneratedTest4() {
        final int nodes = 8;
        final List<Integer> weights = Arrays.asList(31, 52, 70, 65, 70, 26, 39, 28, 86, 75, 8, 55, 5, 83, 25, 63, 8,
                74, 62, 36, 87, 14, 59, 61, 26, 93, 9, 86);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        // matching [(3,7)(2,4)(1,5)(0,6)]
        checkMatching(matching, graph, 73);
    }

    @Test
    public void computerGeneratedTest5() {
        final int nodes = 6;
        final List<Integer> weights = Arrays.asList(29, 45, 64, 26, 78, 59, 98, 70, 39, 55, 41, 80, 48, 84, 34);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        // matching [(4,5)(2,3)(0,1)]
        checkMatching(matching, graph, 118);

    }

    @Test
    public void computerGeneratedTest6() {
        final int nodes = 8;
        final List<Integer> weights = Arrays.asList(4, 55, 8, 91, 47, 3, 67, 9, 65, 5, 96, 76, 8, 94, 27, 94, 22, 28,
                81, 95, 4, 36, 61, 38, 31, 34, 53, 88);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        // matching [(0,3)(5,6)(1,4)(2,7)]
        checkMatching(matching, graph, 75);

    }

    @Test
    public void computerGeneratedTest7() {
        final int nodes = 8;
        final List<Integer> weights = Arrays.asList(23, 25, 74, 57, 34, 9, 8, 96, 60, 20, 83, 87, 2, 89, 99, 76, 34,
                94, 83, 12, 27, 46, 89, 66, 78, 79, 34, 32);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        // matching [(3,5)(2,6)(1,4)(0,7)]
        checkMatching(matching, graph, 74);
    }

    @Test
    public void computerGeneratedTest8() {
        final int nodes = 8;
        final List<Integer> weights = Arrays.asList(7, 91, 5, 5, 90, 35, 39, 26, 0, 71, 47, 46, 75, 44, 46, 63, 42, 80,
                42, 77, 53, 68, 19, 26, 20, 22, 96, 61);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        // matching [(5,6)(4,7)(1,2)(0,3)]
        checkMatching(matching, graph, 73);
    }

    @Test
    public void computerGeneratedTest9() {
        final int nodes = 8;
        final List<Integer> weights = Arrays.asList(40, 46, 67, 41, 45, 38, 95, 91, 36, 90, 16, 11, 46, 6, 75, 0, 7,
                70, 65, 92, 77, 84, 53, 96, 26, 60, 91, 38);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        // matching [(4,7)(2,3)(1,5)(0,6)]
        checkMatching(matching, graph, 86);
    }

    @Test
    public void computerGeneratedTest10() {
        final int nodes = 16;
        final List<Integer> weights = Arrays.asList(54, 11, 22, 42, 62, 29, 97, 70, 44, 73, 3, 20, 29, 71, 20, 27, 24,
                77, 40, 60, 8, 20, 37, 82, 75, 57, 4, 79, 95, 73, 52, 40, 97, 80, 96, 0, 64, 38, 69, 45, 3, 29, 71, 33,
                24, 84, 69, 64, 40, 76, 34, 76, 90, 97, 31, 67, 63, 96, 30, 62, 91, 33, 59, 98, 86, 18, 86, 79, 57, 31,
                48, 1, 86, 91, 51, 16, 85, 25, 76, 68, 40, 20, 49, 34, 52, 82, 47, 10, 8, 2, 54, 68, 24, 37, 84, 61, 5,
                54, 31, 78, 10, 18, 55, 83, 17, 40, 11, 41, 14, 71, 9, 25, 60, 10, 9, 29, 21, 37, 15, 41);
        final Graph graph = new Graph(nodes, createEdges(nodes), weights);
        final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);

        // matching [(10,12)(9,15)(8,13)(4,5)(3,6)(2,14)(1,7)(0,11)]
        checkMatching(matching, graph, 102);
    }

    private void checkMatching(final Set<Long> matching, final IGraph graph, final int expectedWeight) {
        int actualWeight = 0;
        final int[] actualOccurances = new int[graph.getNodes()];
        final int[] expectedOccurances = new int[graph.getNodes()];
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

        assertArrayEquals(expectedOccurances, actualOccurances);
        assertEquals(expectedWeight, actualWeight);
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
