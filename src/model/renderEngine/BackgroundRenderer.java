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
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import model.elements.IArea;
import model.elements.IBuilding;
import model.elements.IMultiElement;
import model.elements.Node;
import model.elements.IStreet;
import model.elements.IWay;
import model.map.IPixelConverter;
import model.map.ITile;

public class BackgroundRenderer extends AbstractRenderer implements IRenderer {

    // streets
    private static final Font streetNameFont;
    private static final int streetNameMinZoomstep;
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
    private static final int buildingNumberMinZoomstep;
    private static final Font buildingNumberFont;
    private static final Color buildingNumberColor;

    // defines the render styles for all types of streets, ways, areas and
    // buildings
    static {
        // // STREETS AND WAYS ////
        streetNameFont = new Font("Times New Roman", Font.PLAIN, 12);
        streetNameMinZoomstep = 17;
        wayMinZoomstep = new int[24];
        wayStyles = new WayStyle[24];

        // pedestrian / living street / residential / unclassified (white +
        // light gray outline)
        wayMinZoomstep[0] = 13;
        wayStyles[0] = new WayStyle(wayMinZoomstep[0], new float[]{1.5f, 3f, 6f, 9f, 12f, 13f, 14f}, new float[]{2.5f,
                4f, 7f, 10f, 13f, 14f, 15f}, Color.WHITE, new Color(200, 200, 200));

        // service (white + light gray outline, small)
        wayMinZoomstep[1] = 15;
        wayStyles[1] = new WayStyle(wayMinZoomstep[1], new float[]{2, 2, 4, 6, 8}, new float[]{3, 3, 5, 7, 9},
                Color.WHITE, new Color(200, 200, 200));

        // secondary (orange)
        wayMinZoomstep[2] = 9;
        wayStyles[2] = new WayStyle(wayMinZoomstep[2], new float[]{1.1225f, 1.25f, 2f, 2f, 6f, 6f, 11f, 11f, 16f},
                new float[]{1.1225f, 1.25f, 2f, 3f, 7f, 7f, 12f, 12f, 17f}, new Color(248, 213, 168), new Color(208,
                        167, 114));

        // tertiary (yellow)
        wayMinZoomstep[3] = 12;
        wayStyles[3] = new WayStyle(wayMinZoomstep[3], new float[]{2f, 6f, 6f, 11f, 11f, 16f}, new float[]{3f, 7f, 7f,
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
        wayStyles[14] = new WayStyle(wayMinZoomstep[14], new float[]{0.5f, 1f, 1.1225f, 1.25f, 2f, 2.5f, 6f, 6f, 11f,
                11f, 16f}, new float[]{0.5f, 1f, 1.1225f, 1.25f, 2f, 3.4f, 7f, 7f, 12f, 12f, 17f}, new Color(230, 165,
                65), new Color(211, 138, 29));

        // motorway (red/pink + gray red outline)
        wayMinZoomstep[15] = 5;
        wayStyles[15] = new WayStyle(wayMinZoomstep[15], new float[]{1, 1, 1.1225f, 1.1225f, 1.1225f, 1.25f, 2f, 3f,
                6f, 13f, 13f, 13f, 17f}, new float[]{1, 1, 1, 1.1225f, 1.1225f, 1.25f, 2f, 4f, 7f, 14f, 14f, 14f, 18f},
                new Color(220, 158, 158), new Color(194, 108, 108));

        // trunk (green + gray green outline)
        wayMinZoomstep[16] = 5;
        wayStyles[16] = new WayStyle(wayMinZoomstep[16], new float[]{1, 1, 1, 1.1225f, 1.1225f, 1.25f, 2f, 3f, 6f, 13f,
                13f, 13f, 17f}, new float[]{1, 1, 1, 1.1225f, 1.1225f, 1.25f, 2f, 4f, 7f, 14f, 14f, 14f, 18f},
                new Color(148, 212, 148), new Color(131, 158, 131));

        // primary_link (see primary)
        wayMinZoomstep[17] = 11;
        wayStyles[17] = new WayStyle(wayMinZoomstep[17], new float[]{1, 1, 3, 3, 7, 7, 11, 11, 11}, new float[]{1, 1,
                4, 4, 8, 8, 12, 12, 12}, new Color(230, 165, 65), new Color(211, 138, 29));

        // motorway_link (see motorway)
        wayMinZoomstep[18] = 11;
        wayStyles[18] = new WayStyle(wayMinZoomstep[18], new float[]{1, 1, 3, 3, 7, 7, 11, 11, 11}, new float[]{1, 1,
                4, 4, 8, 8, 12, 12, 12}, new Color(220, 158, 158), new Color(194, 108, 108));

        // trunk_link (see trunk)
        wayMinZoomstep[19] = 11;
        wayStyles[19] = new WayStyle(wayMinZoomstep[19], new float[]{1, 1, 3, 3, 7, 7, 11, 11, 11}, new float[]{1, 1,
                4, 4, 8, 8, 12, 12, 12}, new Color(148, 212, 148), new Color(131, 158, 131));

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
        areaMinZoomstep = new int[24];
        areaStyles = new ShapeStyle[24];

        // forest (dark green)
        areaStyles[0] = new ShapeStyle(1, new Color(172, 208, 157));
        areaMinZoomstep[0] = 8;

        // wood (dark green [brighter])
        areaStyles[1] = new ShapeStyle(1, new Color(174, 209, 160));
        areaMinZoomstep[1] = 10;

        // grass / meadow / grassland ... (light yellow-green)
        areaStyles[2] = new ShapeStyle(1, new Color(205, 236, 165));
        areaMinZoomstep[2] = 10;

        // grassfield (ligth grey-brown)
        areaStyles[3] = new ShapeStyle(1, new Color(181, 181, 141));
        areaMinZoomstep[3] = 10;

        // residential / railway (gray)
        areaMinZoomstep[4] = 10;
        areaStyles[4] = new ShapeStyle(areaMinZoomstep[4], new float[]{0}, new float[]{0, 0, 0, 0, 0, 0, 1}, new Color(
                218, 218, 218), new Color(200, 200, 200));

        // water / reservoir (light blue)
        areaStyles[5] = new ShapeStyle(1, new Color(181, 208, 208));
        areaMinZoomstep[5] = 8;

        // industrial (light purple)
        areaStyles[6] = new ShapeStyle(1, new Color(223, 209, 214));
        areaMinZoomstep[6] = 12;

        // park (very light green)
        areaStyles[7] = new ShapeStyle(1, new Color(205, 247, 201));
        areaMinZoomstep[7] = 14;

        // retail (light pink)
        areaMinZoomstep[8] = 12;
        areaStyles[8] = new ShapeStyle(areaMinZoomstep[8], new float[]{0}, new float[]{0, 0, 0, 0, 1}, new Color(240,
                216, 216), new Color(226, 200, 198));

        // heath / fell (light brown)
        areaStyles[9] = new ShapeStyle(1, new Color(214, 217, 159));
        areaMinZoomstep[9] = 12;

        // sand (light yellow)
        areaStyles[10] = new ShapeStyle(1, new Color(240, 228, 184));
        areaMinZoomstep[10] = 13;

        // mud /scree (very light pink-grey)
        areaStyles[11] = new ShapeStyle(1, new Color(228, 219, 208));
        areaMinZoomstep[11] = 12;

        // quarry (gray)
        areaStyles[12] = new ShapeStyle(1, new Color(195, 195, 195));
        areaMinZoomstep[12] = 12;

        // cemetery (darker green)
        areaMinZoomstep[13] = 13;
        areaStyles[13] = new ShapeStyle(areaMinZoomstep[13], new float[]{0}, new float[]{0, 0, 0, 1}, new Color(170,
                202, 174), new Color(134, 149, 135));

        // parking (light yellow)
        areaMinZoomstep[14] = 15;
        areaStyles[14] = new ShapeStyle(areaMinZoomstep[14], new float[]{0}, new float[]{0, 1},
                new Color(246, 238, 182), new Color(239, 221, 236));

        // pedestrian (light gray)
        areaMinZoomstep[15] = 11;
        areaStyles[15] = new ShapeStyle(areaMinZoomstep[15], new float[]{0}, new float[]{0, 0, 0, 0, 0, 1}, new Color(
                237, 237, 237), new Color(200, 200, 200));

        // farmland (light orange-brown)
        areaStyles[16] = new ShapeStyle(1, new Color(235, 221, 199));
        areaMinZoomstep[16] = 10;

        // playground (very light turquoise + light blue outline)
        areaStyles[17] = new ShapeStyle(0, 1, new Color(204, 255, 241), new Color(148, 217, 197));
        areaMinZoomstep[17] = 15;

        // pitch (light turquoise + dark green outline)
        areaMinZoomstep[18] = 14;
        areaStyles[18] = new ShapeStyle(areaMinZoomstep[18], new float[]{0}, new float[]{0, 1},
                new Color(138, 211, 175), new Color(111, 170, 141));

        // sports_centre stadium (turquoise)
        areaStyles[19] = new ShapeStyle(1, new Color(51, 204, 153));
        areaMinZoomstep[19] = 14;

        // track (light turquoise + dark green outline)
        areaMinZoomstep[20] = 14;
        areaStyles[20] = new ShapeStyle(areaMinZoomstep[20], new float[]{0}, new float[]{0, 1},
                new Color(116, 220, 186), new Color(111, 170, 141));

        // golf_course (light green)
        areaStyles[21] = new ShapeStyle(1, new Color(181, 226, 181));
        areaMinZoomstep[21] = 15;

        // school university college kindergarten (very light yellow)
        areaMinZoomstep[22] = 13;
        areaStyles[22] = new ShapeStyle(areaMinZoomstep[22], new float[]{0}, new float[]{0, 0, 1}, new Color(240, 240,
                216), new Color(217, 180, 169));

        // zoo (very light green)
        areaMinZoomstep[23] = 14;
        areaStyles[23] = new ShapeStyle(areaMinZoomstep[23], new float[]{0}, new float[]{0, 1},
                new Color(164, 242, 161), new Color(111, 170, 141));

        areaOrder = new int[]{7, 1, 22, 3, 4, 2, 9, 12, 16, 10, 6, 8, 13, 19, 21, 17, 15, 0, 23, 18, 20, 11, 5, 14};

        // // BUILDINGS ////
        buildingStyles = new ShapeStyle[1];

        // default (light gray + gray outline)
        buildingMinZoomstep = 14;
        buildingStyles[0] = new ShapeStyle(buildingMinZoomstep, new float[]{0}, new float[]{0, 0.5f, 0.75f, 1},
                new Color(217, 208, 201), new Color(188, 174, 162));

        buildingNumberMinZoomstep = 18;
        buildingNumberFont = new Font("Times New Roman", Font.PLAIN, 10);
        buildingNumberColor = new Color(96, 96, 96);
    }

    public BackgroundRenderer(final IPixelConverter converter) {
        setConverter(converter);
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

        final Point tileLocation = getTileLocation(tile, image);

        if (drawAreas(tile, tileLocation, g) && drawBuildings(tile, tileLocation, g) && drawWays(tile, tileLocation, g)) {
            // && drawStreetNames(tile, tileLocation, g)) {
            g.dispose();
            fireChange();
            return true;
        }

        g.dispose();
        return false;
    }

    // private boolean drawStreetNames(final ITile tile, final Point tileLoc,
    // final Graphics2D g) {
    // final Font font = streetNameFont;
    // final FontMetrics metrics = g.getFontMetrics(font);
    // final int zoom = tile.getZoomStep();
    // final int middleOffset = (metrics.getAscent() - metrics.getDescent()) /
    // 2;
    // g.setColor(Color.BLACK);
    //
    // for (final Iterator<Street> streetIt = tile.getStreets();
    // streetIt.hasNext();) {
    // final Street street = streetIt.next();
    // final String text = street.getName();
    // if (streetNameMinZoomstep <= tile.getZoomStep() &&
    // !text.equals("Unbekannte Straße")) {
    //
    // final int textSize = metrics.stringWidth(text);
    // int distanceCount = 0;
    // final int length = converter.getPixelDistance(street.getLength(),
    // tile.getZoomStep());
    //
    // if (length > textSize) {
    //
    // final Iterator<Node> nodeIt = street.iterator();
    // Point fromCoord = nodeIt.next().getLocation();
    //
    // while (nodeIt.hasNext()) {
    // final Point toCoord = nodeIt.next().getLocation();
    //
    // final int distance = converter.getPixelDistance((int)
    // toCoord.distance(fromCoord), zoom);
    // final int start = (distance - textSize) / 2;
    //
    // if (start >= 5 && distanceCount + start >= 0) {
    // distanceCount = -300;
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
    // final int offset = street.getType() < 9 ? middleOffset : middleOffset +
    // 8;
    // final AffineTransform at = AffineTransform.getTranslateInstance(
    // from.x - Math.sin(direction) * offset + xDist * scale, from.y +
    // Math.cos(direction)
    // * offset + yDist * scale);
    // at.rotate(direction);
    //
    // g.setFont(font.deriveFont(at));
    // g.drawString(text, 0, 0);
    // }
    //
    // distanceCount = Math.min(0, distance + distanceCount);
    // fromCoord = toCoord;
    // }
    // }
    // }
    // }

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
    //
    // return true;
    // }

    private boolean drawAreas(final ITile tile, final Point tileLoc, final Graphics2D g) {
        final Iterator<IArea> iterator = tile.getTerrain();
        if (iterator == null) {
            return false;
        }

        final int zoom = tile.getZoomStep();
        final Path2D[] path = new Path2D[areaStyles.length];

        for (int i = 0; i < path.length; i++) {
            path[i] = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        }

        while (iterator.hasNext()) {
            final IArea iArea = iterator.next();

            if (iArea == null) {
                return false;
            }
            if (areaMinZoomstep[iArea.getType()] <= zoom) {
                // TODO polygons sometimes do not have same start and endpoint!
                appendPath(iArea, tileLoc, zoom, path[iArea.getType()]);
            }
        }

        for (final int i : areaOrder) {
            if (areaStyles[i].mainStroke(g, zoom)) {
                g.fill(path[i]);
            }

            if (areaStyles[i].outlineStroke(g, zoom)) {
                g.draw(path[i]);
            }
        }

        return true;
    }

    private boolean drawWays(final ITile tile, final Point tileLoc, final Graphics2D g) {
        if (tile.getStreets() == null || tile.getWays() == null) {
            return false;
        }

        final int zoom = tile.getZoomStep();
        final Path2D[] path = new Path2D.Float[wayStyles.length];

        for (int i = 0; i < path.length; i++) {
            path[i] = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        }

        if (!appendWays(tile.getStreets(), zoom, path, tileLoc) || !appendWays(tile.getWays(), zoom, path, tileLoc)) {
            return false;
        }

        for (final int[] layer : wayOrder) {
            for (final int way : layer) {
                // TODO remove
                if (wayMinZoomstep[way] <= zoom) {
                    if (wayStyles[way].outlineStroke(g, zoom)) {
                        g.draw(path[way]);
                    }
                }
            }
            for (final int way : layer) {
                // TODO remove
                if (wayMinZoomstep[way] <= zoom) {
                    if (wayStyles[way].mainStroke(g, zoom)) {
                        g.draw(path[way]);
                    }
                }
            }
            for (final int way : layer) {
                // TODO remove
                if (wayMinZoomstep[way] <= zoom) {
                    if (wayStyles[way].middleLineStroke(g, zoom)) {
                        g.draw(path[way]);
                    }
                }
            }
        }

        return true;
    }

    private boolean drawBuildings(final ITile tile, final Point tileLoc, final Graphics2D g) {
        final int zoom = tile.getZoomStep();

        if (zoom < buildingMinZoomstep) {
            return true;
        }

        Iterator<IBuilding> iterator = tile.getBuildings();
        if (tile.getBuildings() == null) {
            return false;
        }

        Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);

        while (iterator.hasNext()) {
            final IBuilding iBuilding = iterator.next();
            if (iBuilding == null) {
                return false;
            }

            appendPath(iBuilding, tileLoc, zoom, path);
        }

        // draw buildings
        if (buildingStyles[0].mainStroke(g, zoom)) {
            g.fill(path);
        }

        // draw outlines
        if (buildingStyles[0].outlineStroke(g, zoom)) {
            g.draw(path);
        }

        // draw building numbers

        // TODO reactivate building numbers

        // if (zoom >= buildingNumberMinZoomstep) {
        // g.setFont(buildingNumberFont);
        // g.setColor(buildingNumberColor);
        //
        // iterator = tile.getBuildings();
        // while (iterator.hasNext()) {
        // final Building building = iterator.next();
        // final Polygon poly = convertPolygon(building.getPolygon(), tileLoc,
        // zoom);
        //
        // final String number = building.getHouseNumber();
        // if (!number.isEmpty()) {
        // final Rectangle2D fontRect =
        // g.getFontMetrics(buildingNumberFont).getStringBounds(number, g);
        // final Rectangle2D polyRect = poly.getBounds();
        //
        // if (fontRect.getWidth() < polyRect.getWidth() && fontRect.getHeight()
        // < polyRect.getHeight()) {
        // final Point2D.Float center = calculateCenter(poly);
        // center.setLocation((center.getX() + polyRect.getCenterX()) / 2f,
        // (center.getY() + polyRect.getCenterY()) / 2f);
        //
        // g.drawString(number, (float) (center.x - fontRect.getWidth() / 2f),
        // (float) (center.y
        // - fontRect.getHeight() / 2f + g.getFontMetrics().getAscent()));
        // }
        // }
        // }
        // }

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

    private boolean appendWays(final Iterator<? extends IWay> iterator, final int zoom, final Path2D[] path,
            final Point tileLoc) {
        while (iterator.hasNext()) {
            final IWay iWay = iterator.next();
            if (iWay == null) {
                return false;
            }
            if (wayMinZoomstep[iWay.getType()] <= zoom) {
                appendPath(iWay, tileLoc, zoom, path[iWay.getType()]);
            }
        }
        return true;
    }

    private void appendPath(final IMultiElement element, final Point tileLoc, final int zoomStep, final Path2D path) {
        path.moveTo(converter.getPixelDistancef(element.getX(0) - tileLoc.x, zoomStep),
                converter.getPixelDistancef(element.getY(0) - tileLoc.y, zoomStep));

        for (int i = 0; i < element.size(); i++) {
            path.lineTo(converter.getPixelDistancef(element.getX(i) - tileLoc.x, zoomStep),
                    converter.getPixelDistancef(element.getY(0) - tileLoc.y, zoomStep));
        }
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