package adminTool.labeling.roadGraph;

public enum RoadType {
    Road(0),
    Junction(1),
    Overlap(2),
    BadShape(3),
    UnconnectedJunction(4),
    Stub(5),
    Cycle(6);

    private final int index;
    private RoadType(final int index) {
        this.index = index;
    }
    public int getIndex() {
        return index;
    }
}
