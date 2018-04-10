package model.renderEngine;

import java.util.ArrayList;
import java.util.List;

import model.map.IMapManager;
import model.map.IMapState;

public class SmartImageLoader implements IImageLoader {

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
    private int lastRowCount;
    private int lastVisibleColumns;
    private final int prefetchCount = 1;

    private int priority;

    private boolean lastPOIVisibility;
    private boolean lastRouteVisibility;
    private boolean lastLabelVisibility;

    public SmartImageLoader(final IMapManager manager) {
        priority = Integer.MAX_VALUE - 16;
        mapManager = manager;

        lastPOIVisibility = true;
        lastRouteVisibility = true;
        lastLabelVisibility = true;

        // set zoomStep to minZoomStep - 1, so on first update all tiles in
        // current view will be rendered
        state = mapManager.getState();
        lastZoomStep = state.getMinZoom() - 1;
        lastRow = mapManager.getRow(state.getZoom());
        lastColumn = mapManager.getColumn(state.getZoom());

        routeRenderer = new RouteRenderer(mapManager);

        final ColorScheme colorScheme = new OSMColorScheme();
        final int processors = Runtime.getRuntime().availableProcessors();
        backgroundFetcher = new ParallelImageFetcher(mapManager, () -> new BackgroundRenderer(mapManager, colorScheme),
                processors);

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
        delayedLoader.start();
    }

    @Override
    public int getTileSize() {
        return state.getPixelTileSize();
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        mapManager = manager;
        state = manager.getState();

        // set zoomStep to -1, so on first update all tiles in current view will
        // be rendered
        lastZoomStep = state.getMinZoom() - 1;
        lastRow = mapManager.getRow(state.getZoom());
        lastColumn = mapManager.getColumn(state.getZoom());

        backgroundFetcher.setMapManager(manager);
        POIFetcher.setMapManager(manager);
        routeFetcher.setMapManager(manager);
        labelFetcher.setMapManager(manager);

        backgroundAccessor.setMapManager(manager);
        POIAccessor.setMapManager(manager);
        routeAccessor.setMapManager(manager);
        labelAccessor.setMapManager(manager);
    }

    private void loadCurrentTiles(final IImageFetcher imageFetcher) {
        for (int i = lastRow; i < lastRowCount + lastRow; i++) {
            for (int j = lastColumn; j < lastVisibleColumns + lastColumn; j++) {
                imageFetcher.loadImage(mapManager.getTileID(i, j, lastZoomStep), priority);
            }
        }
    }

    private boolean loadNewTiles() {
        boolean ret = false;

        final int zoom = state.getZoom();
        final int row = mapManager.getRow(zoom);
        final int column = mapManager.getColumn(zoom);
        final int visibleRows = mapManager.getVisibleRows(zoom);
        final int visibleColumns = mapManager.getVisibleColumns(zoom);

        if (zoom != lastZoomStep) {
            for (int i = row; i < visibleRows + row; i++) {
                for (int j = column; j < visibleColumns + column; j++) {
                    loadTile(mapManager.getTileID(i, j, zoom));
                }
            }

            ret = true;
        } else {
            int fromColumn = column;
            int toColumn = column + visibleColumns;
            if (column < lastColumn) {
                // upper bound for x
                fromColumn = Math.min(column + visibleColumns, lastColumn);
                for (int x = column; x < fromColumn; x++) {
                    for (int y = row; y < row + visibleRows; y++) {
                        loadTile(mapManager.getTileID(y, x, zoom));
                    }
                }
                for (int x = column - 1; x >= column - 2; x--) {
                    for (int y = row; y < row + visibleRows; y++) {
                        loadBorderTile(mapManager.getTileID(y, x, zoom));
                    }
                }

                ret = true;
            } else if (column + visibleColumns > lastColumn + lastVisibleColumns) {
                toColumn = Math.max(lastColumn + lastVisibleColumns, column);
                for (int x = toColumn; x < column + visibleColumns; x++) {
                    for (int y = row; y < row + visibleRows; y++) {
                        loadTile(mapManager.getTileID(y, x, zoom));
                    }
                }
                for (int x = column + visibleColumns; x < column + visibleColumns + 2; x++) {
                    for (int y = row; y < row + visibleRows; y++) {
                        loadBorderTile(mapManager.getTileID(y, x, zoom));
                    }
                }

                ret = true;
            }
            if (row < lastRow) {
                for (int y = row; y < Math.min(row + visibleRows, lastRow); y++) {
                    for (int x = fromColumn; x < toColumn; x++) {
                        loadTile(mapManager.getTileID(y, x, zoom));
                    }
                }
                for (int y = row - 1; y >= row - 2; y--) {
                    for (int x = fromColumn; x < toColumn; x++) {
                        loadBorderTile(mapManager.getTileID(y, x, zoom));
                    }
                }

                ret = true;
            } else if (row + visibleRows > lastRow + lastRowCount) {
                for (int y = Math.max(lastRow + lastRowCount, row); y < row + visibleRows; y++) {
                    for (int x = fromColumn; x < toColumn; x++) {
                        loadTile(mapManager.getTileID(y, x, zoom));
                    }
                }
                for (int y = row + visibleRows; y < row + visibleRows + 2; y++) {
                    for (int x = fromColumn; x < toColumn; x++) {
                        loadBorderTile(mapManager.getTileID(y, x, zoom));
                    }
                }

                ret = true;
            }
        }

        lastZoomStep = zoom;
        lastRow = row;
        lastColumn = column;
        lastRowCount = visibleRows;
        lastVisibleColumns = visibleColumns;

        return ret;
    }

    private void loadZoomTiles() {
        final IMapState state = mapManager.getState();
        final int zoom = state.getZoom();
        final int height = state.getCoordSectionHeight(zoom);
        final int width = state.getCoordSectionWidth(zoom);
        final int x = (int) state.getCoordX();
        final int y = (int) state.getCoordY();

        // prefetch tiles in higher layer
        if (zoom > state.getMinZoom()) {
            final int zoomedStep = zoom - 1;
            final int zoomedHeight = height * 2;
            final int zoomedWidth = width * 2;
            final int zoomedX = Math.max(0, x - height / 2);
            final int zoomedY = Math.max(0, y - width / 2);

            final int coordTileSize = state.getCoordTileSize(zoomedStep);
            final int startRow = zoomedY / coordTileSize;
            final int startColumn = zoomedX / coordTileSize;
            final int endRow = (zoomedY + zoomedHeight) / coordTileSize + 1;
            final int endColumn = (zoomedX + zoomedWidth) / coordTileSize + 1;

            for (int i = startRow; i < endRow; i++) {
                for (int j = startColumn; j < endColumn; j++) {
                    loadZoomTile(mapManager.getTileID(i, j, zoomedStep));
                }
            }
        }

        // prefetch tiles in deeper layer
        if (zoom < state.getMaxZoom()) {
            final int zoomedStep = zoom + 1;
            final int zoomedHeight = height / 2;
            final int zoomedWidth = width / 2;
            final int zoomedX = x + zoomedWidth / 2;
            final int zoomedY = y + zoomedHeight / 2;

            final int coordTileSize = state.getCoordTileSize(zoomedStep);
            final int startRow = zoomedY / coordTileSize;
            final int startColumn = zoomedX / coordTileSize;
            final int endRow = (zoomedY + zoomedHeight) / coordTileSize + 1;
            final int endColumn = (zoomedX + zoomedWidth) / coordTileSize + 1;

            for (int i = startRow; i < endRow; i++) {
                for (int j = startColumn; j < endColumn; j++) {
                    loadZoomTile(mapManager.getTileID(i, j, zoomedStep));
                }
            }
        }
    }

    private void loadBorderTiles() {
        for (int column = Math.max(lastColumn - prefetchCount, 0); column < lastColumn + lastVisibleColumns
                + prefetchCount; column++) {
            for (int row = Math.max(lastRow - prefetchCount, 0); row < lastRow; row++) {
                // prefetch tiles which are over the current view
                loadBorderTile(mapManager.getTileID(row, column, lastZoomStep));
            }

            for (int row = lastRow + lastRowCount; row < lastRow + lastRowCount + prefetchCount; row++) {
                // prefetch tiles which are under the current view
                loadBorderTile(mapManager.getTileID(row, column, lastZoomStep));
            }
        }

        for (int row = lastRow; row < lastRow + lastRowCount; row++) {
            for (int column = Math.max(lastColumn - prefetchCount, 0); column < lastColumn; column++) {
                // prefetch tiles which are to the left of the current view
                loadBorderTile(mapManager.getTileID(row, column, lastZoomStep));
            }

            for (int column = lastColumn + lastVisibleColumns; column < lastColumn + lastVisibleColumns
                    + prefetchCount; column++) {
                // prefetch tiles which are to the right of the current view
                loadBorderTile(mapManager.getTileID(row, column, lastZoomStep));
            }
        }
    }

    protected void loadTile(final long id) {
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

    protected void loadZoomTile(final long id) {
        backgroundFetcher.loadImage(id, priority + 8);
    }

    protected void loadBorderTile(final long id) {
        backgroundFetcher.loadImage(id, priority + 8);
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
                    loadZoomTiles();
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