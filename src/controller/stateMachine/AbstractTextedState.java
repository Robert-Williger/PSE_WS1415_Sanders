package controller.stateMachine;

import java.util.List;

import model.elements.StreetNode;
import model.targets.IRoutePoint;

abstract class AbstractTextedState extends AbstractActionState {

    @Override
    public IState searchPoint() {
        final String address = getStore().getCurrentAddress();
        final StreetNode node = getTextProcessor().parse(address);
        if (node != null) {
            final IRoutePoint point = getPoint();
            point.setAddress(address);
            point.setStreetNode(node);

            return UnaddedState.getInstance();
        }
        final List<String> suggestions = getTextProcessor().suggest(address);
        getSidebarView().setAddressSuggestions(suggestions);

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
    public IState cancel() {
        endCurrentAction();

        return DefaultState.getInstance();
    }

    @Override
    protected IState getTextedState() {
        return this;
    }

    @Override
    protected IState getLocatedState() {
        return UnaddedState.getInstance();
    }

}
