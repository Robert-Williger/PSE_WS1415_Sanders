package model.elements;

import java.awt.Polygon;

public class Area extends MultiElement {

    private final int type;

    public Area(final int[] xPoints, final int[] yPoints, final int type) {
        super(xPoints, yPoints);

        this.type = type;
    }

    //TODO store me?
    public Polygon getPolygon() {
        return new Polygon(xPoints, yPoints, size());
    }

    public int getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + type;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Area other = (Area) obj;
        if (type != other.type) {
            return false;
        }
        return true;
    }

}