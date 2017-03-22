package view;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

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

public class MapView extends JPanel implements IMapView {
    private static final long serialVersionUID = 1L;

    private final ScalableViewport viewport;
    private final MapLayer mapLayer;
    private final PointLayer pointLayer;

    private final IMapListener mapListener;
    private final ChangeListener mapChangeListener;
    private final IPointListListener pointListListener;

    private final SmoothMapMover smoothMapMover;

    private IMap map;
    private IPointList list;

    public MapView(final IImageLoader loader, final IPointList list, final IMap map) {
        viewport = new ScalableViewport(1);
        pointLayer = new PointLayer();
        mapLayer = new MapLayer();

        mapChangeListener = new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent e) {
                pointLayer.repaint();
                viewport.setViewPosition(MapView.this.map.getViewLocation());
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
        this.list = list;
        mapLayer.setLoader(loader);
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
        addComponentListener(listener);
    }

    @Override
    public Image createScreenshot() {
        final BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

        paint(image.getGraphics());

        return image;
    }

    @Override
    public IRoutePoint getRoutePoint(final int x, final int y) {
        return pointLayer.getRoutePoint(x, y);
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
            this.alpha = alpha;
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
                    MapView.this.repaint();
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
        private static final int POINT_DIAMETER = 19;
        private static final int POINT_RADIUS_SQUARE = (int) ((POINT_DIAMETER / 2.0) * (POINT_DIAMETER / 2.0));

        private final RoutePointViewFlyweight routePointView;

        public PointLayer() {
            setOpaque(false);
            routePointView = new RoutePointViewFlyweight(POINT_DIAMETER);
        }

        @Override
        public void paint(final Graphics g) {
            super.paint(g);
            for (final IRoutePoint point : list) {
                routePointView.paint(g, point.getState(), Integer.toString(point.getTargetIndex() + 1),
                        point.getX() - POINT_DIAMETER / 2, point.getY() - POINT_DIAMETER / 2);
            }
        }

        public IRoutePoint getRoutePoint(final int x, final int y) {
            for (final IRoutePoint point : list) {
                final int xDist = x - point.getX();
                final int yDist = y - point.getY();
                if (xDist * xDist + yDist * yDist <= POINT_RADIUS_SQUARE) {
                    return point;
                }
            }

            return null;
        }

        public void add(final IRoutePoint point) {
            // final RoutePointView view = new RoutePointView(point);
            // view.addMouseWheelListener(new MouseWheelListener() {
            //
            // @Override
            // public void mouseWheelMoved(final MouseWheelEvent e) {
            // // TODO improve this?
            // SmoothMapView.this.processMouseWheelEvent(
            // new MouseWheelEvent(SmoothMapView.this, e.getID(), e.getWhen(), e.getModifiers(),
            // e.getX() - viewport.getX(), e.getY() - viewport.getY(), e.getClickCount(),
            // e.isPopupTrigger(), e.getScrollType(), e.getScrollAmount(), e.getWheelRotation()));
            // }
            // });
            // add(view);
            // routePoints.put(point, view);
            // for (final IDragListener listener : listenerList.getListeners(IDragListener.class)) {
            // view.addRoutePointListener(listener);
            // }
            point.addChangeListener((e) -> {
                repaint();
            });
            repaint();
        }

        public void remove(final IRoutePoint point) {
            // final RoutePointView view = routePoints.get(point);
            // remove(view);
            // routePoints.remove(point);
            repaint();
        }
    }

    private class SmoothMapMover extends Thread {
        private final int SLEEP_TIME = 10;
        private final int MAX_STEPS = 20;
        private double scalePerStep;
        private int currentStep;

        private final Runnable[] runnables;

        public SmoothMapMover() {
            currentStep = 2 * MAX_STEPS;
            runnables = createRunnables();
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                if (currentStep < 2 * MAX_STEPS) {
                    SwingUtilities.invokeLater(runnables[currentStep++]);
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    viewport.setZoomEnabled(false);
                    viewport.setBlendingEnabled(false);
                    synchronized (this) {
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
                currentStep = 0;
                if (steps > 0) {
                    scalePerStep = 1.0 / MAX_STEPS;
                } else {
                    scalePerStep = -0.5 / MAX_STEPS;
                }
                notify();
            }
        }

        private Runnable[] createRunnables() {
            final Runnable[] ret = new Runnable[2 * MAX_STEPS];
            for (int i = 0; i < MAX_STEPS; i++) {
                final int step = i + 1;
                ret[i] = () -> viewport.setScale(1 + scalePerStep * step);
                ret[i + MAX_STEPS] = () -> viewport.setAlpha(1 - 1.0f / MAX_STEPS * step);
            }
            ret[MAX_STEPS] = () -> {
                viewport.setZoomEnabled(false);
                viewport.setBlendingEnabled(true);
                viewport.setAlpha(1 - 1.0f / MAX_STEPS);
            };
            return ret;
        }
    }

}