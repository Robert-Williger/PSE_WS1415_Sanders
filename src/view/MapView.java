package view;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JPanel;
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

    private final MapLayer mapLayer;
    private final PointLayer pointLayer;

    private final IMapListener mapListener;
    private final ChangeListener mapChangeListener;
    private final IPointListListener pointListListener;

    private final ZoomInfo zoomInfo;

    private final SmoothMapMover smoothMapMover;

    private IMap map;
    private IPointList list;

    public MapView(final IImageLoader loader, final IPointList list, final IMap map) {
        zoomInfo = new ZoomInfo();
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
                smoothMapMover.zoom(steps, deltaX, deltaY);
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
                add(new MapPaintingLayer(accessor, tileSize));
            }
        }
    }

    private class MapPaintingLayer extends JComponent {
        private static final long serialVersionUID = 1L;

        private IImageAccessor accessor;
        private int tileSize;

        public MapPaintingLayer(final IImageAccessor accessor, final int tileSize) {
            this.accessor = accessor;
            this.tileSize = tileSize;
            setOpaque(false);
        }

        @Override
        public void paint(final Graphics g) {
            if (accessor.isVisible()) {
                final int zoom = map.getZoom();
                final Graphics2D g2 = (Graphics2D) g;
                switch (zoomInfo.state) {
                    case ZoomInfo.ZOOMING:
                        paint(g2, zoom + zoomInfo.zoomOffset, zoomInfo.deltaX, zoomInfo.deltaY, zoomInfo.scale);
                        break;
                    case ZoomInfo.BLENDING:
                        paint(g2, map.getX(), map.getY(), zoom);
                        g2.setComposite(zoomInfo.composite);
                        paint(g2, zoom + zoomInfo.zoomOffset, zoomInfo.deltaX, zoomInfo.deltaY, zoomInfo.scale);
                        break;
                    default:
                        paint(g2, map.getX(), map.getY(), zoom);
                        break;
                }
            }
        }

        private void paint(final Graphics2D g, final int zoom, final double deltaX, final double deltaY,
                final double scale) {
            final double width = (int) (map.getWidth() / scale);
            final double height = (int) (map.getHeight() / scale);

            final double x = map.getX(zoom) - deltaX + deltaX * zoomInfo.s / scale * zoomInfo.destScale;
            final double y = map.getY(zoom) - deltaY + deltaY * zoomInfo.s / scale * zoomInfo.destScale;

            g.scale(scale, scale);
            paint(g, x, y, width, height, zoom);
            g.scale(1 / scale, 1 / scale);
        }

        private void paint(final Graphics2D g, final int x, final int y, final int zoom) {
            paint(g, x, y, map.getWidth(), map.getHeight(), zoom);
        }

        private void paint(final Graphics2D g, final double x, final double y, final double width, final double height,
                final int zoom) {
            final double xOffset = (x - width / 2) % tileSize;
            final double yOffset = (y - height / 2) % tileSize;
            final int startColumn = (int) ((x - width / 2) / tileSize);
            final int startRow = (int) ((y - height / 2) / tileSize);
            final int endColumn = (int) ((x + width / 2) / tileSize);
            final int endRow = (int) ((y + height / 2) / tileSize);

            paint(g, zoom, xOffset, yOffset, startRow, endRow, startColumn, endColumn);
        }

        private void paint(final Graphics2D g, final int zoom, final double xOffset, final double yOffset,
                final int startRow, final int endRow, final int startColumn, final int endColumn) {
            g.translate(-xOffset, -yOffset);
            int x;
            int y = 0;
            for (int row = startRow; row <= endRow; row++) {
                x = 0;
                for (int column = startColumn; column <= endColumn; column++) {
                    g.drawImage(accessor.getImage(row, column, zoom), x, y, this);
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
            final Graphics2D g2 = (Graphics2D) g;
            switch (zoomInfo.state) {
                case ZoomInfo.ZOOMING:
                    paint(g2, zoomInfo.deltaX, zoomInfo.deltaY, zoomInfo.scale);
                    break;
                default:
                    paint(g2, map.getX(), map.getY());
                    break;
            }
        }

        private void paint(final Graphics2D g, final int x, final int y) {
            for (final IRoutePoint point : list) {
                paintPoint(g, point, x, y);
            }
        }

        private void paint(final Graphics2D g, final double deltaX, final double deltaY, final double scale) {
            final int sZoom = map.getZoom() + zoomInfo.zoomOffset;
            final int dZoom = map.getZoom();
            final double s = zoomInfo.s;

            // 1 -> 0.5
            // interpolate map position linear
            final double mapX = (1 - s) * (map.getX(sZoom) - (int) deltaX) + s * map.getX(dZoom);
            final double mapY = (1 - s) * (map.getY(sZoom) - (int) deltaY) + s * map.getY(dZoom);

            for (final IRoutePoint point : list) {
                // interpolate point position linear
                final double pointX = (1 - s) * point.getX(sZoom) + s * point.getX(dZoom);
                final double pointY = (1 - s) * point.getY(sZoom) + s * point.getY(dZoom);
                routePointView.paint(g, point.getState(), Integer.toString(point.getTargetIndex() + 1),
                        pointX - mapX + map.getWidth() / 2 - POINT_DIAMETER / 2,
                        pointY - mapY + map.getHeight() / 2 - POINT_DIAMETER / 2);
            }
        }

        private void paintPoint(final Graphics2D g, final IRoutePoint point, final int x, final int y) {
            final int zoom = map.getZoom();
            routePointView.paint(g, point.getState(), Integer.toString(point.getTargetIndex() + 1),
                    point.getX(zoom) - x + map.getWidth() / 2 - POINT_DIAMETER / 2,
                    point.getY(zoom) - y + map.getHeight() / 2 - POINT_DIAMETER / 2);
        }

        public IRoutePoint getRoutePoint(final int x, final int y) {
            for (final IRoutePoint point : list) {
                final int xDist = x - point.getX(map.getZoom());
                final int yDist = y - point.getY(map.getZoom());
                if (xDist * xDist + yDist * yDist <= POINT_RADIUS_SQUARE) {
                    return point;
                }
            }

            return null;
        }
    }

    private class SmoothMapMover extends Thread {
        private final int SLEEP_TIME = 10;
        private final int MAX_STEPS = 22;
        private double scalePerStep;
        private int currentStep;

        private final Runnable[] runnables;
        private final AlphaComposite[] composites;

        public SmoothMapMover() {
            currentStep = 2 * MAX_STEPS;
            runnables = createRunnables();
            composites = createComposites();
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                if (currentStep < 2 * MAX_STEPS) {
                    SwingUtilities.invokeLater(runnables[currentStep++]);
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (final InterruptedException e) {
                    }
                } else {
                    zoomInfo.state = ZoomInfo.DEFAULT;
                    zoomInfo.scale = 1;
                    zoomInfo.s = 0;
                    SwingUtilities.invokeLater(() -> repaint());
                    synchronized (this) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }

        public void zoom(final int steps, final double deltaX, final double deltaY) {
            synchronized (this) {
                zoomInfo.zoomOffset = steps > 0 ? -1 : 1;
                zoomInfo.destScale = steps > 0 ? 2 : 0.5;
                zoomInfo.deltaX = deltaX;
                zoomInfo.deltaY = deltaY;
                zoomInfo.state = ZoomInfo.ZOOMING;
                // TODO respect multiple steps
                scalePerStep = steps > 0 ? 1.0 / MAX_STEPS : -0.5 / MAX_STEPS;
                currentStep = 0;

                notify();
            }
        }

        private Runnable[] createRunnables() {
            final Runnable[] ret = new Runnable[2 * MAX_STEPS];
            for (int i = 0; i < MAX_STEPS; i++) {
                final int step = i;
                ret[i] = () -> {
                    zoomInfo.scale = 1 + scalePerStep * (step + 1);
                    zoomInfo.s = (double) (step + 1) / MAX_STEPS;
                    repaint();
                };
                ret[i + MAX_STEPS] = () -> {
                    zoomInfo.composite = composites[step];

                    repaint();
                };
            }
            ret[MAX_STEPS] = () -> {
                zoomInfo.state = ZoomInfo.BLENDING;
                zoomInfo.composite = composites[0];

                repaint();
            };
            return ret;
        }

        private AlphaComposite[] createComposites() {
            final AlphaComposite[] ret = new AlphaComposite[MAX_STEPS];
            for (int i = 0; i < MAX_STEPS; i++) {
                final float alpha = 1 - 1.0f / MAX_STEPS * (i + 1);
                ret[i] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            }
            return ret;
        }
    }

    private class ZoomInfo {
        private int zoomOffset;
        private double scale;
        private double destScale;
        private double deltaX;
        private double deltaY;

        // factor for linear interpolation for state = ZOOMING; s grows from 0 to 1
        private double s;

        private AlphaComposite composite;
        private int state;

        private static final int ZOOMING = 0;
        private static final int BLENDING = 1;
        private static final int DEFAULT = 2;
    }
}