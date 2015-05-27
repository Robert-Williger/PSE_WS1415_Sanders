package controller.stateMachine;

import model.IApplication;
import view.IApplicationView;

class PointStateMachine extends AbstractStateMachine {

    public PointStateMachine() {

    }

    @Override
    protected IState getStartState() {
        return NoPointState.getInstance();
    }

    @Override
    protected void initialize(final IApplication model, final IApplicationView view) {
    }
}
