package model.renderEngine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.PrimitiveIterator;

import model.map.IMapManager;
import model.map.accessors.CollectiveUtil;
import model.map.accessors.ICollectiveAccessor;

public class RouteRenderer extends AbstractRenderer implements IRouteRenderer {

    private Graphics2D g;
    private IRenderRoute route;
    private ICollectiveAccessor streetAccessor;
    // for error correction of floating point number comparison
    private static final float EPSILON = 1.0001f;
    private static final float MIN_STROKE_WIDTH_PIXEL = 4f;
    private static final float MAX_STROKE_WIDTH_PIXEL = 8f;
    private static final float NORMAL_STROKE_WIDTH = 240f;
    private static final Color routeColor = new Color(0, 175, 251);

    public RouteRenderer(final IMapManager manager) {
        super(manager);
    }

    @Override
    protected boolean render(final Image image) {
        if (image == null) {
            return false;
        }

        if (route == null) {
            return false;
        }

        g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        if (drawRoute()) {
            g.dispose();
            fireChange();
            return true;
        }

        g.dispose();
        return false;
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        super.setMapManager(manager);
        streetAccessor = manager.createCollectiveAccessor("street");
    }

    private boolean drawRoute() {
        final PrimitiveIterator.OfLong iterator = tileAccessor.getElements("street");

        final Path2D.Float path = new Path2D.Float();

        while (iterator.hasNext()) {
            final long street = iterator.nextLong();
            streetAccessor.setID(street);

            final int id = streetAccessor.getAttribute("graphID");

            // TODO optimize this --> no switch case needed
            switch (route.getStreetUse(id)) {
                case full:
                    path.append(drawLines().getPathIterator(null), false);
                    break;
                case part:
                    path.append(createStreetPartPath(route.getStreetPart(id)).getPathIterator(null), false);
                    break;
                case multiPart:
                    for (final Intervall streetPart : route.getStreetMultiPart(id)) {
                        path.append(createStreetPartPath(streetPart).getPathIterator(null), false);
                    }
                    break;
                default:
                    break;
            }
        }

        final float thickness = Math.min(
                MAX_STROKE_WIDTH_PIXEL,
                Math.max(MIN_STROKE_WIDTH_PIXEL,
                        converter.getPixelDistancef(NORMAL_STROKE_WIDTH, tileAccessor.getZoom())));

        final int cr = routeColor.getRed();
        final int cg = routeColor.getGreen();
        final int cb = routeColor.getBlue();

        for (int i = 1; i <= 5; i++) {
            g.setStroke(new BasicStroke((10 - i) * thickness / 10f, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND));
            g.setColor(new Color((int) (cr / 2f + i * cr / 10f), (int) (cg / 2f + i * cg / 10f), (int) (cb / 2 + i * cb
                    / 10f)));
            g.draw(path);
        }
        return true;
    }

    private Path2D.Float createStreetPartPath(final Intervall streetPart) {
        final Path2D.Float path = new Path2D.Float();

        final int x = tileAccessor.getX();
        final int y = tileAccessor.getY();
        final int zoom = tileAccessor.getZoom();
        final int size = streetAccessor.size();
        final int length = CollectiveUtil.getLength(streetAccessor);

        float currentLength = 0f;

        int lastCoordX = streetAccessor.getX(0);
        int lastCoordY = streetAccessor.getY(0);

        int lastPixelX = converter.getPixelDistance(lastCoordX - x, zoom);
        int lastPixelY = converter.getPixelDistance(lastCoordY - y, zoom);

        final Point2D startRenderPoint = new Point2D.Float();
        final Point2D endRenderPoint = new Point2D.Float();

        for (int i = 1; i < size && currentLength <= length * streetPart.getEnd(); i++) {
            final int currentCoordX = streetAccessor.getX(i);
            final int currentCoordY = streetAccessor.getY(i);
            final double distance = Point.distance(lastCoordX, lastCoordY, currentCoordX, currentCoordY);

            final int xDist = converter.getPixelDistance(currentCoordX - lastCoordX, zoom);
            final int yDist = converter.getPixelDistance(currentCoordY - lastCoordY, zoom);

            int currentPixelX = converter.getPixelDistance(currentCoordX - x, zoom);
            int currentPixelY = converter.getPixelDistance(currentCoordY - y, zoom);

            if (currentLength >= length * streetPart.getEnd()) {
                // rendering already finished
                return path;
            } else if (currentLength + distance > length * streetPart.getStart()) {
                // rendering in this iteration
                if (currentLength <= length * streetPart.getStart()) {
                    final float startOffsetLength = length * streetPart.getStart() - currentLength;
                    final float startOffset = (float) (startOffsetLength / distance);

                    startRenderPoint.setLocation(lastPixelX + xDist * startOffset, lastPixelY + yDist * startOffset);
                    path.moveTo(startRenderPoint.getX(), startRenderPoint.getY());
                } else {
                    startRenderPoint.setLocation(lastPixelX, lastPixelY);
                    path.lineTo(startRenderPoint.getX(), startRenderPoint.getY());
                }

                if ((currentLength + distance) * EPSILON >= length * streetPart.getEnd()) {
                    final float endOffsetLength = length * streetPart.getEnd() - currentLength;
                    final float endOffset = (float) (endOffsetLength / distance);

                    endRenderPoint.setLocation(lastPixelX + xDist * endOffset, lastPixelY + yDist * endOffset);
                    path.lineTo(endRenderPoint.getX(), endRenderPoint.getY());
                    return path;
                }

            }

            lastCoordX = currentCoordX;
            lastCoordY = currentCoordY;
            lastPixelX = currentPixelX;
            lastPixelY = currentPixelY;

            currentLength += distance;
        }

        return path;
    }

    private Path2D.Float drawLines() {
        final Path2D.Float path = new Path2D.Float();

        final int x = tileAccessor.getX();
        final int y = tileAccessor.getY();
        final int zoom = tileAccessor.getZoom();
        final int size = streetAccessor.size();

        path.moveTo(converter.getPixelDistance(streetAccessor.getX(0) - x, zoom),
                converter.getPixelDistance(streetAccessor.getY(0) - y, zoom));

        for (int i = 1; i < size; i++) {
            path.lineTo(converter.getPixelDistance(streetAccessor.getX(i) - x, zoom),
                    converter.getPixelDistance(streetAccessor.getY(i) - y, zoom));
        }

        return path;
    }

    @Override
    public void setRenderRoute(final IRenderRoute route) {
        this.route = route;
    }
}