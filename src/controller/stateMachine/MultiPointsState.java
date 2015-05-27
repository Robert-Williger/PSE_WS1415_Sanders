package controller.stateMachine;

import model.targets.IRoutePoint;

class MultiPointsState extends AbstractPointState {

    private static final MultiPointsState state = new MultiPointsState();

    private boolean tspEnabled;

    public static MultiPointsState getInstance() {
        return state;
    }

    private MultiPointsState() {

    }

    @Override
    public IState addPoint() {
        add();
        return this;
    }

    @Override
    public IState removePoint(final IRoutePoint point) {
        remove(point);
        return getSize() > 2 ? this : TwoPointsState.getInstance();
    }

    @Override
    public IState resetPoints() {
        reset();
        return NoPointState.getInstance();
    }

    @Override
    public IState startCalculation() {
        return this;
    }

    @Override
    public IState setTSPEnabled(final boolean enabled) {
        tspEnabled = enabled;
        getRouteManager().setTSPEnabled(enabled);
        return this;
    }

    @Override
    public void entry() {
        getRouteManager().setTSPEnabled(tspEnabled);
        getSidebarView().setTSPChangeable(true);
        getSidebarView().setResettable(true);
        getSidebarView().setStartable(true);
    }

    @Override
    public void exit() {
        getSidebarView().setTSPChangeable(false);
        getRouteManager().setTSPEnabled(false);
    }
}
