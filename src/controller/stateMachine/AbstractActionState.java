package controller.stateMachine;

import java.awt.Point;

import model.map.AddressNode;
import model.targets.IRoutePoint;
import model.targets.PointState;

abstract class AbstractActionState extends AbstractState {

    @Override
    public IState removePoint(final IRoutePoint point) {
        getList().remove(point.getIndex());
        endCurrentAction();

        if (getList().remove(point)) {
            getImageLoader().setRenderRoute(null);
            getSidebarView().setRouteLength(0);
            if (getList().getSize() < 2) {
                getSidebarView().setStartable(false);
            }
        }

        return DefaultState.getInstance();
    }

    @Override
    public IState resetPoints() {
        endCurrentAction();

        getImageLoader().setRenderRoute(null);
        getSidebarView().setRouteLength(0);
        getSidebarView().setStartable(false);
        getSidebarView().setResettable(false);
        getList().reset();

        return DefaultState.getInstance();
    }

    @Override
    public IState selectPoint(final IRoutePoint point) {
        if (point != getStore().getPoint()) {
            endCurrentAction();

            getStore().setPoint(point);
            getStore().storePoint();
            point.setState(PointState.editing);
            getSidebarView().setPointOrderChangable(true);

            return EditingState.getInstance();
        }

        return this;
    }

    @Override
    public IState setAddressText(final String address) {
        getStore().setCurrentAddress(address);

        final IRoutePoint point = getStore().getPoint();
        if ((point == null || !address.equals(point.getAddress())) && !address.isEmpty()) {
            getSidebarView().setAddressSuggestions(getTextProcessor().suggest(address));
            return getTextedState();
        }

        return this;
    }

    @Override
    public IState locatePoint(final Point point) {
        final AddressNode node = getMap().getAddressNode(point);
        if (node != null) {
            final IRoutePoint routePoint = getPoint();

            routePoint.setAddress(node.getAddress());
            routePoint.setStreetNode(node.getStreetNode());
            routePoint.setLocation(null);
            getSidebarView().setCancelable(true);

            return getLocatedState();
        }
        cancelPointRelocation();

        return this;
    }

    @Override
    public IState startCalculation() {
        endCurrentAction();

        return CalculatingState.getInstance();
    }

    @Override
    public IState setRouteSolver(final int solver) {
        getRouteManager().setRouteSolver(solver);

        return this;
    }

    protected abstract void cancelPointRelocation();

    protected abstract void endCurrentAction();

    protected abstract IRoutePoint getPoint();

    protected abstract IState getLocatedState();

    protected abstract IState getTextedState();

}
