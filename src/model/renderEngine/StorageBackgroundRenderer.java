package model.renderEngine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import model.elements.IArea;
import model.elements.IBuilding;
import model.elements.IMultiElement;
import model.elements.IStreet;
import model.elements.IWay;
import model.map.IPixelConverter;
import model.map.ITile;

public class StorageBackgroundRenderer extends AbstractRenderer implements IRenderer {

    private final ConcurrentLinkedQueue<Path2D[]> freeList;

    // ways and streets
    private static final WayStyle[] wayStyles;
    private static final int[] wayMinZoomstep;
    private static final int[][] wayOrder;
    // areas
    private static final ShapeStyle[] areaStyles;
    private static final int[] areaMinZoomstep;
    private static final int[] areaOrder;
    // buildings
    private static final ShapeStyle[] buildingStyles;
    private static final int buildingMinZoomstep;

    private static final int maxElements;

    // defines the render styles for all types of streets, ways, areas and
    // buildings
    static {
        // // STREETS AND WAYS ////
        wayMinZoomstep = new int[24];
        wayStyles = new WayStyle[24];

        // pedestrian / living street / residential / unclassified (white +
        // light gray outline)
        wayMinZoomstep[0] = 13;
        wayStyles[0] = new WayStyle(wayMinZoomstep[0], new float[]{1.5f, 3f, 6f, 9f, 12f, 13f, 14f}, new float[]{1.5f,
                3f, 6f, 10f, 13f, 14f, 15f}, Color.WHITE, new Color(200, 200, 200));

        // service (white + light gray outline, small)
        wayMinZoomstep[1] = 15;
        wayStyles[1] = new WayStyle(wayMinZoomstep[1], new float[]{2, 2, 4, 6, 8}, new float[]{2, 2, 4, 7, 9},
                Color.WHITE, new Color(200, 200, 200));

        // secondary (orange)
        wayMinZoomstep[2] = 9;
        wayStyles[2] = new WayStyle(wayMinZoomstep[2], new float[]{1.1225f, 1.25f, 2f, 3f, 6f, 6f, 11f, 11f, 16f},
                new float[]{1.1225f, 1.25f, 2f, 3f, 7f, 7f, 12f, 12f, 17f}, new Color(248, 213, 168), new Color(208,
                        167, 114));

        // tertiary (yellow)
        wayMinZoomstep[3] = 12;
        wayStyles[3] = new WayStyle(wayMinZoomstep[3], new float[]{2f, 6f, 6f, 11f, 11f, 16f}, new float[]{2f, 7f, 7f,
                12f, 12f, 17f}, new Color(248, 248, 186), new Color(200, 200, 200));

        // road (gray + darker gray outline)
        wayMinZoomstep[4] = 13;
        wayStyles[4] = new WayStyle(wayMinZoomstep[4], new float[]{1.5f, 3f, 6f, 9f, 12f, 13f, 14f}, new float[]{2.5f,
                4f, 7f, 10f, 13f, 14f, 15f}, new Color(200, 200, 200), new Color(170, 170, 170));

        // track (transparent white + brown dots/short lines)
        wayMinZoomstep[5] = 15;
        wayStyles[5] = new WayStyle(wayMinZoomstep[5], new float[]{2f, 4f, 6f, 8f, 10f}, new float[]{0, 0, 0, 0},
                new float[]{1f, 2f, 3f, 4f, 5f}, new Color(255, 255, 255, 90), null, new Color(139, 69, 19),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, new float[]{2f, 3f, 6f, 9f, 12f}, new float[]{3f, 4.5f,
                        8f, 12f, 16f});

        // footway (transparent white + light pink dots/short lines)
        wayMinZoomstep[6] = 15;
        wayStyles[6] = new WayStyle(wayMinZoomstep[6], new float[]{2f, 4f, 6f, 8f, 10f}, new float[]{0, 0, 0, 0},
                new float[]{1f, 2f, 3f, 4f, 5f}, new Color(255, 255, 255, 90), null, new Color(250, 150, 150),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, new float[]{2f, 3f, 6f, 9f, 12f}, new float[]{3f, 4.5f,
                        8f, 12f, 16f});

        // cycleway (transparent white + blue dots/short lines)
        wayMinZoomstep[7] = 15;
        wayStyles[7] = new WayStyle(wayMinZoomstep[7], new float[]{2f, 4f, 6f, 8f, 10f}, new float[]{0, 0, 0, 0},
                new float[]{1f, 2f, 3f, 4f, 5f}, new Color(255, 255, 255, 90), null, new Color(64, 71, 245, 200),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, new float[]{2f, 3f, 6f, 9f, 12f}, new float[]{3f, 4.5f,
                        8f, 12f, 16f});

        // bridleway (transparent white + short green lines)
        wayMinZoomstep[8] = 15;
        wayStyles[8] = new WayStyle(wayMinZoomstep[8], new float[]{2f, 4f, 6f, 8f, 10f}, new float[]{0, 0, 0, 0},
                new float[]{1f, 2f, 3f, 4f, 5f}, new Color(255, 255, 255, 90), null, new Color(76, 164, 75),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, new float[]{2f, 3f, 6f, 9f, 12f}, new float[]{3f, 4.5f,
                        8f, 12f, 16f});

        // path (transparent white + short black lines)
        wayMinZoomstep[9] = 15;
        wayStyles[9] = new WayStyle(wayMinZoomstep[9], new float[]{2f, 4f, 6f, 8f, 10f}, new float[]{0, 0, 0, 0},
                new float[]{1f, 2f, 3f, 4f, 5f}, new Color(255, 255, 255, 90), null, new Color(50, 50, 50, 200),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, new float[]{2f, 3f, 6f, 9f, 12f}, new float[]{3f, 4.5f,
                        8f, 12f, 16f});

        // river (light blue big)
        wayMinZoomstep[10] = 10;
        wayStyles[10] = new WayStyle(wayMinZoomstep[10], new float[]{1.1225f, 1.25f, 2f, 3f, 6f, 13f}, new Color(181,
                208, 208));

        // stream (light blue small)
        wayMinZoomstep[11] = 11;
        wayStyles[11] = new WayStyle(wayMinZoomstep[11], new float[]{0.5f, 0.5f, 1, 1.1225f, 1.25f, 2f, 3f, 6f},
                new Color(181, 208, 208));

        // rail/light_rail (black outline + black middleline)
        wayMinZoomstep[12] = 14;
        wayStyles[12] = new WayStyle(wayMinZoomstep[12], new float[]{0, 0, 1, 2, 4, 5}, new float[]{0.1f, 0.2f, 0.4f,
                0.4f, 0.4f, 0.4f}, new float[]{0f, 0f, 2, 4, 8, 10}, null, Color.DARK_GRAY, Color.DARK_GRAY,
                BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, new float[]{1, 1, 1, 1, 1, 1}, new float[]{0, 0, 4, 4, 6,
                        6});

        // tram (small gray)
        wayMinZoomstep[13] = 16;
        wayStyles[13] = new WayStyle(wayMinZoomstep[13], new float[]{0.3f, 0.6f, 1}, new Color(68, 68, 68));

        // primary street (orange + dark orange outline)
        wayMinZoomstep[14] = 7;
        wayStyles[14] = new WayStyle(wayMinZoomstep[14], new float[]{0.5f, 1f, 1.1225f, 1.25f, 2f, 3.5f, 6f, 6f, 11f,
                11f, 16f}, new float[]{0.5f, 1f, 1.1225f, 1.25f, 2f, 3.5f, 7f, 7f, 12f, 12f, 17f}, new Color(248, 178,
                156), new Color(240, 92, 43));

        // motorway (red/pink + gray red outline)
        wayMinZoomstep[15] = 5;
        wayStyles[15] = new WayStyle(wayMinZoomstep[15], new float[]{1, 1, 1.1225f, 1.1225f, 1.1225f, 1.25f, 2f, 4f,
                6f, 13f, 13f, 13f, 17f}, new float[]{1, 1, 1, 1.1225f, 1.1225f, 1.25f, 2f, 4f, 7f, 14f, 14f, 14f, 18f},
                new Color(231, 145, 161), new Color(194, 108, 108));

        // trunk (green + gray green outline)
        wayMinZoomstep[16] = 5;
        wayStyles[16] = new WayStyle(wayMinZoomstep[16], new float[]{1, 1, 1, 1.1225f, 1.1225f, 1.25f, 2f, 4f, 6f, 13f,
                13f, 13f, 17f}, new float[]{1, 1, 1, 1.1225f, 1.1225f, 1.25f, 2f, 4f, 7f, 14f, 14f, 14f, 18f},
                new Color(148, 212, 148), new Color(131, 158, 131));

        // primary_link (see primary)
        wayMinZoomstep[17] = 11;
        wayStyles[17] = new WayStyle(wayMinZoomstep[17], new float[]{1, 1, 3, 3, 7, 7, 11, 11, 11}, new float[]{1, 1,
                3, 4, 8, 8, 12, 12, 12}, new Color(248, 178, 156), new Color(240, 92, 43));

        // motorway_link (see motorway)
        wayMinZoomstep[18] = 11;
        wayStyles[18] = new WayStyle(wayMinZoomstep[18], new float[]{1, 1, 3, 3, 7, 7, 11, 11, 11}, new float[]{1, 1,
                3, 4, 8, 8, 12, 12, 12}, new Color(231, 145, 161), new Color(194, 108, 108));

        // trunk_link (see trunk)
        wayMinZoomstep[19] = 11;
        wayStyles[19] = new WayStyle(wayMinZoomstep[19], new float[]{1, 1, 3, 3, 7, 7, 11, 11, 11}, new float[]{1, 1,
                3, 4, 8, 8, 12, 12, 12}, new Color(148, 212, 148), new Color(131, 158, 131));

        // track[career] (dark green)
        wayMinZoomstep[20] = 17;
        wayStyles[20] = new WayStyle(wayMinZoomstep[20], new float[]{1f, 2f, 3f}, new Color(111, 170, 141));

        // steps (light gray + red lines)
        wayMinZoomstep[21] = 17;
        wayStyles[21] = new WayStyle(wayMinZoomstep[21], new float[]{4, 6}, new float[]{0}, new float[]{4, 6},
                new Color(230, 230, 230), null, new Color(250, 128, 114), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                new float[]{2.25f, 3f}, new float[]{0.75f, 1f});

        // wall (dark gray)
        wayMinZoomstep[22] = 15;
        wayStyles[22] = new WayStyle(wayMinZoomstep[22], new float[]{0.5f, 0.5f, 1f, 1f, 1.5f},
                new Color(158, 158, 158));

        // hedge (green)
        wayMinZoomstep[23] = 15;
        wayStyles[23] = new WayStyle(wayMinZoomstep[23], new float[]{1f, 1.5f, 2f, 2.5f}, new Color(174, 209, 160));

        wayOrder = new int[][]{{10}, {11}, {19}, {17}, {18}, {12}, {22}, {23}, {0, 1}, {3}, {2}, {4}, {16}, {14}, {13},
                {15}, {5}, {6}, {7}, {8}, {9}, {20}, {21}};

        // // AREAS ////
        areaMinZoomstep = new int[25];
        areaStyles = new ShapeStyle[25];

        // forest (dark green)
        areaMinZoomstep[0] = 8;
        areaStyles[0] = new ShapeStyle(areaMinZoomstep[0], 1, new Color(172, 208, 157));

        // wood (dark green)
        areaMinZoomstep[1] = 10;
        areaStyles[1] = new ShapeStyle(areaMinZoomstep[1], 1, new Color(174, 209, 160));

        // scrub (dark green [brighter])
        areaMinZoomstep[2] = 10;
        areaStyles[2] = new ShapeStyle(areaMinZoomstep[2], 1, new Color(181, 226, 180));

        // grass / meadow / grassland ... (light yellow-green)
        areaMinZoomstep[3] = 10;
        areaStyles[3] = new ShapeStyle(areaMinZoomstep[3], 1, new Color(205, 236, 165));

        // grassfield (ligth grey-brown)
        areaMinZoomstep[4] = 10;
        areaStyles[4] = new ShapeStyle(areaMinZoomstep[4], 1, new Color(181, 181, 141));

        // residential / railway (gray)
        areaMinZoomstep[5] = 10;
        areaStyles[5] = new ShapeStyle(areaMinZoomstep[5], new float[]{0}, new float[]{0, 0, 0, 0, 0, 0, 1}, new Color(
                218, 218, 218), new Color(200, 200, 200));

        // water / reservoir (light blue)
        areaMinZoomstep[6] = 8;
        areaStyles[6] = new ShapeStyle(areaMinZoomstep[6], 1, new Color(181, 208, 208));

        // industrial (light purple)
        areaMinZoomstep[7] = 12;
        areaStyles[7] = new ShapeStyle(areaMinZoomstep[7], 1, new Color(223, 209, 214));

        // park (very light green)
        areaMinZoomstep[8] = 14;
        areaStyles[8] = new ShapeStyle(areaMinZoomstep[8], 1, new Color(205, 247, 201));

        // retail (light pink)
        areaMinZoomstep[9] = 12;
        areaStyles[9] = new ShapeStyle(areaMinZoomstep[9], new float[]{0}, new float[]{0, 0, 0, 0, 1}, new Color(240,
                216, 216), new Color(226, 200, 198));

        // heath / fell (light brown)
        areaMinZoomstep[10] = 12;
        areaStyles[10] = new ShapeStyle(areaMinZoomstep[10], 1, new Color(214, 217, 159));

        // sand (light yellow)
        areaMinZoomstep[11] = 13;
        areaStyles[11] = new ShapeStyle(areaMinZoomstep[11], 1, new Color(240, 228, 184));

        // mud /scree (very light pink-grey)
        areaMinZoomstep[12] = 12;
        areaStyles[12] = new ShapeStyle(areaMinZoomstep[12], 1, new Color(228, 219, 208));

        // quarry (gray)
        areaMinZoomstep[13] = 12;
        areaStyles[13] = new ShapeStyle(areaMinZoomstep[13], 1, new Color(195, 195, 195));

        // cemetery (darker green)
        areaMinZoomstep[14] = 13;
        areaStyles[14] = new ShapeStyle(areaMinZoomstep[14], new float[]{0}, new float[]{0, 0, 0, 1}, new Color(170,
                202, 174), new Color(134, 149, 135));

        // parking (light yellow)
        areaMinZoomstep[15] = 15;
        areaStyles[15] = new ShapeStyle(areaMinZoomstep[15], new float[]{0}, new float[]{0, 1},
                new Color(246, 238, 182), new Color(239, 221, 236));

        // pedestrian (light gray)
        areaMinZoomstep[16] = 11;
        areaStyles[16] = new ShapeStyle(areaMinZoomstep[16], new float[]{0}, new float[]{0, 0, 0, 0, 0, 1}, new Color(
                237, 237, 237), new Color(200, 200, 200));

        // farmland (light orange-brown)
        areaMinZoomstep[17] = 10;
        areaStyles[17] = new ShapeStyle(areaMinZoomstep[17], 1, new Color(235, 221, 199));

        // playground (very light turquoise + light blue outline)
        areaMinZoomstep[18] = 15;
        areaStyles[18] = new ShapeStyle(areaMinZoomstep[18], 0, 1, new Color(204, 255, 241), new Color(148, 217, 197));

        // pitch (light turquoise + dark green outline)
        areaMinZoomstep[19] = 14;
        areaStyles[19] = new ShapeStyle(areaMinZoomstep[19], new float[]{0}, new float[]{0, 1},
                new Color(138, 211, 175), new Color(111, 170, 141));

        // sports_centre stadium (turquoise)
        areaMinZoomstep[20] = 14;
        areaStyles[20] = new ShapeStyle(areaMinZoomstep[20], 1, new Color(51, 204, 153));

        // track (light turquoise + dark green outline)
        areaMinZoomstep[21] = 14;
        areaStyles[21] = new ShapeStyle(areaMinZoomstep[21], new float[]{0}, new float[]{0, 1},
                new Color(116, 220, 186), new Color(111, 170, 141));

        // golf_course (light green)
        areaMinZoomstep[22] = 15;
        areaStyles[22] = new ShapeStyle(areaMinZoomstep[22], 1, new Color(181, 226, 181));

        // school university college kindergarten (very light yellow)
        areaMinZoomstep[23] = 13;
        areaStyles[23] = new ShapeStyle(areaMinZoomstep[23], new float[]{0}, new float[]{0, 0, 1}, new Color(240, 240,
                216), new Color(217, 180, 169));

        // zoo (very light green)
        areaMinZoomstep[24] = 14;
        areaStyles[24] = new ShapeStyle(areaMinZoomstep[24], new float[]{0}, new float[]{0, 1},
                new Color(164, 242, 161), new Color(111, 170, 141));

        areaOrder = new int[]{8, 1, 5, 23, 4, 10, 13, 17, 11, 7, 9, 14, 20, 22, 16, 0, 2, 18, 3, 24, 19, 21, 12, 6, 15};

        // // BUILDINGS ////
        buildingStyles = new ShapeStyle[1];

        // default (light gray + gray outline)
        buildingMinZoomstep = 14;
        buildingStyles[0] = new ShapeStyle(buildingMinZoomstep, new float[]{0}, new float[]{0, 0.5f, 0.75f, 1},
                new Color(217, 208, 201), new Color(188, 174, 162));

        maxElements = Math.max(areaStyles.length, wayStyles.length);
    }

    public StorageBackgroundRenderer(final IPixelConverter converter) {
        setConverter(converter);
        freeList = new ConcurrentLinkedQueue<Path2D[]>();
    }

    @Override
    public boolean render(final ITile tile, final Image image) {
        final Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        // TODO better with this hints?
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        final Point tileLocation = getTileLocation(tile, image);

        final Path2D[] paths = setupPaths();
        final boolean rendered = drawAreas(tile, tileLocation, paths, g) | drawBuildings(tile, tileLocation, paths, g)
                | drawWays(tile, tileLocation, paths, g);

        g.dispose();
        freeList.offer(paths);

        if (rendered) {
            fireChange();
        }

        return rendered;
    }

    private Path2D[] setupPaths() {
        Path2D[] paths = freeList.poll();
        if (paths == null) {
            paths = new Path2D.Float[maxElements];
            for (int i = 0; i < paths.length; i++) {
                paths[i] = new Path2D.Float(Path2D.WIND_EVEN_ODD);
            }
        }
        return paths;
    }

    private void clearPaths(final Path2D[] paths, final int max) {
        for (int i = 0; i < max; i++) {
            paths[i].reset();
        }
    }

    private boolean drawAreas(final ITile tile, final Point tileLoc, final Path2D[] paths, final Graphics2D g) {
        final Iterator<IArea> iterator = tile.getTerrain();

        boolean rendered = false;
        final int zoom = tile.getZoomStep();
        clearPaths(paths, areaStyles.length);

        if (!tile.getTerrain().hasNext()) {
            return false;
        }
        while (iterator.hasNext()) {
            final IArea area = iterator.next();

            if (zoom >= areaMinZoomstep[area.getType()]) {
                final Path2D path = paths[area.getType()];
                appendPath(area, tileLoc, zoom, path);
                rendered = true;
                // TODO polygons sometimes do not have same start and endpoint!
                // ... improve this
                path.closePath();
            }
        }

        for (final int i : areaOrder) {
            if (areaStyles[i].mainStroke(g, zoom)) {
                g.fill(paths[i]);
            }

            if (areaStyles[i].outlineStroke(g, zoom)) {
                g.draw(paths[i]);
            }
        }

        return rendered;
    }

    private boolean drawWays(final ITile tile, final Point tileLoc, final Path2D[] paths, final Graphics2D g) {
        Iterator<IStreet> streets = tile.getStreets();
        Iterator<IWay> ways = tile.getWays();

        final int zoom = tile.getZoomStep();
        clearPaths(paths, wayStyles.length);

        if (!appendWays(streets, zoom, paths, tileLoc) & !appendWays(ways, zoom, paths, tileLoc)) {
            return false;
        }

        for (final int[] layer : wayOrder) {
            for (final int way : layer) {
                if (wayMinZoomstep[way] <= zoom && wayStyles[way].outlineStroke(g, zoom)) {
                    g.draw(paths[way]);
                }
            }
            for (final int way : layer) {
                if (wayMinZoomstep[way] <= zoom && wayStyles[way].mainStroke(g, zoom)) {
                    g.draw(paths[way]);
                }
            }
            for (final int way : layer) {
                if (wayMinZoomstep[way] <= zoom && wayStyles[way].middleLineStroke(g, zoom)) {
                    g.draw(paths[way]);
                }
            }
        }

        return true;
    }

    private boolean drawBuildings(final ITile tile, final Point tileLoc, final Path2D[] paths, final Graphics2D g) {
        final int zoom = tile.getZoomStep();
        clearPaths(paths, 1);

        if (zoom < buildingMinZoomstep) {
            return false;
        }

        final Iterator<IBuilding> iterator = tile.getBuildings();
        if (!iterator.hasNext()) {
            return false;
        }

        while (iterator.hasNext()) {
            final IBuilding building = iterator.next();

            appendPath(building, tileLoc, zoom, paths[0]);
        }

        // draw buildings
        if (buildingStyles[0].mainStroke(g, zoom)) {
            g.fill(paths[0]);
        }

        // draw outlines
        if (buildingStyles[0].outlineStroke(g, zoom)) {
            g.draw(paths[0]);
        }

        // draw building numbers

        return true;
    }

    private boolean appendWays(final Iterator<? extends IWay> iterator, final int zoom, final Path2D[] path,
            final Point tileLoc) {
        boolean appended = false;
        while (iterator.hasNext()) {
            final IWay iWay = iterator.next();

            if (wayMinZoomstep[iWay.getType()] <= zoom) {
                appended = true;
                appendPath(iWay, tileLoc, zoom, path[iWay.getType()]);
            }
        }
        return appended;
    }

    private void appendPath(final IMultiElement element, final Point tileLoc, final int zoomStep, final Path2D path) {

        path.moveTo(converter.getPixelDistancef(element.getX(0) - tileLoc.x, zoomStep),
                converter.getPixelDistancef(element.getY(0) - tileLoc.y, zoomStep));

        for (int i = 0; i < element.size(); i++) {
            path.lineTo(converter.getPixelDistancef(element.getX(i) - tileLoc.x, zoomStep),
                    converter.getPixelDistancef(element.getY(i) - tileLoc.y, zoomStep));
        }
    }
}