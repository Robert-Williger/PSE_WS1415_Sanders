package model.map;

import java.awt.Point;
import java.util.Iterator;

import model.elements.Building;
import model.elements.Street;
import model.elements.StreetNode;

public abstract class AbstractTile implements ITile {

    private final long id;

    public AbstractTile(final int zoomStep, final int row, final int column) {
        id = ((((long) zoomStep << 29) | row) << 29) | column;
    }

    @Override
    public final long getID() {
        return id;
    }

    @Override
    public final int getRow() {
        return (int) ((id >> 29) & 0x1FFFFFFFL);
    }

    @Override
    public final int getColumn() {
        return (int) (id & 0x1FFFFFFFL);
    }

    @Override
    public final int getZoomStep() {
        return (int) (id >> 58);
    }

    @Override
    public final String getAddress(final Point coordinate) {
        final StreetNode node = getStreetNode(coordinate);
        return node != null ? node.getStreet().getName() : null;
    }

    @Override
    public final StreetNode getStreetNode(final Point coordinate) {
        StreetNode ret = null;

        long minDistSq = Long.MAX_VALUE;

        for (final Iterator<Street> streetIt = getStreets(); streetIt.hasNext();) {
            final Street street = streetIt.next();

            float totalLength = 0;
            final int maxLength = street.getLength();

            final int[] xPoints = street.getXPoints();
            final int[] yPoints = street.getYPoints();

            int lastX = xPoints[0];
            int lastY = yPoints[0];

            for (int i = 1; i < street.size(); i++) {
                int currentX = xPoints[i];
                int currentY = yPoints[i];

                if (currentX != lastX || currentY != lastY) {
                    final long dx = currentX - lastX;
                    final long dy = currentY - lastY;
                    final long square = (dx * dx + dy * dy);
                    final float length = (float) Math.sqrt(square);
                    double s = ((coordinate.x - lastX) * dx + (coordinate.y - lastY) * dy) / (double) square;

                    if (s < 0) {
                        s = 0;
                    } else if (s > 1) {
                        s = 1;
                    }

                    final double distX = lastX + s * dx - coordinate.x;
                    final double distY = lastY + s * dy - coordinate.y;
                    final long distanceSq = (long) (distX * distX + distY * distY);

                    if (distanceSq < minDistSq) {
                        ret = new StreetNode((float) ((totalLength + s * length) / maxLength), street);
                        minDistSq = distanceSq;
                    }

                    totalLength += length;
                }
                lastX = currentX;
                lastY = currentY;
            }
        }

        return ret;
    }

    @Override
    public final Building getBuilding(final Point coordinate) {
        for (final Iterator<Building> iterator = getBuildings(); iterator.hasNext();) {
            final Building building = iterator.next();
            if (building.getPolygon().contains(coordinate)) {
                return building;
            }
        }

        return null;
    }
}