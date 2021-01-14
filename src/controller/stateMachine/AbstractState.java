package controller.stateMachine;

import model.IApplication;
import model.addressIndex.IAddressMatcher;
import model.map.IMap;
import model.renderEngine.IImageLoader;
import model.routing.IRouteManager;
import model.targets.IPointList;
import model.targets.IRoutePoint;
import view.IApplicationView;
import view.ISidebarView;

class AbstractState implements IState {

    private static StateStore store = new StateStore();
    private static IApplication application;
    private static IApplicationView applicationView;

    public static void setApplication(final IApplication app) {
        application = app;
        store.setApplication(app);
    }

    public static void setApplicationView(final IApplicationView applicationView) {
        AbstractState.applicationView = applicationView;
        AbstractState.applicationView = applicationView;
    }

    protected StateStore getStore() {
        return store;
    }

    protected IApplication getApplication() {
        return application;
    }

    protected IImageLoader getImageLoader() {
        return application.getImageLoader();
    }

    protected IMap getMap() {
        return application.getMap();
    }

    protected IApplicationView getApplicationView() {
        return applicationView;
    }

    protected ISidebarView getSidebarView() {
        return applicationView.getSidebar();
    }

    protected IRouteManager getRouteManager() {
        return application.getRouteManager();
    }

    protected IPointList getList() {
        return application.getRouteManager().getPointList();
    }

    protected IAddressMatcher getTextProcessor() {
        return application.getTextProcessing();
    }

    @Override
    public IState cancel() {
        return this;
    }

    @Override
    public IState confirm() {
        return this;
    }

    @Override
    public IState addPoint() {
        return this;
    }

    @Override
    public IState removePoint(final IRoutePoint point) {
        return this;
    }

    @Override
    public IState searchPoint() {
        return this;
    }

    @Override
    public IState movePointUp() {
        return this;
    }

    @Override
    public IState movePointDown() {
        return this;
    }

    @Override
    public IState resetPoints() {
        return this;
    }

    @Override
    public IState startCalculation() {
        return this;
    }

    @Override
    public IState cancelCalculation() {
        return this;
    }

    @Override
    public IState changeOrder(final int fromIndex, final int toIndex) {
        return this;
    }

    @Override
    public IState locatePoint(final int x, final int y) {
        return this;
    }

    @Override
    public IState setAddressText(final String adress) {
        return this;
    }

    @Override
    public IState selectPoint(final IRoutePoint point) {
        return this;
    }

    @Override
    public IState setRouteSolver(int solver) {
        return this;
    }

    @Override
    public void entry() {

    }

    @Override
    public void exit() {

    }
}
