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
import java.util.Iterator;
import java.util.Map;

import model.elements.IBuilding;
import model.elements.IMultiElement;
import model.elements.Label;
import model.map.IPixelConverter;
import model.map.ITile;

public class LabelRenderer extends AbstractRenderer implements IRenderer {

    private static final LabelStyle[] styles;

    private static final int buildingNumberMinZoomstep = 18;
    private static final Font buildingNumberFont = new Font("Times New Roman", Font.PLAIN, 10);
    private static final Color buildingNumberColor = new Color(96, 96, 96);

    static {
        styles = new LabelStyle[31];

        styles[2] = new LabelStyle(8, 14, new int[]{12, 12, 12, 14, 14, 16, 16}, Color.DARK_GRAY, true);
        styles[4] = new LabelStyle(8, 14, new int[]{11, 12, 14, 16, 16, 16, 16}, Color.DARK_GRAY, true);
        styles[5] = new LabelStyle(10, 15, new int[]{10, 11, 12, 13, 14, 16}, new Color[]{Color.GRAY, Color.GRAY,
                Color.DARK_GRAY, Color.DARK_GRAY, Color.DARK_GRAY, Color.DARK_GRAY}, true);
        styles[6] = new LabelStyle(12, 16, new int[]{10, 11, 12, 13, 14}, new Color[]{Color.GRAY, Color.GRAY,
                Color.GRAY, Color.DARK_GRAY, Color.DARK_GRAY}, true);
        styles[8] = new LabelStyle(13, 16, new int[]{11, 12, 13, 14, 14}, new Color[]{Color.GRAY, Color.GRAY,
                Color.GRAY, Color.DARK_GRAY, Color.DARK_GRAY}, true);

        styles[30] = new LabelStyle(10, 20, new int[]{10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10}, Color.black, false);
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

    public LabelRenderer(final IPixelConverter converter) {
        setConverter(converter);
    }

    @Override
    public boolean render(final ITile tile, final Image image) {
        final Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);

        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Map<?, ?> map = (Map<?, ?>) (tk.getDesktopProperty("awt.font.desktophints"));
        if (map != null) {
            g.addRenderingHints(map);
        }

        final Point tileLocation = getTileLocation(tile, image);

        boolean rendered = drawBuildingNumbers(tile, tileLocation, g) | drawLabels(tile, tileLocation, g);

        g.dispose();
        if (rendered) {
            fireChange();
        }

        return rendered;
    }

    private boolean drawLabels(final ITile tile, final Point tileLocation, final Graphics2D g) {
        final int zoom = tile.getZoomStep();

        final Iterator<Label> iterator = tile.getLabels();
        boolean rendered = false;
        while (iterator.hasNext()) {
            rendered = true;
            final Label label = iterator.next();

            if (styles[label.getType()] != null && styles[label.getType()].mainStroke(g, zoom)) {
                // TODO avoid object generation
                final float angle = label.getRotation();
                final double sin = Math.sin(angle);
                final double cos = Math.cos(angle);
                if (styles[label.getType()].outlineStroke(g, zoom)) {
                    TextLayout text = new TextLayout(label.getName(), g.getFont(), g.getFontRenderContext());

                    final Shape shape = text.getOutline(g.getTransform());
                    final Rectangle2D rect = text.getBounds();

                    final double xOffset = -cos * rect.getWidth() / 2 - sin * rect.getHeight() / 2 + sin
                            * text.getDescent();
                    final double yOffset = cos * rect.getHeight() / 2 - sin * rect.getWidth() / 2 - cos
                            * text.getDescent();
                    double x = converter.getPixelDistancef(label.getX() - tileLocation.x, tile.getZoomStep()) + xOffset;
                    double y = converter.getPixelDistancef(label.getY() - tileLocation.y, tile.getZoomStep()) + yOffset;

                    g.translate(x, y);
                    g.rotate(angle);
                    g.draw(shape);
                    styles[label.getType()].mainStroke(g, zoom);
                    g.drawString(label.getName(), 0, 0);
                    g.rotate(-angle);
                    g.translate(-x, -y);
                } else if (styles[label.getType()].mainStroke(g, zoom)) {
                    final FontMetrics fontMetrics = g.getFontMetrics(g.getFont());
                    final int width = fontMetrics.stringWidth(label.getName());
                    final int height = fontMetrics.getHeight();
                    final int descent = fontMetrics.getDescent();
                    final double xOffset = -cos * width / 2 - sin * height / 2 + sin * descent;
                    final double yOffset = cos * height / 2 - sin * width / 2 - cos * descent;
                    double x = converter.getPixelDistancef(label.getX() - tileLocation.x, tile.getZoomStep()) + xOffset;
                    double y = converter.getPixelDistancef(label.getY() - tileLocation.y, tile.getZoomStep()) + yOffset;

                    g.translate(x, y);
                    g.rotate(label.getRotation());
                    g.drawString(label.getName(), 0, 0);
                    g.rotate(-label.getRotation());
                    g.translate(-x, -y);
                }

            }
        }

        return rendered;
    }

    private boolean drawBuildingNumbers(final ITile tile, final Point tileLocation, final Graphics2D g) {
        final int zoom = tile.getZoomStep();

        boolean rendered = false;

        if (zoom >= buildingNumberMinZoomstep) {
            final FontMetrics metrics = g.getFontMetrics(buildingNumberFont);

            g.setFont(buildingNumberFont);
            g.setColor(buildingNumberColor);

            Iterator<IBuilding> iterator = tile.getBuildings();

            while (iterator.hasNext()) {
                final IBuilding building = iterator.next();

                int minX = Integer.MAX_VALUE;
                int minY = Integer.MAX_VALUE;
                int maxX = Integer.MIN_VALUE;
                int maxY = Integer.MIN_VALUE;

                for (int i = 0; i < building.size(); i++) {
                    int x = building.getX(i);
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    int y = building.getY(i);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }

                int width = converter.getPixelDistance(maxX - minX, zoom);
                int height = converter.getPixelDistance(maxY - minY, zoom);
                minX = converter.getPixelDistance(minX - tileLocation.x, zoom);
                minY = converter.getPixelDistance(minY - tileLocation.y, zoom);

                final String number = building.getHouseNumber();
                if (!number.isEmpty()) {
                    final Rectangle2D fontRect = metrics.getStringBounds(number, g);

                    if (fontRect.getWidth() < width && fontRect.getHeight() < height) {
                        rendered = true;
                        final Point2D.Float center = calculateCenter(building, tileLocation, zoom);
                        center.setLocation((center.getX() + minX + width / 2) / 2f,
                                (center.getY() + minY + height / 2) / 2f);

                        g.drawString(number, (float) (center.x - fontRect.getWidth() / 2f), (float) (center.y
                                - fontRect.getHeight() / 2f + metrics.getAscent()));
                    }
                }
            }
        }

        return rendered;
    }

    private Point2D.Float calculateCenter(final IMultiElement element, final Point tileLocation, final int zoom) {
        float x = 0f;
        float y = 0f;

        int totalPoints = element.size() - 1;
        for (int i = 0; i < totalPoints; i++) {
            x += element.getX(i);
            y += element.getY(i);
        }

        if (element.getX(0) != element.getX(totalPoints) || element.getY(0) != element.getY(totalPoints)) {
            x += element.getX(totalPoints);
            y += element.getY(totalPoints);
            totalPoints++;
        }

        x = x / totalPoints;
        y = y / totalPoints;

        return new Point.Float(converter.getPixelDistancef(x - tileLocation.x, zoom), converter.getPixelDistancef(y
                - tileLocation.y, zoom));
    }
}
