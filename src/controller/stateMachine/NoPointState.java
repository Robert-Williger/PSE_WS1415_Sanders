package controller.stateMachine;

class NoPointState extends AbstractPointState {

    private static final NoPointState state = new NoPointState();

    public static NoPointState getInstance() {
        return state;
    }

    private NoPointState() {

    }

    @Override
    public IState addPoint() {
        add();
        return OnePointState.getInstance();
    }

    @Override
    public void entry() {
        getSidebarView().setResettable(false);
        getSidebarView().setTSPChangeable(false);
    }

    @Override
    public void exit() {
        getSidebarView().setResettable(true);
    }
}
