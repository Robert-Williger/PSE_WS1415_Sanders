package model.routing;

import model.AbstractProgressable;

public abstract class AbstractRouteSolver extends AbstractProgressable implements IRouteSolver {

    protected IDirectedGraph graph;
    private static final Route emptyRoute;

    public AbstractRouteSolver(final IDirectedGraph graph) {
        this.graph = graph;
    }

    protected Route emptyRoute() {
        return emptyRoute;
    }

    protected abstract ISPSPSolver createSPSPSolver();

    static {
        emptyRoute = new Route(new Path[0], null) {
            @Override
            public int getTargetIndex(final int index) {
                return index;
            }
        };
    }
}