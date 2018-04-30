package model.routing;

import model.IModel;
import model.Progressable;
import model.renderEngine.IRenderRoute;
import model.targets.IPointList;

public interface IRouteManager extends Progressable, IModel {

    IRenderRoute calculateRoute();

    String[] getRouteSolvers();

    IPointList getPointList();

    void setRouteSolver(int solver);

    boolean isCalculating();

    int getRouteSolver();
}