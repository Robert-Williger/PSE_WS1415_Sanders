package controller.stateMachine;

import model.targets.IRoutePoint;

class TwoPointsState extends AbstractPointState {

    private static final TwoPointsState state = new TwoPointsState();

    public static TwoPointsState getInstance() {
        return state;
    }

    private TwoPointsState() {

    }

    @Override
    public IState addPoint() {
        add();
        return MultiPointsState.getInstance();
    }

    @Override
    public IState removePoint(final IRoutePoint point) {
        remove(point);
        getSidebarView().setStartable(false);
        return OnePointState.getInstance();
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
}
