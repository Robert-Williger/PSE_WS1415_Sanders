package adminTool.elements;

import java.awt.Point;
import java.util.Iterator;

public class StreetNode extends Node {

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
        calculateLocation();
    }

    private void calculateLocation() {

        final float totalLength = street.getLength();
        final Iterator<Node> iterator = street.iterator();
        Node lastNode = iterator.next();
        float currentOffsetLength = 0f;

        while (iterator.hasNext()) {
            final Node currentNode = iterator.next();
            final double distance = Point.distance(currentNode.getX(), currentNode.getY(), lastNode.getX(),
                    lastNode.getY());

            if (currentOffsetLength + distance > totalLength * offset || !iterator.hasNext()) {
                final int xDistance = currentNode.getX() - lastNode.getX();
                final int yDistance = currentNode.getY() - lastNode.getY();

                final float partOffsetLength = totalLength * offset - currentOffsetLength;
                final float partOffset = (float) (partOffsetLength / distance);
                setLocation((int) (lastNode.getX() + xDistance * partOffset + 0.49f), (int) (lastNode.getY()
                        + yDistance * partOffset + 0.49f));

                return;
            }

            currentOffsetLength += distance;
            lastNode = currentNode;
        }

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