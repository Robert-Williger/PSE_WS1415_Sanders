package controller.stateMachine;

import model.targets.IRoutePoint;

class DefaultState extends AbstractActionState {

    private static final DefaultState state = new DefaultState();

    public static DefaultState getInstance() {
        return state;
    }

    private DefaultState() {

    }

    @Override
    protected IRoutePoint getPoint() {
        final IRoutePoint point = getRouteManager().createPoint();
        getStore().setPoint(point);
        getList().add(point);

        return point;
    }

    @Override
    public void entry() {
        getStore().setPoint(null);
        getSidebarView().setCancelable(false);
        getSidebarView().clearTextField();
    }

    @Override
    protected IState getLocatedState() {
        return UnaddedState.getInstance();
    }

    @Override
    protected IState getTextedState() {
        return TextedState.getInstance();
    }

    @Override
    protected void endCurrentAction() {
    }

    @Override
    protected void cancelPointRelocation() {
    }

}
