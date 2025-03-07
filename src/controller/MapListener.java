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
    private final SmoothMover smoothMover;

    private long startTime;
    private long elapsedTime;
    private boolean pressed;

    public MapListener(final IApplication application, final IStateMachine machine) {
        this.application = application;
        this.machine = machine;
        this.smoothMover = new SmoothMover();
        this.startPoint = new Point();
        this.movement = new Point();

        smoothMover.start();
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
    public void mouseMoved(final MouseEvent e) {}

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
            smoothMover.haltSpeed();
        }
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            pressed = false;
            if (elapsedTime != 0) {
                smoothMover.setSpeed((int) (movement.getX() / elapsedTime * 10_000_000),
                        (int) (movement.getY() / elapsedTime * 10_000_000));
            }
        }
    }

    @Override
    public void mouseEntered(final MouseEvent e) {}

    @Override
    public void mouseExited(final MouseEvent e) {}

    private class SmoothMover extends Thread {
        private static final int MIN_SMOOTH_DRAG_SPEED = 5;
        private static final int MAX_SMOOTH_DRAG_SPEED = 150;

        private final Vector vector;
        private final Vector buffer;

        public SmoothMover() {
            vector = new Vector();
            buffer = new Vector();
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                if (vector.getLength() > 0.1) {

                    SwingUtilities.invokeLater(() -> {
                        vector.scale(0.95);
                        int moveX = (int) Math.round(vector.getX() + buffer.getX());
                        int moveY = (int) Math.round(vector.getY() + buffer.getY());
                        application.getMap().move(moveX, moveY);
                        buffer.setDirection(vector.getX() - (moveX - buffer.getX()),
                                vector.getY() - (moveY - buffer.getY()));
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
            buffer.setDirection(0, 0);
            vector.setDirection(0, 0);
        }

        public void setSpeed(final int x, final int y) {
            if (new Vector(x, y).getLength() >= MIN_SMOOTH_DRAG_SPEED) {
                vector.setDirection(x, y);
                final double length = vector.getLength();
                if (length > MAX_SMOOTH_DRAG_SPEED) {
                    vector.scale(MAX_SMOOTH_DRAG_SPEED / length);
                }
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
            this(1, 0);
        }

        public Vector(final double x, final double y) {
            setDirection(x, y);
        }

        public void setDirection(final double x, final double y) {
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