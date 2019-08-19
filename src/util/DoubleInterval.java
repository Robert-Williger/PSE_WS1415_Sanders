package util;

public class DoubleInterval {

    private final double start;
    private final double end;

    public DoubleInterval(final double start, final double end) {
        this.start = start;
        this.end = end;
    }

    public double getStart() {
        return start;
    }

    public double getEnd() {
        return end;
    }

}