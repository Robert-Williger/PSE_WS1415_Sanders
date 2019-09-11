package model.routing;

import model.IModel;
import model.Progressable;
import model.renderEngine.renderers.IRenderRoute;
import model.targets.IPointList;

public interface IRouteManager extends Progressable, IModel {

    IRenderRoute calculateRoute();

    String[] getRouteSolvers();

    IPointList getPointList();

    void setRouteSolver(int solver);

    boolean isCalculating();

    int getRouteSolver();
}