package controller;

import java.awt.Point;
import java.awt.event.MouseEvent;

import controller.stateMachine.IStateMachine;
import model.IApplication;
import model.map.IMap;
import model.targets.IRoutePoint;
import model.targets.IRoutePoint.State;
import view.IDragListener;
import view.IMapView;

public class PointListener implements IDragListener {

    private static final int DRAG_MOVE_AREA = 40;
    private static final int MAX_DRAG_MOVEMENT = 20;

    private final IApplication application;
    private final IStateMachine machine;
    private final IMapView view;
    private final DragThread thread;

    private Point start;
    private boolean dragged;
    private IRoutePoint point;

    public PointListener(final IApplication application, final IStateMachine machine, final IMapView view) {
        this.machine = machine;
        this.application = application;
        this.view = view;
        this.thread = new DragThread();

        thread.start();
    }

    public void setRoutePoint(final IRoutePoint point) {
        this.point = point;
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        if (start != null) {
            if (point.getState() != State.added) {
                final int x = e.getX();
                final int y = e.getY();

                final IMap map = application.getMap();
                point.setAddressPoint(null);
                point.setLocation(x + map.getX() - map.getWidth() / 2, y + map.getY() - map.getHeight() / 2);

                final int xMovement;
                final int yMovement;

                if (x < DRAG_MOVE_AREA) {
                    xMovement = Math.max(-MAX_DRAG_MOVEMENT, (x - DRAG_MOVE_AREA) / 3);
                } else if (x > view.getWidth() - DRAG_MOVE_AREA) {
                    xMovement = Math.min(MAX_DRAG_MOVEMENT, (x + DRAG_MOVE_AREA - view.getWidth()) / 3);
                } else {
                    xMovement = 0;
                }

                if (y < DRAG_MOVE_AREA) {
                    yMovement = Math.max(-MAX_DRAG_MOVEMENT, (y - DRAG_MOVE_AREA) / 3);
                } else if (y > view.getHeight() - DRAG_MOVE_AREA) {
                    yMovement = Math.min(MAX_DRAG_MOVEMENT, (y + DRAG_MOVE_AREA - view.getHeight()) / 3);
                } else {
                    yMovement = 0;
                }

                thread.setMapMovement(xMovement, yMovement);

                start = e.getPoint();
                dragged = true;
            }
        }
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            machine.selectPoint(point);
            if (e.getClickCount() == 2) {
                final int zoom = application.getMap().getZoom();
                application.getMap().center(point.getX(zoom), point.getY(zoom));
                application.getImageLoader().update();
            }
        }
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (e.getClickCount() == 1) {
                machine.selectPoint(point);
            }

            thread.setPoint(point);
            start = e.getPoint();
        }
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        if (dragged) { // Check whether dragged before
            if (point.getState() != State.added) {
                final int zoom = application.getMap().getZoom();
                machine.locatePoint(point.getX(zoom), point.getY(zoom));
            }
            thread.setPoint(null);

            start = null;
            dragged = false;
        }
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
    }

    @Override
    public void mouseExited(final MouseEvent e) {
    }

    private class DragThread extends Thread {
        private int x;
        private int y;
        private IRoutePoint point;

        @Override
        public void run() {
            while (!isInterrupted()) {
                if ((x != 0 || y != 0) && (point.getState() == IRoutePoint.State.editing
                        || point.getState() == IRoutePoint.State.unadded)) {
                    application.getMap().move(x, y);
                    application.getImageLoader().update();
                    try {
                        Thread.sleep(10);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                        interrupt();
                    }
                } else {
                    synchronized (this) {
                        try {
                            wait();
                        } catch (final InterruptedException e) {
                            e.printStackTrace();
                            interrupt();
                        }
                    }
                }
            }
        }

        public void setPoint(final IRoutePoint point) {
            setMapMovement(0, 0);
            this.point = point;
        }

        public void setMapMovement(final int x, final int y) {
            this.x = x;
            this.y = y;
            if (x != 0 || y != 0) {
                synchronized (this) {
                    notify();
                }
            }
        }
    }
}