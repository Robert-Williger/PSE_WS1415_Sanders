package model.elements;

public class MultiElement {

    // TODO store in one array
    protected final int[] xPoints;
    protected final int[] yPoints;

    public MultiElement(final int[] xPoints, final int[] yPoints) {
        this.xPoints = xPoints;
        this.yPoints = yPoints;
    }

    public int size() {
        return xPoints.length;
    }

    public int[] getXPoints() {
        return xPoints;
    }

    public int[] getYPoints() {
        return yPoints;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + java.util.Arrays.hashCode(xPoints);
        result = prime * result + java.util.Arrays.hashCode(yPoints);
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        MultiElement other = (MultiElement) obj;
        if (!java.util.Arrays.equals(xPoints, other.xPoints)) {
            return false;
        }
        if (!java.util.Arrays.equals(yPoints, other.yPoints)) {
            return false;
        }
        return true;
    }
}