package model.map.accessors;

import java.awt.Point;

import model.map.accessors.CollectiveAccessor;

public class StreetAccessor extends CollectiveAccessor {

    public StreetAccessor(final int[] x, final int[] y, final int[] distribution, final int[] data) {
        super(x, y, distribution, data);
    }

    @Override
    public int getAttribute(final String identifier) {
        switch (identifier) {
            case "name":
                return data[getIntID() + 1];
            case "graphId":
                return data[getIntID()] & 0x7FFFFFFF;
            case "oneway":
                return data[(int) getID()] >>> 7;
            case "length":
                return getLength();
            default:
                return super.getAttribute(identifier);
        }
    }

    private int getLength() {
        float totalLength = 0f;

        int lastX = getX(0);
        int lastY = getY(0);
        int size = size();
        for (int i = 1; i < size; i++) {
            int currentX = getX(i);
            int currentY = getY(i);
            totalLength += Point.distance(currentX, currentY, lastX, lastY);
            lastX = currentX;
            lastY = currentY;
        }

        return (int) totalLength;
    }
}
