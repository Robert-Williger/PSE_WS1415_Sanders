package model.renderEngine;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongConsumer;

import model.map.IMapManager;
import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.IPointAccessor;
import model.map.accessors.IStringAccessor;

public class LabelRenderer extends AbstractRenderer {
    private Graphics2D g;

    private IPointAccessor labelAccessor;
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
        styles[8] = new LabelStyle(13, 16, new int[] { 11, 12, 13, 14, 14 },
                new Color[] { Color.GRAY, Color.GRAY, Color.GRAY, Color.DARK_GRAY, Color.DARK_GRAY }, true);

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
    protected boolean render(final Image image) {
        g = (Graphics2D) image.getGraphics();
        g.addRenderingHints(hints);

        boolean rendered = drawBuildingNumbers() | drawLabels();

        g.dispose();
        if (rendered) {
            fireChange();
        }

        return rendered;
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        super.setMapManager(manager);

        labelAccessor = manager.createPointAccessor("label");
        buildingAccessor = manager.createCollectiveAccessor("building");
        stringAccessor = manager.createStringAccessor();
    }

    private boolean drawLabels() {
        final int zoom = tileAccessor.getZoom();
        final int x = tileAccessor.getX();
        final int y = tileAccessor.getY();

        final LongConsumer consumer = (label) -> {
            labelAccessor.setID(label);

            if (styles[labelAccessor.getType()] != null && styles[labelAccessor.getType()].mainStroke(g, zoom)) {
                // TODO avoid object generation
                final float angle = labelAccessor.getAttribute("rotation");
                final double sin = Math.sin(angle);
                final double cos = Math.cos(angle);
                // TODO avoid code redundance
                if (styles[labelAccessor.getType()].outlineStroke(g, zoom)) {
                    final String name = stringAccessor.getString(labelAccessor.getAttribute("name"));
                    TextLayout text = new TextLayout(name, g.getFont(), g.getFontRenderContext());

                    final Shape shape = text.getOutline(g.getTransform());
                    final Rectangle2D rect = text.getBounds();

                    final double xOffset = -cos * rect.getWidth() / 2 - sin * rect.getHeight() / 2
                            + sin * text.getDescent();
                    final double yOffset = cos * rect.getHeight() / 2 - sin * rect.getWidth() / 2
                            - cos * text.getDescent();
                    double transX = converter.getPixelDistance(labelAccessor.getX() - x, zoom) + xOffset;
                    double transY = converter.getPixelDistance(labelAccessor.getY() - y, zoom) + yOffset;

                    g.translate(transX, transY);
                    g.rotate(angle);
                    g.draw(shape);
                    styles[labelAccessor.getType()].mainStroke(g, zoom);
                    g.drawString(name, 0, 0);
                    g.rotate(-angle);
                    g.translate(-transX, -transY);
                } else if (styles[labelAccessor.getType()].mainStroke(g, zoom)) {
                    final String name = stringAccessor.getString(labelAccessor.getAttribute("name"));
                    final int rotation = labelAccessor.getAttribute("rotation");

                    final FontMetrics fontMetrics = g.getFontMetrics(g.getFont());
                    final int width = fontMetrics.stringWidth(name);
                    final int height = fontMetrics.getHeight();
                    final int descent = fontMetrics.getDescent();
                    final double xOffset = -cos * width / 2 - sin * height / 2 + sin * descent;
                    final double yOffset = cos * height / 2 - sin * width / 2 - cos * descent;
                    double transX = converter.getPixelDistance(labelAccessor.getX() - x, zoom) + xOffset;
                    double transY = converter.getPixelDistance(labelAccessor.getY() - y, zoom) + yOffset;

                    g.translate(transX, transY);
                    g.rotate(rotation);
                    g.drawString(name, 0, 0);
                    g.rotate(-rotation);
                    g.translate(-transX, -transY);
                }
            }
        };
        tileAccessor.forEach("label", consumer);

        // TODO return false if not rendered.
        return true;

    }

    private boolean drawBuildingNumbers() {
        boolean rendered = false;

        final int zoom = tileAccessor.getZoom();
        final int x = tileAccessor.getX();
        final int y = tileAccessor.getY();

        if (zoom >= buildingNumberMinZoomstep) {
            final FontMetrics metrics = g.getFontMetrics(buildingNumberFont);

            g.setFont(buildingNumberFont);
            g.setColor(buildingNumberColor);

            final LongConsumer consumer = (building) -> {
                buildingAccessor.setID(building);

                final String number = stringAccessor.getString(buildingAccessor.getAttribute("number"));
                if (!number.isEmpty()) {
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

                    final Rectangle2D fontRect = metrics.getStringBounds(number, g);

                    if (fontRect.getWidth() < width && fontRect.getHeight() < height) {
                        final Point2D.Float center = calculateCenter();
                        center.setLocation((center.getX() + minX + width / 2) / 2f,
                                (center.getY() + minY + height / 2) / 2f);
                        g.drawString(number, (float) (center.x - fontRect.getWidth() / 2f),
                                (float) (center.y - fontRect.getHeight() / 2f + metrics.getAscent()));
                    }
                }
            };
            tileAccessor.forEach("building", consumer);
        }

        // TODO return false...
        return rendered;
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
