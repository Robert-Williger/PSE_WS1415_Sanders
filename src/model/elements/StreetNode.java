package model.elements;

import java.awt.Point;
import java.util.Iterator;

public class StreetNode extends Node {

    private boolean validLoc;
    private final float offset;
    private Street street;

    private static final float EPSILON = 0.00001f;

    public StreetNode(final float offset, final Street street) {
        this.offset = Math.max(0, Math.min(1, offset));
        setStreet(street);
    }

    public float getOffset() {
        return offset;
    }

    public Street getStreet() {
        return street;
    }

    public void setStreet(final Street street) {
        this.street = street;
        validLoc = false;
    }

    private void calculateLocation() {

        if (street.getNodes().size() == 1) {
            setLocation(street.getNodes().get(0).getLocation());
        } else {
            final float totalLength = street.getLength();
            final Iterator<Node> iterator = street.getNodes().iterator();
            Point lastNodeLocation = iterator.next().getLocation();
            float currentOffsetLength = 0f;

            while (iterator.hasNext()) {
                final Point currentNodeLocation = iterator.next().getLocation();
                final double distance = currentNodeLocation.distance(lastNodeLocation);

                if (currentOffsetLength + distance > totalLength * offset || !iterator.hasNext()) {
                    final int xDistance = currentNodeLocation.x - lastNodeLocation.x;
                    final int yDistance = currentNodeLocation.y - lastNodeLocation.y;

                    final float partOffsetLength = totalLength * offset - currentOffsetLength;
                    final float partOffset = (float) (partOffsetLength / distance);
                    setLocation((int) (lastNodeLocation.x + xDistance * partOffset + 0.49f), (int) (lastNodeLocation.y
                            + yDistance * partOffset + 0.49f));

                    validLoc = true;

                    return;
                }

                currentOffsetLength += distance;
                lastNodeLocation = currentNodeLocation;
            }
        }

    }

    @Override
    public Point getLocation() {
        if (!validLoc) {
            calculateLocation();
        }

        return super.getLocation();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime;
        result = prime * result + Float.floatToIntBits(offset);
        result = prime * result + ((street == null) ? 0 : street.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StreetNode other = (StreetNode) obj;
        if (!(Math.abs(offset - other.offset) < EPSILON)) {
            return false;
        }
        if (street == null) {
            if (other.street != null) {
                return false;
            }
        } else if (!street.equals(other.street)) {
            return false;
        }
        return true;
    }
}