package controller.stateMachine;

class EditingChangedState extends AbstractEditingState {

    private static final EditingChangedState state = new EditingChangedState();

    public static EditingChangedState getInstance() {
        return state;
    }

    private EditingChangedState() {

    }

    @Override
    public IState cancel() {
        getStore().restorePoint();

        return super.cancel();
    }

    @Override
    public IState confirm() {
        endCurrentAction();

        return DefaultState.getInstance();
    }

    @Override
    public void exit() {
        getSidebarView().setConfirmable(false);
    }

    @Override
    public void entry() {
        getSidebarView().setConfirmable(true);
    }

    @Override
    protected IState getChangedState() {
        return this;
    }

    @Override
    protected IState getTextedState() {
        return EditingChangedTextedState.getInstance();
    }

    @Override
    protected void endCurrentAction() {
        getImageLoader().setRenderRoute(null);
        getSidebarView().setRouteLength(0);
        super.endCurrentAction();
    }
}
