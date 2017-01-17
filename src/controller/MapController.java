package controller;

import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.SwingUtilities;

import model.IApplication;
import model.targets.IRoutePoint;
import model.targets.PointState;
import view.IDragListener;
import view.IMapListener;
import view.IMapView;
import view.IRoutePointView;
import controller.stateMachine.IStateMachine;

public class MapController extends AbstractController<IMapView> {

    private static final int DRAG_MOVE_AREA = 40;
    private static final int MAX_DRAG_MOVEMENT = 20;

    private final DragMoveThread dragMove;
    private final SmoothDragThread smoothDrag;

    private final IStateMachine machine;

    private final IApplication application;

    public MapController(final IMapView view, final IApplication application, final IStateMachine machine) {
        this.machine = machine;
        this.application = application;
        dragMove = new DragMoveThread();
        smoothDrag = new SmoothDragThread();

        initialize(view);
    }

    private void initialize(final IMapView view) {
        dragMove.start();
        smoothDrag.start();

        view.addRoutePointListener(new IDragListener() {

            private Point start;
            private boolean dragged;

            @Override
            public void mouseDragged(final MouseEvent e) {
                if (start != null) {
                    final IRoutePointView pointView = (IRoutePointView) e.getSource();
                    final IRoutePoint point = pointView.getRoutePoint();
                    if (point.getState() != PointState.added) {
                        final int x = e.getX();
                        final int y = e.getY();
                        point.setLocation(x, y);

                        final int xMovement;
                        final int yMovement;

                        if (x < DRAG_MOVE_AREA) {
                            xMovement = Math.max(-MAX_DRAG_MOVEMENT, (x - DRAG_MOVE_AREA) / 3);
                        } else if (x > view.getSize().width - DRAG_MOVE_AREA) {
                            xMovement = Math.min(MAX_DRAG_MOVEMENT, (x + DRAG_MOVE_AREA - view.getSize().width) / 3);
                        } else {
                            xMovement = 0;
                        }

                        if (y < DRAG_MOVE_AREA) {
                            yMovement = Math.max(-MAX_DRAG_MOVEMENT, (y - DRAG_MOVE_AREA) / 3);
                        } else if (y > view.getSize().height - DRAG_MOVE_AREA) {
                            yMovement = Math.min(MAX_DRAG_MOVEMENT, (y + DRAG_MOVE_AREA - view.getSize().height) / 3);
                        } else {
                            yMovement = 0;
                        }

                        dragMove.setMapMovement(xMovement, yMovement);

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
                    // final IRoutePointView view = (IRoutePointView)
                    // e.getSource();
                    // TODO
                    // final IRoutePoint point = view.getRoutePoint();
                    if (e.getClickCount() == 2) {
                        // TODO
                        // application.getMap().center(point.getAccessPoint().getLocation());
                        application.getImageLoader().update();
                    }
                }
            }

            @Override
            public void mousePressed(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    final IRoutePointView view = (IRoutePointView) e.getSource();
                    final IRoutePoint point = view.getRoutePoint();

                    if (e.getClickCount() == 1) {
                        machine.selectPoint(point);
                    }

                    dragMove.setPoint(point);
                    start = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                if (dragged) { // Check whether dragged before
                    final IRoutePointView view = (IRoutePointView) e.getSource();
                    final IRoutePoint point = view.getRoutePoint();
                    if (point.getState() != PointState.added) {
                        machine.locatePoint(point.getX(), point.getY());
                    }
                    dragMove.setPoint(null);

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

        });

        view.addMapListener(new IMapListener() {

            private final Point startPoint;
            private final Point movement;
            private long startTime;
            private long elapsedTime;
            private boolean pressed;
            // private static final int SCALE_FACTOR = 10_000_000;

            {
                startPoint = new Point();
                movement = new Point();
            }

            @Override
            public void mouseDragged(final MouseEvent e) {
                if (pressed) {
                    movement.setLocation(startPoint.x - e.getX(), startPoint.y - e.getY());
                    application.getMap().moveView(movement.x, movement.y);
                    long time = System.nanoTime();
                    elapsedTime = time - startTime;
                    startTime = time;
                    application.getImageLoader().update();
                    startPoint.setLocation(e.getX(), e.getY());
                }

                // TODO scale smooth drag to sample rate of mouse!
            }

            @Override
            public void mouseMoved(final MouseEvent e) {
            }

            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    machine.locatePoint(e.getX(), e.getY());
                }
            }

            @Override
            public void mousePressed(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    pressed = true;
                    startTime = System.nanoTime();
                    startPoint.setLocation(e.getX(), e.getY());
                    smoothDrag.haltSpeed();
                }
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    pressed = false;
                    if (elapsedTime != 0) {
                        smoothDrag.setSpeed((int) (movement.getX() / elapsedTime * 10_000_000), (int) (movement.getY()
                                / elapsedTime * 10_000_000));
                    }
                }
            }

            @Override
            public void mouseEntered(final MouseEvent e) {
            }

            @Override
            public void mouseExited(final MouseEvent e) {
            }

            @Override
            public void mouseWheelMoved(final MouseWheelEvent e) {
                application.getMap().zoom(-e.getWheelRotation(), e.getPoint());
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

        });

        view.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(final ComponentEvent e) {
                application.getMap().setViewSize(view.getSize());
                application.getImageLoader().update();
            }

        });
    }

    private class DragMoveThread extends Thread {
        private int x;
        private int y;
        private IRoutePoint point;

        @Override
        public void run() {
            while (!isInterrupted()) {
                if ((x != 0 || y != 0)
                        && (point.getState() == PointState.editing || point.getState() == PointState.unadded)) {
                    application.getMap().moveView(x, y);
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

    private class SmoothDragThread extends Thread {
        private static final int MIN_SMOOTH_DRAG_SPEED = 5;

        private final Vector vector;
        private double x;
        private double y;

        public SmoothDragThread() {
            vector = new Vector();
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                if (vector.getLength() > 0.1) {

                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            vector.scale(0.95);
                            application.getMap().moveView((int) vector.getX() + (int) x, (int) vector.getY() + (int) y);
                            application.getImageLoader().update();

                            x += vector.getX() - (int) vector.getX() - (int) x;
                            y += vector.getY() - (int) vector.getY() - (int) y;
                        }
                    });

                    try {
                        Thread.sleep(10);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    synchronized (this) {
                        try {
                            wait();
                        } catch (final InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        public void haltSpeed() {
            vector.setDirection(0, 0);
        }

        public void setSpeed(final int x, final int y) {
            if (new Vector(x, y).getLength() >= MIN_SMOOTH_DRAG_SPEED) {
                vector.setDirection(x, y);
                synchronized (this) {
                    notify();
                }
            }
        }
    }

    private class Vector {

        private double x;
        private double y;

        public Vector() {
            setDirection(0, 0);
        }

        public Vector(final int x, final int y) {
            setDirection(x, y);
        }

        public void setDirection(final int x, final int y) {
            this.x = x;
            this.y = y;
        }

        public void scale(final double factor) {
            x *= factor;
            y *= factor;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getLength() {
            return Math.sqrt(x * x + y * y);
        }
    }

}