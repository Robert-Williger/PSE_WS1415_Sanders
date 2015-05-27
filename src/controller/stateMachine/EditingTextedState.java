package controller.stateMachine;

class EditingTextedState extends AbstractEditingTextedState {

    private static final EditingTextedState state = new EditingTextedState();

    public static EditingTextedState getInstance() {
        return state;
    }

    private EditingTextedState() {

    }

    @Override
    protected IState getChangedState() {
        return EditingChangedTextedState.getInstance();
    }
}
