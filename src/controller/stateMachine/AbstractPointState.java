package controller.stateMachine;

import model.targets.IRoutePoint;
import model.targets.PointState;

class AbstractPointState extends AbstractState {

    private static int size;

    protected void add() {
        final IRoutePoint point = getStore().getPoint();
        point.setLocation(null);
        point.setState(PointState.added);
        size++;
    }

    protected void remove(final IRoutePoint point) {
        if (getList().remove(point)) {
            getImageLoader().setRenderRoute(null);
            getSidebarView().setRouteLength(0);
            size--;
        }
    }

    protected void reset() {
        getImageLoader().setRenderRoute(null);
        getSidebarView().setRouteLength(0);
        getSidebarView().setStartable(false);
        getList().reset();
        size = 0;
    }

    protected int getSize() {
        return size;
    }
}
