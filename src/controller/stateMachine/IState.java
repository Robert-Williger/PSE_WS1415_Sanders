package controller.stateMachine;

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

    IState locatePoint(int x, int y);

    IState setAddressText(String address);

    IState setRouteSolver(int solver);

    void entry();

    void exit();

}