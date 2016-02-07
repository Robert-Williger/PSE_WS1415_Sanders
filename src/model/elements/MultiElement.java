package model.elements;

import java.util.Arrays;

public abstract class MultiElement implements IMultiElement {

    protected final int[] points;

    public MultiElement(final int[] points) {
        this.points = points;
    }

    @Deprecated
    public MultiElement(final int[] x, final int[] y) {
        points = new int[x.length * 2];
        for (int i = 0; i < x.length; i++) {
            points[i << 1] = x[i];
            points[(i << 1) + 1] = y[i];
        }
    }

    public int size() {
        return points.length >> 1;
    }

    public int getX(final int i) {
        return points[i << 1];
    }

    public int getY(final int i) {
        return points[(i << 1) + 1];
    }

    public abstract IMultiElement getSubElement(final int[] subarray);

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(points);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MultiElement)) {
            return false;
        }
        MultiElement other = (MultiElement) obj;
        if (!Arrays.equals(points, other.points)) {
            return false;
        }
        return true;
    }
}