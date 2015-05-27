package model.routing;

public class InterNode {
    private final long edge;
    private final float offset;

    public InterNode(final long edge, final float offset) {
        this.edge = edge;
        this.offset = offset;
    }

    public long getEdge() {
        return edge;
    }

    public float getOffset() {
        return offset;
    }

}