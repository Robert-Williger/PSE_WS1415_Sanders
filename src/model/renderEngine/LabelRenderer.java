package model.renderEngine;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongConsumer;

import model.map.IMapManager;
import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.IPointAccessor;
import model.map.accessors.IStringAccessor;
import util.Vector2D;

public class LabelRenderer extends AbstractRenderer {
    private Graphics2D g;

    private IPointAccessor pointLabelAccessor;
    private ICollectiveAccessor lineLabelAccessor;
    private ICollectiveAccessor buildingAccessor;
    private IStringAccessor stringAccessor;

    private static final LabelStyle[] styles;

    private static final int buildingNumberMinZoomstep = 18;
    private static final Font buildingNumberFont = new Font("Times New Roman", Font.PLAIN, 10);
    private static final Color buildingNumberColor = new Color(96, 96, 96);

    private static final HashMap<Object, Object> hints;

    static {
        styles = new LabelStyle[31];

        styles[2] = new LabelStyle(8, 14, new int[] { 12, 12, 12, 14, 14, 16, 16 }, Color.DARK_GRAY, true);
        styles[4] = new LabelStyle(8, 14, new int[] { 11, 12, 14, 16, 16, 16, 16 }, Color.DARK_GRAY, true);
        styles[5] = new LabelStyle(10, 15, new int[] { 10, 11, 12, 13, 14, 16 }, new Color[] { Color.GRAY, Color.GRAY,
                Color.DARK_GRAY, Color.DARK_GRAY, Color.DARK_GRAY, Color.DARK_GRAY }, true);
        styles[6] = new LabelStyle(12, 16, new int[] { 10, 11, 12, 13, 14 },
                new Color[] { Color.GRAY, Color.GRAY, Color.GRAY, Color.DARK_GRAY, Color.DARK_GRAY }, true);

        // TODO check this style.
        styles[7] = new LabelStyle(12, 16, new int[] { 10, 11, 12, 13, 14 },
                new Color[] { Color.GRAY, Color.GRAY, Color.GRAY, Color.DARK_GRAY, Color.DARK_GRAY }, true);

        styles[8] = new LabelStyle(13, 16, new int[] { 11, 12, 13, 14, 14 },
                new Color[] { Color.GRAY, Color.GRAY, Color.GRAY, Color.DARK_GRAY, Color.DARK_GRAY }, true);

        styles[10] = new LabelStyle(13, 21, new int[] { 8, 8, 9, 10, 10, 10, 10, 10, 10, 10 }, Color.black, false);
        styles[30] = new LabelStyle(10, 20, new int[] { 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10 }, Color.black,
                false);

        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Map<?, ?> map = (Map<?, ?>) (tk.getDesktopProperty("awt.font.desktophints"));

        if (map != null) {
            hints = new HashMap<>(map);
        } else {
            hints = new HashMap<>();
        }

        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        // case "continent":
        // return 0;
        // case "country":
        // return 1;
        // case "state":
        // return 2;
        // case "county":
        // return 3;
        // case "city":
        // return 4;
        // case "suburb":
        // return 5;
        // case "neighbourhood":
        // return 6;
        // case "town":
        // return 7;
        // case "village":
        // return 8;
        // case "hamlet":
        // return 9;
        //
        // case "region":
        // return 10;
        // case "province":
        // return 11;
        // case "district":
        // return 12;
        // case "municipality":
        // return 13;
        // case "borough":
        // return 14;
        // case "quarter":
        // return 15;
        // case "city_block":
        // return 16;
        // case "plot":
        // return 17;
        // case "isolated_dwelling":
        // return 18;

    }

    public LabelRenderer(final IMapManager manager) {
        super(manager);
    }

    @Override
    protected void render(final Image image) {
        g = (Graphics2D) image.getGraphics();
        g.addRenderingHints(hints);

        drawBuildingNamesAndNumbers();
        drawLabels();

        g.dispose();
        if (rendered) {
            fireChange();
        }
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        super.setMapManager(manager);

        pointLabelAccessor = manager.createPointAccessor("pointLabel");
        lineLabelAccessor = manager.createCollectiveAccessor("lineLabel");
        buildingAccessor = manager.createCollectiveAccessor("building");
        stringAccessor = manager.createStringAccessor();
    }

    private void drawLabels() {
        final int zoom = tileAccessor.getZoom();
        final int x = tileAccessor.getX();
        final int y = tileAccessor.getY();

        drawPointLabels(zoom, x, y);
        drawLineLabels(zoom, x, y);
    }

    private void drawPointLabels(final int zoom, final int x, final int y) {
        final LongConsumer consumer = label -> {
            pointLabelAccessor.setID(label);

            final LabelStyle style = styles[pointLabelAccessor.getType()];
            if (style != null && style.isVisible(zoom)) {
                final String name = stringAccessor.getString(pointLabelAccessor.getAttribute("name"));
                drawStringPart(style, zoom, x, y, pointLabelAccessor.getX(), pointLabelAccessor.getY(), 0, name);

                rendered = true;
            }
        };

        tileAccessor.forEach("pointLabel", consumer);
    }

    private void drawLineLabels(final int zoom, final int x, final int y) {
        final LongConsumer consumer = label -> {
            lineLabelAccessor.setID(label);

            final LabelStyle style = styles[10];
            if (lineLabelAccessor.getAttribute("zoom") == zoom && style != null && style.isVisible(zoom)) {
                String name = stringAccessor.getString(lineLabelAccessor.getAttribute("name"));

                final Point last = new Point(lineLabelAccessor.getX(0), lineLabelAccessor.getY(0));
                final Point current = new Point();
                final Vector2D horizontal = new Vector2D(1, 0);
                final FontMetrics fontMetrics = g.getFontMetrics(style.getFont(zoom));

                double length = 0;
                for (int i = 1; i < lineLabelAccessor.size() && !name.isEmpty(); ++i) {
                    current.setLocation(lineLabelAccessor.getX(i), lineLabelAccessor.getY(i));
                    Vector2D vector = new Vector2D(current.getX() - last.getX(), current.getY() - last.getY());
                    length += converter.getPixelDistance(vector.norm(), zoom);
                    if (length < 0)
                        continue;

                    int stringPos = 0;
                    while (true) {
                        final double charWidth = fontMetrics.charWidth(name.charAt(stringPos));
                        length -= charWidth;
                        ++stringPos;
                        if (length < 0 || stringPos == name.length()) {
                            break;
                        }
                    }

                    final float angle = (float) Vector2D.angle(horizontal, vector);
                    final int midX = last.x + (current.x - last.x) / 2;
                    final int midY = last.y + (current.y - last.y) / 2;
                    drawStringPart(style, zoom, x, y, midX, midY, angle, name.substring(0, stringPos));

                    name = name.substring(stringPos);
                    last.setLocation(current);
                }

                rendered = true;
            }
        };

        tileAccessor.forEach("lineLabel", consumer);

    }

    private void drawStringPart(final LabelStyle style, final int zoom, final int x, final int y, final int midX,
            final int midY, final double angle, String part) {
        final double sin = Math.sin(angle);
        final double cos = Math.cos(angle);
        // TODO FontMetrics in stlye avoids object creation.
        final FontMetrics fontMetrics = g.getFontMetrics(style.getFont(zoom));
        final int width = fontMetrics.stringWidth(part);
        final int height = fontMetrics.getHeight();
        final int descent = fontMetrics.getDescent();
        final double xOffset = -cos * width / 2 - sin * height / 2 + sin * descent;
        final double yOffset = cos * height / 2 - sin * width / 2 - cos * descent;

        final double transX = converter.getPixelDistance(midX - x, zoom) + xOffset;
        final double transY = converter.getPixelDistance(midY - y, zoom) + yOffset;

        g.translate(transX, transY);
        g.rotate(angle);

        if (style.outlineStroke(g, zoom)) {
            g.drawString(part, -1, 0);
            g.drawString(part, 0, -1);
            g.drawString(part, 1, 0);
            g.drawString(part, 0, 1);
        }

        if (style.mainStroke(g, zoom)) {
            g.drawString(part, 0, 0);
        }

        g.rotate(-angle);
        g.translate(-transX, -transY);
    }

    private void drawBuildingNamesAndNumbers() {
        final int zoom = tileAccessor.getZoom();
        final int x = tileAccessor.getX();
        final int y = tileAccessor.getY();

        if (zoom >= buildingNumberMinZoomstep) {
            final FontMetrics metrics = g.getFontMetrics(buildingNumberFont);

            g.setFont(buildingNumberFont);
            g.setColor(buildingNumberColor);

            final LongConsumer consumer = building -> {
                buildingAccessor.setID(building);

                // only draw number if name cannot be drawn.
                rendered |= draw(buildingAccessor.getAttribute("name"), x, y, zoom, metrics)
                        || draw(buildingAccessor.getAttribute("number"), x, y, zoom, metrics);

            };
            tileAccessor.forEach("building", consumer);
        }

    }

    private boolean draw(final int id, final int x, final int y, final int zoom, final FontMetrics metrics) {
        if (id != -1) {
            final String number = stringAccessor.getString(id);

            final int size = buildingAccessor.size();
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;

            for (int i = 0; i < size; i++) {
                int cx = buildingAccessor.getX(i);
                minX = Math.min(minX, cx);
                maxX = Math.max(maxX, cx);
                int cy = buildingAccessor.getY(i);
                minY = Math.min(minY, cy);
                maxY = Math.max(maxY, cy);
            }

            int width = converter.getPixelDistance(maxX - minX, zoom);
            int height = converter.getPixelDistance(maxY - minY, zoom);
            minX = converter.getPixelDistance(minX - x, zoom);
            minY = converter.getPixelDistance(minY - y, zoom);

            final int fontWidth = metrics.stringWidth(number);
            final int fontHeight = metrics.getHeight();

            if (fontWidth < width && fontHeight < height) {
                final Point2D.Float center = calculateCenter();
                center.setLocation((center.getX() + minX + width / 2) / 2f, (center.getY() + minY + height / 2) / 2f);
                g.drawString(number, (float) (center.x - fontWidth / 2f),
                        (float) (center.y - fontHeight / 2f + metrics.getAscent()));

                return true;
            }
        }

        return false;
    }

    private Point2D.Float calculateCenter() {
        final int size = buildingAccessor.size();
        float x = 0f;
        float y = 0f;

        for (int i = 0; i < size; i++) {
            x += buildingAccessor.getX(i);
            y += buildingAccessor.getY(i);
        }

        x = x / size;
        y = y / size;

        return new Point.Float(converter.getPixelDistance(x - tileAccessor.getX(), tileAccessor.getZoom()),
                converter.getPixelDistance(y - tileAccessor.getY(), tileAccessor.getZoom()));
    }
}
