package model.renderEngine.renderers;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.PrimitiveIterator.OfInt;

import model.map.IElementIterator;
import model.map.IMapManager;
import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.IPointAccessor;
import model.map.accessors.IStringAccessor;
import model.renderEngine.schemes.styles.LabelStyle;
import util.Vector2D;

public class LabelRenderer extends AbstractRenderer implements IRenderer {
    private IStringAccessor stringAccessor;
    private IElementIterator pointLabelIterator;
    private IElementIterator lineLabelIterator;
    private IElementIterator buildingIterator;

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

        styles[10] = new LabelStyle(13, 21, new int[] { 8, 8, 9, 10, 10, 10, 10, 10, 10, 10 }, Color.GRAY, false);
        styles[30] = new LabelStyle(10, 20, new int[] { 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10 }, Color.black,
                false);

        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Map<?, ?> map = (Map<?, ?>) (tk.getDesktopProperty("awt.font.desktophints"));

        hints = new HashMap<>();
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        if (map != null) {
            hints.putAll(map);
        }

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
        setMapManager(manager);
    }

    @Override
    protected boolean render(final Graphics2D g, int row, int column, int zoom, int x, int y) {
        g.setRenderingHints(hints);
        return drawBuildingNamesAndNumbers(g, row, column, zoom, x, y) | drawPointLabels(g, row, column, zoom, x, y)
                | drawLineLabels(g, row, column, zoom, x, y);
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        super.setMapManager(manager);

        stringAccessor = manager.createStringAccessor();
        pointLabelIterator = manager.getElementIterator("pointLabel");

        ICollectiveAccessor lineLabelAccessor = manager.createCollectiveAccessor("lineLabel");
        lineLabelIterator = manager.getElementIterator("lineLabel").filter((id, zoom, leaf) -> {
            lineLabelAccessor.setId(id);
            return lineLabelAccessor.getAttribute("zoom") == zoom || leaf;
        });
        buildingIterator = manager.getElementIterator("building");
    }

    private boolean drawPointLabels(Graphics2D g, int row, int column, int zoom, int x, int y) {
        boolean rendered = false;

        IPointAccessor pointLabelAccessor = manager.createPointAccessor("pointLabel");
        for (final OfInt it = pointLabelIterator.iterator(row, column, zoom); it.hasNext();) {
            pointLabelAccessor.setId(it.nextInt());

            final LabelStyle style = styles[pointLabelAccessor.getType()];
            if (style != null && style.isVisible(zoom)) {
                final String name = stringAccessor.getString(pointLabelAccessor.getAttribute("name"));
                drawStringPart(g, g.getFontMetrics(style.getFont(zoom)), name, style, pointLabelAccessor.getX(),
                        pointLabelAccessor.getY(), 0, x, y, zoom);

                rendered = true;
            }
        }

        return rendered;
    }

    private boolean drawLineLabels(final Graphics2D g, int row, int column, int zoom, int x, int y) {
        boolean rendered = false;

        ICollectiveAccessor lineLabelAccessor = manager.createCollectiveAccessor("lineLabel");
        for (final OfInt it = lineLabelIterator.iterator(row, column, zoom); it.hasNext();) {
            lineLabelAccessor.setId(it.nextInt());

            final LabelStyle style = styles[10];
            if (lineLabelAccessor.getAttribute("zoom") == zoom && style != null && style.isVisible(zoom)) {
                g.setColor(Color.BLACK);
                String name = stringAccessor.getString(lineLabelAccessor.getAttribute("name"));

                final Point last = new Point(lineLabelAccessor.getX(0), lineLabelAccessor.getY(0));
                final Point current = new Point();
                final Vector2D horizontal = new Vector2D(1, 0);
                final FontMetrics fontMetrics = g.getFontMetrics(style.getFont(zoom));

                double length = 0;
                for (int i = 1; i < lineLabelAccessor.size() && !name.isEmpty(); ++i) {
                    current.setLocation(lineLabelAccessor.getX(i), lineLabelAccessor.getY(i));
                    Vector2D vector = new Vector2D(current.getX() - last.getX(), current.getY() - last.getY());

                    double oldLength = length; // negative length
                    double segmentLength = converter.getPixelDistance(vector.norm(), zoom);
                    length += segmentLength;
                    if (length > 0) {
                        int stringPos = 0;
                        int stringWidth = 0;
                        do {
                            stringWidth = fontMetrics.stringWidth(name.substring(0, ++stringPos));
                        } while (length - stringWidth > 0 && stringPos < name.length());
                        length -= stringWidth;

                        final float angle = (float) Vector2D.angle(horizontal, vector);
                        final double factor = (stringWidth / 2 - oldLength) / segmentLength;
                        final double midX = last.x + (current.x - last.x) * factor;
                        final double midY = last.y + (current.y - last.y) * factor;
                        drawStringPart(g, fontMetrics, name.substring(0, stringPos), style, midX, midY, angle, x, y,
                                zoom);

                        name = name.substring(stringPos);
                    }
                    last.setLocation(current);
                }

            }
            rendered = true;
        }

        return rendered;
    }

    private void drawStringPart(Graphics2D g, FontMetrics fontMetrics, String part, LabelStyle style, double midX,
            double midY, double angle, int x, int y, int zoom) {
        final double sin = Math.sin(angle);
        final double cos = Math.cos(angle);
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

    private boolean drawBuildingNamesAndNumbers(Graphics2D g, int row, int column, int zoom, int x, int y) {
        boolean rendered = false;

        ICollectiveAccessor buildingAccessor = manager.createCollectiveAccessor("building");
        if (zoom >= buildingNumberMinZoomstep) {
            final FontMetrics metrics = g.getFontMetrics(buildingNumberFont);

            g.setFont(buildingNumberFont);
            g.setColor(buildingNumberColor);

            for (final OfInt it = buildingIterator.iterator(row, column, zoom); it.hasNext();) {
                buildingAccessor.setId(it.nextInt());

                // only draw number if name cannot be drawn.
                rendered |= draw(g, buildingAccessor, buildingAccessor.getAttribute("name"), x, y, zoom, metrics)
                        || draw(g, buildingAccessor, buildingAccessor.getAttribute("number"), x, y, zoom, metrics);

            }
        }

        return rendered;
    }

    private boolean draw(Graphics2D g, ICollectiveAccessor buildingAccessor, int id, int x, int y, int zoom,
            FontMetrics metrics) {
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
                final Point2D.Float center = calculateCenter(buildingAccessor, x, y, zoom);
                center.setLocation((center.getX() + minX + width / 2) / 2f, (center.getY() + minY + height / 2) / 2f);
                g.drawString(number, (float) (center.x - fontWidth / 2f),
                        (float) (center.y - fontHeight / 2f + metrics.getAscent()));

                return true;
            }
        }

        return false;
    }

    private Point2D.Float calculateCenter(ICollectiveAccessor buildingAccessor, int x, int y, int zoom) {
        final int size = buildingAccessor.size();
        float midX = 0f;
        float midY = 0f;

        for (int i = 0; i < size; i++) {
            midX += buildingAccessor.getX(i);
            midY += buildingAccessor.getY(i);
        }

        midX = midX / size;
        midY = midY / size;

        return new Point.Float(converter.getPixelDistance(midX - x, zoom), converter.getPixelDistance(midY - y, zoom));
    }
}
