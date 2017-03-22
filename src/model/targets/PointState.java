package model.targets;

public enum PointState {
    added(0), editing(1), unadded(2);

    public final static int STATES = 3;
    private final int index;

    private PointState(final int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}