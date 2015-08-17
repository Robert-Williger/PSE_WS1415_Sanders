package view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.map.IMap;
import model.renderEngine.IImageAccessor;
import model.renderEngine.IImageLoader;
import model.targets.IPointList;
import model.targets.IPointListListener;
import model.targets.IRoutePoint;

public class MapView extends JPanel implements IMapView {
    private static final long serialVersionUID = 1L;

    private final JViewport viewport;
    private final MapLayer mapLayer;
    private final PointLayer pointLayer;

    private final ChangeListener mapListener;
    private final IPointListListener pointListListener;

    private IMap map;

    public MapView(final IImageLoader loader, final IPointList list, final IMap map) {
        viewport = new JViewport();
        pointLayer = new PointLayer();
        mapLayer = new MapLayer();

        mapListener = new ChangeListener() {

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

        setModels(loader, list, map);
        initialize();
    }

    public void setModels(final IImageLoader loader, final IPointList list, final IMap map) {
        this.map = map;
        mapLayer.setLoader(loader);
        pointLayer.reset();
        list.addPointListListener(pointListListener);
        map.addChangeListener(mapListener);

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
    public void addMapListener(final IMapListener listener) {
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

    private class MapLayer extends JPanel {
        private static final long serialVersionUID = 1L;

        private IImageLoader loader;
        private final PaintingLayer background;
        private final PaintingLayer route;
        private final PaintingLayer pois;

        private int tileWidth;
        private int tileHeight;

        public MapLayer() {
            super(new FullLayout());
            setOpaque(false);

            final ChangeListener listener = new ChangeListener() {

                @Override
                public void stateChanged(final ChangeEvent e) {
                    MapView.this.repaint();
                }

            };
            background = new PaintingLayer(listener);
            route = new PaintingLayer(listener);
            pois = new PaintingLayer(listener);

            add(pois);
            add(route);
            add(background);
        }

        public void setLoader(final IImageLoader loader) {
            this.loader = loader;

            final Image image = loader.getBackgroundAccessor().getImage(0, 0);
            tileWidth = image.getWidth(null);
            tileHeight = image.getHeight(null);

            background.setImageAccessor(loader.getBackgroundAccessor());
            route.setImageAccessor(loader.getRouteAccessor());
            pois.setImageAccessor(loader.getPOIAccessor());
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(loader.getBackgroundAccessor().getColumns() * tileWidth, loader
                    .getBackgroundAccessor().getRows() * tileHeight);
        }
    }

    private class PaintingLayer extends JComponent {
        private static final long serialVersionUID = 1L;

        private final ChangeListener listener;

        private IImageAccessor imageAccessor;
        private int tileWidth;
        private int tileHeight;

        public PaintingLayer(final ChangeListener listener) {
            this.listener = listener;
            setOpaque(false);
        }

        public void setImageAccessor(final IImageAccessor imageAccessor) {
            this.imageAccessor = imageAccessor;

            final Image image = imageAccessor.getImage(0, 0);
            tileWidth = image.getWidth(null);
            tileHeight = image.getHeight(null);

            imageAccessor.addChangeListener(listener);
        }

        @Override
        public void paint(final Graphics g) {
            if (imageAccessor.isVisible()) {
                for (int row = 0; row < imageAccessor.getRows(); row++) {
                    for (int column = 0; column < imageAccessor.getColumns(); column++) {
                        final Image image = imageAccessor.getImage(row, column);
                        g.drawImage(image, column * tileWidth, row * tileHeight, this);
                    }
                }
            }
        }
    }

    private class PointLayer extends JPanel {
        private static final long serialVersionUID = 1L;

        private final HashMap<IRoutePoint, RoutePointView> routePoints;

        public PointLayer() {
            routePoints = new HashMap<IRoutePoint, RoutePointView>();
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
                    MapView.this.processMouseWheelEvent(new MouseWheelEvent(MapView.this, e.getID(), e.getWhen(), e
                            .getModifiers(), e.getX() - viewport.getX(), e.getY() - viewport.getY(), e.getClickCount(),
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
}