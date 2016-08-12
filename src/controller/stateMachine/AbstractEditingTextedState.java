package controller.stateMachine;

import java.util.List;

import model.elements.AccessPoint;
import model.targets.IRoutePoint;

abstract class AbstractEditingTextedState extends AbstractEditingState {

    @Override
    public IState searchPoint() {
        final String address = getStore().getCurrentAddress();
        final AccessPoint accessPoint = getTextProcessor().parse(address);
        if (accessPoint != null) {
            final IRoutePoint point = getPoint();
            point.setAddress(address);
            point.setAccessPoint(accessPoint);

            return EditingChangedState.getInstance();
        }
        final List<String> suggestions = getTextProcessor().suggest(address);
        getApplicationView().getSidebar().setAddressSuggestions(suggestions);

        return this;
    }

    @Override
    public void entry() {
        getSidebarView().setSearchable(true);
    }

    @Override
    public void exit() {
        getSidebarView().setSearchable(false);
    }

    @Override
    protected IState getTextedState() {
        return this;
    }
}
