package model.elements;

import java.awt.Point;

public class POI extends Node {

    private final int type;

    public POI(final Point location, final int type) {
        this(location.x, location.y, type);
    }

    public POI(final int x, final int y, final int type) {
        super(x, y);
        this.type = type;
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
        final POI other = (POI) obj;
        if (type != other.type) {
            return false;
        }
        return true;
    }

}