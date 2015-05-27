package view;

import model.targets.IRoutePoint;

public interface IRoutePointView extends IView {

    void addRoutePointListener(IDragListener listener);

    IRoutePoint getRoutePoint();

}