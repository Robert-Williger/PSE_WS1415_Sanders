package controller.stateMachine;

import model.renderEngine.IRenderRoute;
import view.ISidebarView;

public class CalculatingState extends AbstractState {

    private static final CalculatingState state = new CalculatingState();

    private IRenderRoute route;

    public static CalculatingState getInstance() {
        return state;
    }

    private CalculatingState() {

    }

    @Override
    public IState startCalculation() {
        route = getRouteManager().calculateRoute();

        return DefaultState.getInstance();
    }

    @Override
    public IState cancelCalculation() {
        getRouteManager().cancelCalculation();

        return DefaultState.getInstance();
    }

    @Override
    public void entry() {
        final ISidebarView sidebar = getSidebarView();
        getApplicationView().setCalculating(true);
        sidebar.setTSPChangeable(false);
        sidebar.setCancelable(false);
        sidebar.setStartable(false);
        sidebar.setResettable(false);
    }

    @Override
    public void exit() {
        final ISidebarView sidebar = getSidebarView();

        if (route != null) {
            sidebar.setRouteLength(route.getLength());
            getImageLoader().setRenderRoute(route);
            getMap().center(route.getBounds());
            getImageLoader().update();
            route = null;
        }

        getApplicationView().setCalculating(false);
        sidebar.setTSPChangeable(getList().getSize() > 2);
        sidebar.setPOIChangeable(true);
        sidebar.setStartable(true);
        sidebar.setResettable(true);
    }
}
