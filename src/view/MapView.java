package view;

import java.awt.AlphaComposite;
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
import model.map.IMapListener;
import model.renderEngine.IImageAccessor;
import model.renderEngine.IImageLoader;
import model.targets.IPointList;
import model.targets.IPointListListener;
import model.targets.IRoutePoint;

public class MapView extends JPanel implements IMapView {
    private static final long serialVersionUID = 1L;

    // private final ScalableViewport viewport;
    private final MapLayer mapLayer;
    private final PointLayer pointLayer;

    private final IMapListener mapListener;
    private final ChangeListener mapChangeListener;
    private final IPointListListener pointListListener;

    private final SmoothChangeInformation smoothChangeInfo;

    private final SmoothMapMover smoothMapMover;

    private IMap map;
    private IPointList list;

    public MapView(final IImageLoader loader, final IPointList list, final IMap map) {
        // viewport = new ScalableViewport(1);
        smoothChangeInfo = new SmoothChangeInformation();
        pointLayer = new PointLayer();
        mapLayer = new MapLayer();

        mapChangeListener = new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent e) {
                pointLayer.repaint();
            }

        };

        pointListListener = new IPointListListener() {

            @Override
            public void pointAdded(final IRoutePoint point) {
                point.addChangeListener((e) -> {
                    pointLayer.repaint();
                });
                pointLayer.repaint();
            }

            @Override
            public void pointRemoved(final IRoutePoint point) {
                pointLayer.repaint();
            }

        };

        mapListener = new IMapListener() {

            @Override
            public void mapZoomed(final int steps, final double deltaX, final double deltaY) {
                smoothChangeInfo.isZooming = true;
                smoothChangeInfo.scale = 1;
                smoothChangeInfo.zoomOffset = steps > 0 ? -1 : 1;
                smoothChangeInfo.deltaX = deltaX;
                smoothChangeInfo.deltaY = deltaY;
                smoothMapMover.zoom(steps);
            }

            @Override
            public void mapResized(final int width, final int height) {
                // viewport.updateSize(width, height);
            }

            @Override
            public void mapMoved(final double deltaX, final double deltaY) {
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

        repaint();
    }

    private void initialize() {
        setLayout(new FullLayout());
        setOpaque(false);

        add(pointLayer);
        add(mapLayer);
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

        public MapLayer() {
            super(new FullLayout());
            setOpaque(false);
        }

        public void setLoader(final IImageLoader loader) {
            removeAll();

            final int tileSize = loader.getTileSize();
            for (final IImageAccessor accessor : loader.getImageAccessors()) {
                accessor.addChangeListener((e) -> repaint());
                add(new PaintingLayer(accessor, tileSize));
            }
        }
    }

    private class PaintingLayer extends JComponent {
        private static final long serialVersionUID = 1L;

        private IImageAccessor accessor;
        private int tileSize;

        public PaintingLayer(final IImageAccessor accessor, final int tileSize) {
            this.accessor = accessor;
            this.tileSize = tileSize;
            setOpaque(false);
        }

        @Override
        public void paint(final Graphics g) {
            if (accessor.isVisible()) {
                final int zoom = map.getZoom();
                final Graphics2D g2 = (Graphics2D) g;
                if (smoothChangeInfo.isZooming) {
                    paint(g2, zoom + smoothChangeInfo.zoomOffset);
                } else if (smoothChangeInfo.isBlending) {

                } else {
                    paint(g, map.getX(), map.getY(), zoom);
                }
            }
        }

        private void paint(final Graphics2D g, final int zoom) {
            final double scale = smoothChangeInfo.scale;

            double x = accessor.getX(zoom) - smoothChangeInfo.deltaX;
            double y = accessor.getY(zoom) - smoothChangeInfo.deltaY;
            final int width = (int) (map.getWidth() / scale);
            final int height = (int) (map.getHeight() / scale);

            if (scale >= 1) {
                x += smoothChangeInfo.deltaX * (scale - 1) / scale * 2;
                y += smoothChangeInfo.deltaY * (scale - 1) / scale * 2;
            } else {
                x += (-smoothChangeInfo.deltaX) * (scale - 1) / scale;
                y += (-smoothChangeInfo.deltaY) * (scale - 1) / scale;
            }

            g.scale(scale, scale);
            paint(g, (int) x, (int) y, width, height, zoom);
            g.scale(1 / scale, 1 / scale);
        }

        private void paint(final Graphics g, final int x, final int y, final int zoom) {
            paint(g, x, y, map.getWidth(), map.getHeight(), zoom);
        }

        private void paint(final Graphics g, final int x, final int y, final int width, final int height,
                final int zoom) {
            final int xOffset = (x - width / 2) % tileSize;
            final int yOffset = (y - height / 2) % tileSize;
            final int startColumn = (x - width / 2) / tileSize;
            final int startRow = (y - height / 2) / tileSize;
            final int endColumn = (x + width / 2) / tileSize;
            final int endRow = (y + height / 2) / tileSize;

            paint(g, zoom, xOffset, yOffset, startRow, endRow, startColumn, endColumn);
        }

        private void paint(final Graphics g, final int zoom, final int xOffset, final int yOffset, final int startRow,
                final int endRow, final int startColumn, final int endColumn) {
            g.translate(-xOffset, -yOffset);
            int x;
            int y = 0;
            for (int row = startRow; row <= endRow; row++) {
                x = 0;
                for (int column = startColumn; column <= endColumn; column++) {
                    g.drawImage(accessor.getImage(row, column, zoom), x, y, this);
                    g.drawRect(x, y, tileSize, tileSize);

                    x += tileSize;
                }
                y += tileSize;
            }

            g.translate(xOffset, yOffset);
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
                        point.getX() - map.getX() + map.getWidth() / 2 - POINT_DIAMETER / 2,
                        point.getY() - map.getY() + map.getHeight() / 2 - POINT_DIAMETER / 2);
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
                    smoothChangeInfo.isZooming = false;
                    smoothChangeInfo.isBlending = false;
                    SwingUtilities.invokeLater(() -> repaint());
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
                // TODO resprect multiple steps
                scalePerStep = steps > 0 ? 1.0 / MAX_STEPS : -0.5 / MAX_STEPS;
                notify();
            }
        }

        private Runnable[] createRunnables() {
            final Runnable[] ret = new Runnable[2 * MAX_STEPS];
            for (int i = 0; i < MAX_STEPS; i++) {
                final int step = i + 1;
                ret[i] = () -> {
                    smoothChangeInfo.scale = 1 + scalePerStep * step;
                    repaint();
                };
                ret[i + MAX_STEPS] = () -> {
                    smoothChangeInfo.alpha = 1 - 1.0f / MAX_STEPS * step;
                    repaint();
                };
            }
            ret[MAX_STEPS] = () -> {
                smoothChangeInfo.isZooming = false;
                smoothChangeInfo.isBlending = true;
                smoothChangeInfo.alpha = 1 - 1.0f / MAX_STEPS;
                repaint();
            };
            return ret;
        }
    }

    private class SmoothChangeInformation {
        private int zoomOffset;
        private double scale;
        private double deltaX;
        private double deltaY;
        private float alpha;
        private boolean isZooming;
        private boolean isBlending;
    }
}