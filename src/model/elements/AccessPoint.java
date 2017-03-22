package model.elements;

public class AccessPoint {

    private float offset;
    private int street;

    public AccessPoint() {

    }

    public AccessPoint(final float offset, final int street) {
        this.offset = Math.max(0, Math.min(1, offset));
        this.street = street;
    }

    public void setOffset(final float offset) {
        this.offset = offset;
    }

    public void setStreet(final int street) {
        this.street = street;
    }

    public float getOffset() {
        return offset;
    }

    public int getStreet() {
        return street;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Float.floatToIntBits(offset);
        result = prime * result + street;
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
        if (!(obj instanceof AccessPoint)) {
            return false;
        }
        AccessPoint other = (AccessPoint) obj;
        if (Float.floatToIntBits(offset) != Float.floatToIntBits(other.offset)) {
            return false;
        }
        if (street != other.street) {
            return false;
        }
        return true;
    }

}