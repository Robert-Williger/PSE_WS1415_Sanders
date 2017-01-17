package model.renderEngine;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.Map;
import java.util.PrimitiveIterator;

import model.map.IMapManager;
import model.map.accessors.ICollectiveAccessor;

public class BackgroundRenderer extends AbstractRenderer implements IRenderer {
    private static final Map<RenderingHints.Key, Object> hints;

    private Graphics2D g;
    private ColorScheme colorScheme;
    private final Path2D[] paths;
    private ICollectiveAccessor areaAccessor;
    private ICollectiveAccessor buildingAccessor;
    private ICollectiveAccessor wayAccessor;
    private ICollectiveAccessor streetAccessor;

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
        hints = new HashMap<RenderingHints.Key, Object>();
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    }

    @Override
    protected boolean render(final Image image) {
        g = (Graphics2D) image.getGraphics();
        g.addRenderingHints(hints);

        final boolean rendered = drawAreas() | drawBuildings() | drawWays();

        g.dispose();

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
        final PrimitiveIterator.OfLong iterator = tileAccessor.getElements("area");
        if (!iterator.hasNext()) {
            return false;
        }

        final int zoom = tileAccessor.getZoom();
        final ShapeStyle[] areaStyles = colorScheme.getAreaStyles();
        boolean rendered = false;

        clearPaths(paths, areaStyles.length);

        while (iterator.hasNext()) {
            final long area = iterator.nextLong();
            areaAccessor.setID(area);
            final int type = areaAccessor.getType();

            if (areaStyles[type].isVisible(zoom)) {
                final Path2D path = paths[type];
                appendPath(path, areaAccessor);
                rendered = true;
                // TODO polygons sometimes do not have same start and endpoint!
                // ... improve this
                path.closePath();
            }
        }

        for (final int i : colorScheme.getAreaOrder()) {
            if (areaStyles[i].mainStroke(g, zoom)) {
                g.fill(paths[i]);
            }

            if (areaStyles[i].outlineStroke(g, zoom)) {
                g.draw(paths[i]);
            }
        }

        return rendered;
    }

    private boolean drawWays() {
        PrimitiveIterator.OfLong streets = tileAccessor.getElements("street");
        PrimitiveIterator.OfLong ways = tileAccessor.getElements("way");

        final int zoom = tileAccessor.getZoom();
        final WayStyle[] wayStyles = colorScheme.getWayStyles();
        clearPaths(paths, wayStyles.length);

        if (!appendWays(streets, streetAccessor) & !appendWays(ways, wayAccessor)) {
            return false;
        }
        // if (!appendWays(ways, wayAccessor)) {
        // return false;
        // }

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

        return true;
    }

    private boolean drawBuildings() {
        clearPaths(paths, 1);

        final ShapeStyle[] buildingStyles = colorScheme.getBuildingStyles();
        final int zoom = tileAccessor.getZoom();

        if (!buildingStyles[0].isVisible(zoom)) {
            return false;
        }

        final PrimitiveIterator.OfLong iterator = tileAccessor.getElements("building");
        if (!iterator.hasNext()) {
            return false;
        }

        while (iterator.hasNext()) {
            final long buildingID = iterator.nextLong();
            buildingAccessor.setID(buildingID);

            appendPath(paths[0], buildingAccessor);
        }

        if (buildingStyles[0].mainStroke(g, zoom)) {
            g.fill(paths[0]);
        }

        if (buildingStyles[0].outlineStroke(g, zoom)) {
            g.draw(paths[0]);
        }

        return true;
    }

    private boolean appendWays(final PrimitiveIterator.OfLong iterator, final ICollectiveAccessor accessor) {
        boolean appended = false;
        final int zoom = tileAccessor.getZoom();

        while (iterator.hasNext()) {
            final long wayID = iterator.nextLong();
            accessor.setID(wayID);

            final int type = accessor.getType();

            if (colorScheme.getWayStyles()[type].isVisible(zoom)) {
                appended = true;
                appendPath(paths[type], accessor);
            }
        }
        return appended;
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
}