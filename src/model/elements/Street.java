package model.elements;

import java.awt.Point;

public class Street extends Way {

    private final long id;
    private final int length;

    // TODO avoid storage of empty street name
    public Street(final int[] xPoints, final int[] yPoints, final int type, final String name, final long id) {
        super(xPoints, yPoints, type, name);
        this.length = calculateLength();
        this.id = id;
    }

    public int getLength() {
        return length;
    }

    private int calculateLength() {
        if (xPoints.length > 1) {
            float totalLength = 0f;

            int lastX = xPoints[0];
            int lastY = yPoints[0];

            for (int i = 1; i < size(); i++) {
                int currentX = xPoints[i];
                int currentY = yPoints[i];
                totalLength += Point.distance(currentX, currentY, lastX, lastY);
                lastX = currentX;
                lastY = currentY;
            }

            return (int) totalLength;
        }

        return 0;
    }

    public long getID() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (id ^ (id >>> 32));
        // no need to add length to hashCode because it is fully depending on
        // the already hashed nodes/street
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Street other = (Street) obj;
        if (id != other.id) {
            return false;
        }
        // no need to compare length because it is fully depending on the
        // already compared nodes/street

        return true;
    }

}