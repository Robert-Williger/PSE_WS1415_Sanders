package controller.stateMachine;

import java.awt.Point;

import model.targets.IRoutePoint;

public interface IState {

    IState cancel();

    IState confirm();

    IState startCalculation();

    IState cancelCalculation();

    IState addPoint();

    IState searchPoint();

    IState movePointUp();

    IState movePointDown();

    IState resetPoints();

    IState changeOrder(int fromIndex, int toIndex);

    IState selectPoint(IRoutePoint point);

    IState removePoint(IRoutePoint point);

    IState locatePoint(Point point);

    IState setAddressText(String address);

    IState setTSPEnabled(boolean enabled);

    void entry();

    void exit();

}