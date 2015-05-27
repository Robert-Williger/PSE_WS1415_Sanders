package model.renderEngine;

public class Intervall {

    private final float start;
    private final float end;

    public Intervall(final float start, final float end) {
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
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Intervall)) {
            return false;
        }

        final Intervall interObj = (Intervall) obj;

        return start == interObj.start && end == interObj.end;
    }

}