package model.map;

import java.awt.Point;
import java.util.Iterator;

import model.elements.IBuilding;
import model.elements.IStreet;
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
    public final StreetNode getStreetNode(final Point coordinate) {
        StreetNode ret = null;

        long minDistSq = Long.MAX_VALUE;

        for (final Iterator<IStreet> streetIt = getStreets(); streetIt.hasNext();) {
            final IStreet iStreet = streetIt.next();

            float totalLength = 0;
            final int maxLength = iStreet.getLength();

            int lastX = iStreet.getX(0);
            int lastY = iStreet.getY(0);

            for (int i = 1; i < iStreet.size(); i++) {
                int currentX = iStreet.getX(i);
                int currentY = iStreet.getY(i);

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
                        ret = new StreetNode((float) ((totalLength + s * length) / maxLength), iStreet);
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
    public final IBuilding getBuilding(final Point coordinate) {
        for (final Iterator<IBuilding> iterator = getBuildings(); iterator.hasNext();) {
            final IBuilding building = iterator.next();
            if (building.contains(coordinate.x, coordinate.y)) {
                return building;
            }
        }

        return null;
    }
}