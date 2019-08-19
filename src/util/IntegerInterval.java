package util;

public class IntegerInterval {
    private int start;
    private int end;

    public IntegerInterval(final int start, final int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public void setStart(final int start) {
        this.start = start;
    }

    public void setEnd(final int end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "IntegerInterval [start=" + start + ", end=" + end + "]";
    }

}