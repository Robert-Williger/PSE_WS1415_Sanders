package controller;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import controller.stateMachine.IStateMachine;
import model.IApplication;
import model.map.IMap;
import view.IDragListener;

public class MapListener implements IDragListener {
    private final IApplication application;
    private final IStateMachine machine;
    private final Point startPoint;
    private final Point movement;
    private final DragThread thread;

    private long startTime;
    private long elapsedTime;
    private boolean pressed;

    public MapListener(final IApplication application, final IStateMachine machine) {
        this.application = application;
        this.machine = machine;
        this.thread = new DragThread();
        this.startPoint = new Point();
        this.movement = new Point();

        thread.start();
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        if (pressed) {
            movement.setLocation(startPoint.x - e.getX(), startPoint.y - e.getY());
            application.getMap().move(movement.x, movement.y);
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
            final IMap map = application.getMap();
            machine.locatePoint(e.getX() + map.getX() - map.getWidth() / 2,
                    e.getY() + map.getY() - map.getHeight() / 2);
        }
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            pressed = true;
            movement.setLocation(0, 0);
            startTime = System.nanoTime();
            startPoint.setLocation(e.getX(), e.getY());
            thread.haltSpeed();
        }
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            pressed = false;
            if (elapsedTime != 0) {
                thread.setSpeed((int) (movement.getX() / elapsedTime * 10_000_000),
                        (int) (movement.getY() / elapsedTime * 10_000_000));
            }
        }
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
    }

    @Override
    public void mouseExited(final MouseEvent e) {
    }

    private class DragThread extends Thread {
        private static final int MIN_SMOOTH_DRAG_SPEED = 5;

        private final Vector vector;

        public DragThread() {
            vector = new Vector();
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                if (vector.getLength() > 0.1) {

                    SwingUtilities.invokeLater(() -> {
                        vector.scale(0.95);
                        application.getMap().move(vector.getX(), vector.getY());
                        application.getImageLoader().update();
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

    class Vector {

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