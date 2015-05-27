package controller.stateMachine;

import model.targets.IRoutePoint;

class UnaddedTextedState extends AbstractTextedState {

    private static final UnaddedTextedState state = new UnaddedTextedState();

    public static UnaddedTextedState getInstance() {
        return state;
    }

    private UnaddedTextedState() {

    }

    @Override
    protected IRoutePoint getPoint() {
        return getStore().getPoint();
    }

    @Override
    protected void endCurrentAction() {
        final IRoutePoint point = getStore().getPoint();
        point.setState(null);
        getList().remove(point);
    }

    @Override
    protected void cancelPointRelocation() {
        getStore().getPoint().setLocation(null);
    }

}
