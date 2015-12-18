package model.renderEngine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Iterator;

import model.elements.IMultiElement;
import model.elements.IStreet;
import model.map.IPixelConverter;
import model.map.ITile;

public class RouteRenderer extends AbstractRenderer implements IRouteRenderer {

    private IRenderRoute route;
    // for error correction of floating point number comparison
    private static final float EPSILON = 1.0001f;
    private static final float MIN_STROKE_WIDTH_PIXEL = 4f;
    private static final float MAX_STROKE_WIDTH_PIXEL = 8f;
    private static final float NORMAL_STROKE_WIDTH = 240f;
    private static final Color routeColor = new Color(0, 175, 251);

    public RouteRenderer(final IPixelConverter converter) {
        this.converter = converter;
    }

    @Override
    public boolean render(final ITile tile, final Image image) {
        if (tile == null || image == null) {
            return false;
        }

        if (route == null) {
            return false;
        }

        final Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        final Point tileLocation = getTileLocation(tile, image);
        if (drawRoute(tile, tileLocation, g)) {
            g.dispose();
            fireChange();
            return true;
        }

        g.dispose();
        return false;
    }

    private boolean drawRoute(final ITile tile, final Point tileLoc, final Graphics2D g) {
        final Iterator<IStreet> iterator = tile.getStreets();
        if (tile.getStreets() == null) {
            return false;
        }

        final Path2D.Float path = new Path2D.Float();

        while (iterator.hasNext()) {
            final IStreet street = iterator.next();
            if (street == null) {
                return false;
            }

            final int id = street.getID();

            // TODO optimize this --> no switch case needed
            switch (route.getStreetUse(id)) {
                case full:
                    path.append(drawLines(street, tileLoc, tile.getZoomStep()).getPathIterator(null), false);
                    break;
                case part:
                    path.append(
                            createStreetPartPath(street, tileLoc, tile.getZoomStep(),
                                    route.getStreetPart(street.getID())).getPathIterator(null), false);
                    break;
                case multiPart:
                    for (final Intervall streetPart : route.getStreetMultiPart(street.getID())) {
                        path.append(createStreetPartPath(street, tileLoc, tile.getZoomStep(), streetPart)
                                .getPathIterator(null), false);
                    }
                    break;
                default:
                    break;
            }
        }

        final float thickness = Math.min(MAX_STROKE_WIDTH_PIXEL,
                Math.max(MIN_STROKE_WIDTH_PIXEL, converter.getPixelDistancef(NORMAL_STROKE_WIDTH, tile.getZoomStep())));

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

    private Path2D.Float createStreetPartPath(final IStreet street, final Point tileLoc, final int zoomStep,
            final Intervall streetPart) {
        final Path2D.Float path = new Path2D.Float();
        float currentLength = 0f;

        int lastCoordX = street.getX(0);
        int lastCoordY = street.getY(0);
        int lastPixelX = converter.getPixelDistance(lastCoordX - tileLoc.x, zoomStep);
        int lastPixelY = converter.getPixelDistance(lastCoordY - tileLoc.y, zoomStep);

        final Point2D startRenderPoint = new Point2D.Float();
        final Point2D endRenderPoint = new Point2D.Float();

        for (int i = 1; i < street.size() && currentLength <= street.getLength() * streetPart.getEnd(); i++) {

            final int currentCoordX = street.getX(i);
            final int currentCoordY = street.getY(i);
            final double distance = Point.distance(lastCoordX, lastCoordY, currentCoordX, currentCoordY);

            final int xDist = converter.getPixelDistance(currentCoordX - lastCoordX, zoomStep);
            final int yDist = converter.getPixelDistance(currentCoordY - lastCoordY, zoomStep);

            int currentPixelX = converter.getPixelDistance(currentCoordX - tileLoc.x, zoomStep);
            int currentPixelY = converter.getPixelDistance(currentCoordY - tileLoc.y, zoomStep);

            if (currentLength >= street.getLength() * streetPart.getEnd()) {
                // rendering already finished
                return path;
            } else if (currentLength + distance > street.getLength() * streetPart.getStart()) {
                // rendering in this iteration
                if (currentLength <= street.getLength() * streetPart.getStart()) {
                    final float startOffsetLength = street.getLength() * streetPart.getStart() - currentLength;
                    final float startOffset = (float) (startOffsetLength / distance);

                    startRenderPoint.setLocation(lastPixelX + xDist * startOffset, lastPixelY + yDist * startOffset);
                    path.moveTo(startRenderPoint.getX(), startRenderPoint.getY());
                } else {
                    startRenderPoint.setLocation(lastPixelX, lastPixelY);
                    path.lineTo(startRenderPoint.getX(), startRenderPoint.getY());
                }

                if ((currentLength + distance) * EPSILON >= street.getLength() * streetPart.getEnd()) {
                    final float endOffsetLength = street.getLength() * streetPart.getEnd() - currentLength;
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

    private Path2D.Float drawLines(final IMultiElement element, final Point tileLoc, final int zoomStep) {
        final Path2D.Float path = new Path2D.Float();

        int x = converter.getPixelDistance(element.getX(0) - tileLoc.x, zoomStep);
        int y = converter.getPixelDistance(element.getY(0) - tileLoc.y, zoomStep);

        path.moveTo(x, y);
        for (int i = 1; i < element.size(); i++) {

            x = converter.getPixelDistance(element.getX(i) - tileLoc.x, zoomStep);
            y = converter.getPixelDistance(element.getY(i) - tileLoc.y, zoomStep);

            path.lineTo(x, y);
        }

        return path;
    }

    @Override
    public void setRenderRoute(final IRenderRoute route) {
        this.route = route;
    }
}