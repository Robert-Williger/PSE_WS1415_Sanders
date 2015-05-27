package controller.stateMachine;

class EditingState extends AbstractEditingState {

    private static final EditingState state = new EditingState();

    public static EditingState getInstance() {
        return state;
    }

    private EditingState() {

    }

    @Override
    public void entry() {
        getSidebarView().setCancelable(true);
        getSidebarView().setPointOrderChangable(true);
    }

    @Override
    protected IState getTextedState() {
        return EditingTextedState.getInstance();
    }

    @Override
    protected IState getChangedState() {
        return EditingChangedState.getInstance();
    }

}
