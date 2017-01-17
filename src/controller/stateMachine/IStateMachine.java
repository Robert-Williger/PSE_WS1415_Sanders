package controller.stateMachine;

import model.targets.IRoutePoint;

public interface IStateMachine {

    void cancel();

    void confirm();

    void startCalculation();

    void cancelCalculation();

    void addPoint();

    void searchPoint();

    void movePointUp();

    void movePointDown();

    void resetPoints();

    void changeOrder(int fromIndex, int toIndex);

    void selectPoint(IRoutePoint point);

    void removePoint(IRoutePoint point);

    void locatePoint(int x, int y);

    void setAddressText(String address);

    void setRouteSolver(int solver);

}