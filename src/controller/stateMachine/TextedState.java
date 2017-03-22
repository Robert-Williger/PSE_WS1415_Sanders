package controller.stateMachine;

import model.targets.IRoutePoint;
import model.targets.RoutePoint;

class TextedState extends AbstractTextedState {

    private static final TextedState state = new TextedState();

    public static TextedState getInstance() {
        return state;
    }

    private TextedState() {

    }

    @Override
    public void entry() {
        super.entry();
        getSidebarView().setCancelable(true);
    }

    @Override
    public void exit() {
        getSidebarView().setSearchable(false);
    }

    @Override
    protected IRoutePoint getPoint() {
        final IRoutePoint point = new RoutePoint();
        getStore().setPoint(point);
        getList().add(point);

        return point;
    }

    @Override
    protected void cancelPointRelocation() {
    }

    @Override
    protected void endCurrentAction() {
    }

}
