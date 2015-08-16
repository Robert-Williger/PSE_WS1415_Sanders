package model.renderEngine;

import java.awt.BasicStroke;
import java.awt.Color;

import model.map.IPixelConverter;

public class DefaultOSMColorPattern implements IColorPattern {

    private final int[] streetType;
    private final int[] wayMinZoomstep;
    private final WayStyle[] wayStyles;
    private final int[] wayType;
    private final ShapeStyle[] areaStyles;
    private final int[] areaType;
    private final ShapeStyle[] buildingStyles;

    public DefaultOSMColorPattern() {
        final int streets = 14;
        final int ways = 15;

        // // STREETS AND WAYS ////
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

        // // BUILDINGS ////
        buildingStyles = new ShapeStyle[1];

        // default (light gray + gray outline)
        buildingStyles[0] = new ShapeStyle(new float[]{15f, 15f}, new float[]{0.5f, 1f}, new Color(216, 208, 201),
                new Color(170, 170, 170));
    }

    @Override
    public WayStyle getWayStyle(final int type) {
        return null;
    }

    @Override
    public ShapeStyle getAreaStyle(final int type) {
        return null;
    }

    @Override
    public ShapeStyle getBuildingStyle(final int type) {
        return null;
    }

    @Override
    public void setConverter(final IPixelConverter converter) {

    }

}
