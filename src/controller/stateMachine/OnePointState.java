package controller.stateMachine;

import model.targets.IRoutePoint;

class OnePointState extends AbstractPointState {

    private static final OnePointState state = new OnePointState();

    public static OnePointState getInstance() {
        return state;
    }

    private OnePointState() {

    }

    @Override
    public IState addPoint() {
        add();
        getSidebarView().setStartable(true);
        return TwoPointsState.getInstance();
    }

    @Override
    public IState removePoint(final IRoutePoint point) {
        remove(point);
        return NoPointState.getInstance();
    }

    @Override
    public IState resetPoints() {
        reset();
        return NoPointState.getInstance();
    }
}
