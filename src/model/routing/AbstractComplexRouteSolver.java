package model.routing;

public abstract class AbstractComplexRouteSolver extends AbstractProgressable implements IComplexRouteSolver {

    protected IGraph graph;

    public AbstractComplexRouteSolver(final IGraph graph) {
        this.graph = graph;
    }

    protected abstract ISPSPSolver createSPSPSolver();

}