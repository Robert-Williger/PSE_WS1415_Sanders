package model.renderEngine;

import java.util.ArrayList;
import java.util.List;

import model.map.IMapManager;
import model.map.IMapSection;
import model.map.ITileState;
import model.map.accessors.ITileIdConversion;
import model.renderEngine.renderers.BackgroundRenderer;
import model.renderEngine.renderers.IRenderRoute;
import model.renderEngine.renderers.IRouteRenderer;
import model.renderEngine.renderers.LabelRenderer;
import model.renderEngine.renderers.POIRenderer;
import model.renderEngine.renderers.RouteRenderer;
import model.renderEngine.schemes.ColorScheme;
import model.renderEngine.schemes.GoogleColorScheme;
import model.renderEngine.threadPool.ThreadPool;

public class ImageLoader implements IImageLoader {

    private IMapManager mapManager;
    private ITileState tileState;
    private ITileIdConversion conversion;
    private IMapSection section;

    private final List<Layer> layers;

    private final IImageFetcher routeFetcher;
    private final IRouteRenderer routeRenderer;
    private final IImageAccessor routeAccessor;

    private final List<IImageAccessor> imageAccessors;

    private final DelayedLoader delayedLoader;

    private final ThreadPool pool;

    private int lastZoomStep;
    private int lastFirstRow;
    private int lastFirstColumn;
    private int lastLastRow;
    private int lastLastColumn;
    private final int prefetchCount = 1;

    public ImageLoader(final IMapManager mapManager) {
        conversion = mapManager.getTileIdConversion();
        routeRenderer = new RouteRenderer(mapManager);

        final ColorScheme colorScheme = new GoogleColorScheme();
        final int processors = Runtime.getRuntime().availableProcessors();
        pool = new ThreadPool(processors);

        final IImageFetcher backgroundFetcher = new ImageFetcher(new BackgroundRenderer(mapManager, colorScheme),
                mapManager, pool, 1024);
        final IImageFetcher POIFetcher = new ImageFetcher(new POIRenderer(mapManager), mapManager, pool, 128);
        routeFetcher = new ImageFetcher(routeRenderer, mapManager, pool, 128);
        final IImageFetcher labelFetcher = new ImageFetcher(new LabelRenderer(mapManager), mapManager, pool, 128);

        ImageAccessor backgroundAccessor = new ImageAccessor(conversion, backgroundFetcher, "Hintergrund");
        ImageAccessor POIAccessor = new ImageAccessor(conversion, POIFetcher, "Points of Interest");
        routeAccessor = new ImageAccessor(conversion, routeFetcher, "Route");
        ImageAccessor labelAccessor = new ImageAccessor(conversion, labelFetcher, "Beschriftung");

        imageAccessors = new ArrayList<>(4);

        imageAccessors.add(labelAccessor);
        imageAccessors.add(routeAccessor);
        imageAccessors.add(POIAccessor);
        imageAccessors.add(backgroundAccessor);

        layers = new ArrayList<>(4);
        layers.add(new Layer(labelFetcher, labelAccessor, false));
        layers.add(new Layer(routeFetcher, routeAccessor, false));
        layers.add(new Layer(POIFetcher, POIAccessor, false));
        layers.add(new Layer(backgroundFetcher, backgroundAccessor, true));

        setMapManager(mapManager);

        delayedLoader = new DelayedLoader();
        delayedLoader.start();
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        mapManager = manager;
        section = mapManager.getMapSection();
        tileState = mapManager.getTileState();
        conversion = mapManager.getTileIdConversion();

        // set zoomStep to -1, so on first update all tiles in current view will be rendered
        lastZoomStep = -1;
        lastFirstRow = 0;
        lastFirstColumn = 0;

        for (final Layer layer : layers) {
            layer.setMapManager(manager);
        }
    }

    @Override
    public int getTileSize() {
        return tileState.getTileSize();
    }

    @Override
    public void update() {
        if (section.getZoom() != lastZoomStep) {
            pool.flush();
        }

        for (final Layer layer : layers) {
            if (layer.lastVisible != layer.imageAccessor.isVisible()) {
                layer.lastVisible = layer.imageAccessor.isVisible();
                if (layer.lastVisible)
                    loadCurrentTiles(layer.imageFetcher);
            }
        }

        if (loadNewTiles()) {
            delayedLoader.moved = true;
            synchronized (delayedLoader) {
                delayedLoader.notify();
            }
        }
    }

    @Override
    public List<IImageAccessor> getImageAccessors() {
        return imageAccessors;
    }

    @Override
    public void setRenderRoute(final IRenderRoute route) {
        routeRenderer.setRenderRoute(route);
        routeFetcher.flush();

        routeAccessor.setVisible(route != null);

        loadCurrentTiles(routeFetcher);
    }

    private void loadTile(final IImageFetcher imageFetcher, final long id) {
        imageFetcher.loadImage(id, 0);
    }

    private void loadCurrentTiles(final IImageFetcher imageFetcher) {
        for (int i = lastFirstRow; i <= lastLastRow; i++) {
            for (int j = lastFirstColumn; j <= lastLastColumn; j++) {
                loadTile(imageFetcher, conversion.getId(i, j, lastZoomStep));
            }
        }
    }

    private boolean loadNewTiles() {
        boolean ret = false;

        final int zoom = section.getZoom();

        final int firstRow = tileState.getFirstRow(zoom);
        final int firstColumn = tileState.getFirstColumn(zoom);
        final int lastRow = tileState.getLastRow(zoom);
        final int lastColumn = tileState.getLastColumn(zoom);

        if (zoom != lastZoomStep) {
            loadTiles(firstRow, lastRow, firstColumn, lastColumn, zoom);
            ret = true;
        } else {
            int fromColumn = firstColumn;
            int toColumn = lastColumn;
            if (firstColumn < lastFirstColumn) {
                fromColumn = Math.min(lastColumn, lastFirstColumn);
                loadTiles(firstRow, lastRow, firstColumn, fromColumn, zoom);
                ret = true;
            }
            // no else if for resize of map view..
            if (lastColumn > lastLastColumn) {
                toColumn = Math.max(lastLastColumn, firstColumn);
                loadTiles(firstRow, lastRow, toColumn, lastColumn, zoom);
                ret = true;
            }
            if (firstRow < lastFirstRow) {
                loadTiles(firstRow, Math.min(lastRow, lastFirstRow), fromColumn, toColumn, zoom);
                ret = true;
            }
            if (lastRow > lastLastRow) {
                loadTiles(Math.max(lastLastRow, firstRow), lastRow, fromColumn, toColumn, zoom);
                ret = true;
            }
        }

        lastZoomStep = zoom;
        lastFirstRow = firstRow;
        lastFirstColumn = firstColumn;
        lastLastRow = lastRow;
        lastLastColumn = lastColumn;

        return ret;
    }

    private void loadTiles(final int r1, final int r2, final int c1, final int c2, final int zoom) {
        for (int y = r1; y <= r2; y++) {
            for (int x = c1; x <= c2; x++) {
                loadVisibleTile(conversion.getId(y, x, zoom));
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadBorderTiles(final int r1, final int r2, final int c1, final int c2, final int zoom) {
        for (int row = r1; row <= r2; row++) {
            for (int column = c1; column <= c2; column++) {
                loadBorderTile(conversion.getId(row, column, zoom));
            }
        }
    }

    private void loadBorderTiles() {
        // up
        loadBorderTiles(lastFirstRow - prefetchCount, lastFirstRow - prefetchCount, lastFirstColumn - prefetchCount,
                lastLastColumn + prefetchCount, lastZoomStep);
        // bot
        loadBorderTiles(lastLastRow + prefetchCount, lastLastRow + prefetchCount, lastFirstColumn - prefetchCount,
                lastLastColumn + prefetchCount, lastZoomStep);
        // left
        loadBorderTiles(lastFirstRow, lastLastRow, lastFirstColumn - prefetchCount, lastFirstColumn - prefetchCount,
                lastZoomStep);
        // right
        loadBorderTiles(lastFirstRow, lastLastRow, lastLastColumn + prefetchCount, lastLastColumn + prefetchCount,
                lastZoomStep);
    }

    protected void loadVisibleTile(final long id) {
        for (final Layer layer : layers) {
            if (layer.imageAccessor.isVisible()) {
                loadTile(layer.imageFetcher, id);
            }
        }
    }

    protected void loadBorderTile(final long id) {
        for (final Layer layer : layers) {
            if (layer.prefetch && layer.imageAccessor.isVisible())
                loadTile(layer.imageFetcher, id);
        }
    }

    private static class Layer {
        private final IImageFetcher imageFetcher;
        private final IImageAccessor imageAccessor;
        private boolean lastVisible;
        private boolean prefetch;

        public Layer(IImageFetcher imageFetcher, IImageAccessor imageAccessor, boolean prefetch) {
            super();
            this.imageFetcher = imageFetcher;
            this.imageAccessor = imageAccessor;
            this.prefetch = prefetch;
            this.lastVisible = true;
        }

        public void setMapManager(final IMapManager manager) {
            imageFetcher.setMapManager(manager);
        }
    }

    private class DelayedLoader extends Thread {
        private static final int DELAY_TIME = 500;
        private boolean moved;

        @Override
        public void run() {
            while (!isInterrupted()) {
                synchronized (this) {
                    while (!moved) {
                        try {
                            wait();
                        } catch (InterruptedException e) {}
                    }
                    moved = false;
                }
                try {
                    Thread.sleep(DELAY_TIME);
                } catch (InterruptedException e) {
                    interrupt();
                }
                if (!moved)
                    loadBorderTiles();

            }
        }
    }
}