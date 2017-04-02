package controller.stateMachine;

import java.util.List;

import model.targets.AddressPoint;
import model.targets.IRoutePoint;

abstract class AbstractEditingTextedState extends AbstractEditingState {

    @Override
    public IState searchPoint() {
        final String address = getStore().getCurrentAddress();
        final AddressPoint addressPoint = getTextProcessor().parse(address);
        if (addressPoint != null) {
            final IRoutePoint point = getPoint();
            point.setAddressPoint(addressPoint);

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
