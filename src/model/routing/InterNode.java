package model.routing;

public class InterNode {
    private final int edge;
    private final int correspondingEdge;
    private final float offset;

    public InterNode(final int edge, final int correspondingEdge, final float offset) {
        this.edge = edge;
        this.correspondingEdge = correspondingEdge;
        this.offset = offset;
    }

    public int getEdge() {
        return edge;
    }

    public int getCorrespondingEdge() {
        return correspondingEdge;
    }

    public float getOffset() {
        return offset;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + correspondingEdge;
        result = prime * result + edge;
        result = prime * result + Float.floatToIntBits(offset);
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
        if (!(obj instanceof InterNode)) {
            return false;
        }
        InterNode other = (InterNode) obj;
        if (correspondingEdge != other.correspondingEdge) {
            return false;
        }
        if (edge != other.edge) {
            return false;
        }
        if (Float.floatToIntBits(offset) != Float.floatToIntBits(other.offset)) {
            return false;
        }
        return true;
    }
}