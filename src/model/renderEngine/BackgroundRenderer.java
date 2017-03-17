package model.renderEngine;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongConsumer;

import model.map.IMapManager;
import model.map.accessors.ICollectiveAccessor;

public class BackgroundRenderer extends AbstractRenderer {
    private static final Map<RenderingHints.Key, Object> hints;

    private Graphics2D g;
    private ColorScheme colorScheme;
    private final Path2D[] paths;
    private ICollectiveAccessor areaAccessor;
    private ICollectiveAccessor buildingAccessor;
    private ICollectiveAccessor wayAccessor;
    private ICollectiveAccessor streetAccessor;

    public long nonGraphicsTime;
    public long graphicsTime;

    public BackgroundRenderer(final IMapManager manager, final ColorScheme colorScheme) {
        super(manager);

        final int maxElements = Math.max(colorScheme.getAreaStyles().length,
                Math.max(colorScheme.getWayStyles().length, colorScheme.getBuildingStyles().length));

        this.colorScheme = colorScheme;
        this.paths = new Path2D.Float[maxElements];
        for (int i = 0; i < paths.length; i++) {
            paths[i] = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        }

    }

    static {
        hints = new HashMap<>();
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    }

    @Override
    protected boolean render(final Image image) {
        long start = System.currentTimeMillis();
        g = (Graphics2D) image.getGraphics();
        g.addRenderingHints(hints);
        graphicsTime += (System.currentTimeMillis() - start);

        final boolean rendered = drawAreas() | drawBuildings() | drawWays();

        start = System.currentTimeMillis();
        g.dispose();
        graphicsTime += (System.currentTimeMillis() - start);

        if (rendered) {
            fireChange();
        }

        return rendered;
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        super.setMapManager(manager);
        areaAccessor = manager.createCollectiveAccessor("area");
        buildingAccessor = manager.createCollectiveAccessor("building");
        wayAccessor = manager.createCollectiveAccessor("way");
        streetAccessor = manager.createCollectiveAccessor("street");
    }

    private void clearPaths(final Path2D[] paths, final int max) {
        for (int i = 0; i < max; i++) {
            paths[i].reset();
        }
    }

    private boolean drawAreas() {
        long start = System.currentTimeMillis();

        // final PrimitiveIterator.OfLong iterator = tileAccessor.getElements("area");
        // if (!iterator.hasNext()) {
        // return false;
        // }

        final int zoom = tileAccessor.getZoom();
        final ShapeStyle[] areaStyles = colorScheme.getAreaStyles();
        clearPaths(paths, areaStyles.length);

        tileAccessor.forEach("area", createConsumer(zoom, areaAccessor, areaStyles));

        // while (iterator.hasNext()) {
        // final long area = iterator.nextLong();
        // areaAccessor.setID(area);
        // final int type = areaAccessor.getType();
        //
        // if (areaStyles[type].isVisible(zoom)) {
        // final Path2D path = paths[type];
        // appendPath(path, areaAccessor);
        // rendered = true;
        // // TODO polygons sometimes do not have same start and endpoint!
        // // ... improve this
        // path.closePath();
        // }
        // }

        nonGraphicsTime += (System.currentTimeMillis() - start);
        start = System.currentTimeMillis();

        boolean rendered = false;
        for (final int i : colorScheme.getAreaOrder()) {
            if (areaStyles[i].mainStroke(g, zoom)) {
                g.fill(paths[i]);
                rendered = true;
            }

            if (areaStyles[i].outlineStroke(g, zoom)) {
                g.draw(paths[i]);
                rendered = true;
            }
        }

        graphicsTime += (System.currentTimeMillis() - start);
        return rendered;
    }

    private boolean drawWays() {
        long start = System.currentTimeMillis();

        final int zoom = tileAccessor.getZoom();
        final WayStyle[] wayStyles = colorScheme.getWayStyles();
        clearPaths(paths, wayStyles.length);

        // TODO improve this!

        tileAccessor.forEach("street", createConsumer(zoom, streetAccessor, wayStyles));
        tileAccessor.forEach("way", createConsumer(zoom, wayAccessor, wayStyles));

        nonGraphicsTime += (System.currentTimeMillis() - start);
        start = System.currentTimeMillis();

        boolean rendered = false;
        for (final int[] layer : colorScheme.getWayOrder()) {
            for (final int way : layer) {
                if (wayStyles[way].isVisible(zoom) && wayStyles[way].outlineStroke(g, zoom)) {
                    g.draw(paths[way]);
                    rendered = true;
                }
            }
            for (final int way : layer) {
                if (wayStyles[way].isVisible(zoom) && wayStyles[way].mainStroke(g, zoom)) {
                    g.draw(paths[way]);
                    rendered = true;
                }
            }
            for (final int way : layer) {
                if (wayStyles[way].isVisible(zoom) && wayStyles[way].middleLineStroke(g, zoom)) {
                    g.draw(paths[way]);
                    rendered = true;
                }
            }
        }

        graphicsTime += (System.currentTimeMillis() - start);

        return rendered;
    }

    private boolean drawBuildings() {
        long start = System.currentTimeMillis();

        final ShapeStyle[] buildingStyles = colorScheme.getBuildingStyles();
        final int zoom = tileAccessor.getZoom();

        if (!buildingStyles[0].isVisible(zoom)) {
            return false;
        }

        clearPaths(paths, 1);
        final LongConsumer consumer = (building) -> {
            buildingAccessor.setID(building);

            appendPath(paths[0], buildingAccessor);
        };
        tileAccessor.forEach("building", consumer);

        nonGraphicsTime += (System.currentTimeMillis() - start);
        start = System.currentTimeMillis();
        boolean rendered = false;

        if (buildingStyles[0].mainStroke(g, zoom)) {
            g.fill(paths[0]);
            rendered = true;
        }

        if (buildingStyles[0].outlineStroke(g, zoom)) {
            g.draw(paths[0]);
            rendered = true;
        }

        graphicsTime += (System.currentTimeMillis() - start);

        return rendered;
    }

    private void appendPath(final Path2D path, final ICollectiveAccessor accessor) {
        final int x = tileAccessor.getX();
        final int y = tileAccessor.getY();
        final int zoom = tileAccessor.getZoom();
        final int size = accessor.size();

        path.moveTo(converter.getPixelDistancef(accessor.getX(0) - x, zoom),
                converter.getPixelDistancef(accessor.getY(0) - y, zoom));

        for (int i = 1; i < size; i++) {
            path.lineTo(converter.getPixelDistancef(accessor.getX(i) - x, zoom),
                    converter.getPixelDistancef(accessor.getY(i) - y, zoom));
        }
    }

    private LongConsumer createConsumer(final int zoom, final ICollectiveAccessor accessor, final ShapeStyle[] styles) {
        return (area) -> {
            accessor.setID(area);
            final int type = accessor.getType();

            if (styles[type].isVisible(zoom)) {
                final Path2D path = paths[type];
                appendPath(path, accessor);
                // TODO polygons sometimes do not have same start and endpoint!
                // ... improve this
                // path.closePath();
            }
        };
    }
}