package util;

public class FloatInterval {

    private final float start;
    private final float end;

    public FloatInterval(final float start, final float end) {
        this.start = start;
        this.end = end;
    }

    public float getStart() {
        return start;
    }

    public float getEnd() {
        return end;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(end);
        result = prime * result + Float.floatToIntBits(start);
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
        if (!(obj instanceof FloatInterval)) {
            return false;
        }
        FloatInterval other = (FloatInterval) obj;
        if (Float.floatToIntBits(end) != Float.floatToIntBits(other.end)) {
            return false;
        }
        if (Float.floatToIntBits(start) != Float.floatToIntBits(other.start)) {
            return false;
        }
        return true;
    }

}