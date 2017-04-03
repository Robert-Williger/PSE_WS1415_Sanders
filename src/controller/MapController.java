package controller;

import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import model.IApplication;
import model.map.IMap;
import model.targets.IRoutePoint;
import view.IDragListener;
import view.IMapListener;
import view.IMapView;
import controller.stateMachine.IStateMachine;

public class MapController extends AbstractController<IMapView> implements IMapListener {
    private final IApplication application;
    private final IMapView view;

    private final PointListener pointListener;
    private final IDragListener mapListener;

    private IDragListener currentListener;

    public MapController(final IMapView view, final IApplication application, final IStateMachine machine) {
        this.application = application;
        this.view = view;
        this.pointListener = new PointListener(application, machine, view);
        this.mapListener = new MapListener(application, machine);

        view.addMapListener(this);
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        currentListener.mouseDragged(e);
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        currentListener.mouseClicked(e);
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        final IMap map = application.getMap();
        final IRoutePoint point = view.getRoutePoint(e.getX() + map.getX() - map.getWidth() / 2,
                e.getY() + map.getY() - map.getHeight() / 2);
        if (point == null) {
            currentListener = mapListener;
        } else {
            currentListener = pointListener;
            pointListener.setRoutePoint(point);
        }

        currentListener.mousePressed(e);
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        currentListener.mouseReleased(e);
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
    }

    @Override
    public void mouseExited(final MouseEvent e) {
    }

    @Override
    public void componentResized(final ComponentEvent e) {
        application.getMap().setSize(view.getWidth(), view.getHeight());
        application.getImageLoader().update();
    }

    @Override
    public void componentMoved(final ComponentEvent e) {
    }

    @Override
    public void componentShown(final ComponentEvent e) {
    }

    @Override
    public void componentHidden(final ComponentEvent e) {
    }

    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
        application.getMap().zoom(-e.getWheelRotation(), (double) e.getX() / view.getWidth() - 0.5,
                (double) e.getY() / view.getHeight() - 0.5);
        application.getImageLoader().update();
    }

    @Override
    public void keyTyped(final KeyEvent e) {
    }

    @Override
    public void keyPressed(final KeyEvent e) {
    }

    @Override
    public void keyReleased(final KeyEvent e) {
    }

}