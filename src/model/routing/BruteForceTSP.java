package model.routing;

import java.util.List;

import model.IProgressListener;

public class BruteForceTSP extends AbstractRouteSolver {

    private boolean canceled;
    private ISPSPSolver solver;
    private Path[] completeMapping;

    public BruteForceTSP(final IDirectedGraph undirectedGraph) {
        super(undirectedGraph);

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
            public void stepCommenced(final String step) {

            }
        });
    }

    @Override
    public Route calculateRoute(final List<InterNode> edges) {
        canceled = false;

        fireProgressDone(-1);
        final int n = edges.size();

        // Array of current permutation, path index and minimum total length
        // int[] state = new int[n + 2];

        // paths[pathArrayIndex] = minimum path
        // paths[1 - pathArrayIndex] = swap path
        // Path[][] paths = new Path[2][n];

        // for (int i = 0; i < state.length - 2; i++) {
        // state[i] = -1;
        // }
        // state[state.length - 2] = 0; // pathArrayIndex
        // state[state.length - 1] = Integer.MAX_VALUE; // minimum total weight

        completeMapping = createCompleteMapping(edges);

        if (completeMapping == null) {
            return emptyRoute();
        }

        // permutate(state, paths, 0);
        // int[] targetIndices = new int[n];
        // for (int i = 0; i < targetIndices.length; i++) {
        // targetIndices[state[i]] = i;
        // }

        int[] permutation = permutate(n);
        Path[] path = new Path[edges.size()];
        for (int i = 0; i < permutation.length; i++) {
            path[i] = getPath(permutation[i], permutation[(i + 1) % permutation.length], n);
        }

        // return canceled ? emptyRoute() : new Route(paths[state[state.length -
        // 2]], targetIndices);
        int[] targetIndices = new int[permutation.length];
        for (int i = 0; i < permutation.length; i++) {
            targetIndices[permutation[i]] = i;
        }
        return canceled ? emptyRoute() : new Route(path, targetIndices);
    }

    private Path[] createCompleteMapping(final List<InterNode> points) {
        final int size = points.size();
        Path[] completeMapping = new Path[size * size];

        final double progressStep = 100.0 / (size - 1);

        int i = 0;
        for (int u = 0; u < size && !canceled; u++) {
            for (int v = 0; v < size && !canceled; v++) {
                if (u != v) {
                    final Path path = solver.calculateShortestPath(points.get(u), points.get(v));
                    if (path == null) {
                        canceled = true;
                        return null;
                    }
                    completeMapping[i] = path;
                }
                ++i;
            }
            fireProgressDone(progressStep);
        }

        return completeMapping;
    }

    private Path getPath(final int from, final int to, final int nodes) {
        return completeMapping[from * nodes + to];
    }

    private int[] permutate(final int size) {
        int[] curPerm = new int[size];
        int[] minPerm = new int[size];

        int minLength = 0;
        for (int i = 0; i < size; i++) {
            curPerm[i] = i;
            minPerm[i] = i;
            minLength += getPath(i, (i + 1) % size, size).getLength();
        }

        permutate(curPerm, minPerm, minLength, minLength, 0);

        return minPerm;
    }

    private int permutate(final int[] curPerm, final int[] minPerm, final int curLen, int minLen, final int depth) {
        if (depth == curPerm.length - 1) {
            if (curLen < minLen) {
                System.arraycopy(curPerm, 0, minPerm, 0, curPerm.length);
                return curLen;
            }

            return minLen;
        }

        permutate(curPerm, minPerm, curLen, minLen, depth + 1);

        final int size = curPerm.length;
        for (int i = depth + 1; i < size; i++) {

            // int newLength = curLen;

            // newLength += getPath(curPerm[depth], curPerm[depth + 1],
            // size).getLength();
            // newLength += getPath(curPerm[(depth - 1 + size) % size],
            // curPerm[depth], size).getLength();
            // newLength += getPath(curPerm[i], curPerm[(i + 1) % size],
            // size).getLength();
            // newLength += getPath(curPerm[i - 1], curPerm[i],
            // size).getLength();

            swap(i, depth, curPerm);

            int length = 0;
            for (int index = 0; index < size; index++) {
                length += getPath(curPerm[index], curPerm[(index + 1) % size], size).getLength();
            }

            // newLength += getPath(curPerm[depth], curPerm[depth + 1],
            // size).getLength();
            // newLength += getPath(curPerm[(depth - 1 + size) % size],
            // curPerm[depth], size).getLength();
            // newLength += getPath(curPerm[i], curPerm[(i + 1) % size],
            // size).getLength();
            // newLength += getPath(curPerm[i - 1], curPerm[i],
            // size).getLength();

            minLen = permutate(curPerm, minPerm, length, minLen, depth + 1);
            swap(i, depth, curPerm);
        }

        return minLen;
    }

    private void swap(final int i, final int j, final int[] array) {
        final int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    // private void permutate(final int[] state, final Path[][] paths, final int
    // depth) {
    // final int nodes = paths[0].length;
    // if (depth == nodes) {
    //
    // Path[] swap = paths[1 - state[state.length - 2]];
    //
    // swap[0] = getPath(state[0], state[nodes - 1], nodes);
    // int distance = swap[0].getLength();
    //
    // for (int i = 1; i < nodes; i++) {
    // swap[i] = getPath(state[i - 1], state[i], nodes);
    // distance += swap[i].getLength();
    // }
    //
    // if (distance < state[state.length - 1]) {
    // state[state.length - 1] = distance;
    // state[state.length - 2] = 1 - state[state.length - 2];
    // }
    //
    // return;
    // }
    // for (int i = 0; i < nodes && !canceled; i++) {
    // if (state[i] == -1) {
    // state[i] = depth;
    // permutate(state, paths, depth + 1);
    // state[i] = -1;
    // }
    // }
    // }

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
