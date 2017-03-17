package controller.stateMachine;

import java.awt.Rectangle;

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
            final Rectangle bounds = route.getBounds();
            getMap().center(bounds.getX(), bounds.getY(), bounds.getHeight(), bounds.getWidth());
            getImageLoader().update();
            route = null;
        }

        sidebar.setStartable(true);
        sidebar.setResettable(true);
    }
}
