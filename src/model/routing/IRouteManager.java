package model.routing;

import model.renderEngine.IRenderRoute;
import model.targets.IPointList;
import model.targets.IRoutePoint;

public interface IRouteManager extends Progressable {

    IRenderRoute calculateRoute();

    void setTSPEnabled(boolean enabled);

    IRoutePoint createPoint();

    IPointList getPointList();

}