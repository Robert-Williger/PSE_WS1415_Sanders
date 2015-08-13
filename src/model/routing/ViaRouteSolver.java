package model.routing;

import java.util.ArrayList;
import java.util.List;

import model.IProgressListener;

public class ViaRouteSolver extends AbstractComplexRouteSolver {
    private final IGraph graph;
    private int state;
    private final ISPSPSolver solver;
    private int paths;

    public ViaRouteSolver(final IGraph graph) {
        this.graph = graph;
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
    public List<Path> calculateRoute(final List<InterNode> edges) {
        fireProgressDone(-1);
        solver.cancelCalculation();
        // 0 working, 1 ready, 2 cancel/error
        state = 0;

        final List<Path> ret = new ArrayList<Path>();

        paths = edges.size();
        for (int i = 1; i < paths && state == 0; i++) {
            ret.add(solver.calculateShortestPath(edges.get(i - 1), edges.get(i)));
        }
        fireProgressDone(100);
        return ret;
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