package controller.stateMachine;

import javax.swing.SwingUtilities;

import model.IApplication;
import view.IApplicationView;

public class StateMachine extends AbstractStateMachine {

    public StateMachine(final IApplication model, final IApplicationView view) {
        super(model, view);
    }

    @Override
    protected IState getStartState() {
        return DefaultState.getInstance();
    }

    @Override
    protected void initialize(final IApplication model, final IApplicationView view) {
        view.getSidebar().setPointOrderChangable(false);
        view.getSidebar().setAddable(false);
    }

    @Override
    public void startCalculation() {
        super.startCalculation();
        state = CalculatingState.getInstance();

        new Thread() {
            @Override
            public void run() {
                final IState newState = state.startCalculation();
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        state.exit();
                        newState.entry();
                        state = newState;
                    }

                });
            }
        }.start();

    }
}
