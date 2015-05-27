package model.renderEngine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.AbstractModel;
import model.elements.Area;
import model.elements.Building;
import model.elements.Node;
import model.elements.Street;
import model.elements.Way;
import model.map.IPixelConverter;
import model.map.ITile;

public class BackgroundRenderer extends AbstractModel implements IRenderer {

    private static final float REFERENCE_DISTANCE_COORD;
    private static final float REFERENCE_DISTANCE_PIXEL;
    private static int minZoomstepOffset;
    // streets
    private static final int[] streetType;
    private static final Font streetNameFont;
    private static final int streetNameMinZoomstep;
    // ways
    private static final int[] wayType;
    // ways and streets
    private static final WayStyle[] wayStyles;
    private static final int[] wayMinZoomstep;
    private static final int[][] wayOrder;
    // areas
    private static final ShapeStyle[] areaStyles;
    private static final int[] areaType;
    private static final int[] areaOrder;
    // buildings
    private static final ShapeStyle[] buildingStyles;
    private static final int buildingMinZoomstep;
    private static final int buildingNumberMinZoomstep;
    private static final Font buildingNumberFont;
    private static final Color buildingNumberColor;

    private IPixelConverter converter;

    // defines the render styles for all types of streets, ways, areas and
    // buildings
    static {

        REFERENCE_DISTANCE_COORD = 10000f;
        REFERENCE_DISTANCE_PIXEL = 39f;

        final int streets = 14;
        final int ways = 15;

        // // STREETS AND WAYS ////
        streetNameFont = new Font("Times New Roman", Font.PLAIN, 12);
        streetNameMinZoomstep = 7;
        streetType = new int[streets];
        wayMinZoomstep = new int[streets + ways];
        wayStyles = new WayStyle[streets + ways];

        // default (white + light gray outline)
        wayStyles[0] = new WayStyle(new float[]{8f, 15f}, new float[]{9f, 17f}, Color.WHITE, new Color(200, 200, 200));
        streetType[0] = 0;
        wayMinZoomstep[0] = 4;

        // secondary (orange)
        wayStyles[1] = new WayStyle(new float[]{13f, 15f}, new float[]{15f, 17f}, new Color(248, 213, 168), new Color(
                208, 167, 114));
        streetType[1] = 1;
        wayMinZoomstep[1] = 2;

        // tertiary (yellow)
        wayStyles[2] = new WayStyle(new float[]{8f, 15f}, new float[]{9f, 17f}, new Color(248, 248, 186), new Color(
                200, 200, 200));
        streetType[2] = 2;
        wayMinZoomstep[2] = 2;

        // unclassified (see [0] default)
        wayStyles[3] = wayStyles[0];
        streetType[3] = 0;
        wayMinZoomstep[3] = 2;

        // residential (see [0] default)
        wayStyles[4] = wayStyles[0];
        streetType[4] = 0;
        wayMinZoomstep[4] = 2;

        // service (see [0] default, small)
        wayStyles[5] = new WayStyle(new float[]{4f, 7f}, new float[]{5f, 9f}, Color.WHITE, new Color(200, 200, 200));
        streetType[5] = 5;
        wayMinZoomstep[5] = 4;

        // living street (see [0] default)
        wayStyles[6] = wayStyles[0];
        streetType[6] = 0;
        wayMinZoomstep[6] = 4;

        // pedestrian (see [0] default)
        wayStyles[7] = wayStyles[0];
        streetType[7] = 0;
        wayMinZoomstep[7] = 4;

        // road (gray + darker gray outline)
        wayStyles[8] = new WayStyle(new float[]{8f, 15f}, new float[]{9f, 17f}, new Color(200, 200, 200), new Color(
                170, 170, 170));
        streetType[8] = 8;
        wayMinZoomstep[8] = 3;

        // track (transparent white + brown dots/short lines)
        wayStyles[9] = new WayStyle(new float[]{2.5f, 8f}, new float[]{0f, 0f}, new float[]{1.5f, 4f}, new Color(255,
                255, 255, 90), null, new Color(139, 69, 19), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                new float[]{3f, 9f}, new float[]{4f, 12f});
        streetType[9] = 9;
        wayMinZoomstep[9] = 4;

        // footway (transparent white + light pink dots/short lines)
        wayStyles[10] = new WayStyle(new float[]{2.5f, 8f}, new float[]{0f, 0f}, new float[]{1.5f, 4f}, new Color(255,
                255, 255, 90), null, new Color(250, 150, 150), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                new float[]{3f, 9f}, new float[]{4f, 12f});
        streetType[10] = 10;
        wayMinZoomstep[10] = 4;

        // cycleway (transparent white + blue dots/short lines)
        wayStyles[11] = new WayStyle(new float[]{2.5f, 8f}, new float[]{0f, 0f}, new float[]{1.5f, 4f}, new Color(255,
                255, 255, 90), null, new Color(64, 71, 245, 200), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                new float[]{3f, 9f}, new float[]{4f, 12f});
        streetType[11] = 11;
        wayMinZoomstep[11] = 4;

        // bridleway (transparent white + short green lines)
        wayStyles[12] = new WayStyle(new float[]{2.5f, 8f}, new float[]{0f, 0f}, new float[]{1.5f, 4f}, new Color(255,
                255, 255, 90), null, new Color(76, 164, 75), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                new float[]{3f, 9f}, new float[]{4f, 12f});
        streetType[12] = 12;
        wayMinZoomstep[12] = 4;

        // path (transparent white + short black lines)
        wayStyles[13] = new WayStyle(new float[]{2.5f, 8f}, new float[]{0f, 0f}, new float[]{1.5f, 4f}, new Color(255,
                255, 255, 90), null, new Color(50, 50, 50, 200), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                new float[]{3f, 9f}, new float[]{4f, 12f});
        streetType[13] = 13;
        wayMinZoomstep[13] = 4;

        // // WAYS ////
        // indicates at which index relative to streetOrder the ways should be
        // rendered
        wayType = new int[ways];

        // default (gray)
        wayStyles[streets] = new WayStyle(new float[]{8f, 16f}, Color.GRAY, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND);
        wayType[0] = streets;
        wayMinZoomstep[0] = 2;

        // river (light blue big)
        wayStyles[streets + 1] = new WayStyle(new float[]{20f, 20f}, new Color(181, 208, 208));
        wayType[1] = streets + 1;
        wayMinZoomstep[wayType[1]] = 0;

        // stream (light blue small)
        wayStyles[streets + 2] = new WayStyle(5f, new Color(181, 208, 208));
        wayType[2] = streets + 2;
        wayMinZoomstep[wayType[2]] = 1;

        // rail/light_rail (black outline + black middleline - large)
        wayStyles[streets + 3] = new WayStyle(new float[]{1.1f, 4.125f}, new float[]{0.3f, 1.125f}, new float[]{2.2f,
                8.25f}, null, Color.DARK_GRAY, Color.DARK_GRAY, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                new float[]{0.6f, 2.25f}, new float[]{2.5f, 9.375f});
        wayType[3] = streets + 3;
        wayMinZoomstep[wayType[3]] = 4;

        // tram (small gray)
        wayStyles[streets + 4] = new WayStyle(new float[]{0.3f, 2}, new Color(68, 68, 68));
        wayType[4] = streets + 4;
        wayMinZoomstep[wayType[4]] = 6;

        // primary street (orange + dark orange outline)
        wayStyles[streets + 5] = new WayStyle(new float[]{14, 15}, new float[]{16, 17}, new Color(230, 165, 65),
                new Color(189, 113, 0));
        wayType[5] = streets + 5;
        wayMinZoomstep[wayType[5]] = 1;

        // motorway (red/pink + gray red outline)
        wayStyles[streets + 6] = new WayStyle(new float[]{37, 15}, new float[]{39, 17}, new Color(220, 158, 158),
                new Color(194, 108, 108));
        wayType[6] = streets + 6;
        wayMinZoomstep[wayType[6]] = 0;

        // trunk (green + gray green outline)
        wayStyles[streets + 7] = new WayStyle(new float[]{14, 15}, new float[]{15, 17}, new Color(148, 212, 148),
                new Color(131, 158, 131));
        wayType[7] = streets + 7;
        wayMinZoomstep[wayType[7]] = 2;

        // primary_link (see primary)
        wayStyles[streets + 8] = new WayStyle(new float[]{10, 12}, new float[]{12, 14}, new Color(230, 165, 65),
                new Color(189, 113, 0));
        wayType[8] = streets + 8;
        wayMinZoomstep[wayType[8]] = 4;

        // motorway_link (see motorway)
        wayStyles[streets + 9] = new WayStyle(new float[]{10, 12}, new float[]{12, 14}, new Color(220, 158, 158),
                new Color(194, 108, 108));
        wayType[9] = streets + 9;
        wayMinZoomstep[wayType[9]] = 3;

        // trunk_link (see trunk)
        wayStyles[streets + 10] = new WayStyle(new float[]{10, 12}, new float[]{12, 14}, new Color(148, 212, 148),
                new Color(131, 158, 131));
        wayType[10] = streets + 10;
        wayMinZoomstep[wayType[10]] = 4;

        // track[career] (dark green)
        wayStyles[streets + 11] = new WayStyle(new float[]{1f, 3f}, new Color(111, 170, 141));
        wayType[11] = streets + 11;
        wayMinZoomstep[wayType[11]] = 7;

        // steps (light gray + red lines)
        wayStyles[streets + 12] = new WayStyle(new float[]{2f, 6f}, new float[]{0f, 0f}, new float[]{2f, 6f},
                new Color(230, 230, 230), null, new Color(250, 128, 114), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                new float[]{0.75f, 3f}, new float[]{0.25f, 1f});
        wayType[12] = streets + 12;
        wayMinZoomstep[wayType[12]] = 7;

        // wall (dark gray)
        wayStyles[streets + 13] = new WayStyle(new float[]{0.3f, 1}, new Color(158, 158, 158));
        wayType[13] = streets + 13;
        wayMinZoomstep[wayType[13]] = 5;

        // hedge (green)
        wayStyles[streets + 14] = new WayStyle(new float[]{0.75f, 2.5f}, new Color(174, 209, 160));
        wayType[14] = streets + 14;
        wayMinZoomstep[wayType[14]] = 5;

        // wayOrder = new int[]{wayType[1], wayType[2], wayType[0], wayType[10],
        // wayType[8], wayType[9], wayType[3], 7, 5,
        // 0, 2, 1, 8, wayType[7], wayType[5], wayType[4], wayType[6], 9, 10,
        // 11, 12, 13, wayType[11],
        // wayType[12], wayType[13], wayType[14]};

        wayOrder = new int[][]{{wayType[1]}, {wayType[2]}, {wayType[0]}, {wayType[10]}, {wayType[8]}, {wayType[9]},
                {wayType[3]}, {wayType[13]}, {wayType[14]}, {0, 3, 4, 5, 6, 7}, {2}, {1}, {8}, {wayType[7]},
                {wayType[5]}, {wayType[4]}, {wayType[6]}, {9}, {10}, {11}, {12}, {13}, {wayType[11]}, {wayType[12]}};

        // // AREAS ////
        areaStyles = new ShapeStyle[25];
        areaType = new int[25];

        // default (empty)
        areaStyles[0] = new ShapeStyle(0f, null);
        areaType[0] = 0;

        // forest (dark green)
        areaStyles[1] = new ShapeStyle(1f, new Color(160, 206, 133));
        areaType[1] = 1;

        // wood (dark green [brighter])
        areaStyles[2] = new ShapeStyle(1f, new Color(174, 209, 160));
        areaType[2] = 2;

        // grass / meadow / grassland ... (light yellow-green)
        areaStyles[3] = new ShapeStyle(1f, new Color(205, 236, 165));
        areaType[3] = 3;

        // grassfield (ligth grey-brown)
        areaStyles[4] = new ShapeStyle(1f, new Color(181, 181, 141));
        areaType[4] = 4;

        // residential / railway (gray)
        areaStyles[5] = new ShapeStyle(new float[]{1f, 1f}, new float[]{1f, 1f}, new Color(218, 218, 218), new Color(
                200, 200, 200));
        areaType[5] = 5;

        // water / reservoir (light blue)
        areaStyles[6] = new ShapeStyle(1f, new Color(181, 208, 208));
        areaType[6] = 6;

        // industrial (light purple)
        areaStyles[7] = new ShapeStyle(1f, new Color(223, 209, 214));
        areaType[7] = 7;

        // retail (light pink)
        areaStyles[8] = new ShapeStyle(new float[]{1f, 1f}, new float[]{1f, 1f}, new Color(240, 216, 216), new Color(
                226, 200, 198));
        areaType[8] = 8;

        // heath / fell (light brown)
        areaStyles[9] = new ShapeStyle(1f, new Color(214, 217, 159));
        areaType[9] = 9;

        // sand (light yellow)
        areaStyles[10] = new ShapeStyle(1f, new Color(240, 228, 184));
        areaType[10] = 10;

        // mud /scree (very light pink-grey)
        areaStyles[11] = new ShapeStyle(1f, new Color(228, 219, 208));
        areaType[11] = 11;

        // quarry (gray)
        areaStyles[12] = new ShapeStyle(1f, new Color(195, 195, 195));
        areaType[12] = 12;

        // cemetery (darker green)
        areaStyles[13] = new ShapeStyle(new float[]{1f, 1f}, new float[]{1f, 1f}, new Color(170, 202, 174), new Color(
                134, 149, 135));
        areaType[13] = 13;

        // parking (light yellow)
        areaStyles[14] = new ShapeStyle(new float[]{1f, 1f}, new float[]{1f, 1f}, new Color(246, 238, 182), new Color(
                239, 221, 236));
        areaType[14] = 14;

        // pedestrian (light gray)
        areaStyles[15] = new ShapeStyle(new float[]{1f, 1f}, new float[]{1f, 1f}, new Color(237, 237, 237), new Color(
                200, 200, 200));
        areaType[15] = 15;

        // farmland (light orange-brown)
        areaStyles[16] = new ShapeStyle(1f, new Color(235, 221, 199));
        areaType[16] = 16;

        // playground (very light turquoise + light blue outline)
        areaStyles[17] = new ShapeStyle(new float[]{1f, 1f}, new float[]{2f, 2f}, new Color(204, 255, 241), new Color(
                148, 217, 197));
        areaType[17] = 17;

        // pitch (light turquoise + dark green outline)
        areaStyles[18] = new ShapeStyle(new float[]{1f, 1f}, new float[]{1f, 1f}, new Color(138, 211, 175), new Color(
                111, 170, 141));
        areaType[18] = 18;

        // sports_centre stadium (turquoise)
        areaStyles[19] = new ShapeStyle(1f, new Color(51, 204, 153));
        areaType[19] = 19;

        // track (light turquoise + dark green outline)
        areaStyles[20] = new ShapeStyle(new float[]{1f, 1f}, new float[]{1f, 1f}, new Color(116, 220, 186), new Color(
                111, 170, 141));
        areaType[20] = 20;

        // golf_course (light green)
        areaStyles[21] = new ShapeStyle(1f, new Color(181, 226, 181));
        areaType[21] = 21;

        // school university college kindergarten (very light yellow)
        areaStyles[22] = new ShapeStyle(new float[]{1f, 1f}, new float[]{1f, 1f}, new Color(240, 240, 216), new Color(
                217, 180, 169));
        areaType[22] = 22;

        // zoo (very light green)
        areaStyles[23] = new ShapeStyle(new float[]{1f, 1f}, new float[]{1f, 1f}, new Color(164, 242, 161), new Color(
                111, 170, 141));
        areaType[23] = 23;

        // park (very light green)
        areaStyles[24] = new ShapeStyle(1f, new Color(205, 247, 201));
        areaType[24] = 24;

        areaOrder = new int[]{0, 24, 2, 22, 4, 5, 3, 9, 12, 16, 10, 7, 8, 13, 19, 21, 17, 15, 1, 23, 18, 20, 11, 6, 14};

        // // BUILDINGS ////
        buildingStyles = new ShapeStyle[1];

        // default (light gray + gray outline)
        buildingStyles[0] = new ShapeStyle(new float[]{15f, 15f}, new float[]{0.5f, 1f}, new Color(216, 208, 201),
                new Color(170, 170, 170));
        buildingMinZoomstep = 5;
        buildingNumberMinZoomstep = 8;
        buildingNumberFont = new Font("Times New Roman", Font.PLAIN, 10);
        buildingNumberColor = new Color(96, 96, 96);
    }

    public BackgroundRenderer(final IPixelConverter converter) {
        setConverter(converter);
    }

    private static void calculateZoomOffset(final IPixelConverter converter) {
        minZoomstepOffset = 9;
        while (converter.getPixelDistancef(REFERENCE_DISTANCE_COORD, 5 + minZoomstepOffset) - REFERENCE_DISTANCE_PIXEL > 10f
                && Math.abs(minZoomstepOffset) < 10) {
            minZoomstepOffset--;
        }
    }

    @Override
    public boolean render(final ITile tile, final Image image) {
        if (tile == null || image == null) {
            return false;
        }

        final Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Map<?, ?> map = (Map<?, ?>) (tk.getDesktopProperty("awt.font.desktophints"));
        if (map != null) {
            g.addRenderingHints(map);
        }

        if (drawAreas(tile, g) && drawWays(tile, g) && drawBuildings(tile, g)) {// &&
                                                                                // drawStreetNames(tile,
                                                                                // g))
                                                                                // {

            g.dispose();
            fireChange();
            return true;
        }

        g.dispose();
        return false;
    }

    private boolean drawStreetNames(final ITile tile, final Graphics2D g) {
        final Font font = streetNameFont;
        final FontMetrics metrics = g.getFontMetrics(font);
        final int zoom = tile.getZoomStep();
        final Point tileLoc = tile.getLocation();
        final int middleOffset = (metrics.getAscent() - metrics.getDescent()) / 2;
        g.setColor(Color.BLACK);

        for (final Street street : tile.getStreets()) {
            final String text = street.getName();
            if (streetNameMinZoomstep + minZoomstepOffset <= tile.getZoomStep() && !text.equals("Unbekannte Straße")) {

                final int textSize = metrics.stringWidth(text);
                int distanceCount = 0;
                final int length = converter.getPixelDistance(street.getLength(), tile.getZoomStep());

                if (length > textSize) {

                    final List<Node> nodes = street.getNodes();

                    final Iterator<Node> iterator = nodes.iterator();
                    Point fromCoord = iterator.next().getLocation();

                    while (iterator.hasNext()) {
                        final Point toCoord = iterator.next().getLocation();

                        final int distance = converter.getPixelDistance((int) toCoord.distance(fromCoord), zoom);
                        final int start = (distance - textSize) / 2;

                        if (start >= 5 && distanceCount + start >= 0) {
                            distanceCount = -300;
                            final Point2D.Double from;
                            final Point2D.Double to;

                            double direction = getAngle(fromCoord, toCoord);

                            if (direction > -Math.PI / 2 && direction < Math.PI / 2) {
                                from = new Point2D.Double(converter.getPixelDistancef(fromCoord.x - tileLoc.x, zoom),
                                        converter.getPixelDistancef(fromCoord.y - tileLoc.y, zoom));
                                to = new Point2D.Double(converter.getPixelDistancef(toCoord.x - tileLoc.x, zoom),
                                        converter.getPixelDistancef(toCoord.y - tileLoc.y, zoom));
                            } else {
                                to = new Point2D.Double(converter.getPixelDistancef(fromCoord.x - tileLoc.x, zoom),
                                        converter.getPixelDistancef(fromCoord.y - tileLoc.y, zoom));
                                from = new Point2D.Double(converter.getPixelDistancef(toCoord.x - tileLoc.x, zoom),
                                        converter.getPixelDistancef(toCoord.y - tileLoc.y, zoom));
                                direction += Math.PI;
                            }

                            final double xDist = to.getX() - from.getX();
                            final double yDist = to.getY() - from.getY();
                            final double scale = (double) start / distance;

                            final int offset = street.getType() < 9 ? middleOffset : middleOffset + 8;
                            final AffineTransform at = AffineTransform.getTranslateInstance(
                                    from.x - Math.sin(direction) * offset + xDist * scale, from.y + Math.cos(direction)
                                            * offset + yDist * scale);
                            at.rotate(direction);

                            g.setFont(font.deriveFont(at));
                            g.drawString(text, 0, 0);
                        }

                        distanceCount = Math.min(0, distance + distanceCount);
                        fromCoord = toCoord;
                    }
                }
            }
        }

        // final Rectangle tileRect = new Rectangle(tileLoc, new
        // Dimension(converter.getCoordDistance(256, zoom),
        // converter.getCoordDistance(256, zoom)));
        // for (final Way way : tile.getWays()) {
        // final String text = way.getName();
        // final int textSize = metrics.stringWidth(text);
        // int distanceCount = 0;
        //
        // if ((way.getType() != 3 && way.getType() != 4)
        // && wayMinZoomstep[wayType[way.getType()]] + minZoomstepOffset <=
        // tile.getZoomStep()
        // && !text.equals("Unbekannte Straße")) {
        //
        // final List<Node> nodes = way.getNodes();
        //
        // final Iterator<Node> iterator = nodes.iterator();
        // Point fromCoord = iterator.next().getLocation();
        //
        // while (iterator.hasNext()) {
        // final Point toCoord = iterator.next().getLocation();
        // final int distance = converter.getPixelDistance((int)
        // toCoord.distance(fromCoord), zoom);
        //
        // final int start = (distance - textSize) / 2;
        //
        // if (start >= 5 && distanceCount + start >= 0) {
        // distanceCount = -300;
        //
        // if (tileRect.intersectsLine(new Line2D.Float(fromCoord, toCoord))) {
        // final Point2D.Double from;
        // final Point2D.Double to;
        //
        // double direction = getAngle(fromCoord, toCoord);
        //
        // if (direction > -Math.PI / 2 && direction < Math.PI / 2) {
        // from = new Point2D.Double(converter.getPixelDistancef(fromCoord.x -
        // tileLoc.x, zoom),
        // converter.getPixelDistancef(fromCoord.y - tileLoc.y, zoom));
        // to = new Point2D.Double(converter.getPixelDistancef(toCoord.x -
        // tileLoc.x, zoom),
        // converter.getPixelDistancef(toCoord.y - tileLoc.y, zoom));
        // } else {
        // to = new Point2D.Double(converter.getPixelDistancef(fromCoord.x -
        // tileLoc.x, zoom),
        // converter.getPixelDistancef(fromCoord.y - tileLoc.y, zoom));
        // from = new Point2D.Double(converter.getPixelDistancef(toCoord.x -
        // tileLoc.x, zoom),
        // converter.getPixelDistancef(toCoord.y - tileLoc.y, zoom));
        // direction += Math.PI;
        // }
        //
        // final double xDist = to.getX() - from.getX();
        // final double yDist = to.getY() - from.getY();
        // final double scale = (double) start / distance;
        //
        // final AffineTransform at = AffineTransform.getTranslateInstance(
        // from.x - Math.sin(direction) * middleOffset + xDist * scale,
        // from.y + Math.cos(direction) * middleOffset + yDist * scale);
        // at.rotate(direction);
        //
        // if (way.getType() == 1 || way.getType() == 2) {
        // final TextLayout layout = new TextLayout(text, font.deriveFont(14f),
        // g.getFontRenderContext());
        // g.setColor(Color.white);
        // g.setStroke(new BasicStroke(0.25f));
        // g.draw(layout.getOutline(at));
        //
        // g.setColor(new Color(102, 153, 204));
        // at.translate(-Math.sin(direction), -Math.cos(direction));
        // g.setFont(font.deriveFont(at).deriveFont(14f));
        // } else {
        // g.setColor(Color.black);
        // g.setFont(font.deriveFont(at));
        // }
        //
        // g.drawString(text, 0, 0);
        // }
        // }
        //
        // distanceCount = Math.min(0, distance + distanceCount);
        // fromCoord = toCoord;
        // }
        // }
        // }

        return true;
    }

    private boolean drawAreas(final ITile tile, final Graphics2D g) {
        if (tile.getTerrain() == null) {
            return false;
        }

        final Point tileLoc = tile.getLocation();
        final Path2D.Float[] path = new Path2D.Float[areaStyles.length];

        for (int i = 0; i < path.length; i++) {
            path[i] = new Path2D.Float(i != 18 ? Path2D.WIND_EVEN_ODD : Path2D.WIND_NON_ZERO);
        }

        for (final Area area : tile.getTerrain()) {
            if (area == null) {
                return false;
            }
            path[area.getType()].append(
                    convertPolygon(area.getPolygon(), tileLoc, tile.getZoomStep()).getPathIterator(null), false);
        }

        for (final int i : areaOrder) {
            if (areaStyles[i].mainStroke(g, tile.getZoomStep())) {
                g.fill(path[i]);
            }

            if (areaStyles[i].outlineStroke(g, tile.getZoomStep())) {
                g.draw(path[i]);
            }
        }

        return true;
    }

    private boolean drawWays(final ITile tile, final Graphics2D g) {
        if (tile.getStreets() == null) {
            return false;
        }
        if (tile.getWays() == null) {
            return false;
        }

        final Point tileLoc = tile.getLocation();
        final Path2D.Float[] path = new Path2D.Float[wayStyles.length];

        for (int i = 0; i < path.length; i++) {
            path[i] = new Path2D.Float();
        }

        for (final Street street : tile.getStreets()) {
            if (street == null) {
                return false;
            }
            if (wayMinZoomstep[street.getType()] + minZoomstepOffset <= tile.getZoomStep()) {
                appendPath(street.getNodes(), tileLoc, tile.getZoomStep(), path[street.getType()]);
            }
        }

        for (final Way way : tile.getWays()) {
            if (way == null) {
                return false;
            }
            if (wayMinZoomstep[wayType[way.getType()]] + minZoomstepOffset <= tile.getZoomStep()) {
                appendPath(way.getNodes(), tileLoc, tile.getZoomStep(), path[wayType[way.getType()]]);
            }
        }

        for (final int[] layer : wayOrder) {
            for (final int way : layer) {
                if (wayMinZoomstep[way] + minZoomstepOffset <= tile.getZoomStep()) {
                    if (wayStyles[way].outlineCompositeStroke(g, tile.getZoomStep())) {
                        g.draw(path[way]);
                    } else if (wayStyles[way].outlineStroke(g, tile.getZoomStep())) {
                        g.draw(path[way]);
                    }
                }
            }
            for (final int way : layer) {
                if (wayMinZoomstep[way] + minZoomstepOffset <= tile.getZoomStep()) {

                    if (wayStyles[way].mainStroke(g, tile.getZoomStep())) {
                        g.draw(path[way]);
                    }
                }
            }
            for (final int way : layer) {
                if (wayMinZoomstep[way] + minZoomstepOffset <= tile.getZoomStep()) {
                    if (wayStyles[way].middleLineStroke(g, tile.getZoomStep())) {
                        g.draw(path[way]);
                    }
                }
            }
        }

        return true;
    }

    private boolean drawBuildings(final ITile tile, final Graphics2D g) {
        if (tile.getZoomStep() < buildingMinZoomstep + minZoomstepOffset) {
            return true;
        }

        if (tile.getBuildings() == null) {
            return false;
        }

        final Point tileLoc = tile.getLocation();

        final Path2D.Float path = new Path2D.Float(Path2D.WIND_EVEN_ODD);

        for (final Building building : tile.getBuildings()) {
            if (building == null) {
                return false;
            }

            final Polygon poly = convertPolygon(building.getPolygon(), tileLoc, tile.getZoomStep());
            path.append(poly.getPathIterator(null), false);
        }

        // draw buildings
        if (buildingStyles[0].mainStroke(g, tile.getZoomStep())) {
            g.fill(path);
        }

        // draw outlines
        if (buildingStyles[0].outlineStroke(g, tile.getZoomStep())) {
            g.draw(path);
        }

        // draw building numbers
        if (tile.getZoomStep() >= buildingNumberMinZoomstep + minZoomstepOffset) {
            g.setFont(buildingNumberFont);
            g.setColor(buildingNumberColor);

            for (final Building building : tile.getBuildings()) {
                final Polygon poly = convertPolygon(building.getPolygon(), tileLoc, tile.getZoomStep());

                final Matcher matcher = Pattern.compile("\\d+[a-z]*").matcher(building.getAddress());
                if (matcher.find()) {
                    final String number = matcher.group();
                    if (!number.isEmpty()) {
                        final Rectangle2D fontRect = g.getFontMetrics(buildingNumberFont).getStringBounds(number, g);
                        final Rectangle2D polyRect = poly.getBounds();

                        if (fontRect.getWidth() < polyRect.getWidth() && fontRect.getHeight() < polyRect.getHeight()) {
                            final Point2D.Float center = calculateCenter(poly);
                            center.setLocation((center.getX() + polyRect.getCenterX()) / 2f,
                                    (center.getY() + polyRect.getCenterY()) / 2f);

                            g.drawString(number, (float) (center.x - fontRect.getWidth() / 2f), (float) (center.y
                                    - fontRect.getHeight() / 2f + g.getFontMetrics().getAscent()));
                        }
                    }
                }
            }
        }

        return true;
    }

    private Polygon convertPolygon(final Polygon polygon, final Point tileLoc, final int zoomStep) {
        final int[] xPoints = new int[polygon.npoints];
        final int[] yPoints = new int[polygon.npoints];

        for (int i = 0; i < polygon.npoints; i++) {
            xPoints[i] = converter.getPixelDistance(polygon.xpoints[i] - tileLoc.x, zoomStep);
            yPoints[i] = converter.getPixelDistance(polygon.ypoints[i] - tileLoc.y, zoomStep);
        }

        final Polygon ret = new Polygon();
        ret.xpoints = xPoints;
        ret.ypoints = yPoints;
        ret.npoints = polygon.npoints;
        return ret;
    }

    private double getAngle(final Point2D from, final Point2D to) {
        final double dy = to.getY() - from.getY();
        final double dx = to.getX() - from.getX();
        return Math.atan2(dy, dx);
    }

    private void appendPath(final List<Node> nodes, final Point tileLoc, final int zoomStep, final Path2D.Float path) {
        final Iterator<Node> iter = nodes.iterator();

        final Node startNode = iter.next();
        path.moveTo(converter.getPixelDistance(startNode.getX() - tileLoc.x, zoomStep),
                converter.getPixelDistance(startNode.getY() - tileLoc.y, zoomStep));

        while (iter.hasNext()) {
            final Node node = iter.next();

            path.lineTo(converter.getPixelDistance(node.getX() - tileLoc.x, zoomStep),
                    converter.getPixelDistance(node.getY() - tileLoc.y, zoomStep));
        }
    }

    @Override
    public void setConverter(final IPixelConverter converter) {
        this.converter = converter;
        ShapeStyle.setConverter(converter);
        calculateZoomOffset(converter);
    }

    private Point2D.Float calculateCenter(final Polygon poly) {
        float x = 0f;
        float y = 0f;
        int totalPoints = poly.npoints - 1;
        for (int i = 0; i < totalPoints; i++) {
            x += poly.xpoints[i];
            y += poly.ypoints[i];
        }

        if (poly.xpoints[0] != poly.xpoints[totalPoints] || poly.ypoints[0] != poly.ypoints[totalPoints]) {
            x += poly.xpoints[totalPoints];
            y += poly.ypoints[totalPoints];
            totalPoints++;
        }

        x = x / totalPoints;
        y = y / totalPoints;

        return new Point.Float(x, y);
    }
}