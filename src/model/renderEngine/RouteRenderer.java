package model.renderEngine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import model.elements.Node;
import model.elements.Street;
import model.map.IPixelConverter;
import model.map.ITile;
import model.targets.IRoutePoint;

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
        final Iterator<Street> iterator = tile.getStreets();
        if (tile.getStreets() == null) {
            return false;
        }

        final Path2D.Float path = new Path2D.Float();

        final Map<Long, Collection<Street>> map = new HashMap<Long, Collection<Street>>();

        while (iterator.hasNext()) {
            final Street street = iterator.next();
            if (street == null) {
                return false;
            }

            final long id = street.getID();

            if (route.getStreetUse(id) != StreetUse.none) {
                if (!map.containsKey(id)) {
                    map.put(id, new HashSet<Street>());
                }

                map.get(id).add(street);
            }
        }

        if (map.isEmpty()) {
            return false;
        }

        for (final Entry<Long, Collection<Street>> entry : map.entrySet()) {
            if (entry.getValue().size() <= 1) {
                for (final Street street : entry.getValue()) {
                    switch (route.getStreetUse(street.getID())) {
                        case full:
                            path.append(
                                    drawLines(street.iterator(), tileLoc, tile.getZoomStep()).getPathIterator(null),
                                    false);
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
            } else {
                switch (route.getStreetUse(entry.getKey())) {
                    case full:
                        Street renderStreet = null;
                        int minLength = Integer.MAX_VALUE;

                        for (final Street street : entry.getValue()) {
                            if (street.getLength() < minLength) {
                                minLength = street.getLength();
                                renderStreet = street;
                            }
                        }

                        path.append(
                                drawLines(renderStreet.iterator(), tileLoc, tile.getZoomStep()).getPathIterator(null),
                                false);
                        break;
                    case part:
                        for (final IRoutePoint routePoint : route.getPointList()) {
                            if (routePoint.getStreetNode().getStreet().getID() == entry.getKey()) {
                                for (final Street street : entry.getValue()) {
                                    if (street.equals(routePoint.getStreetNode().getStreet())) {
                                        path.append(
                                                createStreetPartPath(street, tileLoc, tile.getZoomStep(),
                                                        route.getStreetPart(street.getID())).getPathIterator(null),
                                                false);
                                    }
                                }

                                break;
                            }
                        }
                        break;
                    case multiPart:
                        for (final IRoutePoint routePoint : route.getPointList()) {
                            if (routePoint.getStreetNode().getStreet().getID() == entry.getKey()) {
                                for (final Street street : entry.getValue()) {
                                    if (street.equals(routePoint.getStreetNode().getStreet())) {
                                        for (final Intervall streetPart : route.getStreetMultiPart(street.getID())) {
                                            path.append(
                                                    createStreetPartPath(street, tileLoc, tile.getZoomStep(),
                                                            streetPart).getPathIterator(null), false);
                                        }
                                    }
                                }

                                break;
                            }
                        }
                        break;
                    default:
                        break;
                }
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

    private Path2D.Float createStreetPartPath(final Street street, final Point tileLoc, final int zoomStep,
            final Intervall streetPart) {
        final Path2D.Float path = new Path2D.Float();
        float currentLength = 0f;
        final Iterator<Node> iterator = street.iterator();

        Point firstCoordPoint = iterator.next().getLocation();
        final Point firstPixelPoint = new Point(converter.getPixelDistance(firstCoordPoint.x - tileLoc.x, zoomStep),
                converter.getPixelDistance(firstCoordPoint.y - tileLoc.y, zoomStep));

        final Point secondPixelPoint = new Point(firstPixelPoint);
        Point secondCoordPoint = null;

        final Point2D startRenderPoint = new Point2D.Float();
        final Point2D endRenderPoint = new Point2D.Float();

        while (iterator.hasNext() && currentLength <= street.getLength() * streetPart.getEnd()) {
            secondCoordPoint = iterator.next().getLocation();
            final double distance = secondCoordPoint.distance(firstCoordPoint);

            final int xDist = converter.getPixelDistance(secondCoordPoint.x - firstCoordPoint.x, zoomStep);
            final int yDist = converter.getPixelDistance(secondCoordPoint.y - firstCoordPoint.y, zoomStep);

            secondPixelPoint.setLocation(converter.getPixelDistance(secondCoordPoint.x - tileLoc.x, zoomStep),
                    converter.getPixelDistance(secondCoordPoint.y - tileLoc.y, zoomStep));

            if (currentLength >= street.getLength() * streetPart.getEnd()) {
                // rendering already finished
                return path;
            } else if (currentLength + distance > street.getLength() * streetPart.getStart()) {
                // rendering in this iteration
                if (currentLength <= street.getLength() * streetPart.getStart()) {
                    final float startOffsetLength = street.getLength() * streetPart.getStart() - currentLength;
                    final float startOffset = (float) (startOffsetLength / distance);

                    startRenderPoint.setLocation(firstPixelPoint.x + xDist * startOffset, firstPixelPoint.y + yDist
                            * startOffset);
                    path.moveTo(startRenderPoint.getX(), startRenderPoint.getY());
                } else {
                    startRenderPoint.setLocation(firstPixelPoint);
                    path.lineTo(startRenderPoint.getX(), startRenderPoint.getY());
                }

                if ((currentLength + distance) * EPSILON >= street.getLength() * streetPart.getEnd()) {
                    final float endOffsetLength = street.getLength() * streetPart.getEnd() - currentLength;
                    final float endOffset = (float) (endOffsetLength / distance);

                    endRenderPoint.setLocation(firstPixelPoint.x + xDist * endOffset, firstPixelPoint.y + yDist
                            * endOffset);
                    path.lineTo(endRenderPoint.getX(), endRenderPoint.getY());
                    return path;
                }

            }

            firstCoordPoint = secondCoordPoint;
            firstPixelPoint.setLocation(secondPixelPoint);
            currentLength += distance;
        }

        return path;
    }

    private Path2D.Float drawLines(final Iterator<Node> nodes, final Point tileLoc, final int zoomStep) {
        final Path2D.Float path = new Path2D.Float();

        final Point firstCoordPoint = nodes.next().getLocation();
        final Point firstPixelPoint = new Point(firstCoordPoint);
        firstPixelPoint.x = converter.getPixelDistance(firstPixelPoint.x - tileLoc.x, zoomStep);
        firstPixelPoint.y = converter.getPixelDistance(firstPixelPoint.y - tileLoc.y, zoomStep);

        path.moveTo(firstPixelPoint.x, firstPixelPoint.y);

        final Point secondPixelPoint = new Point(firstPixelPoint);
        Point secondCoordPoint = null;

        while (nodes.hasNext()) {
            secondCoordPoint = nodes.next().getLocation();

            secondPixelPoint.x = converter.getPixelDistance(secondCoordPoint.x - tileLoc.x, zoomStep);
            secondPixelPoint.y = converter.getPixelDistance(secondCoordPoint.y - tileLoc.y, zoomStep);

            path.lineTo(secondPixelPoint.x, secondPixelPoint.y);
        }

        return path;
    }

    @Override
    public void setRenderRoute(final IRenderRoute route) {
        this.route = route;
    }
}