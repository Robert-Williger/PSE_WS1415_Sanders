package model.renderEngine;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import model.elements.dereferencers.IAreaDereferencer;
import model.elements.dereferencers.IBuildingDereferencer;
import model.elements.dereferencers.IMultiElementDereferencer;
import model.elements.dereferencers.IWayDereferencer;
import model.map.IMapManager;

public class BackgroundRenderer extends AbstractRenderer implements IRenderer {
    private static final Map<RenderingHints.Key, Object> hints;

    private Graphics2D g;
    private ColorScheme colorScheme;
    private final Path2D[] paths;

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
    public boolean render(final long tile, final Image image) {
        setTileID(tile);

        g = (Graphics2D) image.getGraphics();
        g.addRenderingHints(hints);

        final boolean rendered = drawAreas() | drawBuildings() | drawWays();

        g.dispose();

        if (rendered) {
            fireChange();
        }

        return rendered;
    }

    private void clearPaths(final Path2D[] paths, final int max) {
        for (int i = 0; i < max; i++) {
            paths[i].reset();
        }
    }

    private boolean drawAreas() {
        final Iterator<Integer> iterator = tile.getTerrain();
        if (!iterator.hasNext()) {
            return false;
        }

        final ShapeStyle[] areaStyles = colorScheme.getAreaStyles();
        final IAreaDereferencer areaDereferencer = tile.getAreaDereferencer();
        boolean rendered = false;

        clearPaths(paths, areaStyles.length);

        while (iterator.hasNext()) {
            final int area = iterator.next();
            areaDereferencer.setID(area);

            final int type = areaDereferencer.getType();
            if (areaStyles[type].isVisible(zoom)) {
                final Path2D path = paths[type];
                appendPath(path, areaDereferencer);
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
        Iterator<Integer> streets = tile.getStreets();
        Iterator<Integer> ways = tile.getWays();

        final WayStyle[] wayStyles = colorScheme.getWayStyles();
        clearPaths(paths, wayStyles.length);

        if (!appendWays(streets) & !appendWays(ways)) {
            return false;
        }

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

        if (!buildingStyles[0].isVisible(zoom)) {
            return false;
        }

        final Iterator<Integer> iterator = tile.getBuildings();
        if (!iterator.hasNext()) {
            return false;
        }

        final IBuildingDereferencer dereferencer = tile.getBuildingDereferencer();

        while (iterator.hasNext()) {
            final int building = iterator.next();
            dereferencer.setID(building);

            appendPath(paths[0], dereferencer);
        }

        if (buildingStyles[0].mainStroke(g, zoom)) {
            g.fill(paths[0]);
        }

        if (buildingStyles[0].outlineStroke(g, zoom)) {
            g.draw(paths[0]);
        }

        return true;
    }

    private boolean appendWays(final Iterator<Integer> iterator) {
        boolean appended = false;
        final IWayDereferencer wayDereferencer = tile.getWayDereferencer();
        while (iterator.hasNext()) {
            final int way = iterator.next();
            wayDereferencer.setID(way);

            final int type = wayDereferencer.getType();
            if (colorScheme.getWayStyles()[type].isVisible(zoom)) {
                appended = true;
                appendPath(paths[type], wayDereferencer);
            }
        }
        return appended;
    }

    private void appendPath(final Path2D path, final IMultiElementDereferencer element) {

        path.moveTo(converter.getPixelDistancef(element.getX(0) - tile.getX(), tile.getZoomStep()),
                converter.getPixelDistancef(element.getY(0) - tile.getY(), tile.getZoomStep()));

        for (int i = 0; i < element.size(); i++) {
            path.lineTo(converter.getPixelDistancef(element.getX(i) - tile.getX(), tile.getZoomStep()),
                    converter.getPixelDistancef(element.getY(i) - tile.getY(), tile.getZoomStep()));
        }
    }
}