package model.routing;

public abstract class AbstractComplexRouteSolver extends AbstractProgressable implements IComplexRouteSolver {

    protected abstract ISPSPSolver createSPSPSolver();

}