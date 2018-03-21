package adminTool.elements;

public class MultiElement implements Typeable {

    private final int[] indices;
    private final int type;

    public MultiElement(final int[] indices, final int type) {
        this.indices = indices;
        this.type = type;
    }

    public int size() {
        return indices.length;
    }

    public int getNode(int index) {
        return indices[index];
    }

    @Override
    public int getType() {
        return type;
    }
}