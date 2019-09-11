package model.renderEngine.renderers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.PrimitiveIterator.OfInt;

import model.map.IElementIterator;
import model.map.IMapManager;
import model.map.Predicate;
import model.map.accessors.CollectiveUtil;
import model.map.accessors.ICollectiveAccessor;
import util.FloatInterval;

public class RouteRenderer extends AbstractRenderer implements IRouteRenderer, IRenderer {

    private IRenderRoute route;
    private IElementIterator streetIterator;
    // for error correction of floating point number comparison
    private static final float EPSILON = 1.0001f;
    private static final float MIN_STROKE_WIDTH_PIXEL = 4f;
    private static final float MAX_STROKE_WIDTH_PIXEL = 8f;
    private static final float NORMAL_STROKE_WIDTH = 240f;
    private static final Color routeColor = new Color(0, 175, 251);

    public RouteRenderer(final IMapManager manager) {
        setMapManager(manager);
    }

    @Override
    protected boolean render(final Graphics2D g, int row, int column, int zoom, int x, int y) {
        if (route == null) {
            return false;
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        return drawRoute(g, row, column, zoom, x, y);
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        super.setMapManager(manager);
        this.route = null;
    }

    private boolean drawRoute(Graphics2D g, int row, int column, int zoom, int x, int y) {
        boolean rendered = false;

        final Path2D.Float path = new Path2D.Float();
        ICollectiveAccessor streetAccessor = manager.createCollectiveAccessor("street");
        for (final OfInt it = streetIterator.iterator(row, column, zoom); it.hasNext();) {
            streetAccessor.setId(it.nextInt());

            // TODO own instance for id to edge mapping
            final int id = streetAccessor.getAttribute("id");
            final int edge = id & 0x7FFFFFFF;

            // TODO optimize this --> no switch case needed

            boolean appended = true;
            switch (route.getStreetUse(edge)) {
                case full:
                    appendPath(path, streetAccessor, x, y, zoom);
                    break;
                case part:
                    appendStreetPartPath(path, streetAccessor, route.getStreetPart(edge), x, y, zoom);
                    break;
                case multiPart:
                    for (final FloatInterval streetPart : route.getStreetMultiPart(edge)) {
                        appendStreetPartPath(path, streetAccessor, streetPart, x, y, zoom);
                    }
                    break;
                default:
                    appended = false;
                    break;
            }
            rendered |= appended;
        }

        final float thickness = Math.min(MAX_STROKE_WIDTH_PIXEL,
                Math.max(MIN_STROKE_WIDTH_PIXEL, converter.getPixelDistance(NORMAL_STROKE_WIDTH, zoom)));

        final int cr = routeColor.getRed();
        final int cg = routeColor.getGreen();
        final int cb = routeColor.getBlue();

        for (int i = 1; i <= 5; i++) {
            g.setStroke(new BasicStroke((10 - i) * thickness / 10f, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND));
            g.setColor(new Color((int) (cr / 2f + i * cr / 10f), (int) (cg / 2f + i * cg / 10f),
                    (int) (cb / 2 + i * cb / 10f)));
            g.draw(path);
        }

        return rendered;
    }

    private void appendStreetPartPath(Path2D path, ICollectiveAccessor streetAccessor, final FloatInterval streetPart,
            int x, int y, int zoom) {
        final int size = streetAccessor.size();

        final int length = CollectiveUtil.getLength(streetAccessor);

        float currentLength = 0f;

        float lastCoordX = streetAccessor.getX(0);
        float lastCoordY = streetAccessor.getY(0);

        float lastPixelX = converter.getPixelDistance(lastCoordX - x, zoom);
        float lastPixelY = converter.getPixelDistance(lastCoordY - y, zoom);

        final Point2D startRenderPoint = new Point2D.Float();
        final Point2D endRenderPoint = new Point2D.Float();

        for (int i = 1; i < size && currentLength <= length * streetPart.getEnd(); i++) {
            final int currentCoordX = streetAccessor.getX(i);
            final int currentCoordY = streetAccessor.getY(i);
            final double distance = Point.distance(lastCoordX, lastCoordY, currentCoordX, currentCoordY);

            final float xDist = converter.getPixelDistance(currentCoordX - lastCoordX, zoom);
            final float yDist = converter.getPixelDistance(currentCoordY - lastCoordY, zoom);

            int currentPixelX = converter.getPixelDistance(currentCoordX - x, zoom);
            int currentPixelY = converter.getPixelDistance(currentCoordY - y, zoom);

            if (currentLength >= length * streetPart.getEnd()) {
                // rendering already finished
                return;
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
                    return;
                }

            }

            lastCoordX = currentCoordX;
            lastCoordY = currentCoordY;
            lastPixelX = currentPixelX;
            lastPixelY = currentPixelY;

            currentLength += distance;
        }
    }

    @Override
    public void setRenderRoute(final IRenderRoute route) {
        this.route = route;

        if (route != null) {
            ICollectiveAccessor streetAccessor = manager.createCollectiveAccessor("street");
            streetIterator = manager.getElementIterator("street").filter(new Predicate() {

                @Override
                public boolean test(int id, int zoom, boolean leaf) {
                    streetAccessor.setId(id);
                    final int edge = streetAccessor.getAttribute("id") & 0x7FFFFFFF;
                    return route.getStreetUse(edge) != StreetUse.none;
                }

                @Override
                public boolean cutOffTrees() {
                    return true;
                }

            });
        }
    }
}