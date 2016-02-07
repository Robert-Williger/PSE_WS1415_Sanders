package model.renderEngine;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import model.map.IMapManager;
import model.map.IMapState;
import model.map.IPixelConverter;

public class ImageLoader implements IImageLoader {

    private IMapManager mapManager;

    private final IImageFetcher backgroundFetcher;
    private final IImageFetcher POIFetcher;
    private final IImageFetcher routeFetcher;
    private final IImageFetcher labelFetcher;

    private final IRenderer backgroundRenderer;
    private final IRenderer POIRenderer;
    private final IRouteRenderer routeRenderer;
    private final IRenderer labelRenderer;

    private final IImageAccessor backgroundAccessor;
    private final IImageAccessor POIAccessor;
    private final IImageAccessor routeAccessor;
    private final IImageAccessor labelAccessor;

    private final List<IImageAccessor> imageAccessors;

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

    public ImageLoader(final IMapManager manager) {
        priority = Integer.MAX_VALUE - 16;
        mapManager = manager;

        lastPOIVisibility = true;
        lastRouteVisibility = true;
        lastLabelVisibility = true;

        // set zoomStep to minZoomStep - 1, so on first update all tiles in
        // current view will be rendered
        lastZoomStep = mapManager.getMapState().getMinZoomStep() - 1;
        lastGridLocation = new Point(mapManager.getCurrentGridLocation());

        backgroundRenderer = new StorageBackgroundRenderer(mapManager.getConverter());
        POIRenderer = new POIRenderer(mapManager.getConverter());
        routeRenderer = new RouteRenderer(mapManager.getConverter());
        labelRenderer = new LabelRenderer(mapManager.getConverter());

        backgroundFetcher = new HighlyCachedImageFetcher(backgroundRenderer, mapManager);
        POIFetcher = new SlightlyCachedImageFetcher(POIRenderer, mapManager);
        routeFetcher = new SlightlyCachedImageFetcher(routeRenderer, mapManager);
        labelFetcher = new SlightlyCachedImageFetcher(labelRenderer, mapManager);

        backgroundAccessor = new ImageAccessor(mapManager, backgroundFetcher);
        POIAccessor = new ImageAccessor(mapManager, POIFetcher);
        routeAccessor = new ImageAccessor(mapManager, routeFetcher);
        labelAccessor = new ImageAccessor(mapManager, labelFetcher);

        imageAccessors = new ArrayList<IImageAccessor>(4);

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
        lastZoomStep = mapManager.getMapState().getMinZoomStep() - 1;
        lastGridLocation = new Point(mapManager.getCurrentGridLocation());

        backgroundRenderer.setConverter(manager.getConverter());
        POIRenderer.setConverter(manager.getConverter());
        routeRenderer.setConverter(manager.getConverter());
        labelRenderer.setConverter(manager.getConverter());

        labelFetcher.setMapManager(manager);
        backgroundFetcher.setMapManager(manager);
        POIFetcher.setMapManager(manager);
        routeFetcher.setMapManager(manager);

        labelAccessor.setMapManager(manager);
        backgroundAccessor.setMapManager(manager);
        POIAccessor.setMapManager(manager);
        routeAccessor.setMapManager(manager);

    }

    private List<Long> getLastViewTiles() {
        final List<Long> newTiles = new LinkedList<Long>();

        for (int i = lastGridLocation.y; i < lastRowCount + lastGridLocation.y; i++) {
            for (int j = lastGridLocation.x; j < lastColumnCount + lastGridLocation.x; j++) {
                newTiles.add(mapManager.getTile(i, j, lastZoomStep).getID());
            }
        }
        return newTiles;
    }

    private List<Long> getNewTiles() {
        final List<Long> newTiles = new LinkedList<Long>();

        final int currentZoomStep = mapManager.getMapState().getZoomStep();
        final Point currentGridLocation = mapManager.getCurrentGridLocation();
        final int currentRows = mapManager.getRows();
        final int currentColumns = mapManager.getColumns();

        if (currentZoomStep != lastZoomStep) {
            for (int i = currentGridLocation.y; i < currentRows + currentGridLocation.y; i++) {
                for (int j = currentGridLocation.x; j < currentColumns + currentGridLocation.x; j++) {
                    newTiles.add(mapManager.getTile(i, j, currentZoomStep).getID());
                }
            }
        } else if (!lastGridLocation.equals(currentGridLocation) || lastRowCount < currentRows
                || lastColumnCount < currentColumns) {
            final Rectangle lastView = new Rectangle(lastGridLocation);
            lastView.height = lastRowCount;
            lastView.width = lastColumnCount;

            for (int i = currentGridLocation.y; i < currentRows + currentGridLocation.y; i++) {
                for (int j = currentGridLocation.x; j < currentColumns + currentGridLocation.x; j++) {
                    if (!lastView.contains(j, i)) {
                        newTiles.add(mapManager.getTile(i, j, currentZoomStep).getID());
                    }
                }
            }
        }

        lastZoomStep = currentZoomStep;
        lastGridLocation = currentGridLocation;
        lastRowCount = currentRows;
        lastColumnCount = currentColumns;

        return newTiles;
    }

    // make sure this gets called after getNewTiles
    private Collection<Long> prefetchZoomTiles() {

        final List<Long> tiles = new LinkedList<Long>();

        final IMapState state = mapManager.getMapState();
        final int zoom = state.getZoomStep();
        final IPixelConverter converter = mapManager.getConverter();
        final Dimension tileSize = mapManager.getTileSize();
        final double zoomFactor = 1.0 / (1 << zoom - state.getMinZoomStep());
        final int height = (int) (state.getSize().height * zoomFactor);
        final int width = (int) (state.getSize().width * zoomFactor);
        final int x = state.getLocation().x;
        final int y = state.getLocation().y;

        if (zoom > state.getMinZoomStep()) {
            final int zoomedStep = zoom - 1;
            final int zoomedHeight = height * 2;
            final int zoomedWidth = width * 2;
            final int zoomedX = Math.max(0, x - height / 2);
            final int zoomedY = Math.max(0, y - width / 2);

            final int startRow = zoomedY / converter.getCoordDistance(tileSize.height, zoomedStep);
            final int startColumn = zoomedX / converter.getCoordDistance(tileSize.width, zoomedStep);
            final int endRow = (zoomedY + zoomedHeight) / converter.getCoordDistance(tileSize.height, zoomedStep) + 1;
            final int endColumn = (zoomedX + zoomedWidth) / converter.getCoordDistance(tileSize.width, zoomedStep) + 1;

            for (int i = startRow; i < endRow; i++) {
                for (int j = startColumn; j < endColumn; j++) {
                    tiles.add(mapManager.getTile(i, j, zoomedStep).getID());
                }
            }
        }

        if (zoom < state.getMaxZoomStep()) {
            final int zoomedStep = zoom + 1;
            final int zoomedHeight = height / 2;
            final int zoomedWidth = width / 2;
            final int zoomedX = x + zoomedWidth / 2;
            final int zoomedY = y + zoomedHeight / 2;

            final int startRow = zoomedY / converter.getCoordDistance(tileSize.height, zoomedStep);
            final int startColumn = zoomedX / converter.getCoordDistance(tileSize.width, zoomedStep);
            final int endRow = (zoomedY + zoomedHeight) / converter.getCoordDistance(tileSize.height, zoomedStep) + 1;
            final int endColumn = (zoomedX + zoomedWidth) / converter.getCoordDistance(tileSize.width, zoomedStep) + 1;

            for (int i = startRow; i < endRow; i++) {
                for (int j = startColumn; j < endColumn; j++) {
                    tiles.add(mapManager.getTile(i, j, zoomedStep).getID());
                }
            }
        }

        return tiles;
    }

    // make sure this gets called after getNewTiles
    private List<Long> prefetchTiles() {
        final List<Long> tiles = new LinkedList<Long>();

        for (int column = Math.max(lastGridLocation.x - prefetchCount, 0); column < lastGridLocation.x
                + lastColumnCount + prefetchCount; column++) {
            for (int row = Math.max(lastGridLocation.y - prefetchCount, 0); row < lastGridLocation.y; row++) {
                // prefetch tiles which are over the current view
                tiles.add(mapManager.getTile(row, column, lastZoomStep).getID());

            }

            for (int row = lastGridLocation.y + lastRowCount; row < lastGridLocation.y + lastRowCount + prefetchCount; row++) {
                // prefetch tiles which are under the current view
                tiles.add(mapManager.getTile(row, column, lastZoomStep).getID());
            }
        }

        for (int row = lastGridLocation.y; row < lastGridLocation.y + lastRowCount; row++) {
            for (int column = Math.max(lastGridLocation.x - prefetchCount, 0); column < lastGridLocation.x; column++) {
                // prefetch tiles which are to the left of the current view
                tiles.add(mapManager.getTile(row, column, lastZoomStep).getID());
            }

            for (int column = lastGridLocation.x + lastColumnCount; column < lastGridLocation.x + lastColumnCount
                    + prefetchCount; column++) {
                // prefetch tiles which are to the right of the current view
                tiles.add(mapManager.getTile(row, column, lastZoomStep).getID());
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