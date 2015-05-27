package controller.stateMachine;

import model.targets.IRoutePoint;

class UnaddedState extends AbstractActionState {

    private static final UnaddedState state = new UnaddedState();

    public static UnaddedState getInstance() {
        return state;
    }

    private UnaddedState() {

    }

    @Override
    public IState addPoint() {
        getPointStateMachine().addPoint();
        getImageLoader().setRenderRoute(null);
        getSidebarView().setRouteLength(0);
        getSidebarView().setAddable(false);

        return DefaultState.getInstance();
    }

    @Override
    public IState cancel() {
        endCurrentAction();

        return DefaultState.getInstance();
    }

    @Override
    protected IRoutePoint getPoint() {
        return getStore().getPoint();
    }

    @Override
    protected IState getLocatedState() {
        return this;
    }

    @Override
    protected IState getTextedState() {
        return UnaddedTextedState.getInstance();
    }

    @Override
    protected void endCurrentAction() {
        final IRoutePoint point = getStore().getPoint();
        point.setState(null);
        getList().remove(point);
        getSidebarView().setAddable(false);
    }

    @Override
    protected void cancelPointRelocation() {
        getStore().getPoint().setLocation(null);
    }

    @Override
    public void entry() {
        getSidebarView().setAddable(true);
    }
}
