package controller.stateMachine;

import java.util.HashSet;
import java.util.Set;

import model.IApplication;
import model.map.AddressPoint;
import model.targets.IRoutePoint;

class StateStore {

    private IApplication application;

    private IRoutePoint storedRoutePoint;
    private AddressPoint storedAddressPoint;
    private int storedIndex;
    private String currentAddress;
    private Set<String> suggestions;

    StateStore() {
        suggestions = new HashSet<>();
        suggestions.add("");
    }

    void setApplication(final IApplication application) {
        this.application = application;
    }

    void setPoint(final IRoutePoint point) {
        storedRoutePoint = point;
    }

    IRoutePoint getPoint() {
        return storedRoutePoint;
    }

    void setCurrentAddress(final String address) {
        currentAddress = address;
    }

    String getCurrentAddress() {
        return currentAddress;
    }

    void storePoint() {
        storedAddressPoint = storedRoutePoint.getAddressPoint();
        storedIndex = storedRoutePoint.getListIndex();
    }

    void restorePoint() {
        storedRoutePoint.setAddressPoint(storedAddressPoint);
        application.getRouteManager().getPointList().changeOrder(storedRoutePoint.getListIndex(), storedIndex);
    }

    Set<String> getSuggestions() {
        return suggestions;
    }

}
