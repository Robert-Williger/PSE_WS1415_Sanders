package model.map;

import java.awt.Point;
import java.util.Iterator;

import model.elements.Building;
import model.elements.Node;
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

        int minDist = Integer.MAX_VALUE;

        for (final Iterator<Street> streetIt = getStreets(); streetIt.hasNext();) {
            final Street street = streetIt.next();

            final Iterator<Node> nodeIt = street.iterator();
            float totalLength = 0;
            final int maxLength = street.getLength();

            Point lastPoint = nodeIt.next().getLocation();
            while (nodeIt.hasNext()) {
                final Point currentPoint = nodeIt.next().getLocation();

                if (!currentPoint.equals(lastPoint)) {
                    final long dx = currentPoint.x - lastPoint.x;
                    final long dy = currentPoint.y - lastPoint.y;
                    final long square = (dx * dx + dy * dy);
                    final float length = (float) Math.sqrt(square);
                    double s = ((coordinate.x - lastPoint.x) * dx + (coordinate.y - lastPoint.y) * dy)
                            / (double) square;

                    if (s < 0) {
                        s = 0;
                    } else if (s > 1) {
                        s = 1;
                    }

                    final double distX = lastPoint.x + s * dx - coordinate.x;
                    final double distY = lastPoint.y + s * dy - coordinate.y;
                    final int distance = (int) Math.sqrt(distX * distX + distY * distY);

                    if (distance < minDist) {
                        ret = new StreetNode((float) ((totalLength + s * length) / maxLength), street);
                        minDist = distance;
                    }

                    totalLength += length;
                }
                lastPoint = currentPoint;
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