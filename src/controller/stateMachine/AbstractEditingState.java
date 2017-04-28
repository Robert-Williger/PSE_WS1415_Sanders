package controller.stateMachine;

import model.targets.IPointList;
import model.targets.IRoutePoint;
import model.targets.IRoutePoint.State;

abstract class AbstractEditingState extends AbstractActionState {

    @Override
    public IState cancel() {
        final IRoutePoint point = getPoint();
        getSidebarView().setPointOrderChangable(false);

        point.setState(State.added);

        return DefaultState.getInstance();
    }

    @Override
    public IState movePointUp() {
        final IPointList list = getList();
        final int fromIndex = getPoint().getListIndex();
        final int toIndex;
        if (fromIndex != 0) {
            toIndex = fromIndex - 1;
        } else {
            toIndex = list.size() - 1;
        }

        return changeOrder(fromIndex, toIndex);
    }

    @Override
    public IState movePointDown() {
        final IPointList list = getList();
        final int fromIndex = getPoint().getListIndex();
        final int toIndex;
        if (fromIndex != list.size() - 1) {
            toIndex = fromIndex + 1;
        } else {
            toIndex = 0;
        }

        return changeOrder(fromIndex, toIndex);
    }

    @Override
    public IState changeOrder(final int fromIndex, final int toIndex) {
        getList().changeOrder(fromIndex, toIndex);

        return getChangedState();
    }

    @Override
    protected IRoutePoint getPoint() {
        return getStore().getPoint();
    }

    @Override
    protected IState getLocatedState() {
        return EditingChangedState.getInstance();
    }

    @Override
    protected void endCurrentAction() {
        final IRoutePoint point = getPoint();
        getSidebarView().setPointOrderChangable(false);

        point.setState(State.added);
    }

    protected abstract IState getChangedState();

    @Override
    protected void cancelPointRelocation() {
    }

}
