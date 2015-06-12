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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (edge ^ (edge >>> 32));
        result = prime * result + Float.floatToIntBits(offset);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InterNode other = (InterNode) obj;
        if (edge != other.edge) {
            return false;
        }
        if (Float.floatToIntBits(offset) != Float.floatToIntBits(other.offset)) {
            return false;
        }
        return true;
    }

}