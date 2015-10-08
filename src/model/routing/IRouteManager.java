package model.routing;

import model.IModel;
import model.renderEngine.IRenderRoute;
import model.targets.IPointList;
import model.targets.IRoutePoint;

public interface IRouteManager extends Progressable, IModel {

    IRenderRoute calculateRoute();

    String[] getRouteSolvers();

    IRoutePoint createPoint();

    IPointList getPointList();

    void setRouteSolver(int solver);

    boolean isCalculating();

    int getRouteSolver();
}