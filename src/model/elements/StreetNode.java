package model.elements;

import java.awt.Point;

public class StreetNode extends Node {

    private final float offset;
    private final IStreet street;

    private static final float EPSILON = 0.00001f;

    public StreetNode(final float offset, final IStreet street) {
        this.offset = Math.max(0, Math.min(1, offset));
        this.street = street;
        calculateLocation();
    }

    public float getOffset() {
        return offset;
    }

    public IStreet getStreet() {
        return street;
    }

    private void calculateLocation() {

        final int size = street.size();

        if (size > 0) {
            final float totalLength = street.getLength();
            final float maxLength = totalLength * offset;

            int lastX = street.getX(0);
            int lastY = street.getY(0);
            float currentOffsetLength = 0f;

            for (int i = 1; i < size; i++) {
                final int currentX = street.getX(i);
                final int currentY = street.getY(i);
                final double distance = Point.distance(lastX, lastY, currentX, currentY);

                if (currentOffsetLength + distance > maxLength || i == size - 1) {
                    final int xDistance = currentX - lastX;
                    final int yDistance = currentY - lastY;

                    final float partOffsetLength = maxLength - currentOffsetLength;
                    final float partOffset = (float) (partOffsetLength / distance);
                    setLocation((int) (lastX + xDistance * partOffset + 0.49f),
                            (int) (lastY + yDistance * partOffset + 0.49f));

                    return;
                }

                currentOffsetLength += distance;
                lastX = currentX;
                lastY = currentY;
            }
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