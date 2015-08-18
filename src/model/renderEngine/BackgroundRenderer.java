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
import java.util.Collection;
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

    private IPixelConverter converter;

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
        wayStyles[0] = new WayStyle(new float[]{1, 8, 15}, new float[]{1.2f, 9, 17}, Color.WHITE, new Color(200, 200,
                200));
        wayMinZoomstep[0] = 13;

        // service (white + light gray outline, small)
        wayStyles[1] = new WayStyle(new float[]{1f, 4, 7}, new float[]{1f, 5, 9}, Color.WHITE, new Color(200, 200, 200));
        wayMinZoomstep[1] = 14;

        // secondary (orange)
        wayStyles[2] = new WayStyle(new float[]{1.2f, 13, 15}, new float[]{1.2f, 15, 17}, new Color(248, 213, 168),
                new Color(208, 167, 114));
        wayMinZoomstep[2] = 12;

        // tertiary (yellow)
        wayStyles[3] = new WayStyle(new float[]{1.2f, 8, 15}, new float[]{1.2f, 9, 17}, new Color(248, 248, 186),
                new Color(200, 200, 200));
        wayMinZoomstep[3] = 13;

        // road (gray + darker gray outline)
        wayStyles[4] = new WayStyle(new float[]{1.2f, 8, 15}, new float[]{1.2f, 9, 17}, new Color(200, 200, 200),
                new Color(170, 170, 170));
        wayMinZoomstep[4] = 13;

        // track (transparent white + brown dots/short lines)
        wayStyles[5] = new WayStyle(new float[]{0f, 2.5f, 8}, new float[]{0, 0, 0}, new float[]{0f, 1.5f, 4},
                new Color(255, 255, 255, 90), null, new Color(139, 69, 19), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, new float[]{0, 3, 9}, new float[]{0, 4, 12});
        wayMinZoomstep[5] = 15;

        // footway (transparent white + light pink dots/short lines)
        wayStyles[6] = new WayStyle(new float[]{0f, 2.5f, 8}, new float[]{0, 0, 0}, new float[]{0f, 1.5f, 4},
                new Color(255, 255, 255, 90), null, new Color(250, 150, 150), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, new float[]{0, 3, 9}, new float[]{0, 4, 12});
        wayMinZoomstep[6] = 15;

        // cycleway (transparent white + blue dots/short lines)
        wayStyles[7] = new WayStyle(new float[]{0f, 2.5f, 8}, new float[]{0, 0, 0}, new float[]{0f, 1.5f, 4},
                new Color(255, 255, 255, 90), null, new Color(64, 71, 245, 200), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, new float[]{0, 3, 9}, new float[]{0, 4, 12});
        wayMinZoomstep[7] = 15;

        // bridleway (transparent white + short green lines)
        wayStyles[8] = new WayStyle(new float[]{0f, 2.5f, 8}, new float[]{0, 0, 0}, new float[]{0f, 1.5f, 4},
                new Color(255, 255, 255, 90), null, new Color(76, 164, 75), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, new float[]{0, 3, 9}, new float[]{0, 4, 12});
        wayMinZoomstep[8] = 15;

        // path (transparent white + short black lines)
        wayStyles[9] = new WayStyle(new float[]{0f, 2.5f, 8}, new float[]{0, 0, 0}, new float[]{0f, 1.5f, 4},
                new Color(255, 255, 255, 90), null, new Color(50, 50, 50, 200), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, new float[]{0, 3, 9}, new float[]{0, 4, 12});
        wayMinZoomstep[9] = 15;

        // river (light blue big)
        wayStyles[10] = new WayStyle(new float[]{0, 20, 20}, new Color(181, 208, 208));
        wayMinZoomstep[10] = 10;

        // stream (light blue small)
        wayStyles[11] = new WayStyle(5, new Color(181, 208, 208));
        wayMinZoomstep[11] = 11;

        // rail/light_rail (black outline + black middleline - large)
        wayStyles[12] = new WayStyle(new float[]{0, 1.1f, 4.125f}, new float[]{0, 0.3f, 1.125f}, new float[]{0, 2.2f,
                8.25f}, null, Color.DARK_GRAY, Color.DARK_GRAY, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                new float[]{0, 0.6f, 2.25f}, new float[]{0, 2.5f, 9.375f});
        wayMinZoomstep[12] = 14;

        // tram (small gray)
        wayStyles[13] = new WayStyle(new float[]{0, 0.3f, 2}, new Color(68, 68, 68));
        wayMinZoomstep[13] = 16;

        // primary street (orange + dark orange outline)
        wayStyles[14] = new WayStyle(new float[]{1.2f, 14, 15}, new float[]{1.2f, 16, 17}, new Color(230, 165, 65),
                new Color(189, 113, 0));
        wayMinZoomstep[14] = 11;

        // motorway (red/pink + gray red outline)
        wayStyles[15] = new WayStyle(new float[]{1.5f, 37, 15}, new float[]{1.5f, 39, 17}, new Color(220, 158, 158),
                new Color(194, 108, 108));
        wayMinZoomstep[15] = 0;

        // trunk (green + gray green outline)
        wayStyles[16] = new WayStyle(new float[]{1.2f, 14, 15}, new float[]{1.2f, 15, 17}, new Color(148, 212, 148),
                new Color(131, 158, 131));
        wayMinZoomstep[16] = 11;

        // primary_link (see primary)
        wayStyles[17] = new WayStyle(new float[]{1.2f, 10, 12}, new float[]{1.2f, 12, 14}, new Color(230, 165, 65),
                new Color(189, 113, 0));
        wayMinZoomstep[17] = 14;

        // motorway_link (see motorway)
        wayStyles[18] = new WayStyle(new float[]{1.5f, 10, 12}, new float[]{1.5f, 12, 14}, new Color(220, 158, 158),
                new Color(194, 108, 108));
        wayMinZoomstep[18] = 13;

        // trunk_link (see trunk)
        wayStyles[19] = new WayStyle(new float[]{1.2f, 10, 12}, new float[]{1.2f, 12, 14}, new Color(148, 212, 148),
                new Color(131, 158, 131));
        wayMinZoomstep[19] = 14;

        // track[career] (dark green)
        wayStyles[20] = new WayStyle(new float[]{1.2f, 1f, 3f}, new Color(111, 170, 141));
        wayMinZoomstep[20] = 17;

        // steps (light gray + red lines)
        wayStyles[21] = new WayStyle(new float[]{0, 2f, 6f}, new float[]{0f, 0f, 0f}, new float[]{0, 2f, 6f},
                new Color(230, 230, 230), null, new Color(250, 128, 114), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                new float[]{0, 0.75f, 3f}, new float[]{0, 0.25f, 1f});
        wayMinZoomstep[21] = 17;

        // wall (dark gray)
        wayStyles[22] = new WayStyle(new float[]{0, 0.3f, 1}, new Color(158, 158, 158));
        wayMinZoomstep[22] = 15;

        // hedge (green)
        wayStyles[23] = new WayStyle(new float[]{0, 0.75f, 2.5f}, new Color(174, 209, 160));
        wayMinZoomstep[23] = 15;

        wayOrder = new int[][]{{10}, {11}, {19}, {17}, {18}, {12}, {22}, {23}, {0, 1}, {3}, {2}, {4}, {16}, {14}, {13},
                {15}, {5}, {6}, {7}, {8}, {9}, {20}, {21}};

        // // AREAS ////
        areaMinZoomstep = new int[24];
        areaStyles = new ShapeStyle[24];

        // forest (dark green)
        areaStyles[0] = new ShapeStyle(1f, new Color(160, 206, 133));
        areaMinZoomstep[0] = 7;

        // wood (dark green [brighter])
        areaStyles[1] = new ShapeStyle(1f, new Color(174, 209, 160));
        areaMinZoomstep[1] = 10;

        // grass / meadow / grassland ... (light yellow-green)
        areaStyles[2] = new ShapeStyle(1f, new Color(205, 236, 165));
        areaMinZoomstep[2] = 10;

        // grassfield (ligth grey-brown)
        areaStyles[3] = new ShapeStyle(1f, new Color(181, 181, 141));
        areaMinZoomstep[3] = 10;

        // residential / railway (gray)
        areaStyles[4] = new ShapeStyle(new float[]{1f, 1f, 1f}, new float[]{0f, 1f, 1f}, new Color(218, 218, 218),
                new Color(200, 200, 200));
        areaMinZoomstep[4] = 12;

        // water / reservoir (light blue)
        areaStyles[5] = new ShapeStyle(1f, new Color(181, 208, 208));
        areaMinZoomstep[5] = 8;

        // industrial (light purple)
        areaStyles[6] = new ShapeStyle(1f, new Color(223, 209, 214));
        areaMinZoomstep[6] = 12;

        // park (very light green)
        areaStyles[7] = new ShapeStyle(1f, new Color(205, 247, 201));
        areaMinZoomstep[7] = 14;

        // retail (light pink)
        areaStyles[8] = new ShapeStyle(new float[]{1f, 1f, 1f}, new float[]{0f, 1f, 1f}, new Color(240, 216, 216),
                new Color(226, 200, 198));
        areaMinZoomstep[8] = 12;

        // heath / fell (light brown)
        areaStyles[9] = new ShapeStyle(1f, new Color(214, 217, 159));
        areaMinZoomstep[9] = 12;

        // sand (light yellow)
        areaStyles[10] = new ShapeStyle(1f, new Color(240, 228, 184));
        areaMinZoomstep[10] = 13;

        // mud /scree (very light pink-grey)
        areaStyles[11] = new ShapeStyle(1f, new Color(228, 219, 208));
        areaMinZoomstep[11] = 12;

        // quarry (gray)
        areaStyles[12] = new ShapeStyle(1f, new Color(195, 195, 195));
        areaMinZoomstep[12] = 12;

        // cemetery (darker green)
        areaStyles[13] = new ShapeStyle(new float[]{1f, 1f, 1f}, new float[]{0f, 1f, 1f}, new Color(170, 202, 174),
                new Color(134, 149, 135));
        areaMinZoomstep[13] = 13;

        // parking (light yellow)
        areaStyles[14] = new ShapeStyle(new float[]{1f, 1f, 1f}, new float[]{0f, 1f, 1f}, new Color(246, 238, 182),
                new Color(239, 221, 236));
        areaMinZoomstep[14] = 15;

        // pedestrian (light gray)
        areaStyles[15] = new ShapeStyle(new float[]{1f, 1f, 1f}, new float[]{0f, 1f, 1f}, new Color(237, 237, 237),
                new Color(200, 200, 200));
        areaMinZoomstep[15] = 13;

        // farmland (light orange-brown)
        areaStyles[16] = new ShapeStyle(1f, new Color(235, 221, 199));
        areaMinZoomstep[16] = 10;

        // playground (very light turquoise + light blue outline)
        areaStyles[17] = new ShapeStyle(new float[]{1f, 1f, 1f}, new float[]{0f, 2f, 2f}, new Color(204, 255, 241),
                new Color(148, 217, 197));
        areaMinZoomstep[17] = 15;

        // pitch (light turquoise + dark green outline)
        areaStyles[18] = new ShapeStyle(new float[]{1f, 1f, 1f}, new float[]{0f, 1f, 1f}, new Color(138, 211, 175),
                new Color(111, 170, 141));
        areaMinZoomstep[18] = 14;

        // sports_centre stadium (turquoise)
        areaStyles[19] = new ShapeStyle(1f, new Color(51, 204, 153));
        areaMinZoomstep[19] = 14;

        // track (light turquoise + dark green outline)
        areaStyles[20] = new ShapeStyle(new float[]{1f, 1f, 1f}, new float[]{0f, 1f, 1f}, new Color(116, 220, 186),
                new Color(111, 170, 141));
        areaMinZoomstep[20] = 14;

        // golf_course (light green)
        areaStyles[21] = new ShapeStyle(1f, new Color(181, 226, 181));
        areaMinZoomstep[21] = 15;

        // school university college kindergarten (very light yellow)
        areaStyles[22] = new ShapeStyle(new float[]{1f, 1f, 1f}, new float[]{0f, 1f, 1f}, new Color(240, 240, 216),
                new Color(217, 180, 169));
        areaMinZoomstep[22] = 13;

        // zoo (very light green)
        areaStyles[23] = new ShapeStyle(new float[]{1f, 1f, 1f}, new float[]{0f, 1f, 1f}, new Color(164, 242, 161),
                new Color(111, 170, 141));
        areaMinZoomstep[23] = 14;

        areaOrder = new int[]{7, 1, 22, 3, 4, 2, 9, 12, 16, 10, 6, 8, 13, 19, 21, 17, 15, 0, 23, 18, 20, 11, 5, 14};

        // // BUILDINGS ////
        buildingStyles = new ShapeStyle[1];

        // default (light gray + gray outline)
        buildingStyles[0] = new ShapeStyle(new float[]{0, 15f, 15f}, new float[]{0, 0.5f, 1f},
                new Color(216, 208, 201), new Color(170, 170, 170));
        buildingMinZoomstep = 15;
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

        if (drawAreas(tile, g) && drawWays(tile, g) && drawBuildings(tile, g)) {
            // && drawStreetNames(tile, g)) {
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

        for (final Iterator<Street> streetIt = tile.getStreets(); streetIt.hasNext();) {
            final Street street = streetIt.next();
            final String text = street.getName();
            if (streetNameMinZoomstep <= tile.getZoomStep() && !text.equals("Unbekannte Straße")) {

                final int textSize = metrics.stringWidth(text);
                int distanceCount = 0;
                final int length = converter.getPixelDistance(street.getLength(), tile.getZoomStep());

                if (length > textSize) {

                    final Iterator<Node> nodeIt = street.getNodes().iterator();
                    Point fromCoord = nodeIt.next().getLocation();

                    while (nodeIt.hasNext()) {
                        final Point toCoord = nodeIt.next().getLocation();

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
        final Iterator<Area> iterator = tile.getTerrain();
        if (iterator == null) {
            return false;
        }

        final Point tileLoc = tile.getLocation();
        final int zoom = tile.getZoomStep();
        final Path2D.Float[] path = new Path2D.Float[areaStyles.length];

        for (int i = 0; i < path.length; i++) {
            path[i] = new Path2D.Float(i != 18 ? Path2D.WIND_EVEN_ODD : Path2D.WIND_NON_ZERO);
        }

        while (iterator.hasNext()) {
            final Area area = iterator.next();

            if (area == null) {
                return false;
            }
            if (areaMinZoomstep[area.getType()] <= zoom) {
                path[area.getType()].append(convertPolygon(area.getPolygon(), tileLoc, zoom).getPathIterator(null),
                        false);
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

    private boolean drawWays(final ITile tile, final Graphics2D g) {
        if (tile.getStreets() == null || tile.getWays() == null) {
            return false;
        }

        final Point tileLoc = tile.getLocation();
        final int zoom = tile.getZoomStep();
        final Path2D.Float[] path = new Path2D.Float[wayStyles.length];

        for (int i = 0; i < path.length; i++) {
            path[i] = new Path2D.Float();
        }

        if (!appendWays(tile.getStreets(), zoom, path, tileLoc) || !appendWays(tile.getWays(), zoom, path, tileLoc)) {
            return false;
        }

        for (final int[] layer : wayOrder) {
            for (final int way : layer) {
                if (wayMinZoomstep[way] <= zoom) {
                    if (wayStyles[way].outlineCompositeStroke(g, zoom)) {
                        g.draw(path[way]);
                    } else if (wayStyles[way].outlineStroke(g, zoom)) {
                        g.draw(path[way]);
                    }
                }
            }
            for (final int way : layer) {
                if (wayMinZoomstep[way] <= zoom) {
                    if (wayStyles[way].mainStroke(g, zoom)) {
                        g.draw(path[way]);
                    }
                }
            }
            for (final int way : layer) {
                if (wayMinZoomstep[way] <= zoom) {
                    if (wayStyles[way].middleLineStroke(g, zoom)) {
                        g.draw(path[way]);
                    }
                }
            }
        }

        return true;
    }

    private boolean drawBuildings(final ITile tile, final Graphics2D g) {
        final int zoom = tile.getZoomStep();

        if (zoom < buildingMinZoomstep) {
            return true;
        }

        Iterator<Building> iterator = tile.getBuildings();
        if (tile.getBuildings() == null) {
            return false;
        }

        final Point tileLoc = tile.getLocation();

        final Path2D.Float path = new Path2D.Float(Path2D.WIND_EVEN_ODD);

        while (iterator.hasNext()) {
            final Building building = iterator.next();
            if (building == null) {
                return false;
            }

            final Polygon poly = convertPolygon(building.getPolygon(), tileLoc, zoom);
            path.append(poly.getPathIterator(null), false);
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
        if (zoom >= buildingNumberMinZoomstep) {
            g.setFont(buildingNumberFont);
            g.setColor(buildingNumberColor);

            iterator = tile.getBuildings();
            while (iterator.hasNext()) {
                final Building building = iterator.next();
                final Polygon poly = convertPolygon(building.getPolygon(), tileLoc, zoom);

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

    private boolean appendWays(final Iterator<? extends Way> iterator, final int zoom, final Path2D.Float[] path,
            final Point tileLoc) {
        while (iterator.hasNext()) {
            final Way way = iterator.next();
            if (way == null) {
                return false;
            }
            if (wayMinZoomstep[way.getType()] <= zoom) {
                appendPath(way.getNodes(), tileLoc, zoom, path[way.getType()]);
            }
        }
        return true;
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