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

    private static class SubElement extends MultiElement {
        private final int from;
        private final int size;

        public SubElement(final int[] indices, final int type, final int from, final int to) {
            super(indices, type);
            this.from = from;
            this.size = to - from;
        }

        public int size() {
            return size;
        }

        public int getNode(int index) {
            return super.getNode(index + from);
        }
    }

    public MultiElement subElement(final int from, final int to) {
        return new SubElement(indices, type, from, to);
    }
}