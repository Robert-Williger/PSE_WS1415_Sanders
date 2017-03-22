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
    private int width;
    private int height;
    private int moveX;
    private int moveY;

    public MapController(final IMapView view, final IApplication application, final IStateMachine machine) {
        this.application = application;
        this.view = view;
        this.pointListener = new PointListener(application, machine, view);
        this.mapListener = new MapListener(application, machine);

        initialize(view);
    }

    private void initialize(final IMapView view) {

        width = view.getWidth();
        height = view.getHeight();

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
        final IRoutePoint point = view.getRoutePoint(e.getX(), e.getY());
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
        final IMap map = application.getMap();
        final int newWidth = view.getWidth();
        final int newHeight = view.getHeight();

        final int xDif = this.width - newWidth + moveX;
        final int yDif = this.height - newHeight + moveY;

        moveX = xDif % 2;
        moveY = yDif % 2;
        width = newWidth;
        height = newHeight;

        map.setSize(newWidth, newHeight);
        map.moveView(xDif / 2, yDif / 2);
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
        application.getMap().zoom(-e.getWheelRotation(), (double) e.getX() / view.getWidth(),
                (double) e.getY() / view.getHeight());
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