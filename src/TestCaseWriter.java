import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import model.routing.BlossomAlgorithm;
import model.routing.Graph;
import model.routing.IGraph;

public class TestCaseWriter {

    private static int count;
    private PrintWriter writer;
    private int testNumber;

    public static void main(final String[] args) {

        while (true) {
            final int nodeNumber = 128;
            final int[] weights = new int[nodeNumber * (nodeNumber - 1) / 2];
            for (int i = 0; i < nodeNumber * (nodeNumber - 1) / 2; i++) {
                weights[i] = (int) (Math.random() * 1000);
            }
            final Graph graph = new Graph(nodeNumber, createEdges(nodeNumber), weights);
            new BlossomAlgorithm().calculatePerfectMatching(graph);
            System.out.println(++count);
        }
        // TestCaseWriter writer = new TestCaseWriter();
        // writer.setTestNumber(9);
        // while (writer.writeTestCase(16))
        // System.out.println(++count);
        // ;
    }

    public boolean writeTestCase(final int nodeNumber) {
        try {
            writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(new File("testcases.txt"), true)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        final int[] weights = new int[nodeNumber * (nodeNumber - 1) / 2];
        for (int i = 0; i < nodeNumber * (nodeNumber - 1) / 2; i++) {
            weights[i] = (int) (Math.random() * 1000);
        }

        final Graph graph = new Graph(nodeNumber, createEdges(nodeNumber), weights);

        final ArrayList<Integer> nodeNumbers = new ArrayList<Integer>();
        for (int i = 0; i < nodeNumber; i++) {
            nodeNumbers.add(i);
        }

        final Collection<Collection<Long>> permutations = createMatchings(nodeNumbers);

        int minWeight = Integer.MAX_VALUE;
        Collection<Long> minCollection = null;
        for (final Collection<Long> edges : permutations) {
            int currentWeight = 0;
            for (final long edge : edges) {
                currentWeight += graph.getWeight(edge);
            }
            if (currentWeight < minWeight) {
                minWeight = currentWeight;
                minCollection = edges;
            }
        }

        Set<Long> matching = null;

        try {
            matching = new BlossomAlgorithm().calculatePerfectMatching(graph);
        } catch (Exception e) {
        }

        if (matching == null || !validMatching(matching, graph, minWeight)) {
            writer.println();
            writer.println("@Test");
            writer.println("public void computerGeneratedTest" + testNumber++ + "() {");
            writer.println("final int nodes = " + nodeNumber + ";");
            writer.print("final List<Integer> weights = Arrays.asList(");
            for (int i = 0; i < weights.length; i++) {
                int weight = weights[i];
                writer.print(weight);
                if (i != weights.length - 1) {
                    writer.print(", ");
                }
            }
            writer.println(");");
            writer.println("final Graph graph = new Graph(nodes, createEdges(nodes), weights);");
            writer.println("final Set<Long> matching = new BlossomAlgorithm().calculatePerfectMatching(graph);");
            writer.println();
            writer.print("// matching [");
            for (final long edge : minCollection) {
                writer.print("(" + getFirstNode(edge) + "," + getSecondNode(edge) + ")");
            }
            writer.println("]");
            writer.println("checkMatching(matching, graph, " + minWeight + ");");
            writer.println("}");
            writer.println();
            writer.flush();

            return false;
        }

        return true;
    }

    public void setTestNumber(final int testNumber) {
        this.testNumber = testNumber;
    }

    private static long[] createEdges(final int nodes) {
        final long[] edges = new long[nodes * (nodes - 1) / 2];

        count = 0;
        for (int i = 0; i < nodes; i++) {
            for (int j = i + 1; j < nodes; j++) {
                edges[count] = getEdge(i, j);
            }
        }

        return edges;
    }

    public int getFirstNode(final long edge) {
        return (int) (edge >> 32);
    }

    public int getSecondNode(final long edge) {
        final long bitmask = 0x00000000FFFFFFFF;
        return (int) (edge & bitmask);
    }

    @SuppressWarnings("unchecked")
    private Collection<Collection<Long>> createMatchings(final ArrayList<Integer> list) {
        final Collection<Collection<Long>> ret = new LinkedList<Collection<Long>>();
        if (list.size() != 2) {
            for (int i = 1; i < list.size(); i++) {
                final ArrayList<Integer> clone = (ArrayList<Integer>) list.clone();
                clone.remove(i);
                clone.remove(0);

                final Collection<Collection<Long>> permutations = createMatchings(clone);
                for (final Collection<Long> permutation : permutations) {
                    permutation.add(getEdge(list.get(i), list.get(0)));
                    ret.add(permutation);
                }

            }
        } else {
            final LinkedList<Long> permutation = new LinkedList<Long>();
            permutation.add(getEdge(list.get(0), list.get(1)));
            ret.add(permutation);
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

    private boolean validMatching(final Set<Long> matching, final IGraph graph, final int expectedWeight) {
        int actualWeight = 0;

        for (final long edge : matching) {
            actualWeight += graph.getWeight(edge);
        }

        return actualWeight == expectedWeight;
    }
}
