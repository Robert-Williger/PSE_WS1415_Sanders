package controller.stateMachine;

import java.util.List;
import java.util.Set;

import model.targets.AddressPoint;
import model.targets.IRoutePoint;
import model.targets.IRoutePoint.State;

abstract class AbstractActionState extends AbstractState {

    @Override
    public IState removePoint(final IRoutePoint point) {
        getList().remove(point.getListIndex());
        getImageLoader().setRenderRoute(null);
        getSidebarView().setRouteLength(0);

        int size = getList().size();
        if (size < 2) {
            getSidebarView().setStartable(false);
            if (size == 0) {
                getSidebarView().setResettable(false);
            }
        }

        endCurrentAction();

        return DefaultState.getInstance();
    }

    @Override
    public IState resetPoints() {
        endCurrentAction();

        getImageLoader().setRenderRoute(null);
        getSidebarView().setRouteLength(0);
        getSidebarView().setStartable(false);
        getSidebarView().setResettable(false);
        getList().clear();

        return DefaultState.getInstance();
    }

    @Override
    public IState selectPoint(final IRoutePoint point) {
        if (point != getStore().getPoint()) {
            endCurrentAction();

            getStore().setPoint(point);
            getStore().storePoint();
            point.setState(State.editing);
            getSidebarView().setPointOrderChangable(true);

            return EditingState.getInstance();
        }

        return this;
    }

    @Override
    public IState setAddressText(final String address) {
        getStore().setCurrentAddress(address);

        final IRoutePoint point = getStore().getPoint();
        final Set<String> set = getStore().getSuggestions();

        if ((point == null || !address.equals(point.getAddress())) && !set.contains(address)) {
            final List<String> suggestions = getTextProcessor().suggest(address);
            set.clear();
            set.addAll(suggestions);
            set.add("");
            getSidebarView().setAddressSuggestions(suggestions);
            return getTextedState();
        }

        return this;
    }

    @Override
    public IState locatePoint(final int x, final int y) {
        final AddressPoint node = getMap().getAddress(x, y);
        if (node != null) {
            final IRoutePoint routePoint = getPoint();

            routePoint.setAddressPoint(node);
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
