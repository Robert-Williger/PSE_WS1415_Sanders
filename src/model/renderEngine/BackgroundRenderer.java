package model.renderEngine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongConsumer;

import model.map.IMapManager;
import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.IStringAccessor;

public class BackgroundRenderer extends AbstractRenderer {
    private static final Map<RenderingHints.Key, Object> hints;

    private Graphics2D g;
    private ColorScheme colorScheme;
    private final Path2D[] paths;
    private ICollectiveAccessor areaAccessor;
    private ICollectiveAccessor buildingAccessor;
    private ICollectiveAccessor streetAccessor;
    private IStringAccessor stringAccessor;

    public long nonGraphicsTime;
    public long graphicsTime;

    private int roadSegments;
    private int junctionSegments;
    private int blockeckSegments;

    private IntWrapper streets;
    private IntWrapper areas;
    private IntWrapper buildings;

    private ArrayList<String> names;

    public BackgroundRenderer(final IMapManager manager, final ColorScheme colorScheme) {
        super(manager);

        final int maxElements = Math.max(colorScheme.getAreaStyles().length,
                Math.max(colorScheme.getWayStyles().length, colorScheme.getBuildingStyles().length));

        this.colorScheme = colorScheme;
        this.paths = new Path2D.Float[maxElements];
        for (int i = 0; i < paths.length; i++) {
            paths[i] = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        }

        streets = new IntWrapper();
        areas = new IntWrapper();
        buildings = new IntWrapper();

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
    protected void render(final Image image) {
        names = new ArrayList<>();

        roadSegments = 0;
        junctionSegments = 0;
        blockeckSegments = 0;

        streets.value = 0;
        areas.value = 0;
        buildings.value = 0;

        long start = System.currentTimeMillis();
        g = (Graphics2D) image.getGraphics();
        g.addRenderingHints(hints);
        graphicsTime = 0;
        nonGraphicsTime = 0;
        graphicsTime += (System.currentTimeMillis() - start);

        drawAreas();
        drawBuildings();
        drawWays();

        g.setColor(Color.BLACK);

        // for (int i = 0; i < names.size(); ++i) {
        // g.drawString(names.get(i), 10, 10 * i + 10);
        // }
        g.drawString("roads: " + roadSegments + ", junctions: " + junctionSegments + ", blocked: " + blockeckSegments,
                10, 10);
        // g.drawString("streets: " + streets.value + ", areas: " + areas.value + ", buildings: " + buildings.value, 10,
        // 10);
        g.setStroke(new BasicStroke(1));
        g.drawRect(0, 0, 256, 256);
        start = System.currentTimeMillis();
        g.dispose();
        graphicsTime += (System.currentTimeMillis() - start);

        if (rendered) {
            fireChange();
        }
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        super.setMapManager(manager);
        areaAccessor = manager.createCollectiveAccessor("area");
        buildingAccessor = manager.createCollectiveAccessor("building");
        streetAccessor = manager.createCollectiveAccessor("street");
        stringAccessor = manager.createStringAccessor();
    }

    private void clearPaths(final Path2D[] paths, final int max) {
        for (int i = 0; i < max; i++) {
            paths[i].reset();
        }
    }

    private void drawAreas() {
        long start = System.currentTimeMillis();

        final int zoom = tileAccessor.getZoom();
        final ShapeStyle[] areaStyles = colorScheme.getAreaStyles();
        clearPaths(paths, areaStyles.length);

        tileAccessor.forEach("area", createConsumer(zoom, areaAccessor, areaStyles, areas));

        nonGraphicsTime += (System.currentTimeMillis() - start);
        start = System.currentTimeMillis();

        for (final int i : colorScheme.getAreaOrder()) {
            if (areaStyles[i].mainStroke(g, zoom)) {
                g.fill(paths[i]);
            }

            if (areaStyles[i].outlineStroke(g, zoom)) {
                g.draw(paths[i]);
            }
        }

        graphicsTime += (System.currentTimeMillis() - start);
    }

    private void drawWays() {
        long start = System.currentTimeMillis();

        final int zoom = tileAccessor.getZoom();
        final WayStyle[] wayStyles = colorScheme.getWayStyles();
        clearPaths(paths, wayStyles.length);

        // TODO improve this!

        tileAccessor.forEach("street", createConsumer(zoom, streetAccessor, wayStyles, streets));

        nonGraphicsTime += (System.currentTimeMillis() - start);
        start = System.currentTimeMillis();

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

        tileAccessor.forEach("street", createStreetConsumer(zoom, streetAccessor, wayStyles));

        graphicsTime += (System.currentTimeMillis() - start);
    }

    private void drawBuildings() {
        long start = System.currentTimeMillis();

        final ShapeStyle[] buildingStyles = colorScheme.getBuildingStyles();
        final int zoom = tileAccessor.getZoom();

        // TODO update for multiple building styles.
        if (!buildingStyles[0].isVisible(zoom)) {
            return;
        }

        clearPaths(paths, buildingStyles.length);
        tileAccessor.forEach("building", createConsumer(zoom, buildingAccessor, buildingStyles, buildings));

        nonGraphicsTime += (System.currentTimeMillis() - start);
        start = System.currentTimeMillis();

        if (buildingStyles[0].mainStroke(g, zoom)) {
            g.fill(paths[0]);
        }

        if (buildingStyles[0].outlineStroke(g, zoom)) {
            g.draw(paths[0]);
        }

        graphicsTime += (System.currentTimeMillis() - start);
    }

    private void appendPath(final Path2D path, final ICollectiveAccessor accessor) {
        // use float for pixel distance with float precission
        final float x = tileAccessor.getX();
        final float y = tileAccessor.getY();
        final int zoom = tileAccessor.getZoom();
        final int size = accessor.size();

        path.moveTo(converter.getPixelDistance(accessor.getX(0) - x, zoom),
                converter.getPixelDistance(accessor.getY(0) - y, zoom));

        for (int i = 1; i < size; i++) {
            path.lineTo(converter.getPixelDistance(accessor.getX(i) - x, zoom),
                    converter.getPixelDistance(accessor.getY(i) - y, zoom));
        }
    }

    private LongConsumer createConsumer(final int zoom, final ICollectiveAccessor accessor, final ShapeStyle[] styles,
            final IntWrapper wrapper) {
        LongConsumer ret = (id) -> {
            accessor.setID(id);
            final int type = accessor.getType();

            if (styles[type].isVisible(zoom)) {
                ++wrapper.value;
                rendered = true;
                final Path2D path = paths[type];
                appendPath(path, accessor);
                // TODO polygons sometimes do not have same start and endpoint!
                // ... improve this
                // path.closePath();
            }
        };
        return ret;
    }

    private LongConsumer createStreetConsumer(final int zoom, final ICollectiveAccessor accessor,
            final ShapeStyle[] styles) {
        return (id) -> {
            accessor.setID(id);
            final int type = accessor.getType();

            final float x = tileAccessor.getX();
            final float y = tileAccessor.getY();
            final int size = accessor.size();

            if (type >= 24) {
                if (type == 24)
                    ++roadSegments;
                else if (type == 25)
                    ++junctionSegments;
                else
                    ++blockeckSegments;

                names.add(stringAccessor.getString(accessor.getAttribute("name")) + ", " + id);
                if (styles[type].isVisible(zoom)) {
                    rendered = true;
                    final int offset = (zoom - 9) / 3;
                    final int dotSize = 2 * offset + 1;
                    g.setColor(new Color(0, 0, 0, 60));
                    g.fillOval((int) converter.getPixelDistance(accessor.getX(0) - x, zoom) - offset,
                            (int) converter.getPixelDistance(accessor.getY(0) - y, zoom) - offset, dotSize, dotSize);

                    g.fillOval((int) converter.getPixelDistance(accessor.getX(size - 1) - x, zoom) - offset,
                            (int) converter.getPixelDistance(accessor.getY(size - 1) - y, zoom) - offset, dotSize,
                            dotSize);
                }
            }
        };
    }

    private static class IntWrapper {
        int value;
    }
}