package model.renderEngine;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import model.IFactory;
import model.map.IMapManager;
import model.map.IMapState;
import model.map.IPixelConverter;

public class ImageLoader implements IImageLoader {

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

    public ImageLoader(final IMapManager manager) {
        priority = Integer.MAX_VALUE - 16;
        mapManager = manager;

        lastPOIVisibility = true;
        lastRouteVisibility = true;
        lastLabelVisibility = true;

        // set zoomStep to minZoomStep - 1, so on first update all tiles in
        // current view will be rendered
        lastZoomStep = mapManager.getState().getMinZoomStep() - 1;
        lastRow = mapManager.getRow();
        lastColumn = mapManager.getColumn();

        // TODO make this variable

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

        imageAccessors.add(labelAccessor);
        imageAccessors.add(routeAccessor);
        imageAccessors.add(POIAccessor);
        imageAccessors.add(backgroundAccessor);
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        mapManager = manager;

        // set zoomStep to -1, so on first update all tiles in current view will
        // be rendered
        lastZoomStep = mapManager.getState().getMinZoomStep() - 1;
        lastRow = mapManager.getRow();
        lastColumn = mapManager.getColumn();

        backgroundFetcher.setMapManager(manager);
        POIFetcher.setMapManager(manager);
        routeFetcher.setMapManager(manager);
        labelFetcher.setMapManager(manager);

        backgroundAccessor.setMapManager(manager);
        POIAccessor.setMapManager(manager);
        routeAccessor.setMapManager(manager);
        labelAccessor.setMapManager(manager);
    }

    private List<Long> getLastViewTiles() {
        final List<Long> newTiles = new LinkedList<>();

        for (int i = lastRow; i < lastVisibleRows + lastRow; i++) {
            for (int j = lastColumn; j < lastVisibleColumns + lastColumn; j++) {
                newTiles.add(mapManager.getID(i, j, lastZoomStep));
            }
        }
        return newTiles;
    }

    private List<Long> getNewTiles() {
        final List<Long> newTiles = new LinkedList<>();

        final int currentZoomStep = mapManager.getState().getZoomStep();
        final int row = mapManager.getRow();
        final int column = mapManager.getColumn();
        final int visibleRows = mapManager.getVisibleRows();
        final int visibleColumns = mapManager.getVisibleColumns();

        if (currentZoomStep != lastZoomStep) {
            for (int i = row; i < visibleRows + row; i++) {
                for (int j = column; j < visibleColumns + column; j++) {
                    newTiles.add(mapManager.getID(i, j, currentZoomStep));
                }
            }
        } else if (lastRow != row || lastColumn != column || lastVisibleRows < visibleRows
                || lastVisibleColumns < visibleColumns) {
            final Rectangle lastView = new Rectangle(lastColumn, lastRow);
            lastView.height = lastVisibleRows;
            lastView.width = lastVisibleColumns;

            for (int i = row; i < visibleRows + row; i++) {
                for (int j = column; j < visibleColumns + column; j++) {
                    if (!lastView.contains(j, i)) {
                        newTiles.add(mapManager.getID(i, j, currentZoomStep));
                    }
                }
            }
        }

        lastZoomStep = currentZoomStep;
        lastRow = row;
        lastColumn = column;
        lastVisibleRows = visibleRows;
        lastVisibleColumns = visibleColumns;

        return newTiles;
    }

    // make sure this gets called after getNewTiles
    private Collection<Long> prefetchZoomTiles() {

        final List<Long> tiles = new LinkedList<>();

        final IMapState state = mapManager.getState();
        final int zoom = state.getZoomStep();
        final IPixelConverter converter = mapManager.getConverter();
        final int tileSize = mapManager.getTileSize();
        final int height = state.getCoordSectionHeight();
        final int width = state.getCoordSectionWidth();
        final int x = (int) state.getX();
        final int y = (int) state.getY();

        // prefetch tiles in higher layer
        if (zoom > state.getMinZoomStep()) {
            final int zoomedStep = zoom - 1;
            final int zoomedHeight = height * 2;
            final int zoomedWidth = width * 2;
            final int zoomedX = Math.max(0, x - height / 2);
            final int zoomedY = Math.max(0, y - width / 2);

            final int startRow = zoomedY / converter.getCoordDistance(tileSize, zoomedStep);
            final int startColumn = zoomedX / converter.getCoordDistance(tileSize, zoomedStep);
            final int endRow = (zoomedY + zoomedHeight) / converter.getCoordDistance(tileSize, zoomedStep) + 1;
            final int endColumn = (zoomedX + zoomedWidth) / converter.getCoordDistance(tileSize, zoomedStep) + 1;

            for (int i = startRow; i < endRow; i++) {
                for (int j = startColumn; j < endColumn; j++) {
                    tiles.add(mapManager.getID(i, j, zoomedStep));
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

            final int startRow = zoomedY / converter.getCoordDistance(tileSize, zoomedStep);
            final int startColumn = zoomedX / converter.getCoordDistance(tileSize, zoomedStep);
            final int endRow = (zoomedY + zoomedHeight) / converter.getCoordDistance(tileSize, zoomedStep) + 1;
            final int endColumn = (zoomedX + zoomedWidth) / converter.getCoordDistance(tileSize, zoomedStep) + 1;

            for (int i = startRow; i < endRow; i++) {
                for (int j = startColumn; j < endColumn; j++) {
                    tiles.add(mapManager.getID(i, j, zoomedStep));
                }
            }
        }

        return tiles;
    }

    // make sure this gets called after getNewTiles
    private List<Long> prefetchTiles() {
        final List<Long> tiles = new LinkedList<>();

        for (int column = Math.max(lastColumn - prefetchCount, 0); column < lastColumn + lastVisibleColumns
                + prefetchCount; column++) {
            for (int row = Math.max(lastRow - prefetchCount, 0); row < lastRow; row++) {
                // prefetch tiles which are over the current view
                tiles.add(mapManager.getID(row, column, lastZoomStep));

            }

            for (int row = lastRow + lastVisibleRows; row < lastRow + lastVisibleRows + prefetchCount; row++) {
                // prefetch tiles which are under the current view
                tiles.add(mapManager.getID(row, column, lastZoomStep));
            }
        }

        for (int row = lastRow; row < lastRow + lastVisibleRows; row++) {
            for (int column = Math.max(lastColumn - prefetchCount, 0); column < lastColumn; column++) {
                // prefetch tiles which are to the left of the current view
                tiles.add(mapManager.getID(row, column, lastZoomStep));
            }

            for (int column = lastColumn + lastVisibleColumns; column < lastColumn + lastVisibleColumns
                    + prefetchCount; column++) {
                // prefetch tiles which are to the right of the current view
                tiles.add(mapManager.getID(row, column, lastZoomStep));
            }
        }

        return tiles;
    }

    @Override
    public void update() {

        Collection<Long> lastTiles = null;

        if (lastLabelVisibility != labelAccessor.isVisible()) {
            lastLabelVisibility = labelAccessor.isVisible();
            if (lastLabelVisibility) {
                lastTiles = getLastViewTiles();

                for (final Long tileID : lastTiles) {
                    labelFetcher.loadImage(tileID, priority);
                }
            }
        }
        if (lastPOIVisibility != POIAccessor.isVisible()) {
            lastPOIVisibility = POIAccessor.isVisible();
            if (lastPOIVisibility) {
                lastTiles = getLastViewTiles();

                for (final Long tileID : lastTiles) {
                    POIFetcher.loadImage(tileID, priority);
                }
            }
        }
        if (lastRouteVisibility != routeAccessor.isVisible()) {
            lastRouteVisibility = routeAccessor.isVisible();
            if (lastRouteVisibility) {
                lastTiles = getLastViewTiles();

                for (final Long tileID : lastTiles) {
                    routeFetcher.loadImage(tileID, priority);
                }
            }
        }

        final List<Long> newTiles = getNewTiles();

        if (newTiles.isEmpty()) {
            // nothing to update -> no update on priority required
            return;
        }

        for (final Long tileID : newTiles) {
            backgroundFetcher.loadImage(tileID, priority);
            if (labelAccessor.isVisible()) {
                labelFetcher.loadImage(tileID, priority);
            }
            if (POIAccessor.isVisible()) {
                POIFetcher.loadImage(tileID, priority);
            }
            if (routeSet && routeAccessor.isVisible()) {
                routeFetcher.loadImage(tileID, priority);
            }
        }

        for (final Long tileID : prefetchTiles()) {
            backgroundFetcher.loadImage(tileID, priority + 8);
        }

        for (final Long tileID : prefetchZoomTiles()) {
            backgroundFetcher.loadImage(tileID, priority + 16);
        }

        priority--;
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

        for (final Long tileID : getLastViewTiles()) {
            routeFetcher.loadImage(tileID, priority);
        }

        for (final Long tileID : prefetchTiles()) {
            routeFetcher.loadImage(tileID, priority + 2);
        }

        priority--;
    }

}