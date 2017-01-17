package controller.stateMachine;

import java.util.HashSet;
import java.util.Set;

import model.IApplication;
import model.elements.AccessPoint;
import model.targets.IRoutePoint;

class StateStore {

    private IApplication application;

    private IRoutePoint storedPoint;
    private AccessPoint storedNode;
    private int storedIndex;
    private String storedAddress;
    private String currentAddress;
    private Set<String> suggestions;

    StateStore() {
        suggestions = new HashSet<String>();
        suggestions.add("");
    }

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
        storedNode = storedPoint.getAccessPoint();
        storedIndex = storedPoint.getListIndex();
    }

    void restorePoint() {
        storedPoint.setAddress(storedAddress);
        storedPoint.setAccessPoint(storedNode);
        application.getRouteManager().getPointList().changeOrder(storedPoint.getListIndex(), storedIndex);
    }

    Set<String> getSuggestions() {
        return suggestions;
    }

}
