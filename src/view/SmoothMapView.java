package view;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.map.IMap;
import model.renderEngine.IImageAccessor;
import model.renderEngine.IImageLoader;
import model.targets.IPointList;
import model.targets.IPointListListener;
import model.targets.IRoutePoint;
import model.map.IMapListener;

public class SmoothMapView extends JPanel implements IMapView {
    private static final long serialVersionUID = 1L;

    private final ScalableViewport viewport;
    private final MapLayer mapLayer;
    private final PointLayer pointLayer;

    private final IMapListener mapListener;
    private final ChangeListener mapChangeListener;
    private final IPointListListener pointListListener;

    private final SmoothMapMover smoothMapMover;

    private IMap map;

    public SmoothMapView(final IImageLoader loader, final IPointList list, final IMap map) {
        viewport = new ScalableViewport(1);
        pointLayer = new PointLayer();
        mapLayer = new MapLayer();

        mapChangeListener = new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent e) {
                pointLayer.repaint();
                viewport.setViewPosition(SmoothMapView.this.map.getViewLocation());
            }

        };

        pointListListener = new IPointListListener() {

            @Override
            public void pointAdded(final IRoutePoint point) {
                pointLayer.add(point);
            }

            @Override
            public void pointRemoved(final IRoutePoint point) {
                pointLayer.remove(point);
            }

        };

        mapListener = new IMapListener() {

            @Override
            public void mapZoomed(final int steps, final double xOffset, final double yOffset) {
                viewport.setOffsets(xOffset, yOffset);
                smoothMapMover.zoom(steps);
            }

            @Override
            public void mapResized(final int width, final int height) {
                viewport.updateSize(width, height);
            }

            @Override
            public void mapMoved(final double deltaX, final double deltaY) {
            }

            @Override
            public void mapZoomInitiated(final int steps, final double xOffset, final double yOffset) {
                viewport.setZoomEnabled(true);
                viewport.setScale(1);
            }
        };

        smoothMapMover = new SmoothMapMover();
        smoothMapMover.start();

        setModels(loader, list, map);
        initialize();
    }

    public void setModels(final IImageLoader loader, final IPointList list, final IMap map) {
        this.map = map;
        mapLayer.setLoader(loader);
        pointLayer.reset();
        list.addPointListListener(pointListListener);
        map.addChangeListener(mapChangeListener);
        map.addMapListener(mapListener);

        viewport.setViewPosition(map.getViewLocation());
    }

    private void initialize() {
        setLayout(new FullLayout());
        setOpaque(false);

        viewport.setView(mapLayer);

        viewport.setOpaque(false);

        add(pointLayer);
        add(viewport);

    }

    @Override
    public void addMapListener(final view.IMapListener listener) {
        addKeyListener(listener);
        addMouseWheelListener(listener);
        addMouseListener(listener);
        addMouseMotionListener(listener);
    }

    @Override
    public void addRoutePointListener(final IDragListener listener) {
        pointLayer.addRoutePointListener(listener);
    }

    @Override
    public Image createScreenshot() {
        final BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

        paint(image.getGraphics());

        return image;
    }

    private class ScalableViewport extends JViewport {
        private static final long serialVersionUID = 1L;
        private double scale;
        private double xOffset;
        private double yOffset;
        private float alpha;
        private BufferedImage buffer;
        private boolean isZooming;
        private boolean isBlending;

        public ScalableViewport(final double scale) {
            this.scale = scale;
            // TODO initialize with reasonable size and grow if necessary.
            this.buffer = new BufferedImage(560, 581, BufferedImage.TYPE_INT_ARGB);
        }

        @Override
        public void paint(final Graphics g) {
            final Graphics2D g2 = (Graphics2D) g;

            if (isZooming) {
                clearComponent(g2);
                paintScaledImage(g2);
            } else if (isBlending) {
                paintNormalImage(g2);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                paintScaledImage(g2);
            } else {
                paintNormalImage(g2);
            }
        }

        public void setZoomEnabled(final boolean isZooming) {
            if (isZooming) {
                final Graphics2D g2 = buffer.createGraphics();
                g2.setColor(getBackground());
                g2.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
                paintNormalImage(g2);
                g2.dispose();
            }
            this.isZooming = isZooming;
        }

        public void setBlendingEnabled(final boolean isBlending) {
            this.isBlending = isBlending;
        }

        public void setScale(final double scale) {
            this.scale = scale;
            repaint();
        }

        public void setAlpha(final float alpha) {
            // avoid illegal alpha values cause of race-conditions.
            this.alpha = Math.max(0.f, Math.min(alpha, 1.0f));
            repaint();
        }

        public void setOffsets(final double xOffset, final double yOffset) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }

        public void updateSize(final int width, final int height) {
            if (width > buffer.getWidth() || height > buffer.getHeight()) {
                buffer = new BufferedImage((int) (width * 1.25), (int) (height * 1.25), BufferedImage.TYPE_INT_ARGB);
            }
        }

        private void clearComponent(final Graphics2D g2) {
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        private void paintNormalImage(final Graphics2D g2) {
            super.paint(g2);
        }

        private void paintScaledImage(final Graphics2D g2) {
            final double x;
            final double y;
            if (scale >= 1) {
                x = getWidth() * (1 - scale) * xOffset;
                y = getHeight() * (1 - scale) * yOffset;
            } else {
                x = getWidth() * (1 - scale) * (1.5 - 2 * xOffset);
                y = getHeight() * (1 - scale) * (1.5 - 2 * yOffset);
            }
            
            g2.translate(x, y);
            g2.scale(scale, scale);
            g2.setClip(0, 0, getWidth(), getHeight());
            g2.drawImage(buffer, 0, 0, null);
            g2.scale(1 / scale, 1 / scale);
            g2.translate(-x, -y);
        }

    }

    private class MapLayer extends JPanel {
        private static final long serialVersionUID = 1L;

        private IImageLoader loader;

        private int tileWidth;
        private int tileHeight;

        public MapLayer() {
            super(new FullLayout());
            setOpaque(false);
        }

        public void setLoader(final IImageLoader loader) {
            this.loader = loader;

            final Image image = loader.getImageAccessors().get(0).getImage(0, 0);
            tileWidth = image.getWidth(null);
            tileHeight = image.getHeight(null);

            removeAll();

            final ChangeListener listener = new ChangeListener() {

                @Override
                public void stateChanged(final ChangeEvent e) {
                    SmoothMapView.this.repaint();
                }

            };
            for (final IImageAccessor accessor : loader.getImageAccessors()) {
                final PaintingLayer layer = new PaintingLayer(accessor);
                accessor.addChangeListener(listener);
                add(layer);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(loader.getImageAccessors().get(0).getColumns() * tileWidth,
                    loader.getImageAccessors().get(0).getRows() * tileHeight);
        }
    }

    private class PaintingLayer extends JComponent {
        private static final long serialVersionUID = 1L;

        private IImageAccessor accessor;
        private int tileSize;

        public PaintingLayer(final IImageAccessor accessor) {
            this.accessor = accessor;
            final Image image = accessor.getImage(0, 0);
            tileSize = image.getWidth(null);

            setOpaque(false);
        }

        @Override
        public void paint(final Graphics g) {
            if (accessor.isVisible()) {
                for (int row = 0; row < accessor.getRows(); row++) {
                    for (int column = 0; column < accessor.getColumns(); column++) {
                        final Image image = accessor.getImage(row, column);
                        g.drawImage(image, column * tileSize, row * tileSize, this);
                        // g.drawRect(column * tileSize, row * tileSize, tileSize, tileSize);
                    }
                }
            }
        }
    }

    private class PointLayer extends JPanel {
        private static final long serialVersionUID = 1L;

        private final HashMap<IRoutePoint, RoutePointView> routePoints;

        public PointLayer() {
            routePoints = new HashMap<>();
            setOpaque(false);
            setLayout(null);
        }

        public void addRoutePointListener(final IDragListener listener) {
            listenerList.add(IDragListener.class, listener);
            for (final IRoutePointView view : routePoints.values()) {
                view.addRoutePointListener(listener);
            }
        }

        public void add(final IRoutePoint point) {
            final RoutePointView view = new RoutePointView(point);
            view.addMouseWheelListener(new MouseWheelListener() {

                @Override
                public void mouseWheelMoved(final MouseWheelEvent e) {
                    // TODO improve this?
                    SmoothMapView.this.processMouseWheelEvent(
                            new MouseWheelEvent(SmoothMapView.this, e.getID(), e.getWhen(), e.getModifiers(),
                                    e.getX() - viewport.getX(), e.getY() - viewport.getY(), e.getClickCount(),
                                    e.isPopupTrigger(), e.getScrollType(), e.getScrollAmount(), e.getWheelRotation()));
                }
            });
            add(view);
            routePoints.put(point, view);
            for (final IDragListener listener : listenerList.getListeners(IDragListener.class)) {
                view.addRoutePointListener(listener);
            }
            repaint();
        }

        public void remove(final IRoutePoint point) {
            final RoutePointView view = routePoints.get(point);
            remove(view);
            routePoints.remove(point);
            repaint();
        }

        public void reset() {
            removeAll();
            routePoints.clear();
        }
    }

    private class SmoothMapMover extends Thread {
        private final int SLEEP_TIME = 10;
        private final int MAX_STEPS = 20;
        private double scalePerSteps;
        private int currentSteps;

        public SmoothMapMover() {
            currentSteps = 2 * MAX_STEPS;
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                if (currentSteps < MAX_STEPS) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            viewport.setScale(1 + scalePerSteps * ++currentSteps);
                        }

                    });
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (currentSteps < 2 * MAX_STEPS) {
                    viewport.setZoomEnabled(false);
                    viewport.setBlendingEnabled(true);
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            viewport.setAlpha(1 - 1.0f / MAX_STEPS * (++currentSteps - MAX_STEPS));
                        }

                    });
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    viewport.setZoomEnabled(false);
                    viewport.setBlendingEnabled(false);
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            viewport.repaint();
                        }

                    });
                    synchronized (this)

                    {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        public void zoom(final int steps) {
            synchronized (this) {
                currentSteps = 0;
                if (steps > 0) {
                    scalePerSteps = 1.0 / MAX_STEPS;
                } else {
                    scalePerSteps = -0.5 / MAX_STEPS;
                }
                notify();
            }
        }
    }
}