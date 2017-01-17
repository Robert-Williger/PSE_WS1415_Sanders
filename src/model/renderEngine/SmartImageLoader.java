package model.renderEngine;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import model.IFactory;
import model.map.IMapManager;
import model.map.IMapState;
import model.map.IPixelConverter;

public class SmartImageLoader implements IImageLoader {

    private IMapManager mapManager;

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
    private Point lastGridLocation;
    private int lastRowCount;
    private int lastColumnCount;
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
        lastZoomStep = mapManager.getState().getMinZoomStep() - 1;
        lastGridLocation = new Point(mapManager.getGridLocation());

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

        imageAccessors = new ArrayList<IImageAccessor>(4);

        imageAccessors.add(labelAccessor);
        imageAccessors.add(routeAccessor);
        imageAccessors.add(POIAccessor);
        imageAccessors.add(backgroundAccessor);

        delayedLoader = new DelayedLoader();
        delayedLoader.start();
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        mapManager = manager;

        // set zoomStep to -1, so on first update all tiles in current view will
        // be rendered
        lastZoomStep = mapManager.getState().getMinZoomStep() - 1;
        lastGridLocation = new Point(mapManager.getGridLocation());

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
        for (int i = lastGridLocation.y; i < lastRowCount + lastGridLocation.y; i++) {
            for (int j = lastGridLocation.x; j < lastColumnCount + lastGridLocation.x; j++) {
                imageFetcher.loadImage(mapManager.getID(i, j, lastZoomStep), priority);
            }
        }
    }

    private boolean loadNewTiles() {
        boolean ret = false;

        final int currentZoomStep = mapManager.getState().getZoomStep();
        final Point currentGridLocation = mapManager.getGridLocation();
        final int currentRows = mapManager.getRows();
        final int currentColumns = mapManager.getColumns();

        if (currentZoomStep != lastZoomStep) {
            for (int i = currentGridLocation.y; i < currentRows + currentGridLocation.y; i++) {
                for (int j = currentGridLocation.x; j < currentColumns + currentGridLocation.x; j++) {
                    loadTile(mapManager.getID(i, j, currentZoomStep));
                }
            }

            ret = true;
        } else {
            int fromX = currentGridLocation.x;
            int toX = currentGridLocation.x + currentColumns;
            if (currentGridLocation.x < lastGridLocation.x) {
                // upper bound for x
                fromX = Math.min(currentGridLocation.x + currentColumns, lastGridLocation.x);
                for (int x = currentGridLocation.x; x < fromX; x++) {
                    for (int y = currentGridLocation.y; y < currentGridLocation.y + currentRows; y++) {
                        loadTile(mapManager.getID(y, x, currentZoomStep));
                    }
                }
                for (int x = currentGridLocation.x - 1; x >= currentGridLocation.x - 2; x--) {
                    for (int y = currentGridLocation.y; y < currentGridLocation.y + currentRows; y++) {
                        loadBorderTile(mapManager.getID(y, x, currentZoomStep));
                    }
                }

                ret = true;
            } else if (currentGridLocation.x + currentColumns > lastGridLocation.x + lastColumnCount) {
                toX = Math.max(lastGridLocation.x + lastColumnCount, currentGridLocation.x);
                for (int x = toX; x < currentGridLocation.x + currentColumns; x++) {
                    for (int y = currentGridLocation.y; y < currentGridLocation.y + currentRows; y++) {
                        loadTile(mapManager.getID(y, x, currentZoomStep));
                    }
                }
                for (int x = currentGridLocation.x + currentColumns; x < currentGridLocation.x + currentColumns + 2; x++) {
                    for (int y = currentGridLocation.y; y < currentGridLocation.y + currentRows; y++) {
                        loadBorderTile(mapManager.getID(y, x, currentZoomStep));
                    }
                }

                ret = true;
            }
            if (currentGridLocation.y < lastGridLocation.y) {
                for (int y = currentGridLocation.y; y < Math.min(currentGridLocation.y + currentRows,
                        lastGridLocation.y); y++) {
                    for (int x = fromX; x < toX; x++) {
                        loadTile(mapManager.getID(y, x, currentZoomStep));
                    }
                }
                for (int y = currentGridLocation.y - 1; y >= currentGridLocation.y - 2; y--) {
                    for (int x = fromX; x < toX; x++) {
                        loadBorderTile(mapManager.getID(y, x, currentZoomStep));
                    }
                }

                ret = true;
            } else if (currentGridLocation.y + currentRows > lastGridLocation.y + lastRowCount) {
                for (int y = Math.max(lastGridLocation.y + lastRowCount, currentGridLocation.y); y < currentGridLocation.y
                        + currentRows; y++) {
                    for (int x = fromX; x < toX; x++) {
                        loadTile(mapManager.getID(y, x, currentZoomStep));
                    }
                }
                for (int y = currentGridLocation.y + currentRows; y < currentGridLocation.y + currentRows + 2; y++) {
                    for (int x = fromX; x < toX; x++) {
                        loadBorderTile(mapManager.getID(y, x, currentZoomStep));
                    }
                }

                ret = true;
            }
        }

        lastZoomStep = currentZoomStep;
        lastGridLocation = currentGridLocation;
        lastRowCount = currentRows;
        lastColumnCount = currentColumns;

        return ret;
    }

    private void loadZoomTiles() {
        final IMapState state = mapManager.getState();
        final int zoom = state.getZoomStep();
        final IPixelConverter converter = mapManager.getConverter();
        final int tileSize = mapManager.getTileSize();
        final double zoomFactor = 1.0 / (1 << zoom - state.getMinZoomStep());
        final int height = (int) (state.getSize().height * zoomFactor);
        final int width = (int) (state.getSize().width * zoomFactor);
        final int x = state.getLocation().x;
        final int y = state.getLocation().y;

        // prefetch tiles in higher layer
        if (zoom > state.getMinZoomStep()) {
            final int zoomedStep = zoom - 1;
            final int zoomedHeight = height * 2;
            final int zoomedWidth = width * 2;
            final int zoomedX = Math.max(0, x - height / 2);
            final int zoomedY = Math.max(0, y - width / 2);

            final int coordTileSize = converter.getCoordDistance(tileSize, zoomedStep);
            final int startRow = zoomedY / coordTileSize;
            final int startColumn = zoomedX / coordTileSize;
            final int endRow = (zoomedY + zoomedHeight) / coordTileSize + 1;
            final int endColumn = (zoomedX + zoomedWidth) / coordTileSize + 1;

            for (int i = startRow; i < endRow; i++) {
                for (int j = startColumn; j < endColumn; j++) {
                    loadZoomTile(mapManager.getID(i, j, zoomedStep));
                }
            }
        }

        // prefetch tiles in deeper layer
        if (zoom < state.getMaxZoomStep()) {
            final int zoomedStep = zoom + 1;
            final int zoomedHeight = height / 2;
            final int zoomedWidth = width / 2;
            final int zoomedX = x + zoomedWidth / 2;
            final int zoomedY = y + zoomedHeight / 2;

            final int coordTileSize = converter.getCoordDistance(tileSize, zoomedStep);
            final int startRow = zoomedY / coordTileSize;
            final int startColumn = zoomedX / coordTileSize;
            final int endRow = (zoomedY + zoomedHeight) / coordTileSize + 1;
            final int endColumn = (zoomedX + zoomedWidth) / coordTileSize + 1;

            for (int i = startRow; i < endRow; i++) {
                for (int j = startColumn; j < endColumn; j++) {
                    loadZoomTile(mapManager.getID(i, j, zoomedStep));
                }
            }
        }
    }

    private void loadBorderTiles() {
        for (int column = Math.max(lastGridLocation.x - prefetchCount, 0); column < lastGridLocation.x
                + lastColumnCount + prefetchCount; column++) {
            for (int row = Math.max(lastGridLocation.y - prefetchCount, 0); row < lastGridLocation.y; row++) {
                // prefetch tiles which are over the current view
                loadBorderTile(mapManager.getID(row, column, lastZoomStep));
            }

            for (int row = lastGridLocation.y + lastRowCount; row < lastGridLocation.y + lastRowCount + prefetchCount; row++) {
                // prefetch tiles which are under the current view
                loadBorderTile(mapManager.getID(row, column, lastZoomStep));
            }
        }

        for (int row = lastGridLocation.y; row < lastGridLocation.y + lastRowCount; row++) {
            for (int column = Math.max(lastGridLocation.x - prefetchCount, 0); column < lastGridLocation.x; column++) {
                // prefetch tiles which are to the left of the current view
                loadBorderTile(mapManager.getID(row, column, lastZoomStep));
            }

            for (int column = lastGridLocation.x + lastColumnCount; column < lastGridLocation.x + lastColumnCount
                    + prefetchCount; column++) {
                // prefetch tiles which are to the right of the current view
                loadBorderTile(mapManager.getID(row, column, lastZoomStep));
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