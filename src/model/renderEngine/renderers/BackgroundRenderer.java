package model.renderEngine.renderers;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.Map;
import java.util.PrimitiveIterator.OfInt;

import model.map.IElementIterator;
import model.map.IMapManager;
import model.map.Predicate;
import model.map.accessors.ICollectiveAccessor;
import model.renderEngine.schemes.ColorScheme;
import model.renderEngine.schemes.styles.ShapeStyle;
import model.renderEngine.schemes.styles.WayStyle;

public class BackgroundRenderer extends AbstractRenderer implements IRenderer {
    private static final Map<RenderingHints.Key, Object> hints;

    private ColorScheme colorScheme;

    private IElementIterator areaIterator;
    private IElementIterator buildingIterator;
    private IElementIterator streetIterator;

    public BackgroundRenderer(final IMapManager manager, final ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
        setMapManager(manager);
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
    protected boolean render(final Graphics2D g, int row, int column, int zoom, int x, int y) {
        g.addRenderingHints(hints);

        final int maxElements = Math.max(colorScheme.getAreaStyles().length,
                Math.max(colorScheme.getWayStyles().length, colorScheme.getBuildingStyles().length));

        final Path2D.Float[] paths = new Path2D.Float[maxElements];
        for (int i = 0; i < paths.length; i++) {
            paths[i] = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        }

        return drawAreas(g, paths, row, column, zoom, x, y) | drawBuildings(g, paths, row, column, zoom, x, y)
                | drawWays(g, paths, row, column, zoom, x, y);
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        super.setMapManager(manager);

        areaIterator = manager.getElementIterator("area")
                .filter(createFilter(manager, "area", colorScheme.getAreaStyles()));
        buildingIterator = manager.getElementIterator("building")
                .filter(createFilter(manager, "building", colorScheme.getBuildingStyles()));
        streetIterator = manager.getElementIterator("street")
                .filter(createFilter(manager, "street", colorScheme.getWayStyles()));
    }

    private Predicate createFilter(final IMapManager manager, String name, ShapeStyle[] styles) {
        ICollectiveAccessor accessor = manager.createCollectiveAccessor(name);
        Predicate filter = (id, zoom, leaf) -> {
            accessor.setId(id);
            return leaf || styles[accessor.getType()].isVisible(zoom);
        };
        return filter;
    }

    private void clearPaths(final Path2D[] paths, final int max) {
        for (int i = 0; i < max; i++) {
            paths[i].reset();
        }
    }

    private boolean drawAreas(Graphics2D g, Path2D[] paths, int row, int column, int zoom, int x, int y) {
        final ShapeStyle[] areaStyles = colorScheme.getAreaStyles();
        boolean rendered = fillPaths(paths, areaIterator, "area", areaStyles, row, column, zoom, x, y);

        for (final int area : colorScheme.getAreaOrder()) {
            if (areaStyles[area].mainStroke(g, zoom)) {
                g.fill(paths[area]);
            }

            if (areaStyles[area].outlineStroke(g, zoom)) {
                g.draw(paths[area]);
            }
        }

        return rendered;
    }

    private boolean drawWays(Graphics2D g, Path2D[] paths, int row, int column, int zoom, int x, int y) {
        final WayStyle[] wayStyles = colorScheme.getWayStyles();
        boolean rendered = fillPaths(paths, streetIterator, "street", wayStyles, row, column, zoom, x, y);

        for (final int[] layer : colorScheme.getWayOrder()) {
            for (final int way : layer) {
                if (wayStyles[way].isVisible(zoom) && wayStyles[way].outlineStroke(g, zoom)) {
                    g.draw(paths[way]);
                }
            }
            for (final int way : layer) {
                if (wayStyles[way].isVisible(zoom) && wayStyles[way].mainStroke(g, zoom)) {
                    g.draw(paths[way]);
                }
            }
            for (final int way : layer) {
                if (wayStyles[way].isVisible(zoom) && wayStyles[way].middleLineStroke(g, zoom)) {
                    g.draw(paths[way]);
                }
            }
        }
        return rendered;
    }

    private boolean drawBuildings(Graphics2D g, Path2D[] paths, int row, int column, int zoom, int x, int y) {
        final ShapeStyle[] buildingStyles = colorScheme.getBuildingStyles();

        // TODO update for multiple building styles.
        if (!buildingStyles[0].isVisible(zoom))
            return false;

        boolean rendered = fillPaths(paths, buildingIterator, "building", buildingStyles, row, column, zoom, x, y);

        if (buildingStyles[0].mainStroke(g, zoom)) {
            g.fill(paths[0]);
        }

        if (buildingStyles[0].outlineStroke(g, zoom)) {
            g.draw(paths[0]);
        }
        return rendered;
    }

    private boolean fillPaths(Path2D[] paths, IElementIterator elementIterator, String accessorName,
            ShapeStyle[] styles, int row, int column, int zoom, int x, int y) {
        boolean rendered = false;
        clearPaths(paths, styles.length);

        final ICollectiveAccessor accessor = manager.createCollectiveAccessor(accessorName);
        accessor.setZoom(zoom);
        for (final OfInt iterator = elementIterator.iterator(row, column, zoom); iterator.hasNext();) {
            accessor.setId(iterator.nextInt());
            final int type = accessor.getType();

            if (styles[type].isVisible(zoom)) {
                appendPath(paths[type], accessor, x, y, zoom);
                rendered = true;
            }
        }

        return rendered;
    }

}