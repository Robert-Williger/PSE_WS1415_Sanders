package model.renderEngine;

import java.util.ArrayList;
import java.util.List;

import model.map.IMapManager;
import model.map.IMapState;
import model.map.accessors.ITileConversion;
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
    private ITileConversion conversion;
    private IMapState state;

    private final PriorityManager priorityManager;
    private final List<Layer> layers;

    private final IImageFetcher routeFetcher;
    private final IRouteRenderer routeRenderer;
    private final IImageAccessor routeAccessor;

    private final List<IImageAccessor> imageAccessors;

    private final DelayedLoader delayedLoader;

    private final ThreadPool pool;

    private int lastZoomStep;
    private int lastRow;
    private int lastColumn;
    private int lastVisibleRows;
    private int lastVisibleColumns;
    private final int prefetchCount = 1;

    public ImageLoader(final IMapManager manager) {
        mapManager = manager;
        state = mapManager.getState();
        conversion = mapManager.getTileConversion();

        // set zoomStep to minZoomStep - 1, so on first update all tiles in
        // current view will be rendered
        lastZoomStep = state.getMinZoom() - 1;
        lastRow = mapManager.getFirstRow(state.getZoom());
        lastColumn = mapManager.getFirstColumn(state.getZoom());

        routeRenderer = new RouteRenderer(mapManager);

        priorityManager = new PriorityManager();
        final ColorScheme colorScheme = new GoogleColorScheme();
        final int processors = Runtime.getRuntime().availableProcessors();
        pool = new ThreadPool(processors);

        final IImageFetcher backgroundFetcher = new ImageFetcher(new BackgroundRenderer(mapManager, colorScheme),
                mapManager, pool, 1024);
        final IImageFetcher POIFetcher = new ImageFetcher(new POIRenderer(mapManager), mapManager, pool, 128);
        routeFetcher = new ImageFetcher(routeRenderer, mapManager, pool, 128);
        final IImageFetcher labelFetcher = new ImageFetcher(new LabelRenderer(mapManager), mapManager, pool, 128);

        ImageAccessor backgroundAccessor = new ImageAccessor(conversion, backgroundFetcher);
        ImageAccessor POIAccessor = new ImageAccessor(conversion, POIFetcher);
        routeAccessor = new ImageAccessor(conversion, routeFetcher);
        ImageAccessor labelAccessor = new ImageAccessor(conversion, labelFetcher);

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

        delayedLoader = new DelayedLoader();
        delayedLoader.start();
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        mapManager = manager;
        state = mapManager.getState();
        conversion = manager.getTileConversion();

        // set zoomStep to minZoomStep -1, so on first update all tiles in current view will
        // be rendered
        lastZoomStep = state.getMinZoom() - 1;
        lastRow = 0;
        lastColumn = 0;

        for (final Layer layer : layers) {
            layer.setMapManager(manager);
        }
    }

    @Override
    public int getTileSize() {
        return state.getPixelTileSize();
    }

    @Override
    public void update() {
        if (mapManager.getState().getZoom() != lastZoomStep) {
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
            // delayedLoader.moved = true;
            // synchronized (delayedLoader) {
            // delayedLoader.notify();
            // }
            priorityManager.update();
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

        priorityManager.update();
    }

    private void loadTile(final IImageFetcher imageFetcher, final long id) {
        imageFetcher.loadImage(id, priorityManager.priority(id));
    }

    private void loadCurrentTiles(final IImageFetcher imageFetcher) {
        for (int i = lastRow; i < lastVisibleRows + lastRow; i++) {
            for (int j = lastColumn; j < lastVisibleColumns + lastColumn; j++) {
                loadTile(imageFetcher, conversion.getId(i, j, lastZoomStep));
            }
        }
    }

    private boolean loadNewTiles() {
        boolean ret = false;

        final int zoom = mapManager.getState().getZoom();

        final int row = mapManager.getFirstRow(zoom);
        final int column = mapManager.getFirstColumn(zoom);
        final int visibleRows = mapManager.getVisibleRows(zoom);
        final int visibleColumns = mapManager.getVisibleColumns(zoom);

        if (zoom != lastZoomStep) {
            loadTiles(row, row + visibleRows, column, column + visibleColumns, zoom);
            ret = true;
        } else {
            int fromColumn = column;
            int toColumn = column + visibleColumns;
            if (column < lastColumn) {
                fromColumn = Math.min(column + visibleColumns, lastColumn);
                loadTiles(row, row + visibleRows, column, fromColumn, zoom);
                loadBorderTiles(row, row + visibleRows, column - 2, column, zoom);
                ret = true;
            }
            // no else if for resize of map view..
            if (column + visibleColumns > lastColumn + lastVisibleColumns) {
                toColumn = Math.max(lastColumn + lastVisibleColumns, column);
                loadTiles(row, row + visibleRows, toColumn, column + visibleColumns, zoom);
                loadBorderTiles(row, row + visibleRows, column + visibleColumns, column + visibleColumns + 2, zoom);
                ret = true;
            }
            if (row < lastRow) {
                loadTiles(row, Math.min(row + visibleRows, lastRow), fromColumn, toColumn, zoom);
                loadBorderTiles(row - 2, row, fromColumn, toColumn, zoom);
                ret = true;
            }
            if (row + visibleRows > lastRow + lastVisibleRows) {
                loadTiles(Math.max(lastRow + lastVisibleRows, row), row + visibleRows, fromColumn, toColumn, zoom);
                loadBorderTiles(row + visibleRows, row + visibleRows + 2, fromColumn, toColumn, zoom);
                ret = true;
            }
        }

        lastZoomStep = zoom;
        lastRow = row;
        lastColumn = column;
        lastVisibleRows = visibleRows;
        lastVisibleColumns = visibleColumns;

        return ret;
    }

    private void loadTiles(final int r1, final int r2, final int c1, final int c2, final int zoom) {
        for (int y = r1; y < r2; y++) {
            for (int x = c1; x < c2; x++) {
                loadVisibleTile(conversion.getId(y, x, zoom));
            }
        }
    }

    private void loadBorderTiles(final int r1, final int r2, final int c1, final int c2, final int zoom) {
        for (int row = r1; row < r2; row++) {
            for (int column = c1; column < c2; column++) {
                loadBorderTile(conversion.getId(row, column, zoom));
            }
        }
    }

    private void loadBorderTiles() {
        // up
        loadBorderTiles(Math.max(lastRow - prefetchCount, 0), lastRow, Math.max(lastColumn - prefetchCount, 0),
                lastColumn + lastVisibleColumns + prefetchCount, lastZoomStep);
        // bot
        loadBorderTiles(lastRow + lastVisibleRows, lastRow + lastVisibleRows + prefetchCount,
                Math.max(lastColumn - prefetchCount, 0), lastColumn + lastVisibleColumns + prefetchCount, lastZoomStep);
        // left
        loadBorderTiles(lastRow, lastRow + lastVisibleRows, Math.max(lastColumn - prefetchCount, 0), lastColumn,
                lastZoomStep);
        // right
        loadBorderTiles(lastRow, lastRow + lastVisibleRows, lastColumn + lastVisibleColumns,
                lastColumn + lastVisibleColumns + prefetchCount, lastZoomStep);
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

    private class PriorityManager {
        private int priority;
        private int midRow;
        private int midColumn;

        public int priority(long id) {
            int row = conversion.getRow(id);
            int column = conversion.getColumn(id);
            int up = row < midRow ? 1 : 0;
            int left = column < midColumn ? 1 : 0;

            return priority + 2 * Math.abs(midRow - row) + 2 * Math.abs(midColumn - column) + up + left;
        }

        public void update() {
            final int zoom = mapManager.getState().getZoom();

            final int row = mapManager.getFirstRow(zoom);
            final int column = mapManager.getFirstColumn(zoom);
            final int visibleRows = mapManager.getVisibleRows(zoom);
            final int visibleColumns = mapManager.getVisibleColumns(zoom);

            midRow = row + visibleRows / 2;
            midColumn = column + visibleColumns / 2;

            priority -= visibleColumns * visibleRows;
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
            imageAccessor.setTileConversion(manager.getTileConversion());
        }
    }

    private class DelayedLoader extends Thread {
        private static final int DELAY_TIME = 500;
        private boolean moved;

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    Thread.sleep(DELAY_TIME);
                } catch (InterruptedException e) {
                    interrupt();
                }
                if (!moved)
                    loadBorderTiles();

                moved = false;
            }
        }
    }
}