package controller.stateMachine;

class EditingChangedTextedState extends AbstractEditingTextedState {

    private static final EditingChangedTextedState state = new EditingChangedTextedState();

    public static EditingChangedTextedState getInstance() {
        return state;
    }

    private EditingChangedTextedState() {

    }

    @Override
    public IState cancel() {
        getStore().restorePoint();

        return super.cancel();
    }

    @Override
    protected IState getChangedState() {
        return this;
    }
}
