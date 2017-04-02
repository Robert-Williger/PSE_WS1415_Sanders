package model.renderEngine;

import java.util.ArrayList;
import java.util.List;

import model.IFactory;
import model.map.IMapManager;
import model.map.IMapState;

public class DefaultImageLoader implements IImageLoader {

    private IMapManager mapManager;
    private IMapState state;

    private final IImageFetcher backgroundFetcher;
    private final IImageFetcher POIFetcher;
    private final IImageFetcher routeFetcher;
    private final IImageFetcher labelFetcher;

    private final IRouteRenderer routeRenderer;

    private final IImageAccessor backgroundAccessor;
    private final IImageAccessor POIAccessor;
    private final IImageAccessor routeAccessor;
    private final IImageAccessor labelAccessor;

    private final List<IImageAccessor> imageAccessors;

    private final DelayedLoader delayedLoader;

    private boolean routeSet;

    private int lastZoomStep;
    private int lastRow;
    private int lastColumn;
    private int lastVisibleRows;
    private int lastVisibleColumns;
    private final int prefetchCount = 1;

    private int priority;

    private boolean lastPOIVisibility;
    private boolean lastRouteVisibility;
    private boolean lastLabelVisibility;

    public DefaultImageLoader(final IMapManager manager) {
        priority = Integer.MAX_VALUE - 16;
        mapManager = manager;
        state = mapManager.getState();

        lastPOIVisibility = true;
        lastRouteVisibility = true;
        lastLabelVisibility = true;

        // set zoomStep to minZoomStep - 1, so on first update all tiles in
        // current view will be rendered
        lastZoomStep = state.getMinZoom() - 1;
        lastRow = mapManager.getRow(state.getZoom());
        lastColumn = mapManager.getColumn(state.getZoom());

        routeRenderer = new RouteRenderer(mapManager);

        backgroundFetcher = new ParallelImageFetcher(mapManager, new IFactory<IRenderer>() {
            final ColorScheme colorScheme = new OSMColorScheme();

            @Override
            public IRenderer create() {
                return new BackgroundRenderer(mapManager, colorScheme);
            }
        });

        POIFetcher = new SequentialImageFetcher(new POIRenderer(mapManager), mapManager);
        routeFetcher = new SequentialImageFetcher(routeRenderer, mapManager);
        labelFetcher = new SequentialImageFetcher(new LabelRenderer(mapManager), mapManager);

        backgroundAccessor = new ImageAccessor(mapManager, backgroundFetcher);
        POIAccessor = new ImageAccessor(mapManager, POIFetcher);
        routeAccessor = new ImageAccessor(mapManager, routeFetcher);
        labelAccessor = new ImageAccessor(mapManager, labelFetcher);

        imageAccessors = new ArrayList<>(4);

        // imageAccessors.add(labelAccessor);
        // imageAccessors.add(routeAccessor);
        // imageAccessors.add(POIAccessor);
        imageAccessors.add(backgroundAccessor);

        delayedLoader = new DelayedLoader();
        // TODO reactivate me
        // delayedLoader.start();
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        mapManager = manager;
        state = mapManager.getState();

        // set zoomStep to minZoomStep -1, so on first update all tiles in current view will
        // be rendered
        lastZoomStep = state.getMinZoom() - 1;
        lastRow = 0;
        lastColumn = 0;

        backgroundFetcher.setMapManager(manager);
        POIFetcher.setMapManager(manager);
        routeFetcher.setMapManager(manager);
        labelFetcher.setMapManager(manager);

        backgroundAccessor.setMapManager(manager);
        POIAccessor.setMapManager(manager);
        routeAccessor.setMapManager(manager);
        labelAccessor.setMapManager(manager);
    }

    @Override
    public int getTileSize() {
        return state.getPixelTileSize();
    }

    @Override
    public void update() {
        if (lastLabelVisibility != labelAccessor.isVisible()) {
            lastLabelVisibility = labelAccessor.isVisible();
            if (lastLabelVisibility) {
                loadCurrentTiles(labelFetcher);
            }
        }
        if (lastPOIVisibility != POIAccessor.isVisible()) {
            lastPOIVisibility = POIAccessor.isVisible();
            if (lastPOIVisibility) {
                loadCurrentTiles(POIFetcher);
            }
        }
        if (lastRouteVisibility != routeAccessor.isVisible()) {
            lastRouteVisibility = routeAccessor.isVisible();
            if (lastRouteVisibility && routeSet) {
                loadCurrentTiles(routeFetcher);
            }
        }

        if (loadNewTiles()) {
            delayedLoader.moved = true;
            synchronized (delayedLoader) {
                delayedLoader.notify();
            }
            priority--;
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

        if (route == null) {
            routeAccessor.setVisible(false);
            routeSet = false;
            return;
        }

        routeAccessor.setVisible(true);
        routeSet = true;

        loadCurrentTiles(routeFetcher);

        priority--;
    }

    private void loadCurrentTiles(final IImageFetcher imageFetcher) {
        for (int i = lastRow; i < lastVisibleRows + lastRow; i++) {
            for (int j = lastColumn; j < lastVisibleColumns + lastColumn; j++) {
                System.out.println(lastZoomStep);
                imageFetcher.loadImage(mapManager.getID(i, j, lastZoomStep), priority);
            }
        }
    }

    private boolean loadNewTiles() {
        boolean ret = false;

        final int zoom = mapManager.getState().getZoom();

        final int row = mapManager.getRow(zoom);
        final int column = mapManager.getColumn(zoom);
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
            } else if (column + visibleColumns > lastColumn + lastVisibleColumns) {
                toColumn = Math.max(lastColumn + lastVisibleColumns, column);
                loadTiles(row, row + visibleRows, toColumn, column + visibleColumns, zoom);
                loadBorderTiles(row, row + visibleRows, column + visibleColumns, column + visibleColumns + 2, zoom);
                ret = true;
            }
            if (row < lastRow) {
                loadTiles(row, Math.min(row + visibleRows, lastRow), fromColumn, toColumn, zoom);
                loadBorderTiles(row - 2, row, fromColumn, toColumn, zoom);
                ret = true;
            } else if (row + visibleRows > lastRow + lastVisibleRows) {
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
                loadVisibleTile(mapManager.getID(y, x, zoom));
            }
        }
    }

    private void loadBorderTiles(final int r1, final int r2, final int c1, final int c2, final int zoom) {
        for (int y = r1; y < r2; y++) {
            for (int x = c1; x < c2; x++) {
                loadBorderTile(mapManager.getID(y, x, zoom));
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
        backgroundFetcher.loadImage(id, priority);
        if (labelAccessor.isVisible()) {
            labelFetcher.loadImage(id, priority);
        }
        if (POIAccessor.isVisible()) {
            POIFetcher.loadImage(id, priority);
        }
        if (routeSet && routeAccessor.isVisible()) {
            routeFetcher.loadImage(id, priority);
        }
    }

    protected void loadBorderTile(final long id) {
        backgroundFetcher.loadImage(id, priority + 8);
    }

    private class DelayedLoader extends Thread {
        private static final int DELAY_TIME = 500;
        private boolean moved;

        @Override
        public void run() {
            while (!isInterrupted()) {
                synchronized (this) {
                    try {
                        wait(DELAY_TIME);
                    } catch (InterruptedException e) {
                        interrupt();
                    }
                }
                if (!moved) {
                    loadBorderTiles();
                    synchronized (this) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            interrupt();
                        }
                    }
                }
                moved = false;
            }
        }
    }
}