package model.routing;

import java.util.List;

import model.IProgressListener;

public class ViaRouteSolver extends AbstractRouteSolver {
    private int state;
    private final ISPSPSolver solver;
    private int paths;

    public ViaRouteSolver(final IDirectedGraph graph) {
        super(graph);

        solver = createSPSPSolver();
        solver.addProgressListener(new IProgressListener() {

            @Override
            public void progressDone(final int progress) {
                fireProgressDone(progress / paths);
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
    public Route calculateRoute(final List<InterNode> edges) {
        fireProgressDone(-1);
        solver.cancelCalculation();
        // 0 working, 1 ready, 2 cancel/error
        state = 0;

        paths = edges.size();
        final Path[] pathArray = new Path[paths - 1];
        final int[] targetIndices = new int[paths];
        targetIndices[0] = 0;
        for (int i = 1; i < paths && state == 0; i++) {
            final Path path = solver.calculateShortestPath(edges.get(i - 1), edges.get(i));
            if (path == null) {
                return emptyRoute();
            }
            pathArray[i - 1] = path;
            targetIndices[i] = i;
        }
        fireProgressDone(100);

        return new Route(pathArray, targetIndices);
    }

    @Override
    public void cancelCalculation() {
        solver.cancelCalculation();
        state = 2;
    }

    @Override
    protected ISPSPSolver createSPSPSolver() {
        return new Dijkstra(graph);
    }

}