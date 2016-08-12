package model.targets;

public enum PointState {
    added(0),
    editing(1),
    unadded(2);

    private PointState(final int index) {
        this.index = index;
    }

    private int index;

    public int getIndex() {
        return index;
    }
}
