package controller.stateMachine;

import model.IApplication;
import model.elements.StreetNode;
import model.targets.IRoutePoint;

class StateStore {

    private IApplication application;

    private IRoutePoint storedPoint;
    private StreetNode storedNode;
    private int storedIndex;
    private String storedAddress;
    private String currentAddress;

    void setApplication(final IApplication application) {
        this.application = application;
    }

    void setPoint(final IRoutePoint point) {
        storedPoint = point;
    }

    IRoutePoint getPoint() {
        return storedPoint;
    }

    void setCurrentAddress(final String address) {
        currentAddress = address;
    }

    String getCurrentAddress() {
        return currentAddress;
    }

    void storePoint() {
        storedAddress = storedPoint.getAddress();
        storedNode = storedPoint.getStreetNode();
        storedIndex = storedPoint.getIndex();
    }

    void restorePoint() {
        storedPoint.setAddress(storedAddress);
        storedPoint.setStreetNode(storedNode);
        application.getRouteManager().getPointList().changeOrder(storedPoint.getIndex(), storedIndex);
    }

}
