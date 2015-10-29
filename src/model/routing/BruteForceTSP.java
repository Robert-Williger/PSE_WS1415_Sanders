package model.routing;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import model.IProgressListener;

public class BruteForceTSP extends AbstractComplexRouteSolver implements IComplexRouteSolver {

    private boolean canceled;
    private ISPSPSolver solver;
    private HashMap<Long, Path> completeMapping;
    private long count;

    public BruteForceTSP(final IGraph graph) {
        super(graph);

        this.solver = createSPSPSolver();
        solver.addProgressListener(new IProgressListener() {

            @Override
            public void progressDone(final int i) {

            }

            @Override
            public void errorOccured(final String message) {
                fireErrorOccured(message);
            }

            @Override
            public void stepCommenced(String step) {

            }
        });
    }

    @Override
    public List<Path> calculateRoute(final List<InterNode> edges) {
        canceled = false;
        fireProgressDone(-1);
        final int n = edges.size();

        // Array of current permutation, path index and minimum total length
        int[] state = new int[n + 2];

        // paths[pathArrayIndex] = minimum path
        // paths[1 - pathArrayIndex] = swap path
        Path[][] paths = new Path[2][n];

        for (int i = 0; i < state.length - 2; i++) {
            state[i] = -1;
        }
        state[state.length - 2] = 0; // pathArrayIndex
        state[state.length - 1] = Integer.MAX_VALUE; // minimum total weight

        completeMapping = new HashMap<Long, Path>();
        final IGraph completeGraph = createCompleteGraph(edges);

        if (completeGraph == null) {
            return new LinkedList<Path>();
        }

        count = 0;
        permutate(completeGraph, state, paths, 0);
        System.out.println(count);

        return canceled ? new LinkedList<Path>() : Arrays.asList(paths[state[state.length - 2]]);
    }

    private IGraph createCompleteGraph(final List<InterNode> points) {
        final int size = points.size();
        final long[] edges = new long[size * (size - 1) / 2];
        final int[] weights = new int[edges.length];

        final double progressStep = 100.0 / (points.size() - 1);

        int i = 0;
        for (int u = 0; u < points.size() && !canceled; u++) {
            for (int v = u + 1; v < points.size() && !canceled; v++) {
                final long edge = graph.getEdge(u, v);
                edges[i] = edge;
                final Path path = solver.calculateShortestPath(points.get(u), points.get(v));
                if (path == null) {
                    canceled = true;
                    return null;
                }
                weights[i] = path.getLength();
                completeMapping.put(edge, path);
                ++i;
            }
            fireProgressDone(progressStep);
        }

        return new Graph(size, edges, weights);
    }

    private void permutate(final IGraph graph, final int[] state, final Path[][] paths, final int depth) {

        if (depth == graph.getNodes()) {

            Path[] swap = paths[1 - state[state.length - 2]];

            swap[0] = completeMapping.get(graph.getEdge(state[0], state[graph.getNodes() - 1]));
            int distance = swap[0].getLength();

            for (int i = 1; i < graph.getNodes(); i++) {
                swap[i] = completeMapping.get(graph.getEdge(state[i - 1], state[i]));
                distance += swap[i].getLength();
            }

            if (distance < state[state.length - 1]) {
                state[state.length - 1] = distance;
                state[state.length - 2] = 1 - state[state.length - 2];
            }

            return;
        }
        for (int i = 0; i < graph.getNodes() && !canceled; i++) {
            ++count;
            if (state[i] == -1) {
                state[i] = depth;
                permutate(graph, state, paths, depth + 1);
                state[i] = -1;
            }
        }
    }

    @Override
    public void cancelCalculation() {
        canceled = true;
        solver.cancelCalculation();
    }

    @Override
    protected ISPSPSolver createSPSPSolver() {
        return new ReusableDijkstra(graph);
    }
}
