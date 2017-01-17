package controller.stateMachine;

import model.IApplication;
import model.targets.IRoutePoint;
import view.IApplicationView;

abstract class AbstractStateMachine implements IStateMachine {

    protected IState state;

    public AbstractStateMachine() {
        state = getStartState();
        state.entry();
    }

    public AbstractStateMachine(final IApplication model, final IApplicationView view) {
        AbstractState.setApplication(model);
        AbstractState.setApplicationView(view);
        initialize(model, view);
        state = getStartState();
        state.entry();
    }

    protected abstract void initialize(final IApplication model, final IApplicationView view);

    protected abstract IState getStartState();

    @Override
    public void cancel() {
        changeState(state.cancel());
    }

    @Override
    public void confirm() {
        changeState(state.confirm());
    }

    @Override
    public void startCalculation() {
        changeState(state.startCalculation());
    }

    @Override
    public void cancelCalculation() {
        changeState(state.cancelCalculation());
    }

    @Override
    public void addPoint() {
        changeState(state.addPoint());
    }

    @Override
    public void searchPoint() {
        changeState(state.searchPoint());
    }

    @Override
    public void movePointUp() {
        changeState(state.movePointUp());
    }

    @Override
    public void movePointDown() {
        changeState(state.movePointDown());
    }

    @Override
    public void resetPoints() {
        changeState(state.resetPoints());
    }

    @Override
    public void changeOrder(final int fromIndex, final int toIndex) {
        changeState(state.changeOrder(fromIndex, toIndex));
    }

    @Override
    public void selectPoint(final IRoutePoint point) {
        changeState(state.selectPoint(point));
    }

    @Override
    public void removePoint(final IRoutePoint point) {
        changeState(state.removePoint(point));
    }

    @Override
    public void locatePoint(final int x, final int y) {
        changeState(state.locatePoint(x, y));
    }

    @Override
    public void setAddressText(final String address) {
        changeState(state.setAddressText(address));
    }

    @Override
    public void setRouteSolver(int solver) {
        changeState(state.setRouteSolver(solver));
    }

    private void changeState(final IState newState) {
        if (state != newState) {
            state.exit();
            state = newState;
            state.entry();
        }
    }

}